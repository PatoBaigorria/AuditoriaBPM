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
    private boolean needsCleanup = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Usar el ViewModel a nivel de actividad
        vm = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idSupervisor = sharedPreferences.getInt("idSupervisor", 0);

        // Inicializa los Spinners
        spnActividad = binding.spnActividad;
        spnLinea = binding.spnLinea;
        
        // Configura el TextView
        final TextView textView = binding.textHome;
        vm.getText().observe(getViewLifecycleOwner(), textView::setText);
        tvNombreOp = binding.tvNombreOp;

        // Inicializar adaptadores con placeholders
        inicializarAdaptadores();

        // Observa el LiveData del nombre del operario
        vm.getMOperario().observe(getViewLifecycleOwner(), operario -> {
            if (operario != null) {
                tvNombreOp.setText(operario.getNombreCompleto());
                idOperario = operario.getIdOperario();
            } else if (needsCleanup) {
                tvNombreOp.setText("");
                needsCleanup = false;
            }
        });

        // Configura el EditText para el legajo
        EditText etLegajo = binding.etLegajo;

        // Agrega un TextWatcher para detectar cambios en el campo de legajo
        etLegajo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 || s.length() <= 5) {
                    tvNombreOp.setText("");
                    inicializarAdaptadores();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Lógica de validación del legajo
        etLegajo.setOnEditorActionListener((v, actionId, event) -> {
            String legajoInput = etLegajo.getText().toString().trim();
            if (!legajoInput.isEmpty()) {
                try {
                    int legajo = Integer.parseInt(legajoInput);
                    vm.cargarOperarioPorLegajo(legajo);
                    vm.cargarDatosPorLegajo(legajo);
                    vm.cargarSupervisorPorId(idSupervisor);
                    Toast.makeText(getContext(), "Cargando datos...", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Por favor, introduce un número de legajo válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                inicializarAdaptadores();
            }
            return true;
        });

        // Observa la lista de actividades
        vm.getMListaActividad().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null && !actividades.isEmpty()) {
                Log.d("HomeFragment", "Recibidas " + actividades.size() + " actividades");
                actividadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, actividades);
                actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnActividad.setAdapter(actividadAdapter);
            } else if (needsCleanup) {
                Log.d("HomeFragment", "Lista de actividades vacía o nula");
                inicializarAdaptadores();
            }
        });

        // Observa la lista de líneas
        vm.getMListaLinea().observe(getViewLifecycleOwner(), lineas -> {
            if (lineas != null && !lineas.isEmpty()) {
                Log.d("HomeFragment", "Recibidas " + lineas.size() + " líneas");
                lineaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, lineas);
                lineaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnLinea.setAdapter(lineaAdapter);
            } else if (needsCleanup) {
                Log.d("HomeFragment", "Lista de líneas vacía o nula");
                inicializarAdaptadores();
            }
        });

        // Configuración del botón "Siguiente"
        binding.btSiguiente.setOnClickListener(v -> {
            String nombreOperario = tvNombreOp.getText().toString();
            if (nombreOperario.isEmpty()) {
                Toast.makeText(getContext(), "El nombre del operario está vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Toma el valor seleccionado directamente de los spinners
            Actividad selectedActividad = (Actividad) spnActividad.getSelectedItem();
            Linea selectedLinea = (Linea) spnLinea.getSelectedItem();

            if (selectedActividad == null || selectedLinea == null) {
                Toast.makeText(getContext(), "Debe seleccionar actividad y línea", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navegar al siguiente fragmento con los datos
            Bundle bundle = new Bundle();
            bundle.putInt("idOperario", idOperario);
            bundle.putInt("idSupervisor", idSupervisor);
            bundle.putInt("idActividad", selectedActividad.getIdActividad());
            bundle.putInt("idLinea", selectedLinea.getIdLinea());
            bundle.putString("nombreOperario", nombreOperario);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_auditoria, bundle);
        });

        getParentFragmentManager().setFragmentResultListener("needsReset", this, (requestKey, result) -> {
            if (result.getBoolean("reset", false)) {
                limpiarTodo();
            }
        });

        return root;
    }

    private void limpiarTodo() {
        needsCleanup = true;
        if (binding != null && binding.etLegajo != null) {
            binding.etLegajo.setText("");
        }
        vm.limpiarTodosDatos();
        inicializarAdaptadores();
    }

    private void inicializarAdaptadores() {
        // Crear lista con placeholder para Actividad
        List<Actividad> actividadesPlaceholder = new ArrayList<>();
        actividadesPlaceholder.add(new Actividad(-1, "Actividad"));
        
        // Crear lista con placeholder para Línea
        List<Linea> lineasPlaceholder = new ArrayList<>();
        lineasPlaceholder.add(new Linea(-1, "Línea"));

        // Inicializar o actualizar el adapter de Actividad
        if (actividadAdapter == null) {
            actividadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, actividadesPlaceholder);
            actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnActividad.setAdapter(actividadAdapter);
        } else {
            actividadAdapter.clear();
            actividadAdapter.addAll(actividadesPlaceholder);
            actividadAdapter.notifyDataSetChanged();
        }

        // Inicializar o actualizar el adapter de Línea
        if (lineaAdapter == null) {
            lineaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, lineasPlaceholder);
            lineaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnLinea.setAdapter(lineaAdapter);
        } else {
            lineaAdapter.clear();
            lineaAdapter.addAll(lineasPlaceholder);
            lineaAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
