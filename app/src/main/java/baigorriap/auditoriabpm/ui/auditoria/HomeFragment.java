package baigorriap.auditoriabpm.ui.auditoria;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    private int idSupervisor;
    private int idOperario;
    private int idActividad;
    private int idLinea;
    private SharedPreferences sharedPreferences;

    private void hideKeyboard() {
        try {
            Activity activity = getActivity();
            if (activity != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = activity.getCurrentFocus();
                if (focusedView != null) {
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error al ocultar el teclado: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        vm = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idSupervisor = sharedPreferences.getInt("idSupervisor", 0);

        spnActividad = binding.spnActividad;
        spnLinea = binding.spnLinea;

        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        binding.etLegajo.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        binding.etLegajo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                String legajo = binding.etLegajo.getText().toString().trim();
                if (!legajo.isEmpty()) {
                    try {
                        int legajoNum = Integer.parseInt(legajo);
                        if (legajoNum > 0) {
                            vm.cargarOperarioPorLegajo(legajoNum);
                            vm.cargarDatosPorLegajo(legajoNum);
                            return true;
                        } else {
                            binding.etLegajo.setError("El legajo debe ser mayor a 0");
                        }
                    } catch (NumberFormatException e) {
                        binding.etLegajo.setError("Ingrese un número válido");
                    }
                } else {
                    binding.etLegajo.setError("Ingrese un legajo");
                }
            }
            return false;
        });

        binding.btSiguiente.setOnClickListener(v -> {
            hideKeyboard();
            String nombreOperario = tvNombreOp.getText().toString();
            if (nombreOperario.isEmpty()) {
                Toast.makeText(getContext(), "El nombre del operario está vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            
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

        vm.getMOperario().observe(getViewLifecycleOwner(), operario -> {
            if (operario != null) {
                tvNombreOp.setText(operario.getNombreCompleto());
                idOperario = operario.getIdOperario();
                binding.etLegajo.setError(null);
            } else {
                tvNombreOp.setText("");
                idOperario = 0;
                String legajoInput = binding.etLegajo.getText().toString().trim();
                if (!legajoInput.isEmpty()) {
                    binding.etLegajo.setError("No se encontró ningún operario con este legajo");
                }
                vm.limpiarSpinnersConHint();
            }
        });

        vm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                binding.etLegajo.setError(error);
            }
        });

        EditText etLegajo = binding.etLegajo;
        etLegajo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (input.isEmpty()) {
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
                    binding.etLegajo.setError(null);
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

        vm.getMListaActividad().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null && !actividades.isEmpty()) {
                actividadAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, actividades);
                actividadAdapter.setDropDownViewResource(R.layout.spinner_item);
                spnActividad.setAdapter(actividadAdapter);
                
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

        vm.getMListaLinea().observe(getViewLifecycleOwner(), lineas -> {
            if (lineas != null && !lineas.isEmpty()) {
                lineaAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, lineas);
                lineaAdapter.setDropDownViewResource(R.layout.spinner_item);
                spnLinea.setAdapter(lineaAdapter);
                
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

        getParentFragmentManager().setFragmentResultListener("needsReset", this, (requestKey, result) -> {
            if (result.getBoolean("reset", false)) {
                vm.limpiarTodosDatos();
                if (binding != null && binding.etLegajo != null) {
                    binding.etLegajo.setText("");
                }
            }
        });

        vm.limpiarSpinnersConHint();
        
        List<Actividad> actividadesIniciales = new ArrayList<>();
        actividadesIniciales.add(new Actividad(-1, "Actividad"));
        actividadAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, actividadesIniciales);
        actividadAdapter.setDropDownViewResource(R.layout.spinner_item);
        spnActividad.setAdapter(actividadAdapter);

        List<Linea> lineasIniciales = new ArrayList<>();
        lineasIniciales.add(new Linea(-1, "Línea"));
        lineaAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, lineasIniciales);
        lineaAdapter.setDropDownViewResource(R.layout.spinner_item);
        spnLinea.setAdapter(lineaAdapter);

        final TextView textView = binding.textHome;
        vm.getText().observe(getViewLifecycleOwner(), textView::setText);
        tvNombreOp = binding.tvNombreOp;

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() == null) {
            vm.limpiarTodosDatos();
            if (binding != null && binding.etLegajo != null) {
                binding.etLegajo.setText("");
                tvNombreOp.setText("");
            }
            
            List<Actividad> actividadesIniciales = new ArrayList<>();
            actividadesIniciales.add(new Actividad(-1, "Actividad"));
            actividadAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, actividadesIniciales);
            actividadAdapter.setDropDownViewResource(R.layout.spinner_item);
            spnActividad.setAdapter(actividadAdapter);

            List<Linea> lineasIniciales = new ArrayList<>();
            lineasIniciales.add(new Linea(-1, "Línea"));
            lineaAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, lineasIniciales);
            lineaAdapter.setDropDownViewResource(R.layout.spinner_item);
            spnLinea.setAdapter(lineaAdapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
