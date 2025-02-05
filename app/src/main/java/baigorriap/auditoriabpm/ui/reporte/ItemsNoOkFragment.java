package baigorriap.auditoriabpm.ui.reporte;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentItemsNoOkBinding;

public class ItemsNoOkFragment extends Fragment {
    private static final String TAG = "ItemsNoOkFragment";
    private FragmentItemsNoOkBinding binding;
    private ItemsNoOkViewModel viewModel;
    private ItemsNoOkAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentItemsNoOkBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ItemsNoOkViewModel.class);
        setupRecyclerView();
        observeViewModel();

        // Obtener el legajo del operario de los argumentos
        if (getArguments() != null && getArguments().containsKey("legajo")) {
            int legajo = getArguments().getInt("legajo");
            Log.d(TAG, "Cargando items NO OK para legajo: " + legajo);
            binding.progressBar.setVisibility(View.VISIBLE);
            viewModel.loadItemsNoOk(legajo, requireContext());
        } else {
            Log.d(TAG, "No se recibió legajo, mostrando diálogo");
            mostrarDialogoLegajo();
        }

        return root;
    }

    private void mostrarDialogoLegajo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ingresar Legajo");
        builder.setCancelable(false); // No permitir cerrar el diálogo tocando fuera

        final EditText input = new EditText(requireContext());
        input.setHint("Legajo del operario");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Buscar", null); // Se configura después para evitar que se cierre automáticamente

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Log.d(TAG, "Diálogo cancelado, volviendo atrás");
            // Volver al fragmento anterior
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.popBackStack();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Configurar el botón después de mostrar el diálogo para evitar que se cierre automáticamente
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String legajoStr = input.getText().toString();
            if (!legajoStr.isEmpty()) {
                try {
                    int legajo = Integer.parseInt(legajoStr);
                    Log.d(TAG, "Legajo ingresado: " + legajo);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    viewModel.loadItemsNoOk(legajo, requireContext());
                    dialog.dismiss(); // Solo cerrar si el legajo es válido
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear legajo: " + legajoStr, e);
                    Toast.makeText(requireContext(), "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Legajo vacío");
                Toast.makeText(requireContext(), "Por favor ingrese el legajo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ItemsNoOkAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getItemsNoOk().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                Log.d(TAG, "Items recibidos: " + items.size());
                adapter.submitList(items);
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Log.e(TAG, "Error observado: " + errorMsg);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                // Si hay un error, mostrar el diálogo nuevamente
                mostrarDialogoLegajo();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
