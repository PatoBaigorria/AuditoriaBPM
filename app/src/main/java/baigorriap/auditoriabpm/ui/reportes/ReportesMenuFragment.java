package baigorriap.auditoriabpm.ui.reportes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentReportesMenuBinding;

public class ReportesMenuFragment extends Fragment {

    private FragmentReportesMenuBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportesMenuBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(
                    R.id.action_nav_gallery_to_nav_home,
                    null,
                    new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, true)
                        .build()
                );
            }
        });

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

        binding.cardItemsNoOk.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_items_nook);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
