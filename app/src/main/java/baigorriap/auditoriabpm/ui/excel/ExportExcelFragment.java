package baigorriap.auditoriabpm.ui.excel;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import baigorriap.auditoriabpm.databinding.FragmentExportExcelBinding;
import baigorriap.auditoriabpm.model.Auditoria;

public class ExportExcelFragment extends Fragment {

    private FragmentExportExcelBinding binding;
    private ExportExcelViewModel exportExcelViewModel;
    private Calendar calendarFrom = Calendar.getInstance(new Locale("es", "ES"));
    private Calendar calendarTo = Calendar.getInstance(new Locale("es", "ES"));
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
    private static final int STORAGE_PERMISSION_CODE = 100;
    private List<Auditoria> pendingAuditorias;
    private static final String TAG = "ExportExcelFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExportExcelBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        exportExcelViewModel = new ViewModelProvider(this).get(ExportExcelViewModel.class);

        setupDatePickers();
        setupExportButton();
        setupObservers();

        return root;
    }

    private void setupDatePickers() {
        binding.etDateFrom.setOnClickListener(v -> showDatePicker(true));
        binding.etDateTo.setOnClickListener(v -> showDatePicker(false));

        updateDateInView(true);
        updateDateInView(false);
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = isFromDate ? calendarFrom : calendarTo;
        Context context = requireContext();
        
        context.getResources().getConfiguration().setLocale(new Locale("es", "ES"));
        context.getResources().updateConfiguration(
            context.getResources().getConfiguration(),
            context.getResources().getDisplayMetrics()
        );

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            android.R.style.Theme_Material_Dialog,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInView(isFromDate);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Aceptar", datePickerDialog);
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", datePickerDialog);

        datePickerDialog.show();
    }

    private void updateDateInView(boolean isFromDate) {
        Calendar calendar = isFromDate ? calendarFrom : calendarTo;
        String formattedDate = dateFormatter.format(calendar.getTime());
        if (isFromDate) {
            binding.etDateFrom.setText(formattedDate);
        } else {
            binding.etDateTo.setText(formattedDate);
        }
    }

    private void setupExportButton() {
        binding.btnExport.setOnClickListener(v -> {
            String fromDate = dateFormatter.format(calendarFrom.getTime());
            String toDate = dateFormatter.format(calendarTo.getTime());
            exportExcelViewModel.fetchAuditorias(fromDate, toDate);
        });
    }

    private void setupObservers() {
        exportExcelViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnExport.setEnabled(!isLoading);
        });

        exportExcelViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        exportExcelViewModel.getAuditorias().observe(getViewLifecycleOwner(), auditorias -> {
            if (auditorias != null && !auditorias.isEmpty()) {
                pendingAuditorias = auditorias;
                checkStoragePermissionAndExport();
            }
        });
    }

    private void checkStoragePermissionAndExport() {
        // En Android 10 (API 29) y superior, no necesitamos permisos para escribir en Downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportToCSV(pendingAuditorias);
            return;
        }

        // Para versiones anteriores, necesitamos el permiso WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE
            );
        } else {
            exportToCSV(pendingAuditorias);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingAuditorias != null) {
                    exportToCSV(pendingAuditorias);
                }
            } else {
                Toast.makeText(requireContext(), 
                    "Se necesita permiso de almacenamiento para exportar el archivo", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void exportToCSV(List<Auditoria> auditorias) {
        try {
            Log.d(TAG, "Starting CSV export with " + auditorias.size() + " auditorias");
            String fileName = "auditorias_" + new SimpleDateFormat("dd-MM-yyyy", new Locale("es", "ES")).format(new Date()) + ".csv";
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Usar MediaStore para Android 10+
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = requireContext().getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream outputStream = resolver.openOutputStream(uri);
                         OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                        
                        // Escribir encabezados
                        String[] headers = {"ID Auditoria", "Fecha", "Supervisor", "Operario", "Linea", "Actividad", "Estado/Fima", "Comentario"};
                        writer.write(String.join(",", headers) + "\n");
                        Log.d(TAG, "Headers written");

                        // Escribir datos
                        for (Auditoria auditoria : auditorias) {
                            String[] data = {
                                String.valueOf(auditoria.getIdAuditoria()),
                                dateFormatter.format(auditoria.getFecha()),
                                String.valueOf(auditoria.getIdSupervisor()),
                                String.valueOf(auditoria.getIdOperario()),
                                String.valueOf(auditoria.getIdLinea()),
                                String.valueOf(auditoria.getIdActividad()),
                                auditoria.isNoConforme() ? "No Conforme" : "Conforme",
                                auditoria.getComentario() != null ? 
                                    "\"" + auditoria.getComentario().replace("\"", "\"\"") + "\"" : 
                                    ""
                            };
                            writer.write(String.join(",", data) + "\n");
                        }
                        writer.flush();
                        Log.d(TAG, "File saved successfully using MediaStore");
                    }
                }
            } else {
                // Método tradicional para Android 9 y anteriores
                File targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!targetDir.exists()) {
                    boolean created = targetDir.mkdirs();
                    Log.d(TAG, "Created directory: " + created);
                }

                File file = new File(targetDir, fileName);
                try (FileWriter writer = new FileWriter(file)) {
                    // Escribir encabezados
                    String[] headers = {"ID Auditoría", "Fecha", "Supervisor", "Operario", "Línea", "Actividad", "Estado", "Comentario"};
                    writer.write(String.join(",", headers) + "\n");
                    Log.d(TAG, "Headers written");

                    // Escribir datos
                    for (Auditoria auditoria : auditorias) {
                        String[] data = {
                            String.valueOf(auditoria.getIdAuditoria()),
                            dateFormatter.format(auditoria.getFecha()),
                            String.valueOf(auditoria.getIdSupervisor()),
                            String.valueOf(auditoria.getIdOperario()),
                            String.valueOf(auditoria.getIdLinea()),
                            String.valueOf(auditoria.getIdActividad()),
                            auditoria.isNoConforme() ? "No Conforme" : "Conforme",
                            auditoria.getComentario() != null ? 
                                "\"" + auditoria.getComentario().replace("\"", "\"\"") + "\"" : 
                                ""
                        };
                        writer.write(String.join(",", data) + "\n");
                    }
                    Log.d(TAG, "File saved successfully using traditional method");
                }
            }

            Toast.makeText(requireContext(), 
                "Archivo CSV guardado en Descargas: " + fileName, 
                Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e(TAG, "Error exporting to CSV", e);
            Toast.makeText(requireContext(), "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during export", e);
            Toast.makeText(requireContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
