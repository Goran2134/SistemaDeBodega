package com.example.sistemadebodega;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sistemadebodega.R;
import com.example.sistemadebodega.model.Categoria;
import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {

    private List<Categoria> listaCategorias;
    private final OnEliminarClickListener eliminarListener;

    public interface OnEliminarClickListener {
        void onEliminarClick(Categoria categoria);
    }

    public CategoriaAdapter(List<Categoria> listaCategorias, OnEliminarClickListener eliminarListener) {
        this.listaCategorias = listaCategorias;
        this.eliminarListener = eliminarListener;
    }

    public void actualizarLista(List<Categoria> nuevaLista) {
        this.listaCategorias = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = listaCategorias.get(position);
        holder.tvNombre.setText(categoria.nombre_categoria);
        holder.tvDesc.setText(categoria.descripcion != null && !categoria.descripcion.isEmpty() ? categoria.descripcion : "Sin descripción");

        holder.btnEliminar.setOnClickListener(v -> {
            if (eliminarListener != null) {
                eliminarListener.onEliminarClick(categoria);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCategorias.size();
    }

    public static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDesc;
        Button btnEliminar;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreCategoriaItem);
            tvDesc = itemView.findViewById(R.id.tvDescCategoriaItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminarCategoriaItem);
        }
    }
}