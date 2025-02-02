package baigorriap.auditoriabpm.ui.auditoria;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentAuditoriaBinding;
import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.ItemAuditoriaRequest;
import baigorriap.auditoriabpm.model.Operario;

public class AuditoriaFragment extends Fragment {

    private FragmentAuditoriaBinding binding;
    private AuditoriaViewModel auditoriaViewModel;
    private TableLayout tableLayout;
    private int idOperario;
    private int idActividad;
    private int idLinea;
    private int idSupervisor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAuditoriaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar el ViewModel a nivel de actividad
        auditoriaViewModel = new ViewModelProvider(requireActivity()).get(AuditoriaViewModel.class);

        // Configurar los observadores
        configurarObservadores();

        // Configurar los botones
        configurarBotones();

        return root;
    }

    private void configurarObservadores() {
        // Configurar observadores para el operario cargado
        auditoriaViewModel.getOperario().observe(getViewLifecycleOwner(), operario -> {
            if (operario != null) {
                idActividad = operario.getIdActividad();
                idLinea = operario.getIdLinea();
            }
        });

        // Observadores para los LiveData del ViewModel
        auditoriaViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        auditoriaViewModel.getMAuditoriaGuardada().observe(getViewLifecycleOwner(), isGuardada -> {
            if (isGuardada != null && isGuardada) {  // Solo muestra el Toast si es verdadero
                Toast.makeText(getContext(), "Auditoría guardada con éxito", Toast.LENGTH_SHORT).show();
                auditoriaViewModel.setMAuditoriaGuardada(false);  // Reseteamos el valor a false después de mostrar el Toast
            }
        });

        auditoriaViewModel.getMListaItemsSeleccionados().observe(getViewLifecycleOwner(), itemsSeleccionados -> {
            if (itemsSeleccionados != null && !itemsSeleccionados.isEmpty()) {
                Log.d("AuditoriaFragment", "Ítems seleccionados: " + itemsSeleccionados.size());
            }
        });
    }

    private void configurarBotones() {
        binding.imgBtnCancelar.setOnClickListener(v -> {
            // Notificar al HomeFragment que necesita resetear
            Bundle result = new Bundle();
            result.putBoolean("reset", true);
            getParentFragmentManager().setFragmentResult("needsReset", result);
            
            // Navegar hacia atrás
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigateUp();
        });

        binding.imgBtnComentarios.setOnClickListener(v -> {
            // Mostrar diálogo para agregar comentarios
            // TODO: Implementar diálogo de comentarios
        });

        binding.imgBtnFirma.setOnClickListener(v -> {
            // Validar que haya un operario cargado
            if (idOperario == 0) {
                Toast.makeText(requireContext(), "Debe cargar un operario", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que haya items seleccionados
            if (!auditoriaViewModel.tieneItemsSeleccionados()) {
                Toast.makeText(requireContext(), "Debe seleccionar al menos un ítem", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que todos los items tengan un estado seleccionado
            if (!auditoriaViewModel.todosLosItemsTienenEstado()) {
                Toast.makeText(requireContext(), "Debe seleccionar un estado para todos los ítems", Toast.LENGTH_SHORT).show();
                return;
            }

            FirmaDialogFragment dialogFragment = FirmaDialogFragment.newInstance();
            dialogFragment.show(getChildFragmentManager(), "FirmaDialog");
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa tableLayout
        tableLayout = binding.tableItems;

        // Obtener datos de los argumentos
        if (getArguments() != null) {
            idOperario = getArguments().getInt("idOperario", 0);
            idSupervisor = getArguments().getInt("idSupervisor", 0);
            idActividad = getArguments().getInt("idActividad", 0);
            idLinea = getArguments().getInt("idLinea", 0);
            
            // Establecer el nombre del operario
            String nombreOperario = getArguments().getString("nombreOperario");
            if (nombreOperario != null && !nombreOperario.isEmpty()) {
                binding.tvCampoNomb.setText(nombreOperario);
            }

            // Crear y establecer el operario en el ViewModel
            Operario operario = new Operario();
            operario.setIdOperario(idOperario);
            operario.setIdActividad(idActividad);
            operario.setIdLinea(idLinea);
            operario.setNombre(nombreOperario);
            auditoriaViewModel.setOperario(operario);
        }
        
        // Configurar la fecha actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        binding.tvCampoFecha.setText(currentDate);

        // Inicializar vistas y configurar listeners
        inicializarVistas();
        configurarListeners();
    }

    private void inicializarVistas() {
        // Obtener y mostrar la fecha actual
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(currentDate);
        binding.tvCampoFecha.setText(formattedDate);

        // Establecer el total de items en el ViewModel
        int totalItems = tableLayout.getChildCount() - 1; // -1 porque la primera fila es el encabezado
        auditoriaViewModel.setTotalItems(totalItems);

        // Lógica para iterar sobre los RadioGroups en el TableLayout
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            if (row != null) {
                String radioGroupId = "radioGroup" + i;
                int resId = getResources().getIdentifier(radioGroupId, "id", requireContext().getPackageName());
                RadioGroup radioGroup = row.findViewById(resId);

                if (radioGroup != null) {
                    // Configurar el listener para cada RadioGroup
                    radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        if (checkedId != -1) {
                            RadioButton selectedRadioButton = row.findViewById(checkedId);
                            String tag = selectedRadioButton.getTag().toString();

                            // Obtener el id del item de la fila
                            int idItem = obtenerIdDelItem(row);
                            if (idItem != -1) {
                                AuditoriaItemBPM.EstadoEnum estado = AuditoriaItemBPM.EstadoEnum.valueOf(tag);
                                auditoriaViewModel.seleccionarEstado(idItem, estado);
                            }
                        }
                    });
                } else {
                    Log.d("RadioGroupDebug", "No se encontró RadioGroup con ID: " + radioGroupId);
                }
            }
        }
    }

    private void configurarListeners() {
        binding.imgBtnCancelar.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancelar Auditoría")
                    .setMessage("¿Estás seguro de que quieres cancelar esta auditoría?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Notificar al HomeFragment que necesita resetear
                        Bundle result = new Bundle();
                        result.putBoolean("reset", true);
                        getParentFragmentManager().setFragmentResult("needsReset", result);
                        
                        // Navegar hacia atrás
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigateUp();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        binding.imgBtnComentarios.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Escribe un comentario");

            // EditText para el comentario
            final EditText input = new EditText(requireContext());
            input.setHint("Ingrese comentario aquí");
            builder.setView(input);

            builder.setPositiveButton("Aceptar", (dialog, which) -> {
                String comentario = input.getText().toString();
                auditoriaViewModel.setComentario(comentario);
                Toast.makeText(getContext(), "Comentario guardado temporalmente", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private int obtenerIdDelItem(TableRow row) {
        if (row.getTag() != null) {
            try {
                return Integer.parseInt(row.getTag().toString());
            } catch (NumberFormatException e) {
                Log.e("TableRowError", "El tag no es un número válido: " + row.getTag());
            }
        }
        return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
