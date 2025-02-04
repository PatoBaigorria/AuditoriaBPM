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

        // Inicializar los spinners con placeholders al inicio
        vm.limpiarSpinnersConHint();
        
        // Crear y configurar adaptadores iniciales
        List<Actividad> actividadesIniciales = new ArrayList<>();
        actividadesIniciales.add(new Actividad(-1, "Actividad"));
        actividadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, actividadesIniciales);
        actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnActividad.setAdapter(actividadAdapter);

        List<Linea> lineasIniciales = new ArrayList<>();
        lineasIniciales.add(new Linea(-1, "Línea"));
        lineaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, lineasIniciales);
        lineaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLinea.setAdapter(lineaAdapter);

        // Configura el TextView
        final TextView textView = binding.textHome;
        vm.getText().observe(getViewLifecycleOwner(), textView::setText);
        tvNombreOp = binding.tvNombreOp;

        // Observa el LiveData del nombre del operario
        vm.getMOperario().observe(getViewLifecycleOwner(), operario -> {
            if (operario != null) {
                tvNombreOp.setText(operario.getNombreCompleto());
                idOperario = operario.getIdOperario();
                binding.etLegajo.setError(null);
            } else {
                tvNombreOp.setText("");
                // Si el campo de legajo no está vacío y tiene 6 dígitos, mostrar error
                String legajoInput = binding.etLegajo.getText().toString().trim();
                if (!legajoInput.isEmpty() && legajoInput.length() == 6) {
                    binding.etLegajo.setError("No se encontró ningún operario con este legajo");
                }
                // Limpiar spinners cuando no hay operario
                vm.limpiarSpinnersConHint();
            }
        });

        // Observar mensajes de error
        vm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                binding.etLegajo.setError(error);
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
                String input = s.toString().trim();
                
                // Limpiar datos si el campo está vacío o si el legajo no tiene 6 dígitos
                if (input.isEmpty() || input.length() != 6) {
                    tvNombreOp.setText("");
                    vm.limpiarSpinnersConHint();
                    if (actividadAdapter != null) {
                        actividadAdapter.clear();
                        actividadAdapter.add(new Actividad(-1, "Actividad"));
                        actividadAdapter.notifyDataSetChanged();
                    }
                    if (lineaAdapter != null) {
                        lineaAdapter.clear();
                        lineaAdapter.add(new Linea(-1, "Línea"));
                        lineaAdapter.notifyDataSetChanged();
                    }
                    
                    // Mostrar mensaje de ayuda si el usuario está escribiendo
                    if (!input.isEmpty() && input.length() < 6) {
                        binding.etLegajo.setError("El legajo debe tener 6 dígitos");
                    } else {
                        binding.etLegajo.setError(null);
                    }
                } else {
                    try {
                        int legajo = Integer.parseInt(input);
                        if (legajo <= 0) {
                            binding.etLegajo.setError("El legajo debe ser un número positivo");
                        } else {
                            binding.etLegajo.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        binding.etLegajo.setError("El legajo debe ser un número válido");
                    }
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
                    // Validar que el legajo tenga 6 dígitos
                    if (legajoInput.length() != 6) {
                        Toast.makeText(getContext(), "El legajo debe tener 6 dígitos", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    // Validar que el legajo sea mayor que 0
                    if (legajo <= 0) {
                        Toast.makeText(getContext(), "El legajo debe ser un número positivo", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    vm.cargarOperarioPorLegajo(legajo);
                    vm.cargarDatosPorLegajo(legajo);
                    vm.cargarSupervisorPorId(idSupervisor);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Por favor, introduce un número de legajo válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                vm.limpiarSpinnersConHint();
                if (actividadAdapter != null) {
                    actividadAdapter.clear();
                    actividadAdapter.notifyDataSetChanged();
                }
                if (lineaAdapter != null) {
                    lineaAdapter.clear();
                    lineaAdapter.notifyDataSetChanged();
                }
            }
            return true;
        });

        // Observa el LiveData de la lista de actividades
        vm.getMListaActividad().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null && !actividades.isEmpty()) {
                actividadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, actividades);
                actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnActividad.setAdapter(actividadAdapter);
                
                // Seleccionar la actividad del operario si existe
                Integer idActividadOperario = vm.getMIdActividad().getValue();
                if (idActividadOperario != null) {
                    for (int i = 0; i < actividades.size(); i++) {
                        if (actividades.get(i).getIdActividad() == idActividadOperario) {
                            spnActividad.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });

        // Observa el LiveData de la lista de líneas
        vm.getMListaLinea().observe(getViewLifecycleOwner(), lineas -> {
            if (lineas != null && !lineas.isEmpty()) {
                lineaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, lineas);
                lineaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnLinea.setAdapter(lineaAdapter);
                
                // Seleccionar la línea del operario si existe
                Integer idLineaOperario = vm.getMIdLinea().getValue();
                if (idLineaOperario != null) {
                    for (int i = 0; i < lineas.size(); i++) {
                        if (lineas.get(i).getIdLinea() == idLineaOperario) {
                            spnLinea.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });

        // Observar cambios en el ID de actividad
        vm.getMIdActividad().observe(getViewLifecycleOwner(), idActividad -> {
            if (idActividad != null && actividadAdapter != null) {
                List<Actividad> actividades = vm.getMListaActividad().getValue();
                if (actividades != null) {
                    for (int i = 0; i < actividades.size(); i++) {
                        if (actividades.get(i).getIdActividad() == idActividad) {
                            spnActividad.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });

        // Observar cambios en el ID de línea
        vm.getMIdLinea().observe(getViewLifecycleOwner(), idLinea -> {
            if (idLinea != null && lineaAdapter != null) {
                List<Linea> lineas = vm.getMListaLinea().getValue();
                if (lineas != null) {
                    for (int i = 0; i < lineas.size(); i++) {
                        if (lineas.get(i).getIdLinea() == idLinea) {
                            spnLinea.setSelection(i);
                            break;
                        }
                    }
                }
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

            if (selectedActividad == null || selectedActividad.getIdActividad() == -1) {
                Toast.makeText(getContext(), "Por favor selecciona una actividad", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedLinea == null || selectedLinea.getIdLinea() == -1) {
                Toast.makeText(getContext(), "Por favor selecciona una línea", Toast.LENGTH_SHORT).show();
                return;
            }

            idActividad = selectedActividad.getIdActividad();
            idLinea = selectedLinea.getIdLinea();

            Bundle bundle = new Bundle();
            bundle.putInt("idOperario", idOperario);
            bundle.putString("nombreOperario", nombreOperario);
            bundle.putInt("idSupervisor", idSupervisor);
            bundle.putInt("idActividad", idActividad);
            bundle.putInt("idLinea", idLinea);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_auditoria, bundle);
        });

        getParentFragmentManager().setFragmentResultListener("needsReset", this, (requestKey, result) -> {
            if (result.getBoolean("reset", false)) {
                vm.limpiarTodosDatos();
                if (binding != null && binding.etLegajo != null) {
                    binding.etLegajo.setText("");
                }
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Verificar si venimos de otra vista y limpiar datos
        if (getArguments() == null) {  // Si no hay argumentos, significa que no es la primera carga
            vm.limpiarTodosDatos();
            if (binding != null && binding.etLegajo != null) {
                binding.etLegajo.setText("");
                tvNombreOp.setText("");
            }
            
            // Inicializar spinners con placeholders
            List<Actividad> actividadesIniciales = new ArrayList<>();
            actividadesIniciales.add(new Actividad(-1, "Actividad"));
            actividadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, actividadesIniciales);
            actividadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnActividad.setAdapter(actividadAdapter);

            List<Linea> lineasIniciales = new ArrayList<>();
            lineasIniciales.add(new Linea(-1, "Línea"));
            lineaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, lineasIniciales);
            lineaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnLinea.setAdapter(lineaAdapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
