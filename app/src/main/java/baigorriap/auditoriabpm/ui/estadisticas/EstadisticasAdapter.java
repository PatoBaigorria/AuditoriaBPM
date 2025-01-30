package baigorriap.auditoriabpm.ui.estadisticas;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import baigorriap.auditoriabpm.databinding.ItemEstadisticaBinding;
import baigorriap.auditoriabpm.model.EstadisticasAuditoria;

public class EstadisticasAdapter extends RecyclerView.Adapter<EstadisticasAdapter.ViewHolder> {
    private final List<Map.Entry<String, EstadisticasAuditoria>> estadisticas = new ArrayList<>();
    private static final String[] MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public void setEstadisticas(Map<String, EstadisticasAuditoria> estadisticasMap) {
        estadisticas.clear();
        
        // Ordenar por fecha (año-mes) en orden descendente
        TreeMap<String, EstadisticasAuditoria> sortedMap = new TreeMap<>((s1, s2) -> s2.compareTo(s1));
        
        // Filtrar la clave TotalesAnuales y meses sin auditorías
        estadisticasMap.forEach((key, value) -> {
            if (!key.equals("TotalesAnuales") && value.getTotal() > 0) {
                sortedMap.put(key, value);
            }
        });
        
        estadisticas.addAll(sortedMap.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEstadisticaBinding binding = ItemEstadisticaBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, EstadisticasAuditoria> entry = estadisticas.get(position);
        holder.bind(entry.getKey(), entry.getValue());
    }

    @Override
    public int getItemCount() {
        return estadisticas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemEstadisticaBinding binding;

        public ViewHolder(ItemEstadisticaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String periodo, EstadisticasAuditoria estadistica) {
            // El periodo viene en formato "YYYY-MM"
            String[] partes = periodo.split("-");
            int mes = Integer.parseInt(partes[1]) - 1; // Los meses van de 0 a 11
            String nombreMes = MESES[mes];
            
            binding.tvPeriodo.setText(nombreMes + " " + partes[0]);
            binding.tvTotal.setText(String.valueOf(estadistica.getTotal()));
            binding.tvOk.setText(String.valueOf(estadistica.getConEstadoOK()));
            binding.tvNoOk.setText(String.valueOf(estadistica.getConEstadoNoOk()));
        }
    }
}
