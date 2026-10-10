package com.example.sistemadebodega;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sistemadebodega.dao.UsuarioDao;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<UsuarioDao.UsuarioConRol> listaUsuarios;
    private final OnEliminarClickListener eliminarListener;

    public interface OnEliminarClickListener {
        void onEliminarClick(UsuarioDao.UsuarioConRol usuario);
    }

    public UsuarioAdapter(List<UsuarioDao.UsuarioConRol> listaUsuarios, OnEliminarClickListener eliminarListener) {
        this.listaUsuarios = listaUsuarios;
        this.eliminarListener = eliminarListener;
    }

    public void actualizarLista(List<UsuarioDao.UsuarioConRol> nuevaLista) {
        this.listaUsuarios = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        UsuarioDao.UsuarioConRol usuario = listaUsuarios.get(position);
        holder.tvNombreCompleto.setText(usuario.nombre_completo);
        holder.tvUsername.setText("Usuario: " + usuario.username);
        holder.tvRol.setText("Rol: " + usuario.nombre_rol);

        // Evento de clic en el botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (eliminarListener != null) {
                eliminarListener.onEliminarClick(usuario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCompleto, tvUsername, tvRol;
        Button btnEliminar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompletoItem);
            tvUsername = itemView.findViewById(R.id.tvUsernameItem);
            tvRol = itemView.findViewById(R.id.tvRolItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
        }
    }
}