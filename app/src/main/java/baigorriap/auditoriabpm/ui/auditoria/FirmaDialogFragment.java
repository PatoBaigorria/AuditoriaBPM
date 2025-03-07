package baigorriap.auditoriabpm.ui.auditoria;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.github.gcacace.signaturepad.views.SignaturePad;
import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.databinding.DialogFirmaBinding;

public class FirmaDialogFragment extends DialogFragment {
    private DialogFirmaBinding binding;
    private AuditoriaViewModel auditoriaViewModel;

    public static FirmaDialogFragment newInstance() {
        return new FirmaDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        auditoriaViewModel = new ViewModelProvider(requireActivity()).get(AuditoriaViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFirmaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar el SignaturePad
        binding.signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
            }

            @Override
            public void onSigned() {
                binding.btnGuardar.setEnabled(true);
            }

            @Override
            public void onClear() {
                binding.btnGuardar.setEnabled(false);
            }
        });

        // Configurar botones
        binding.btnLimpiar.setOnClickListener(v -> binding.signaturePad.clear());

        binding.btnCancelar.setOnClickListener(v -> {
            dismiss();
        });

        binding.btnGuardar.setOnClickListener(v -> {
            if (binding.signaturePad.isEmpty()) {
                Toast.makeText(requireContext(), "Se requiere una firma", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que haya items seleccionados
            if (!auditoriaViewModel.tieneItemsSeleccionados()) {
                Toast.makeText(requireContext(), "Debe seleccionar al menos un ítem", Toast.LENGTH_SHORT).show();
                return;
            }

            guardarFirma();
        });

        // Deshabilitar el botón guardar hasta que haya firma
        binding.btnGuardar.setEnabled(false);
    }

    private void guardarFirma() {
        Log.d("FirmaDialog", "Iniciando guardarFirma()");
        
        // Obtener el SVG de la firma
        String firmaSvg = binding.signaturePad.getSignatureSvg();
        if (firmaSvg == null || firmaSvg.isEmpty()) {
            Toast.makeText(requireContext(), "Error al obtener la firma", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asegurarnos de que el SVG tenga el formato correcto
        if (!firmaSvg.startsWith("<svg")) {
            firmaSvg = String.format("<svg xmlns=\"http://www.w3.org/2000/svg\">%s</svg>", firmaSvg);
        }

        boolean noConforme = binding.checkBoxNoConforme.isChecked();
        Log.d("FirmaDialog", "noConforme: " + noConforme);
        
        // Guardar la firma en el ViewModel
        auditoriaViewModel.setFirma(firmaSvg, noConforme);
        
        // Deshabilitar el botón para evitar múltiples clicks
        binding.btnGuardar.setEnabled(false);
        Log.d("FirmaDialog", "Botón guardar deshabilitado");
        
        // Guardar la auditoría usando el callback
        auditoriaViewModel.guardarAuditoria(new AuditoriaViewModel.GuardarAuditoriaCallback() {
            @Override
            public void onAuditoriaGuardada(boolean exitoso) {
                if (exitoso) {
                    Log.d("FirmaDialog", "Auditoría guardada exitosamente, preparando para cerrar diálogo");
                    
                    // Mostrar el toast de éxito
                    Toast.makeText(requireContext(), "Auditoría guardada con éxito", Toast.LENGTH_SHORT).show();
                    
                    // Esperar un momento para que se muestre el toast
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            if (isAdded() && !isRemoving()) {
                                Log.d("FirmaDialog", "Intentando cerrar el diálogo");
                                dismiss();
                                Log.d("FirmaDialog", "Diálogo cerrado exitosamente");
                                
                                // Navegar al fragmento principal
                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                                navController.navigate(R.id.action_auditoriaFragment_to_homeFragment);
                                Log.d("FirmaDialog", "Navegación completada");
                            } else {
                                Log.e("FirmaDialog", "No se puede cerrar el diálogo: Fragment no está agregado o está siendo removido");
                            }
                        } catch (Exception e) {
                            Log.e("FirmaDialog", "Error al cerrar diálogo o navegar", e);
                        }
                    }, 1000); // Esperar 1 segundo
                } else {
                    Log.d("FirmaDialog", "Error al guardar auditoría, re-habilitando botón");
                    binding.btnGuardar.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("FirmaDialog", "onDestroyView llamado");
        if (binding != null) {
            binding = null;
        }
    }
}
