package baigorriap.auditoriabpm.ui.reporte;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.model.OperarioSinAuditoria;
import baigorriap.auditoriabpm.MenuActivity;

public class OperariosSinAuditoriaAdapter extends RecyclerView.Adapter<OperariosSinAuditoriaAdapter.ViewHolder> {
    private List<OperarioSinAuditoria> operarios = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operario_sin_auditoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OperarioSinAuditoria operario = operarios.get(position);
        holder.tvNombre.setText(operario.getNombreCompleto());
        holder.tvLegajo.setText("Legajo: " + operario.getLegajo());

        holder.btnNuevaAuditoria.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MenuActivity.class);
            intent.putExtra("idOperario", operario.getIdOperario());
            intent.putExtra("nombreOperario", operario.getNombreCompleto());
            intent.putExtra("legajoOperario", operario.getLegajo());
            intent.putExtra("iniciarAuditoria", true);
            intent.putExtra("destino", R.id.nav_auditoria);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return operarios.size();
    }

    public void setOperarios(List<OperarioSinAuditoria> operarios) {
        this.operarios = operarios;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvLegajo;
        MaterialButton btnNuevaAuditoria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvLegajo = itemView.findViewById(R.id.tvLegajo);
            btnNuevaAuditoria = itemView.findViewById(R.id.btnNuevaAuditoria);
        }
    }
}
