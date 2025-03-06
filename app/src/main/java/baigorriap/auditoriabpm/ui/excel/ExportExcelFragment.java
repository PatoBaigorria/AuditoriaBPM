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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import baigorriap.auditoriabpm.databinding.FragmentExportExcelBinding;
import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.ItemBPM;
import baigorriap.auditoriabpm.model.Linea;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.model.Supervisor;
import baigorriap.auditoriabpm.request.ApiClient;

public class ExportExcelFragment extends Fragment {

    private FragmentExportExcelBinding binding;
    private ExportExcelViewModel exportExcelViewModel;
    private Calendar calendarFrom = Calendar.getInstance(new Locale("es", "ES"));
    private Calendar calendarTo = Calendar.getInstance(new Locale("es", "ES"));
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
    private static final int STORAGE_PERMISSION_CODE = 100;
    private List<Auditoria> pendingAuditorias;
    private static final String TAG = "ExportExcelFragment";

    private Map<Integer, String> actividadesCache = new HashMap<>();
    private Map<Integer, String> lineasCache = new HashMap<>();
    private Map<Integer, String> operariosCache = new HashMap<>();
    private Map<Integer, String> itemsBPMCache = new HashMap<>();
    private String supervisorNombre;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarDatosNecesarios();
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
            Log.d(TAG, "Iniciando exportación CSV con " + auditorias.size() + " auditorías");
            binding.progressBar.setMax(auditorias.size());
            binding.progressText.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressText.setText("Iniciando exportación...");
            int procesadas = 0;

