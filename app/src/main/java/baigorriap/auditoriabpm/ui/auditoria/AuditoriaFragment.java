package baigorriap.auditoriabpm.ui.auditoria;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.FragmentAuditoriaBinding;
import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.ItemAuditoriaRequest;
import baigorriap.auditoriabpm.model.ItemBPM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AuditoriaFragment extends Fragment {

    private FragmentAuditoriaBinding binding;
    private AuditoriaViewModel auditoriaViewModel;
    private int idSupervisor;
    private int idOperario;
    private int idActividad;
    private int idLinea;
    private TableLayout tableLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuditoriaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializa el ViewModel
        auditoriaViewModel = new ViewModelProvider(this).get(AuditoriaViewModel.class);

        // Inicializa tableLayout
        tableLayout = binding.tableItems; // Asegúrate de que este ID corresponde a tu XML

        // Obtén los datos del Bundle pasado desde HomeFragment
        if (getArguments() != null) {
            idSupervisor = getArguments().getInt("idSupervisor", -1);
            idOperario = getArguments().getInt("idOperario", -1);
            idActividad = getArguments().getInt("idActividad", -1);
            idLinea = getArguments().getInt("idLinea", -1);

            // Obtener el nombre del operario y establecerlo en el TextView
            String nombreOperario = getArguments().getString("nombreOperario");
            binding.tvCampoNomb.setText(nombreOperario != null ? nombreOperario : "Nombre no disponible");
        }

        // Obtener y mostrar la fecha actual
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(currentDate);
        binding.tvCampoFecha.setText(formattedDate);

        // Lógica para iterar sobre los RadioGroups en el TableLayout
        for (int i = 1; i < tableLayout.getChildCount(); i++) {  // Comienza desde 1 para evitar el encabezado
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            if (row != null) {
                Log.d("RadioGroupDebug", "Fila " + i + " encontrada: " + row.toString());

                // Asegúrate de que el RadioGroup tenga un ID como radioGroup1, radioGroup2, etc.
                String radioGroupId = "radioGroup" + i;  // Usamos la numeración adecuada para las filas de datos
                int resId = getResources().getIdentifier(radioGroupId, "id", requireContext().getPackageName());
                RadioGroup radioGroup = row.findViewById(resId);

                if (radioGroup != null) {
                    Log.d("RadioGroupDebug", "RadioGroup encontrado: " + radioGroupId);

                    // Configurar el listener para cada RadioGroup
                    radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        if (checkedId != -1) {
                            RadioButton selectedRadioButton = row.findViewById(checkedId);
                            String tag = selectedRadioButton.getTag().toString();
                            Log.d("RadioGroupDebug", "RadioButton seleccionado: " + tag);  // Verificación del log

                            // Obtener el id del item de la fila
                            int idItem = obtenerIdDelItem(row);
                            if (idItem != -1) {
                                // Llama a tu método en el ViewModel para manejar la selección
                                AuditoriaItemBPM.EstadoEnum estado = AuditoriaItemBPM.EstadoEnum.valueOf(tag);
                                Log.d("RadioGroupDebug", "Estado seleccionado: " + estado);  // Verificación del log
                                auditoriaViewModel.seleccionarEstado(idItem, estado);  // Llamada al ViewModel
                            }
                        }
                    });
                } else {
                    Log.d("RadioGroupDebug", "No se encontró RadioGroup con ID: " + radioGroupId);
                }
            }
        }



        // Observadores para los LiveData del ViewModel
        setupObservers();

        // Configurar el botón "Guardar"
        binding.imgBtnGuardar.setOnClickListener(v -> guardarAuditoria());

        binding.imgBtnCancelar.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancelar Auditoría")
                    .setMessage("¿Estás seguro de que quieres cancelar esta auditoría?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Navega al HomeFragment
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigate(R.id.action_auditoriaFragment_to_homeFragment);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        return root;
    }

    private void setupObservers() {
        auditoriaViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        auditoriaViewModel.getMAuditoriaGuardada().observe(getViewLifecycleOwner(), isGuardada -> {
            String mensaje = isGuardada ? "Auditoría guardada con éxito" : "Error al guardar la auditoría";
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        });

        auditoriaViewModel.getMListaItemsSeleccionados().observe(getViewLifecycleOwner(), itemsSeleccionados -> {
            if (itemsSeleccionados != null && !itemsSeleccionados.isEmpty()) {
                Log.d("AuditoriaFragment", "Ítems seleccionados: " + itemsSeleccionados.size());
            } else {
                Toast.makeText(getContext(), "No hay ítems seleccionados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarAuditoria() {
        Auditoria auditoria = new Auditoria();
        auditoria.setIdSupervisor(idSupervisor);
        auditoria.setIdOperario(idOperario);
        auditoria.setIdActividad(idActividad);
        auditoria.setIdLinea(idLinea);
        auditoria.setFecha(new Date());
        auditoria.setComentario("Comentario de prueba");

        // Obtén la lista de ítems seleccionados
        List<AuditoriaItemBPM> itemsSeleccionados = auditoriaViewModel.getMListaItemsSeleccionados().getValue();
        if (itemsSeleccionados == null) {
            itemsSeleccionados = new ArrayList<>();
        }

        // Convertir itemsSeleccionados a una lista de ItemAuditoriaRequest
        List<ItemAuditoriaRequest> itemsRequest = new ArrayList<>();
        for (AuditoriaItemBPM item : itemsSeleccionados) {
            ItemAuditoriaRequest requestItem = new ItemAuditoriaRequest();
            requestItem.setIdItemBPM(item.getIdItemBPM());
            requestItem.setEstado(item.getEstado().name()); // Asegúrate de que el estado esté en el formato correcto
            requestItem.setComentario(item.getComentario());
            itemsRequest.add(requestItem);
        }
        auditoriaViewModel.guardarAuditoria(idOperario, idSupervisor, idActividad, idLinea, auditoria.getComentario(), itemsRequest);
    }

    private int obtenerIdDelItem(TableRow row) {
        if (row.getTag() != null) {
            try {
                return Integer.parseInt(row.getTag().toString());
            } catch (NumberFormatException e) {
                Log.e("TableRowError", "El tag no es un número válido: " + row.getTag());
            }
        }
        return -1;  // Valor de error si no hay un tag válido
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
