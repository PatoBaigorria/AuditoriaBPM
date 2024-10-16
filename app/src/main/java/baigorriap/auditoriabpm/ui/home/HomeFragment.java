package baigorriap.auditoriabpm.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentHomeBinding;
import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.Linea;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel vm;
    private Spinner spnActividad, spnLinea;
    private TextView tvNombreOp;
    private ArrayAdapter<Actividad> actividadAdapter;
    private ArrayAdapter<Linea> lineaAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        vm = new ViewModelProvider(this).get(HomeViewModel.class);

        // Inicializa los Spinners
        spnActividad = binding.spnActividad;
        spnLinea = binding.spnLinea;
        vm.limpiarSpinnersConHint();

        // Configura el TextView
        final TextView textView = binding.textHome;
        vm.getText().observe(getViewLifecycleOwner(), textView::setText);
        tvNombreOp = binding.tvNombreOp;

        // Observa el LiveData del nombre del operario
        vm.getMOperario().observe(getViewLifecycleOwner(), operario -> {
            if (operario != null) {
                tvNombreOp.setText(operario.getNombreCompleto());
            } else {
                Toast.makeText(getContext(), "El Legajo NO Existe", Toast.LENGTH_LONG).show();
                tvNombreOp.setText(""); // Limpiar el nombre si no se encuentra
            }
        });

        // Configura el EditText para el legajo
        EditText etLegajo = binding.etLegajo;

        // Agrega un TextWatcher para detectar cambios en el campo de legajo
        etLegajo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No hacer nada antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 || s.length() <= 5) {
                    // Limpiar el TextView del nombre del operario
                    tvNombreOp.setText("");
                    // Restablecer los spinners a su valor de hint
                    vm.limpiarSpinnersConHint();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No hacer nada después del cambio de texto
            }
        });

        // Lógica de validación del legajo
        etLegajo.setOnEditorActionListener((v, actionId, event) -> {
            String legajoInput = etLegajo.getText().toString().trim();
            if (!legajoInput.isEmpty()) {
                try {
                    int legajo = Integer.parseInt(legajoInput);
                    vm.cargarOperarioPorLegajo(legajo);
                    vm.cargarDatosPorLegajo(legajo); // Cargar datos del operario por legajo

                } catch (NumberFormatException e) {
                    // Si el legajo no es un número válido
                    Toast.makeText(getContext(), "Por favor, introduce un número de legajo válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Limpia los spinners si el legajo está vacío
                vm.limpiarSpinnersConHint();
            }
            return false;
        });

        // Observa la lista de actividades
        vm.getMListaActividad().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null && !actividades.isEmpty()) {
                if (actividadAdapter == null) {
                    actividadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, actividades);
                    actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnActividad.setAdapter(actividadAdapter);
                } else {
                    actividadAdapter.clear();
                    actividadAdapter.addAll(actividades);
                    actividadAdapter.notifyDataSetChanged();
                }
            } else {
                // Si la lista de actividades está vacía, limpiar el spinner
                vm.limpiarSpinnersConHint();
                if (actividadAdapter != null) {
                    actividadAdapter.clear();
                    actividadAdapter.notifyDataSetChanged();
                }
            }
        });


        // Observa la lista de líneas
        vm.getMListaLinea().observe(getViewLifecycleOwner(), lineas -> {
            if (lineas != null && !lineas.isEmpty()) {
                if (lineaAdapter == null) {
                    lineaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, lineas);
                    lineaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnLinea.setAdapter(lineaAdapter);
                } else {
                    lineaAdapter.clear();
                    lineaAdapter.addAll(lineas);
                    lineaAdapter.notifyDataSetChanged();
                }
            } else {
                // Si la lista de líneas está vacía, limpiar el spinner
                vm.limpiarSpinnersConHint();
                if (lineaAdapter != null) {
                    lineaAdapter.clear();
                    lineaAdapter.notifyDataSetChanged();
                }
            }
        });
        // Configuración del botón "Siguiente"
        binding.btSiguiente.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_auditoria, null, new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, true) // Esto elimina HomeFragment del backstack
                    .build());
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
