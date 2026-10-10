package com.example.sistemadebodega;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sistemadebodega.R;
import com.example.sistemadebodega.dao.UsuarioDao;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<UsuarioDao.UsuarioConRol> listaUsuarios;

    public UsuarioAdapter(List<UsuarioDao.UsuarioConRol> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
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
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCompleto, tvUsername, tvRol;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompletoItem);
            tvUsername = itemView.findViewById(R.id.tvUsernameItem);
            tvRol = itemView.findViewById(R.id.tvRolItem);
        }
    }
}