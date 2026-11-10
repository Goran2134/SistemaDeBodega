package com.example.sistemadebodega;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Categoria;
import com.example.sistemadebodega.model.Producto;
import com.example.sistemadebodega.utils.SessionManager;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InformeStockActivity extends AppCompatActivity {

    private EditText etBuscar;
    private Spinner spFiltroCategoria;
    private Spinner spFiltroProducto;
    private Spinner spFiltroEstado;
    private TextView tvResumen;
    private RecyclerView recyclerView;

    private AppDatabase db;
    private SessionManager sessionManager;
    private InformeStockAdapter adapter;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<Producto> listaProductosCompleta =
            new ArrayList<>();

    private final List<Producto> listaProductosFiltrada =
            new ArrayList<>();

    private final List<Categoria> listaCategorias =
            new ArrayList<>();

    private final List<Integer> idsCategorias =
            new ArrayList<>();

    private final List<Integer> idsProductos =
            new ArrayList<>();

    private int categoriaSeleccionada = -1;
    private int productoSeleccionado = -1;

    private final String[] estadosFiltro = {
            "Todos los estados",
            "Disponible (Normal)",
            "Stock Crítico",
            "Sin Stock (Agotado)"
    };

    // RF-23: COPIA DE LOS DATOS QUE SE EXPORTARÁN
    private final List<Producto> productosParaExportar =
            new ArrayList<>();

    private final ActivityResultLauncher<String> guardarPdf =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "application/pdf"
                    ),
                    uri -> {
                        if (uri != null) {
                            exportarPdf(uri);
                        }
                    }
            );

    private final ActivityResultLauncher<String> guardarExcel =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ),
                    uri -> {
                        if (uri != null) {
                            exportarExcel(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_stock);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
            return;
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        etBuscar = findViewById(R.id.etBuscarInforme);
        spFiltroCategoria =
                findViewById(R.id.spFiltroCategoriaInforme);
        spFiltroProducto =
                findViewById(R.id.spFiltroProductoInforme);
        spFiltroEstado =
                findViewById(R.id.spFiltroEstadoInforme);
        tvResumen = findViewById(R.id.tvResumenInforme);
        recyclerView = findViewById(R.id.rvInformeStock);

        Button btnVolver =
                findViewById(R.id.btnVolverInforme);

        Button btnExportarPdf =
                findViewById(R.id.btnExportarPdf);

        Button btnExportarExcel =
                findViewById(R.id.btnExportarExcel);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new InformeStockAdapter(
                listaProductosFiltrada
        );

        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> filtroEstadoAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        estadosFiltro
                );

        filtroEstadoAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFiltroEstado.setAdapter(filtroEstadoAdapter);

        btnVolver.setOnClickListener(v -> finish());

        btnExportarPdf.setOnClickListener(v -> {
            if (prepararExportacion()) {
                guardarPdf.launch(
                        generarNombreArchivo("pdf")
                );
            }
        });

        btnExportarExcel.setOnClickListener(v -> {
            if (prepararExportacion()) {
                guardarExcel.launch(
                        generarNombreArchivo("xlsx")
                );
            }
        });

        etBuscar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spFiltroCategoria.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (position >= 0
                                && position < idsCategorias.size()) {

                            categoriaSeleccionada =
                                    idsCategorias.get(position);

                            actualizarOpcionesProductos();
                            aplicarFiltros();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        spFiltroProducto.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (position >= 0
                                && position < idsProductos.size()) {

                            productoSeleccionado =
                                    idsProductos.get(position);

                            aplicarFiltros();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        spFiltroEstado.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        aplicarFiltros();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }

    private void cargarDatos() {

        executor.execute(() -> {
            try {

                List<Producto> productos =
                        db.productoDao().obtenerProductosActivos();

                List<Categoria> categorias =
                        db.categoriaDao().obtenerCategoriasActivas();

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    listaProductosCompleta.clear();
                    listaProductosCompleta.addAll(productos);

                    listaCategorias.clear();
                    listaCategorias.addAll(categorias);

                    actualizarOpcionesCategorias();
                    actualizarOpcionesProductos();
                    aplicarFiltros();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Error al cargar informe de stock",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private void actualizarOpcionesCategorias() {

        List<String> opciones = new ArrayList<>();

        idsCategorias.clear();

        opciones.add("Todas las categorías");
        idsCategorias.add(-1);

        for (Categoria categoria : listaCategorias) {

            idsCategorias.add(categoria.id_categoria);

            opciones.add(
                    categoria.nombre_categoria != null
                            ? categoria.nombre_categoria
                            : "Categoría #" + categoria.id_categoria
            );
        }

        ArrayAdapter<String> adapterCategorias =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        opciones
                );

        adapterCategorias.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFiltroCategoria.setAdapter(adapterCategorias);

        int posicion =
                idsCategorias.indexOf(categoriaSeleccionada);

        if (posicion < 0) {
            categoriaSeleccionada = -1;
            posicion = 0;
        }

        spFiltroCategoria.setSelection(posicion);
    }

    private void actualizarOpcionesProductos() {

        List<String> opciones = new ArrayList<>();

        idsProductos.clear();

        opciones.add("Todos los productos");
        idsProductos.add(-1);

        for (Producto producto : listaProductosCompleta) {

            if (categoriaSeleccionada != -1
                    && producto.id_categoria != categoriaSeleccionada) {
                continue;
            }

            idsProductos.add(producto.id_producto);

            opciones.add(
                    producto.nombre
                            + " (SKU: "
                            + (producto.codigo_sku != null
                            ? producto.codigo_sku : "N/A")
                            + ")"
            );
        }

        ArrayAdapter<String> adapterProductos =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        opciones
                );

        adapterProductos.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFiltroProducto.setAdapter(adapterProductos);

        int posicion =
                idsProductos.indexOf(productoSeleccionado);

        if (posicion < 0) {
            productoSeleccionado = -1;
            posicion = 0;
        }

        spFiltroProducto.setSelection(posicion);
    }

    private void aplicarFiltros() {

        String textoBusqueda = etBuscar.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        int filtroEstado =
                spFiltroEstado.getSelectedItemPosition();

        listaProductosFiltrada.clear();

        for (Producto p : listaProductosCompleta) {

            boolean coincideTexto =
                    textoBusqueda.isEmpty()
                            || (p.nombre != null
                            && p.nombre.toLowerCase(Locale.ROOT)
                            .contains(textoBusqueda))
                            || (p.codigo_sku != null
                            && p.codigo_sku.toLowerCase(Locale.ROOT)
                            .contains(textoBusqueda));

            if (!coincideTexto) {
                continue;
            }

            if (categoriaSeleccionada != -1
                    && p.id_categoria != categoriaSeleccionada) {
                continue;
            }

            if (productoSeleccionado != -1
                    && p.id_producto != productoSeleccionado) {
                continue;
            }

            boolean coincideEstado = true;

            if (filtroEstado == 1) {

                coincideEstado =
                        p.stock_actual > p.stock_minimo;

            } else if (filtroEstado == 2) {

                coincideEstado =
                        p.stock_actual > 0
                                && p.stock_actual <= p.stock_minimo;

            } else if (filtroEstado == 3) {

                coincideEstado = p.stock_actual == 0;
            }

            if (coincideEstado) {
                listaProductosFiltrada.add(p);
            }
        }

        adapter.notifyDataSetChanged();

        tvResumen.setText(
                "Mostrando "
                        + listaProductosFiltrada.size()
                        + " de "
                        + listaProductosCompleta.size()
                        + " productos"
        );
    }

    // RF-23: PREPARAR EXPORTACIÓN
    private boolean prepararExportacion() {

        if (listaProductosFiltrada.isEmpty()) {

            Toast.makeText(
                    this,
                    "No hay productos para exportar",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        productosParaExportar.clear();
        productosParaExportar.addAll(listaProductosFiltrada);

        return true;
    }

    private String generarNombreArchivo(String extension) {

        String fecha = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(new Date());

        return "Informe_Stock_" + fecha + "." + extension;
    }

    private String obtenerEstado(Producto producto) {

        if (producto.stock_actual == 0) {
            return "SIN STOCK";
        }

        if (producto.stock_actual <= producto.stock_minimo) {
            return "STOCK CRÍTICO";
        }

        return "DISPONIBLE";
    }

    private String obtenerCategoria(Producto producto) {

        for (Categoria categoria : listaCategorias) {

            if (categoria.id_categoria == producto.id_categoria) {

                return categoria.nombre_categoria != null
                        ? categoria.nombre_categoria
                        : "Sin nombre";
            }
        }

        return "Sin categoría";
    }

    // RF-23: EXPORTAR PDF
    private void exportarPdf(Uri uri) {

        List<Producto> productos =
                new ArrayList<>(productosParaExportar);

        executor.execute(() -> {

            PdfDocument documento = new PdfDocument();

            try {

                Paint pintura = new Paint(Paint.ANTI_ALIAS_FLAG);

                int anchoPagina = 842;
                int altoPagina = 595;

                int numeroPagina = 1;
                int indice = 0;

                do {

                    PdfDocument.PageInfo paginaInfo =
                            new PdfDocument.PageInfo.Builder(
                                    anchoPagina,
                                    altoPagina,
                                    numeroPagina
                            ).create();

                    PdfDocument.Page pagina =
                            documento.startPage(paginaInfo);

                    Canvas canvas = pagina.getCanvas();

                    canvas.drawColor(Color.WHITE);

                    pintura.setColor(Color.rgb(36, 59, 83));
                    pintura.setTypeface(Typeface.create(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    ));
                    pintura.setTextSize(22);

                    canvas.drawText(
                            "INFORME DE STOCK",
                            35,
                            45,
                            pintura
                    );

                    pintura.setColor(Color.DKGRAY);
                    pintura.setTypeface(Typeface.DEFAULT);
                    pintura.setTextSize(11);

                    String fecha = new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                    ).format(new Date());

                    canvas.drawText(
                            "Fecha: " + fecha,
                            35,
                            68,
                            pintura
                    );

                    canvas.drawText(
                            "Total de productos: " + productos.size(),
                            35,
                            86,
                            pintura
                    );

                    int y = 125;

                    pintura.setColor(Color.rgb(36, 59, 83));
                    pintura.setTypeface(Typeface.create(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    ));
                    pintura.setTextSize(11);

                    canvas.drawText("PRODUCTO", 35, y, pintura);
                    canvas.drawText("SKU", 235, y, pintura);
                    canvas.drawText("ACTUAL", 345, y, pintura);
                    canvas.drawText("MÍNIMO", 430, y, pintura);
                    canvas.drawText("ESTADO", 525, y, pintura);

                    y += 10;

                    pintura.setStrokeWidth(1);
                    canvas.drawLine(
                            35,
                            y,
                            805,
                            y,
                            pintura
                    );

                    y += 22;

                    pintura.setTypeface(Typeface.DEFAULT);
                    pintura.setTextSize(10);
                    pintura.setColor(Color.BLACK);

                    while (indice < productos.size() && y <= 525) {

                        Producto p = productos.get(indice);

                        canvas.drawText(
                                limitarTexto(p.nombre, 29),
                                35,
                                y,
                                pintura
                        );

                        canvas.drawText(
                                limitarTexto(p.codigo_sku, 15),
                                235,
                                y,
                                pintura
                        );

                        canvas.drawText(
                                String.valueOf(p.stock_actual),
                                345,
                                y,
                                pintura
                        );

                        canvas.drawText(
                                String.valueOf(p.stock_minimo),
                                430,
                                y,
                                pintura
                        );

                        canvas.drawText(
                                obtenerEstado(p),
                                525,
                                y,
                                pintura
                        );

                        y += 25;
                        indice++;
                    }

                    pintura.setColor(Color.GRAY);
                    pintura.setTextSize(10);

                    canvas.drawText(
                            "Página " + numeroPagina,
                            735,
                            565,
                            pintura
                    );

                    documento.finishPage(pagina);
                    numeroPagina++;

                } while (indice < productos.size());

                try (OutputStream salida =
                             getContentResolver().openOutputStream(uri)) {

                    if (salida == null) {
                        throw new Exception(
                                "No se pudo abrir el archivo"
                        );
                    }

                    documento.writeTo(salida);
                }

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Informe PDF guardado correctamente",
                            Toast.LENGTH_LONG
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Error al exportar PDF: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {
                documento.close();
            }
        });
    }

    private String limitarTexto(String texto, int maximo) {

        if (texto == null) {
            return "-";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo - 3) + "...";
    }

    // RF-23: EXPORTAR EXCEL
    private void exportarExcel(Uri uri) {

        List<Producto> productos =
                new ArrayList<>(productosParaExportar);

        // Capturar los nombres de categorías antes de entrar
        // al hilo de exportación.
        List<String> categoriasProductos = new ArrayList<>();

        for (Producto producto : productos) {
            categoriasProductos.add(obtenerCategoria(producto));
        }

        executor.execute(() -> {

            try (Workbook workbook = new XSSFWorkbook()) {

                Sheet hoja = workbook.createSheet("Informe Stock");

                Font fuenteTitulo = workbook.createFont();
                fuenteTitulo.setBold(true);
                fuenteTitulo.setFontHeightInPoints((short) 14);

                CellStyle estiloTitulo = workbook.createCellStyle();
                estiloTitulo.setFont(fuenteTitulo);

                Row filaTitulo = hoja.createRow(0);
                Cell celdaTitulo = filaTitulo.createCell(0);
                celdaTitulo.setCellValue("INFORME DE STOCK");
                celdaTitulo.setCellStyle(estiloTitulo);

                Row filaFecha = hoja.createRow(1);

                String fecha = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                ).format(new Date());

                filaFecha.createCell(0).setCellValue(
                        "Fecha: " + fecha
                );

                filaFecha.createCell(1).setCellValue(
                        "Total de productos: " + productos.size()
                );

                String[] encabezados = {
                        "Producto",
                        "SKU",
                        "Categoría",
                        "Unidad",
                        "Stock actual",
                        "Stock mínimo",
                        "Estado"
                };

                Font fuenteEncabezado = workbook.createFont();
                fuenteEncabezado.setBold(true);

                CellStyle estiloEncabezado =
                        workbook.createCellStyle();

                estiloEncabezado.setFont(fuenteEncabezado);

                Row filaEncabezado = hoja.createRow(3);

                for (int i = 0; i < encabezados.length; i++) {

                    Cell celda = filaEncabezado.createCell(i);
                    celda.setCellValue(encabezados[i]);
                    celda.setCellStyle(estiloEncabezado);
                }

                int numeroFila = 4;

                for (int i = 0; i < productos.size(); i++) {

                    Producto p = productos.get(i);

                    Row fila = hoja.createRow(numeroFila++);

                    fila.createCell(0).setCellValue(
                            p.nombre != null ? p.nombre : ""
                    );

                    fila.createCell(1).setCellValue(
                            p.codigo_sku != null
                                    ? p.codigo_sku
                                    : ""
                    );

                    fila.createCell(2).setCellValue(
                            categoriasProductos.get(i)
                    );

                    fila.createCell(3).setCellValue(
                            p.unidad_medida != null
                                    ? p.unidad_medida
                                    : "unidad"
                    );

                    fila.createCell(4).setCellValue(
                            p.stock_actual
                    );

                    fila.createCell(5).setCellValue(
                            p.stock_minimo
                    );

                    fila.createCell(6).setCellValue(
                            obtenerEstado(p)
                    );
                }

                hoja.setColumnWidth(0, 8000);
                hoja.setColumnWidth(1, 4500);
                hoja.setColumnWidth(2, 6000);
                hoja.setColumnWidth(3, 4000);
                hoja.setColumnWidth(4, 4500);
                hoja.setColumnWidth(5, 4500);
                hoja.setColumnWidth(6, 5000);

                try (OutputStream salida =
                             getContentResolver().openOutputStream(uri)) {

                    if (salida == null) {
                        throw new Exception(
                                "No se pudo abrir el archivo"
                        );
                    }

                    workbook.write(salida);
                }

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Informe Excel guardado correctamente",
                            Toast.LENGTH_LONG
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Error al exportar Excel: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
