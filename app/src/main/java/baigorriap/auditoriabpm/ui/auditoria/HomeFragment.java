package baigorriap.auditoriabpm.ui.auditoria;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel vm;
    private Spinner spnActividad, spnLinea;
    private TextView tvNombreOp;
    private ArrayAdapter<Actividad> actividadAdapter;
    private ArrayAdapter<Linea> lineaAdapter;
    private int idSupervisor;
    private int idOperario;
    private int idActividad;
    private int idLinea;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        vm = new ViewModelProvider(this).get(HomeViewModel.class);

        sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idSupervisor = sharedPreferences.getInt("idSupervisor", 0); // Recuperar el idSupervisor

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
                    tvNombreOp.setText("");
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
                    vm.cargarSupervisorPorId(idSupervisor);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Por favor, introduce un número de legajo válido", Toast.LENGTH_SHORT).show();
                }
            } else {
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
                vm.limpiarSpinnersConHint();
                if (lineaAdapter != null) {
                    lineaAdapter.clear();
                    lineaAdapter.notifyDataSetChanged();
                }
            }
        });

        // Observa los valores del ViewModel y guárdalos en las variables cuando cambien
        vm.getMIdSupervisor().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                idSupervisor = id; // Aquí id es un Integer, no un objeto con método
                Log.d("HomeFragment", "idSupervisor asignado: " + idSupervisor);
            } else {
                Log.d("HomeFragment", "El ID del supervisor es nulo");
            }
        });

        vm.getMOperario().observe(getViewLifecycleOwner(), operario -> {
            if (operario != null) {
                idOperario = operario.getIdOperario(); // Asegúrate de que tengas un método getId() en tu modelo de Operario
                tvNombreOp.setText(operario.getNombreCompleto());
                Log.d("HomeFragment", "idOperario asignado: " + idOperario);
            } else {
                Toast.makeText(getContext(), "El Legajo NO Existe", Toast.LENGTH_LONG).show();
                tvNombreOp.setText(""); // Limpiar el nombre si no se encuentra
            }
        });

        vm.getMIdActividad().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                idActividad = id;
            }
        });

        vm.getMIdLinea().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                idLinea = id;
            }
        });

        // Configuración del botón "Siguiente"
        binding.btSiguiente.setOnClickListener(v -> {
            String nombreOperario = tvNombreOp.getText().toString();
            if (nombreOperario.isEmpty()) {
                Toast.makeText(getContext(), "El nombre del operario está vacío", Toast.LENGTH_SHORT).show();
                return; // Evita navegar si no hay nombre
            }
            // Toma el valor seleccionado directamente de los spinners
            Actividad selectedActividad = (Actividad) spnActividad.getSelectedItem();
            Linea selectedLinea = (Linea) spnLinea.getSelectedItem();

            // Asegúrate de que el elemento seleccionado no sea nulo
            if (selectedActividad != null) {
                idActividad = selectedActividad.getIdActividad();
            } else {
                Toast.makeText(getContext(), "Por favor selecciona una actividad", Toast.LENGTH_SHORT).show();
                return; // Evita navegar si no hay actividad seleccionada
            }

            if (selectedLinea != null) {
                idLinea = selectedLinea.getIdLinea();
            } else {
                Toast.makeText(getContext(), "Por favor selecciona una línea", Toast.LENGTH_SHORT).show();
                return; // Evita navegar si no hay línea seleccionada
            }

            // Crea un Bundle y añade el nombre del operario y los IDs
            Bundle bundle = new Bundle();
            bundle.putInt("idOperario", idOperario);
            bundle.putString("nombreOperario", nombreOperario); // Agrega el nombre
            bundle.putInt("idSupervisor", idSupervisor);
            bundle.putInt("idActividad", idActividad);
            bundle.putInt("idLinea", idLinea);

            // Navega a AuditoriaFragment pasando el bundle
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_auditoria, bundle, new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, true) // Elimina HomeFragment del backstack
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
