package baigorriap.auditoriabpm.ui.reporte;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentReporteBinding;

public class ReporteFragment extends Fragment {

    private FragmentReporteBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReporteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupClickListeners();

        return root;
    }

    private void setupClickListeners() {
        binding.cardOperariosSinAuditoria.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_operarios_sin_auditoria);
        });

        binding.cardEstadisticas.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_estadisticas);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
