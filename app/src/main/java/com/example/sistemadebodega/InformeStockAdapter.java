package com.example.sistemadebodega;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.model.Producto;

import java.util.List;

public class InformeStockAdapter extends RecyclerView.Adapter<InformeStockAdapter.InformeViewHolder> {

    private List<Producto> listaProductos;

    public InformeStockAdapter(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public void actualizarLista(List<Producto> nuevaLista) {
        this.listaProductos = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InformeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_informe_stock, parent, false);
        return new InformeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InformeViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.tvNombre.setText(producto.nombre);
        holder.tvSkuCategoria.setText("SKU: " + (producto.codigo_sku != null ? producto.codigo_sku : "N/A") + " | Unidad: " + (producto.unidad_medida != null ? producto.unidad_medida : "unidad"));
        holder.tvStockActual.setText("Stock actual: " + producto.stock_actual);
        holder.tvStockMinimo.setText("Stock Mín: " + producto.stock_minimo);

        // Determinar estado de disponibilidad
        if (producto.stock_actual == 0) {
            holder.tvEstado.setText("SIN STOCK");
            holder.tvEstado.setTextColor(Color.parseColor("#B42318")); // Rojo
            holder.tvEstado.setBackgroundColor(Color.parseColor("#FEF2F2"));
            holder.tvStockActual.setTextColor(Color.parseColor("#B42318"));
        } else if (producto.stock_actual <= producto.stock_minimo) {
            holder.tvEstado.setText("STOCK CRÍTICO");
            holder.tvEstado.setTextColor(Color.parseColor("#B45309")); // Naranja
            holder.tvEstado.setBackgroundColor(Color.parseColor("#FFFBEB"));
            holder.tvStockActual.setTextColor(Color.parseColor("#B45309"));
        } else {
            holder.tvEstado.setText("DISPONIBLE");
            holder.tvEstado.setTextColor(Color.parseColor("#0F766E")); // Verde
            holder.tvEstado.setBackgroundColor(Color.parseColor("#F0FDF4"));
            holder.tvStockActual.setTextColor(Color.parseColor("#0F766E"));
        }
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public static class InformeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEstado, tvSkuCategoria, tvStockActual, tvStockMinimo;

        public InformeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProductoInforme);
            tvEstado = itemView.findViewById(R.id.tvEstadoInforme);
            tvSkuCategoria = itemView.findViewById(R.id.tvSkuCategoriaInforme);
            tvStockActual = itemView.findViewById(R.id.tvStockActualInforme);
            tvStockMinimo = itemView.findViewById(R.id.tvStockMinimoInforme);
        }
    }
}
