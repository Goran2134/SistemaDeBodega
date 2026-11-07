package com.example.sistemadebodega;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoAdapter
        extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    public interface OnProductoAccionListener {
        void onEditar(Producto producto);
        void onDeshabilitar(Producto producto);
        void onReactivar(Producto producto);
    }

    private final List<Producto> productos = new ArrayList<>();
    private final OnProductoAccionListener listener;

    public ProductoAdapter(OnProductoAccionListener listener) {
        this.listener = listener;
    }

    public void actualizarLista(List<Producto> nuevaLista) {
        productos.clear();

        if (nuevaLista != null) {
            productos.addAll(nuevaLista);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);

        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductoViewHolder holder,
            int position
    ) {
        Producto producto = productos.get(position);

        holder.tvNombre.setText(
                producto.nombre == null ? "" : producto.nombre
        );

        holder.tvSku.setText(
                "SKU: " + (producto.codigo_sku == null
                        ? "" : producto.codigo_sku)
        );

        holder.tvDescripcion.setText(
                producto.descripcion == null
                        || producto.descripcion.trim().isEmpty()
                        ? "Sin descripción"
                        : producto.descripcion
        );

        holder.tvStock.setText(
                "Stock actual: " + producto.stock_actual
                        + " " + (producto.unidad_medida == null
                        ? "" : producto.unidad_medida)
        );

        boolean activo = producto.activo == 1;

        holder.tvEstado.setText(
                activo ? "Activo" : "Deshabilitado"
        );

        holder.tvEstado.setTextColor(
                activo ? 0xFF027A48 : 0xFFB42318
        );

        holder.btnEditar.setEnabled(activo);
        holder.btnEditar.setAlpha(activo ? 1f : 0.5f);

        holder.btnEditar.setOnClickListener(v -> {
            if (activo) {
                listener.onEditar(producto);
            }
        });

        holder.btnCambiarEstado.setText(
                activo ? "Deshabilitar" : "Reactivar"
        );

        holder.btnCambiarEstado.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        activo ? 0xFFB42318 : 0xFF027A48
                )
        );

        holder.btnCambiarEstado.setOnClickListener(v -> {
            if (activo) {
                listener.onDeshabilitar(producto);
            } else {
                listener.onReactivar(producto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ProductoViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvNombre, tvSku, tvDescripcion, tvStock, tvEstado;
        Button btnEditar, btnCambiarEstado;

        ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvSku = itemView.findViewById(R.id.tvSkuProducto);
            tvDescripcion = itemView.findViewById(
                    R.id.tvDescripcionProducto
            );
            tvStock = itemView.findViewById(R.id.tvStockProducto);
            tvEstado = itemView.findViewById(R.id.tvEstadoProducto);

            btnEditar = itemView.findViewById(R.id.btnEditarProducto);
            btnCambiarEstado = itemView.findViewById(
                    R.id.btnDeshabilitarProducto
            );
        }
    }
}
