package baigorriap.auditoriabpm.ui.excel;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import baigorriap.auditoriabpm.databinding.FragmentExportExcelBinding;
import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.ItemBPM;
import baigorriap.auditoriabpm.model.Linea;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.model.OperarioSinAuditoria;
import baigorriap.auditoriabpm.model.Supervisor;
import baigorriap.auditoriabpm.request.ApiClient;
import com.opencsv.CSVWriter;

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
            exportToCSV();
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
            exportToCSV();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingAuditorias != null) {
                    exportToCSV();
                }
            } else {
                Toast.makeText(requireContext(), 
                    "Se necesita permiso de almacenamiento para exportar el archivo", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void exportToCSV() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileName = "auditorias_" + timeStamp + ".csv";
        File file = new File(requireContext().getExternalFilesDir(null), fileName);
        CSVWriter writer = null;
        AtomicInteger procesados = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);
        AtomicBoolean writerClosed = new AtomicBoolean(false);

        try {
            writer = new CSVWriter(new FileWriter(file));
            final CSVWriter finalWriter = writer;

            // Escribir encabezados
            String[] encabezados = {"Fecha", "Supervisor", "Operario", "Actividad", "Línea", "Item BPM", "Estado", "Comentario"};
            writer.writeNext(encabezados);

            int totalItems = 0;
            for (Auditoria auditoria : pendingAuditorias) {
                totalItems += auditoria.getAuditoriaItems().size();
            }
            final int finalTotalItems = totalItems;

            Log.d(TAG, "Total de items a procesar: " + totalItems);
            CountDownLatch latch = new CountDownLatch(totalItems);
            Log.d(TAG, "Latch inicializado con " + totalItems + " items");

            Log.d(TAG, "Iniciando exportación a " + fileName);

            for (Auditoria auditoria : pendingAuditorias) {
                Log.d(TAG, "Procesando auditoría ID: " + auditoria.getIdAuditoria() + " con " + auditoria.getAuditoriaItems().size() + " items");
                
                for (AuditoriaItemBPM item : auditoria.getAuditoriaItems()) {
                    final Auditoria finalAuditoria = auditoria;
                    
                    obtenerDescripcionItemBPM(item.getIdItemBPM(), new OnItemBPMCargadoListener() {
                        @Override
                        public void onItemCargado(String descripcionItem) {
                            try {
                                if (!writerClosed.get()) {
                                    String[] campos = {
                                        dateFormatter.format(finalAuditoria.getFecha()),
                                        escaparCSV(supervisorNombre),
                                        escaparCSV(obtenerNombreOperario(finalAuditoria.getIdOperario())),
                                        escaparCSV(obtenerDescripcionActividad(finalAuditoria.getIdActividad())),
                                        escaparCSV(obtenerDescripcionLinea(finalAuditoria.getIdLinea())),
                                        escaparCSV(descripcionItem),
                                        item.getEstado() != null ? item.getEstado().toString() : "No especificado",
                                        escaparCSV(finalAuditoria.getComentario())
                                    };

                                    synchronized (finalWriter) {
                                        if (!writerClosed.get()) {
                                            finalWriter.writeNext(campos);
                                            procesados.incrementAndGet();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error escribiendo línea", e);
                                errores.incrementAndGet();
                            } finally {
                                latch.countDown();
                                updateProgress(procesados.get(), finalTotalItems);
                                checkExportCompletion(latch, finalWriter, writerClosed, procesados.get(), errores.get(), finalTotalItems, file);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al obtener item " + item.getIdItemBPM() + ": " + error);
                            errores.incrementAndGet();
                            latch.countDown();
                            updateProgress(procesados.get(), finalTotalItems);
                            checkExportCompletion(latch, finalWriter, writerClosed, procesados.get(), errores.get(), finalTotalItems, file);
                        }
                    });
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al crear archivo CSV", e);
            if (writer != null && !writerClosed.get()) {
                try {
                    writer.close();
                    writerClosed.set(true);
                } catch (IOException ex) {
                    Log.e(TAG, "Error al cerrar writer", ex);
                }
            }
            onExportError("Error al crear archivo CSV");
        }
    }

    private void checkExportCompletion(CountDownLatch latch, CSVWriter writer, AtomicBoolean writerClosed, 
                                     int procesados, int errores, int total, File file) {
        if (latch.getCount() == 0 && !writerClosed.get()) {
            synchronized (writer) {
                if (!writerClosed.get()) {
                    try {
                        writer.close();
                        writerClosed.set(true);
                        
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.progressText.setVisibility(View.GONE);
                            
                            String mensaje = errores > 0 
                                ? "Exportación completada con " + errores + " errores"
                                : "Archivo CSV exportado exitosamente";
                                
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                            
                            if (errores == 0) {
                                shareFile(file);
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error al cerrar writer", e);
                    }
                }
            }
        }
    }

    private void updateProgress(int procesados, int total) {
        requireActivity().runOnUiThread(() -> {
            binding.progressBar.setProgress(procesados);
            binding.progressText.setText("Procesando " + procesados + " de " + total + " items");
        });
    }

    private void onExportError(String mensaje) {
        requireActivity().runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.progressText.setVisibility(View.GONE);
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
        });
    }

    private void shareFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getApplicationContext().getPackageName() + ".provider",
                file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(Intent.createChooser(intent, "Abrir archivo CSV con..."));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), 
                    "No se encontró una aplicación para abrir archivos CSV", 
                    Toast.LENGTH_LONG).show();
                
                // Intentar compartir como alternativa
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/csv");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                try {
                    startActivity(Intent.createChooser(shareIntent, "Compartir archivo CSV"));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(requireContext(), 
                        "No se pudo compartir el archivo", 
                        Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al compartir archivo", e);
            Toast.makeText(requireContext(), 
                "Error al compartir archivo: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    private String escaparCSV(String texto) {
        if (texto == null) return "";
        // Si el texto contiene comas, comillas o saltos de línea, encerrarlo en comillas
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
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
        final int TOTAL_LOADS = 4; // actividades, líneas, supervisor y operarios

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

        // Cargar supervisor
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

        // Cargar todos los operarios
        ApiClient.getEndPoints().obtenerOperariosSinAuditorias("Bearer " + token).enqueue(new Callback<List<OperarioSinAuditoria>>() {
            @Override
            public void onResponse(Call<List<OperarioSinAuditoria>> call, Response<List<OperarioSinAuditoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (OperarioSinAuditoria operario : response.body()) {
                        operariosCache.put(operario.getIdOperario(), operario.getNombreCompleto());
                    }
                    Log.d(TAG, "Operarios cargados: " + operariosCache.size());
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<List<OperarioSinAuditoria>> call, Throwable t) {
                Log.e(TAG, "Error al cargar operarios", t);
                mostrarError("Error al cargar operarios: " + t.getMessage());
                checkAllLoaded.run();
            }
        });
    }

    private void obtenerDescripcionItemBPM(int idItemBPM, OnItemBPMCargadoListener listener) {
        Log.d(TAG, "Obteniendo descripción para item BPM ID: " + idItemBPM);
        
        // Primero intentar obtener del cache
        if (itemsBPMCache.containsKey(idItemBPM)) {
            String descripcion = itemsBPMCache.get(idItemBPM);
            Log.d(TAG, "Item BPM ID " + idItemBPM + " encontrado en caché: " + descripcion);
            listener.onItemCargado(descripcion);
            return;
        }

        // Si no está en cache, obtener de la API
        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            Log.e(TAG, "Token nulo al intentar obtener item BPM ID " + idItemBPM);
            listener.onItemCargado("Item BPM " + idItemBPM);
            return;
        }

        Log.d(TAG, "Solicitando item BPM ID " + idItemBPM + " a la API");
        ApiClient.getEndPoints().obtenerItemBPMPorId("Bearer " + token, idItemBPM).enqueue(new Callback<ItemBPM>() {
            @Override
            public void onResponse(Call<ItemBPM> call, Response<ItemBPM> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemBPM item = response.body();
                    itemsBPMCache.put(item.getIdItem(), item.getDescripcion());
                    Log.d(TAG, "Item BPM ID " + idItemBPM + " obtenido de API: " + item.getDescripcion());
                    listener.onItemCargado(item.getDescripcion());
                } else {
                    String descripcionPorDefecto = "Item BPM " + idItemBPM;
                    Log.e(TAG, "Error al obtener item ID " + idItemBPM + ". Código: " + response.code() + ". Error: " + response.errorBody());
                    listener.onItemCargado(descripcionPorDefecto);
                }
            }

            @Override
            public void onFailure(Call<ItemBPM> call, Throwable t) {
                Log.e(TAG, "Error en llamada de item " + idItemBPM, t);
                listener.onItemCargado("Item BPM " + idItemBPM);
            }
        });
    }

    private interface OnItemBPMCargadoListener {
        void onItemCargado(String descripcionItem);
        void onError(String error);
    }

    private String obtenerNombreOperario(int idOperario) {
        // Intentar obtener del cache
        String nombre = operariosCache.get(idOperario);
        if (nombre != null) {
            return nombre;
        }
        return "Operario " + idOperario;
    }

    private void mostrarError(String mensaje) {
        requireActivity().runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.progressText.setVisibility(View.GONE);
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
        });
    }

    private String obtenerNombreSupervisor(int idSupervisor) {
        return supervisorNombre != null ? supervisorNombre : "Supervisor " + idSupervisor;
    }

    private String obtenerDescripcionActividad(int idActividad) {
        return actividadesCache.getOrDefault(idActividad, "Actividad " + idActividad);
    }

    private String obtenerDescripcionLinea(int idLinea) {
        return lineasCache.getOrDefault(idLinea, "Línea " + idLinea);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
