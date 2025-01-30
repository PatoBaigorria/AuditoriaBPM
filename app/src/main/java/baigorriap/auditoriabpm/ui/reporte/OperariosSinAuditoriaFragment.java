package baigorriap.auditoriabpm.ui.reporte;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.model.OperarioSinAuditoria;
import baigorriap.auditoriabpm.request.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperariosSinAuditoriaFragment extends Fragment implements TextWatcher {
    private static final String TAG = "OperariosSinAuditoria";
    private RecyclerView rvOperarios;
    private ProgressBar progressBar;
    private OperariosSinAuditoriaAdapter operariosAdapter;
    private TextInputEditText etBusqueda;
    private ChipGroup chipGroupLineas;
    private TextView tvTotalOperarios;
    private TextView tvPorcentajeSinAuditoria;
    private List<OperarioSinAuditoria> todosLosOperarios = new ArrayList<>();
    private Set<Integer> lineasSeleccionadas = new HashSet<>();
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_operarios_sin_auditoria, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            inicializarVistas();
            configurarRecyclerView();
            configurarBusqueda();
            cargarOperariosSinAuditoria();
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar el fragmento", e);
            Toast.makeText(requireContext(), "Error al inicializar el fragmento: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void inicializarVistas() {
        rvOperarios = root.findViewById(R.id.rvOperarios);
        if (rvOperarios == null) {
            throw new IllegalStateException("No se pudo encontrar el RecyclerView con ID rvOperarios");
        }

        progressBar = root.findViewById(R.id.progressBar);
        if (progressBar == null) {
            throw new IllegalStateException("No se pudo encontrar el ProgressBar");
        }

        etBusqueda = root.findViewById(R.id.etBusqueda);
        if (etBusqueda == null) {
            throw new IllegalStateException("No se pudo encontrar el EditText de búsqueda");
        }

        chipGroupLineas = root.findViewById(R.id.chipGroupLineas);
        if (chipGroupLineas == null) {
            throw new IllegalStateException("No se pudo encontrar el ChipGroup");
        }

        tvTotalOperarios = root.findViewById(R.id.tvTotalOperarios);
        if (tvTotalOperarios == null) {
            throw new IllegalStateException("No se pudo encontrar el TextView de total");
        }

        tvPorcentajeSinAuditoria = root.findViewById(R.id.tvPorcentajeSinAuditoria);
        if (tvPorcentajeSinAuditoria == null) {
            throw new IllegalStateException("No se pudo encontrar el TextView de porcentaje");
        }
    }

    private void configurarRecyclerView() {
        operariosAdapter = new OperariosSinAuditoriaAdapter();
        rvOperarios.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOperarios.setAdapter(operariosAdapter);
    }

    private void configurarBusqueda() {
        etBusqueda.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        filtrarOperarios(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void cargarOperariosSinAuditoria() {
        progressBar.setVisibility(View.VISIBLE);
        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            Toast.makeText(requireContext(), "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando llamada a la API");
        ApiClient.getEndPoints().obtenerOperariosSinAuditorias("Bearer " + token).enqueue(new Callback<List<OperarioSinAuditoria>>() {
            @Override
            public void onResponse(Call<List<OperarioSinAuditoria>> call, Response<List<OperarioSinAuditoria>> response) {
                if (!isAdded()) return; // Verificar si el Fragment está adjunto
                
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    todosLosOperarios = response.body();
                    operariosAdapter.setOperarios(todosLosOperarios);
                    actualizarEstadisticas(todosLosOperarios);
                    crearChipsLineas();
                } else {
                    String errorMsg = "Error al cargar operarios. Código: " + response.code();
                    Log.e(TAG, errorMsg);
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<OperarioSinAuditoria>> call, Throwable t) {
                if (!isAdded()) return; // Verificar si el Fragment está adjunto
                
                progressBar.setVisibility(View.GONE);
                String errorMsg = "Error de conexión: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void crearChipsLineas() {
        chipGroupLineas.removeAllViews();
        
        Map<Integer, String> lineasUnicas = todosLosOperarios.stream()
                .collect(Collectors.toMap(
                    OperarioSinAuditoria::getIdLinea,
                    OperarioSinAuditoria::getDescripcionLinea,
                    (desc1, desc2) -> desc1
                ));

        for (Map.Entry<Integer, String> linea : lineasUnicas.entrySet()) {
            Chip chip = new Chip(requireContext());
            chip.setText(linea.getValue());
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    lineasSeleccionadas.add(linea.getKey());
                } else {
                    lineasSeleccionadas.remove(linea.getKey());
                }
                filtrarOperarios(etBusqueda.getText().toString());
            });
            chipGroupLineas.addView(chip);
        }
    }

    private void filtrarOperarios(String query) {
        if (todosLosOperarios == null) return;

        List<OperarioSinAuditoria> operariosFiltrados = todosLosOperarios.stream()
                .filter(operario -> {
                    boolean coincideTexto = query.isEmpty() ||
                            operario.getNombreCompleto().toLowerCase().contains(query.toLowerCase()) ||
                            String.valueOf(operario.getLegajo()).toLowerCase().contains(query.toLowerCase());
                    
                    boolean coincideLinea = lineasSeleccionadas.isEmpty() ||
                            lineasSeleccionadas.contains(operario.getIdLinea());
                    
                    return coincideTexto && coincideLinea;
                })
                .collect(Collectors.toList());

        operariosAdapter.setOperarios(operariosFiltrados);
        actualizarEstadisticas(operariosFiltrados);
    }

    private void actualizarEstadisticas(List<OperarioSinAuditoria> operarios) {
        if (operarios == null) return;
        
        int total = operarios.size();
        tvTotalOperarios.setText(String.format("Total de operarios: %d", total));
        
        if (total > 0) {
            double porcentaje = (double) total / todosLosOperarios.size() * 100;
            tvPorcentajeSinAuditoria.setText(String.format("Porcentaje sin auditoría: %.1f%%", porcentaje));
        } else {
            tvPorcentajeSinAuditoria.setText("Porcentaje sin auditoría: 0%");
        }
    }
}