            String fileName = "auditorias_" + new SimpleDateFormat("dd-MM-yyyy", new Locale("es", "ES")).format(new Date()) + ".html";
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/html");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = requireContext().getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream outputStream = resolver.openOutputStream(uri);
                         OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {

                        // Escribir encabezado del archivo
                        writer.write("<html><head><meta charset='UTF-8'></head><body>\n");
                        writer.write("<h1>REPORTE DE AUDITORÍAS</h1>\n");
                        writer.write("<p>Fecha de exportación: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("es", "ES")).format(new Date()) + "</p>\n");
                        writer.write("<p>Total de auditorías: " + auditorias.size() + "</p>\n");

                        // Procesar cada auditoría
                        for (Auditoria auditoria : auditorias) {
                            procesadas++;
                            final int finalProcesadas = procesadas;
                            requireActivity().runOnUiThread(() -> {
                                binding.progressBar.setProgress(finalProcesadas);
                                binding.progressText.setText("Procesando auditoría " + finalProcesadas + " de " + auditorias.size());
                            });

                            try {
                                String supervisorNombre = obtenerNombreSupervisor(auditoria.getIdSupervisor());
                                String lineaDesc = obtenerDescripcionLinea(auditoria.getIdLinea());
                                List<AuditoriaItemBPM> items = auditoria.getAuditoriaItems();

                                // Escribir tabla HTML para esta auditoría
                                writer.write("<table border='1' style='border-collapse: collapse; width: 100%;'>\n");
                                
                                // Encabezado de la auditoría
                                writer.write("<tr bgcolor='#CCCCCC'><th colspan='2' style='text-align: center;'>AUDITORIA</th></tr>\n");
                                writer.write(String.format("<tr><td><b>Fecha:</b></td><td>%s</td></tr>\n", auditoria.getFecha()));
                                writer.write(String.format("<tr><td><b>Supervisor:</b></td><td>%s</td></tr>\n", supervisorNombre));
                                writer.write(String.format("<tr><td><b>Linea:</b></td><td>%s</td></tr>\n", lineaDesc));
                                if (auditoria.getComentario() != null && !auditoria.getComentario().isEmpty()) {
                                    writer.write(String.format("<tr><td><b>Comentario:</b></td><td>%s</td></tr>\n", escaparHTML(auditoria.getComentario())));
                                }
                                writer.write("<tr><td colspan='2'>&nbsp;</td></tr>\n"); // Espacio en blanco
                                
                                // Encabezados de las columnas de items
                                writer.write("<tr bgcolor='#EEEEEE'>\n");
                                writer.write("<th>Item</th>\n");
                                writer.write("<th>Estado</th>\n");
                                writer.write("</tr>\n");

                                // Escribir items
                                for (AuditoriaItemBPM item : items) {
                                    cargarItemBPM(item.getIdItemBPM(), new OnItemBPMCargadoListener() {
                                        @Override
                                        public void onItemCargado(String descripcionItem) {
                                            try {
                                                writer.write("<tr>\n");
                                                writer.write(String.format("<td>%s</td>\n", escaparHTML(descripcionItem)));
                                                writer.write(String.format("<td>%s</td>\n", escaparHTML(item.getEstado().toString())));
                                                writer.write("</tr>\n");
                                                writer.flush();
                                            } catch (IOException e) {
                                                Log.e(TAG, "Error escribiendo item", e);
                                            }
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Log.e(TAG, "Error cargando item: " + error);
                                            try {
                                                writer.write("<tr>\n");
                                                writer.write("<td>Error al cargar item</td>\n");
                                                writer.write(String.format("<td>%s</td>\n", escaparHTML(item.getEstado().toString())));
                                                writer.write("</tr>\n");
                                                writer.flush();
                                            } catch (IOException e) {
                                                Log.e(TAG, "Error escribiendo item con error", e);
                                            }
                                        }
                                    });
                                }

                                writer.write("</table>\n");
                                writer.write("<br><br>\n"); // Espacio entre auditorías
                                writer.flush(); // Asegurar que se escriban los datos
                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando auditoría " + auditoria.getIdAuditoria(), e);
                            }
                        }

                        // Finalizar
                        writer.write("</body></html>\n");
                        writer.flush();
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.progressText.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Archivo exportado exitosamente", Toast.LENGTH_LONG).show();
                        });

                    } catch (IOException e) {
                        Log.e(TAG, "Error escribiendo archivo", e);
                        mostrarError("Error al escribir el archivo: " + e.getMessage());
                    }
                } else {
                    mostrarError("No se pudo crear el archivo");
                }
            } else {
                // Código para Android 9 y anteriores
                File targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                File file = new File(targetDir, fileName);
                try (FileWriter writer = new FileWriter(file)) {
                    // Mismo código que arriba...
                    // ... (implementar la misma lógica para versiones anteriores)
                } catch (IOException e) {
                    Log.e(TAG, "Error escribiendo archivo", e);
                    mostrarError("Error al escribir el archivo: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error general en exportación", e);
            mostrarError("Error en la exportación: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        requireActivity().runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.progressText.setVisibility(View.GONE);
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
        });
    }

    private String escaparHTML(String texto) {
        if (texto == null) return "";
        // Reemplazar caracteres especiales que pueden causar problemas en HTML
        texto = texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        
        return texto;
    }

    private void cargarDatosNecesarios() {
        String token = ApiClient.leerToken(requireContext());
        if (token == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressText.setVisibility(View.VISIBLE);
        binding.progressText.setText("Cargando datos necesarios...");
        binding.btnExport.setEnabled(false);

        // Contador para saber cuándo se han completado todas las cargas
        final int[] loadCount = {0};
        final int TOTAL_LOADS = 3; // actividades, líneas y supervisor

        Runnable checkAllLoaded = () -> {
            loadCount[0]++;
            if (loadCount[0] == TOTAL_LOADS) {
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.progressText.setVisibility(View.GONE);
                    binding.btnExport.setEnabled(true);
                });
            }
        };

        // Cargar actividades
        ApiClient.getEndPoints().obtenerTodasLasActividades("Bearer " + token).enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Actividad actividad : response.body()) {
                        actividadesCache.put(actividad.getIdActividad(), actividad.getDescripcion());
                    }
                    Log.d(TAG, "Actividades cargadas: " + actividadesCache.size());
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Log.e(TAG, "Error al cargar actividades", t);
                mostrarError("Error al cargar actividades: " + t.getMessage());
                checkAllLoaded.run();
            }
        });

        // Cargar líneas
        ApiClient.getEndPoints().obtenerTodasLasLineas("Bearer " + token).enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Linea linea : response.body()) {
                        lineasCache.put(linea.getIdLinea(), linea.getDescripcion());
                    }
                    Log.d(TAG, "Líneas cargadas: " + lineasCache.size());
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Log.e(TAG, "Error al cargar líneas", t);
                mostrarError("Error al cargar líneas: " + t.getMessage());
                checkAllLoaded.run();
            }
        });

        // Cargar datos del supervisor actual
        ApiClient.getEndPoints().miPerfil("Bearer " + token).enqueue(new Callback<Supervisor>() {
            @Override
            public void onResponse(Call<Supervisor> call, Response<Supervisor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Supervisor supervisor = response.body();
                    supervisorNombre = supervisor.getNombre() + " " + supervisor.getApellido();
                    Log.d(TAG, "Supervisor cargado: " + supervisorNombre);
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<Supervisor> call, Throwable t) {
                Log.e(TAG, "Error al cargar supervisor", t);
                mostrarError("Error al cargar supervisor: " + t.getMessage());
                checkAllLoaded.run();
            }
        });
    }

    private void cargarOperario(int idOperario, OnOperarioCargadoListener listener) {
        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            listener.onError("No se encontró el token");
            return;
        }

        if (operariosCache.containsKey(idOperario)) {
            listener.onOperarioCargado(operariosCache.get(idOperario));
            return;
        }

        ApiClient.getEndPoints().obtenerOperario("Bearer " + token, idOperario).enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();
                    String nombreCompleto = operario.getNombre() + " " + operario.getApellido();
                    operariosCache.put(idOperario, nombreCompleto);
                    listener.onOperarioCargado(nombreCompleto);
                } else {
                    listener.onError("Error al cargar operario: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Log.e(TAG, "Error al cargar operario", t);
                listener.onError("Error al cargar operario: " + t.getMessage());
            }
        });
    }

    private void cargarItemBPM(int idItemBPM, OnItemBPMCargadoListener listener) {
        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            listener.onError("No se encontró el token");
            return;
        }

        if (itemsBPMCache.containsKey(idItemBPM)) {
            listener.onItemCargado(itemsBPMCache.get(idItemBPM));
            return;
        }

        ApiClient.getEndPoints().obtenerItemBPMPorId("Bearer " + token, idItemBPM).enqueue(new Callback<ItemBPM>() {
            @Override
            public void onResponse(Call<ItemBPM> call, Response<ItemBPM> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemBPM item = response.body();
                    itemsBPMCache.put(idItemBPM, item.getDescripcion());
                    listener.onItemCargado(item.getDescripcion());
                } else {
                    listener.onError("Error al cargar item: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ItemBPM> call, Throwable t) {
                Log.e(TAG, "Error al cargar item", t);
                listener.onError("Error al cargar item: " + t.getMessage());
            }
        });
    }

    private interface OnOperarioCargadoListener {
        void onOperarioCargado(String nombreOperario);
        void onError(String error);
    }

    private interface OnItemBPMCargadoListener {
        void onItemCargado(String descripcionItem);
        void onError(String error);
    }

    private String obtenerNombreSupervisor(int idSupervisor) {
        return supervisorNombre != null ? supervisorNombre : "Supervisor " + idSupervisor;
    }

    private void obtenerNombreOperario(int idOperario, OnOperarioCargadoListener listener) {
        cargarOperario(idOperario, listener);
    }

    private String obtenerDescripcionActividad(int idActividad) {
        return actividadesCache.getOrDefault(idActividad, "Actividad " + idActividad);
    }

    private String obtenerDescripcionLinea(int idLinea) {
        return lineasCache.getOrDefault(idLinea, "Línea " + idLinea);
    }

    private void obtenerDescripcionItem(int idItemBPM, OnItemBPMCargadoListener listener) {
        cargarItemBPM(idItemBPM, listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
