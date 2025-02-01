package baigorriap.auditoriabpm.ui.reporte;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentReporteBinding;

public class ReporteFragment extends Fragment {

    private FragmentReporteBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReporteBinding.inflate(inflater, container, false);
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
            mostrarDialogoLegajo();
        });
    }

    private void mostrarDialogoLegajo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ingresar Legajo");

        final EditText input = new EditText(requireContext());
        input.setHint("Legajo del operario");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Buscar", (dialog, which) -> {
            String legajoStr = input.getText().toString();
            if (!legajoStr.isEmpty()) {
                try {
                    int legajo = Integer.parseInt(legajoStr);
                    Bundle args = new Bundle();
                    args.putInt("legajo", legajo);
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_items_nook, args);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Por favor ingrese el legajo", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
