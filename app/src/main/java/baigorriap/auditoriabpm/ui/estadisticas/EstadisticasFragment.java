package baigorriap.auditoriabpm.ui.estadisticas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Calendar;
import java.util.Map;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.ActivityEstadisticasAuditoriaBinding;
import baigorriap.auditoriabpm.model.EstadisticasAuditoria;

public class EstadisticasFragment extends Fragment {
    private ActivityEstadisticasAuditoriaBinding binding;
    private EstadisticasViewModel viewModel;
    private EstadisticasAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityEstadisticasAuditoriaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);

        // Configurar la navegación al presionar el botón de retroceso
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navegar al HomeFragment
                Navigation.findNavController(root).navigate(
                    R.id.action_nav_estadisticas_to_nav_home,
                    null,
                    new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, true)
                        .build()
                );
            }
        });

        setupRecyclerView();
        setupListeners();
        setupObservers();
        setDefaultYears();

        return root;
    }

    private void setupRecyclerView() {
        adapter = new EstadisticasAdapter();
        binding.rvEstadisticas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEstadisticas.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnBuscar.setOnClickListener(v -> {
            try {
                int anioInicio = Integer.parseInt(binding.etAnioInicio.getText().toString());
                int anioFin = Integer.parseInt(binding.etAnioFin.getText().toString());
                
                if (anioInicio > anioFin) {
                    Toast.makeText(getContext(), "El año de inicio debe ser menor o igual al año fin", Toast.LENGTH_SHORT).show();
                    return;
                }

                binding.progressBar.setVisibility(View.VISIBLE);
                viewModel.cargarEstadisticas(anioInicio, anioFin);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Por favor ingrese años válidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        viewModel.getEstadisticas().observe(getViewLifecycleOwner(), this::actualizarEstadisticas);
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
    }

    private void setDefaultYears() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        binding.etAnioInicio.setText(String.valueOf(currentYear));
        binding.etAnioFin.setText(String.valueOf(currentYear));
        binding.btnBuscar.performClick();
    }

    private void actualizarEstadisticas(Map<String, EstadisticasAuditoria> estadisticas) {
        binding.progressBar.setVisibility(View.GONE);
        
        if (estadisticas == null || estadisticas.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para mostrar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular totales anuales
        int totalAnual = 0;
        int totalOk = 0;
        int totalNoOk = 0;

        for (Map.Entry<String, EstadisticasAuditoria> entry : estadisticas.entrySet()) {
            if (!entry.getKey().equals("TotalesAnuales")) {
                EstadisticasAuditoria estadistica = entry.getValue();
                totalAnual += estadistica.getTotal();
                totalOk += estadistica.getConEstadoOK();
                totalNoOk += estadistica.getConEstadoNoOk();
            }
        }

        // Actualizar totales anuales
        binding.tvTotalAnual.setText(String.valueOf(totalAnual));
        binding.tvTotalOk.setText(String.valueOf(totalOk));
        binding.tvTotalNoOk.setText(String.valueOf(totalNoOk));

        // Actualizar lista mensual
        adapter.setEstadisticas(estadisticas);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
