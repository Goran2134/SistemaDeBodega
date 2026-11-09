package com.example.sistemadebodega;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.dao.MovimientoDao;

import java.util.List;

public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder> {

    private List<MovimientoDao.MovimientoConProducto> listaMovimientos;

    public MovimientoAdapter(List<MovimientoDao.MovimientoConProducto> listaMovimientos) {
        this.listaMovimientos = listaMovimientos;
    }

    public void actualizarLista(List<MovimientoDao.MovimientoConProducto> nuevaLista) {
        this.listaMovimientos = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimiento, parent, false);
        return new MovimientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        MovimientoDao.MovimientoConProducto mov = listaMovimientos.get(position);

        holder.tvTipo.setText(mov.tipo_movimiento);
        if ("ENTRADA".equals(mov.tipo_movimiento)) {
            holder.tvTipo.setTextColor(Color.parseColor("#0284C7")); // Azul
            holder.tvCantidad.setText("Cantidad: +" + mov.cantidad);
            holder.tvCantidad.setTextColor(Color.parseColor("#0F766E")); // Verde oscuro
        } else {
            holder.tvTipo.setTextColor(Color.parseColor("#B42318")); // Rojo
            holder.tvCantidad.setText("Cantidad: -" + mov.cantidad);
            holder.tvCantidad.setTextColor(Color.parseColor("#B42318")); // Rojo
        }

        holder.tvFecha.setText(mov.fecha_hora != null ? mov.fecha_hora : "");
        holder.tvProducto.setText(mov.nombre_producto + " (SKU: " + (mov.codigo_sku != null ? mov.codigo_sku : "N/A") + ")");
        holder.tvMotivo.setText("Motivo: " + (mov.motivo != null ? mov.motivo : "-"));

        if (mov.observaciones != null && !mov.observaciones.trim().isEmpty()) {
            holder.tvObservaciones.setText("Obs: " + mov.observaciones);
            holder.tvObservaciones.setVisibility(View.VISIBLE);
        } else {
            holder.tvObservaciones.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaMovimientos.size();
    }

    public static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvFecha, tvProducto, tvCantidad, tvMotivo, tvObservaciones;

        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipoMovimientoItem);
            tvFecha = itemView.findViewById(R.id.tvFechaItem);
            tvProducto = itemView.findViewById(R.id.tvProductoMovimientoItem);
            tvCantidad = itemView.findViewById(R.id.tvCantidadItem);
            tvMotivo = itemView.findViewById(R.id.tvMotivoItem);
            tvObservaciones = itemView.findViewById(R.id.tvObservacionesItem);
        }
    }
}
