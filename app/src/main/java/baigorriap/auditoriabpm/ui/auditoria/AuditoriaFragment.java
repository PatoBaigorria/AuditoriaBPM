package baigorriap.auditoriabpm.ui.auditoria;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
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
        tableLayout = binding.tableItems;

        // Obtén los datos del Bundle pasado desde HomeFragment
        if (getArguments() != null) {
            idSupervisor = getArguments().getInt("idSupervisor", -1);
            idOperario = getArguments().getInt("idOperario", -1);
            idActividad = getArguments().getInt("idActividad", -1);
            idLinea = getArguments().getInt("idLinea", -1);
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

    private void guardarAuditoria() {
        Log.d("GuardarAuditoria", "Se está ejecutando el método guardarAuditoria"); // Log agregado
        boolean todosSeleccionados = true;

        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            RadioGroup radioGroup = row.findViewById(getResources().getIdentifier("radioGroup" + i, "id", requireContext().getPackageName()));
            // Verificar si el RadioGroup tiene algún elemento seleccionado
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                todosSeleccionados = false;
                break;
            }
        }

        if (!todosSeleccionados) {
            // Mostrar un mensaje si algún ítem no tiene selección
            Toast.makeText(getContext(), "Debe seleccionar un estado para cada ítem", Toast.LENGTH_SHORT).show();
        } else {
            // Si todos los ítems están seleccionados, procede a guardar
            Auditoria auditoria = new Auditoria();
            auditoria.setIdSupervisor(idSupervisor);
            auditoria.setIdOperario(idOperario);
            auditoria.setIdActividad(idActividad);
            auditoria.setIdLinea(idLinea);
            auditoria.setFecha(new Date());
            auditoria.setComentario("Comentario de prueba");

            List<AuditoriaItemBPM> itemsSeleccionados = auditoriaViewModel.getMListaItemsSeleccionados().getValue();
            if (itemsSeleccionados == null) {
                itemsSeleccionados = new ArrayList<>();
            }

            List<ItemAuditoriaRequest> itemsRequest = new ArrayList<>();
            for (AuditoriaItemBPM item : itemsSeleccionados) {
                ItemAuditoriaRequest requestItem = new ItemAuditoriaRequest();
                requestItem.setIdItemBPM(item.getIdItemBPM());
                requestItem.setEstado(item.getEstado().name());
                requestItem.setComentario(item.getComentario());
                itemsRequest.add(requestItem);
            }
            auditoriaViewModel.guardarAuditoria(idOperario, idSupervisor, idActividad, idLinea, auditoria.getComentario(), itemsRequest);
            
            requireView().postDelayed(() -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.action_auditoriaFragment_to_homeFragment);
            }, 2000);  // 2000 ms = 2 segundos
        }
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
