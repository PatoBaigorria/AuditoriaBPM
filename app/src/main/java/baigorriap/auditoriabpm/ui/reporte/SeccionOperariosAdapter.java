package baigorriap.auditoriabpm.ui.reporte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.model.OperarioSinAuditoria;

public class SeccionOperariosAdapter extends RecyclerView.Adapter<SeccionOperariosAdapter.ViewHolder> {
    private List<SeccionOperarios> secciones = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seccion_operarios, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SeccionOperarios seccion = secciones.get(position);
        holder.tvTituloSeccion.setText(seccion.getTitulo());

        // Configurar el adaptador para la lista de operarios de esta sección
        OperariosSinAuditoriaAdapter operariosAdapter = new OperariosSinAuditoriaAdapter();
        holder.rvOperarios.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvOperarios.setAdapter(operariosAdapter);
        operariosAdapter.setOperarios(seccion.getOperarios());
    }

    @Override
    public int getItemCount() {
        return secciones.size();
    }

    public void setSecciones(List<SeccionOperarios> secciones) {
        this.secciones = secciones;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;
        RecyclerView rvOperarios;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
            rvOperarios = itemView.findViewById(R.id.rvOperarios);
        }
    }
}
