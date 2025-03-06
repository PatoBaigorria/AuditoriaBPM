package baigorriap.auditoriabpm.ui.excel;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM.EstadoEnum;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import baigorriap.auditoriabpm.databinding.FragmentExportExcelBinding;
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
    private Map<Integer, String> itemsBPMCache = new HashMap<>();
    private Map<Integer, String> supervisoresCache = new HashMap<>();
    private Map<Integer, String> operariosCache = new HashMap<>();
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
        File file = new File(requireContext().getCacheDir(), fileName);
        Log.d(TAG, "Creando archivo CSV en: " + file.getAbsolutePath());
        
        exportarAuditorias(pendingAuditorias, file);
    }

    private void exportarAuditorias(List<Auditoria> pendingAuditorias, File file) {
        Log.d(TAG, "Iniciando exportación de " + pendingAuditorias.size() + " auditorías");
        
        CSVWriter writer = null;
        AtomicInteger procesados = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);
        AtomicBoolean writerClosed = new AtomicBoolean(false);

        try {
            writer = new CSVWriter(new OutputStreamWriter(
                new FileOutputStream(file), 
                "Windows-1252"
            ));
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

            Log.d(TAG, "Iniciando exportación a " + file.getName());

            for (Auditoria auditoria : pendingAuditorias) {
                Log.d(TAG, "Procesando auditoría ID: " + auditoria.getIdAuditoria() + " con " + auditoria.getAuditoriaItems().size() + " items");
                
                for (AuditoriaItemBPM item : auditoria.getAuditoriaItems()) {
                    procesarItem(finalWriter, auditoria, item, latch);
                }
            }

            // Asegurarnos de que el archivo se cierre correctamente
            final File finalFile = file;
            new Thread(() -> {
                try {
                    // Esperar a que todos los items se procesen con un timeout de 30 segundos
                    if (latch.await(30, TimeUnit.SECONDS)) {
                        Log.d(TAG, "Todos los items procesados correctamente");
                    } else {
                        Log.w(TAG, "Timeout esperando procesamiento de items");
                    }
                    checkExportCompletion(latch, finalWriter, writerClosed, 
                                        procesados.get(), errores.get(), 
                                        finalTotalItems, finalFile);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error esperando procesamiento de items", e);
                }
            }).start();

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

    private void procesarItem(CSVWriter writer, Auditoria auditoria, AuditoriaItemBPM item, CountDownLatch latch) {
        Log.d(TAG, "Procesando item BPM: " + item.getIdItemBPM() + " con estado: " + item.getEstado());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        AtomicBoolean supervisorCargado = new AtomicBoolean(false);
        AtomicBoolean operarioCargado = new AtomicBoolean(false);
        AtomicBoolean itemBPMCargado = new AtomicBoolean(false);

        String[] nombreSupervisor = {""};
        String[] nombreOperario = {""};
        String[] descripcionItem = {""};

        Runnable checkAndWrite = () -> {
            if (supervisorCargado.get() && operarioCargado.get() && itemBPMCargado.get()) {
                String estadoLegible = obtenerEstadoLegible(item.getEstado());
                Log.d(TAG, "Estado legible para item " + item.getIdItemBPM() + ": " + estadoLegible);
                
                String[] campos = {
                    dateFormatter.format(auditoria.getFecha()),
                    nombreSupervisor[0],
                    nombreOperario[0],
                    obtenerDescripcionActividad(auditoria.getIdActividad()),
                    obtenerDescripcionLinea(auditoria.getIdLinea()),
                    descripcionItem[0],
                    estadoLegible,
                    auditoria.getComentario()
                };
                synchronized (writer) {
                    writer.writeNext(campos);
                }
                latch.countDown();
            }
        };

        obtenerDescripcionSupervisor(auditoria.getIdSupervisor(), new OnSupervisorCargadoListener() {
            @Override
            public void onSupervisorCargado(String nombre) {
                nombreSupervisor[0] = nombre;
                supervisorCargado.set(true);
                checkAndWrite.run();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando supervisor: " + error);
                nombreSupervisor[0] = "Supervisor " + auditoria.getIdSupervisor();
                supervisorCargado.set(true);
                checkAndWrite.run();
            }
        });

        obtenerDescripcionOperario(auditoria.getIdOperario(), new OnOperarioCargadoListener() {
            @Override
            public void onOperarioCargado(String nombre) {
                nombreOperario[0] = nombre;
                operarioCargado.set(true);
                checkAndWrite.run();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando operario: " + error);
                nombreOperario[0] = "Operario " + auditoria.getIdOperario();
                operarioCargado.set(true);
                checkAndWrite.run();
            }
        });

        obtenerDescripcionItemBPM(item.getIdItemBPM(), new OnItemBPMCargadoListener() {
            @Override
            public void onItemCargado(String descripcion) {
                descripcionItem[0] = descripcion;
                itemBPMCargado.set(true);
                checkAndWrite.run();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando item BPM: " + error);
                // Intentar obtener del cache primero
                if (itemsBPMCache.containsKey(item.getIdItemBPM())) {
                    descripcionItem[0] = itemsBPMCache.get(item.getIdItemBPM());
                } else {
                    descripcionItem[0] = "Item sin descripción";
                }
                itemBPMCargado.set(true);
                checkAndWrite.run();
            }
        });
    }

    private String obtenerEstadoLegible(AuditoriaItemBPM.EstadoEnum estado) {
        switch (estado) {
            case OK:
                return "OK";
            case NOOK:
                return "NO OK";
            case NA:
                return "N/A";
            default:
                return estado.toString();
        }
    }

    private String obtenerDescripcionLinea(int idLinea) {
        return lineasCache.getOrDefault(idLinea, "Línea " + idLinea);
    }

    private String obtenerDescripcionActividad(int idActividad) {
        return actividadesCache.getOrDefault(idActividad, "Actividad " + idActividad);
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
        if (token == null) {
            Log.e(TAG, "No se pudo obtener el token de autenticación");
            return;
        }
        Log.d(TAG, "Token obtenido correctamente: " + token);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressText.setVisibility(View.VISIBLE);
        binding.progressText.setText("Cargando datos necesarios...");
        binding.btnExport.setEnabled(false);

        // Contador para saber cuándo se han completado todas las cargas
        final int[] loadCount = {0};
        final int TOTAL_LOADS = 5; // actividades, líneas, operarios, supervisores y items BPM

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

        // Cargar todos los supervisores
        ApiClient.getEndPoints().obtenerTodosLosSupervisores(token).enqueue(new Callback<List<Supervisor>>() {
            @Override
            public void onResponse(Call<List<Supervisor>> call, Response<List<Supervisor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Supervisor supervisor : response.body()) {
                        String nombreCompleto = supervisor.getApellido() + ", " + supervisor.getNombre();
                        supervisoresCache.put(supervisor.getIdSupervisor(), nombreCompleto);
                    }
                    Log.d(TAG, "Supervisores cargados: " + supervisoresCache.size());
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<List<Supervisor>> call, Throwable t) {
                Log.e(TAG, "Error al cargar supervisores", t);
                mostrarError("Error al cargar supervisores: " + t.getMessage());
                checkAllLoaded.run();
            }
        });

        // Cargar todos los operarios
        ApiClient.getEndPoints().obtenerTodosLosOperarios(token).enqueue(new Callback<List<Operario>>() {
            @Override
            public void onResponse(Call<List<Operario>> call, Response<List<Operario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Operario operario : response.body()) {
                        operariosCache.put(operario.getIdOperario(), operario.getApellido() + ", " + operario.getNombre());
                    }
                    Log.d(TAG, "Operarios cargados: " + operariosCache.size());
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<List<Operario>> call, Throwable t) {
                Log.e(TAG, "Error al cargar operarios", t);
                mostrarError("Error al cargar operarios: " + t.getMessage());
                checkAllLoaded.run();
            }
        });

        // Cargar todos los ItemsBPM
        Log.d(TAG, "Iniciando carga de todos los ItemsBPM");
        Call<List<ItemBPM>> itemsCall = ApiClient.getEndPoints().obtenerTodosLosItemsBPM(token);
        Log.d(TAG, "URL a llamar: " + itemsCall.request().url());
        
        itemsCall.enqueue(new Callback<List<ItemBPM>>() {
            @Override
            public void onResponse(Call<List<ItemBPM>> call, Response<List<ItemBPM>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ItemBPM> items = response.body();
                    Log.d(TAG, "Respuesta exitosa al cargar ItemsBPM. Items recibidos: " + items.size());
                    
                    for (ItemBPM item : items) {
                        int idItem = item.getIdItem();
                        String descripcion = item.getDescripcion();
                        Log.d(TAG, String.format("ItemBPM recibido - ID: %d, Descripción: '%s'", idItem, descripcion));
                        
                        if (descripcion != null && !descripcion.trim().isEmpty()) {
                            itemsBPMCache.put(idItem, descripcion);
                            Log.d(TAG, "Guardando en cache - ID: " + idItem + ", Descripción: '" + descripcion + "'");
                        } else {
                            Log.w(TAG, "ItemBPM con ID " + idItem + " tiene descripción nula o vacía");
                        }
                    }
                    Log.d(TAG, "Total de ItemsBPM en cache: " + itemsBPMCache.size());
                    Log.d(TAG, "Contenido del cache: " + itemsBPMCache);
                } else {
                    Log.e(TAG, "Error al cargar ItemsBPM. Código: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error del servidor: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer la respuesta de error", e);
                    }
                }
                checkAllLoaded.run();
            }

            @Override
            public void onFailure(Call<List<ItemBPM>> call, Throwable t) {
                Log.e(TAG, "Error de red al cargar ItemsBPM: " + t.getMessage(), t);
                checkAllLoaded.run();
            }
        });

        // Cargar actividades
        ApiClient.getEndPoints().obtenerTodasLasActividades(token).enqueue(new Callback<List<Actividad>>() {
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
        ApiClient.getEndPoints().obtenerTodasLasLineas(token).enqueue(new Callback<List<Linea>>() {
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
    }

    private void obtenerDescripcionItemBPM(int idItemBPM, final OnItemBPMCargadoListener listener) {
        Log.d(TAG, "Intentando obtener descripción para ItemBPM " + idItemBPM);
        
        if (idItemBPM <= 0) {
            Log.w(TAG, "ID ItemBPM inválido: " + idItemBPM);
            listener.onItemCargado("No especificado");
            return;
        }

        // Primero intentar obtener del cache
        if (itemsBPMCache.containsKey(idItemBPM)) {
            String descripcionCache = itemsBPMCache.get(idItemBPM);
            Log.d(TAG, "ItemBPM " + idItemBPM + " encontrado en cache. Descripción: '" + descripcionCache + "'");
            listener.onItemCargado(descripcionCache);
            return;
        }
        Log.d(TAG, "ItemBPM " + idItemBPM + " no encontrado en cache. Cache actual: " + itemsBPMCache);

        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            Log.e(TAG, "Token no disponible para ItemBPM " + idItemBPM);
            listener.onItemCargado("Item " + idItemBPM);
            return;
        }

        Log.d(TAG, "Realizando llamada API para ItemBPM " + idItemBPM);
        Call<ItemBPM> call = ApiClient.getEndPoints().obtenerItemBPMPorId(token, idItemBPM);
        Log.d(TAG, "URL a llamar: " + call.request().url());
        
        call.enqueue(new Callback<ItemBPM>() {
            @Override
            public void onResponse(Call<ItemBPM> call, Response<ItemBPM> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemBPM item = response.body();
                    String descripcion = item.getDescripcion();
                    Log.d(TAG, String.format("ItemBPM %d obtenido de API. Descripción: '%s'", idItemBPM, descripcion));
                    
                    if (descripcion != null && !descripcion.trim().isEmpty()) {
                        Log.d(TAG, "Guardando en cache ItemBPM " + idItemBPM + ": '" + descripcion + "'");
                        itemsBPMCache.put(idItemBPM, descripcion);
                        listener.onItemCargado(descripcion);
                    } else {
                        Log.w(TAG, "ItemBPM " + idItemBPM + " tiene descripción nula o vacía");
                        listener.onItemCargado("Item " + idItemBPM);
                    }
                } else {
                    int code = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer el cuerpo del error", e);
                    }
                    Log.e(TAG, String.format("Error HTTP al obtener ItemBPM %d. Código: %d, Error: %s", 
                        idItemBPM, code, errorBody));
                    listener.onItemCargado("Item " + idItemBPM);
                }
            }

            @Override
            public void onFailure(Call<ItemBPM> call, Throwable t) {
                Log.e(TAG, "Error de red al obtener ItemBPM " + idItemBPM, t);
                Log.e(TAG, "URL que falló: " + call.request().url());
                listener.onItemCargado("Item " + idItemBPM);
            }
        });
    }

    private void obtenerDescripcionOperario(int idOperario, final OnOperarioCargadoListener listener) {
        if (idOperario <= 0) {
            listener.onOperarioCargado("No especificado");
            return;
        }

        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            listener.onOperarioCargado("Operario " + idOperario);
            return;
        }

        // Primero intentar obtener del cache de operarios
        if (operariosCache.containsKey(idOperario)) {
            listener.onOperarioCargado(operariosCache.get(idOperario));
            return;
        }

        // Obtener todos los operarios si el cache está vacío
        if (operariosCache.isEmpty()) {
            ApiClient.getEndPoints().obtenerTodosLosOperarios(token).enqueue(new Callback<List<Operario>>() {
                @Override
                public void onResponse(Call<List<Operario>> call, Response<List<Operario>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (Operario op : response.body()) {
                            String nombreCompleto = op.getApellido() + ", " + op.getNombre();
                            operariosCache.put(op.getIdOperario(), nombreCompleto);
                        }
                        // Ahora que tenemos el cache actualizado, intentar obtener el operario de nuevo
                        if (operariosCache.containsKey(idOperario)) {
                            listener.onOperarioCargado(operariosCache.get(idOperario));
                        } else {
                            listener.onOperarioCargado("Operario " + idOperario);
                        }
                    } else {
                        listener.onOperarioCargado("Operario " + idOperario);
                    }
                }

                @Override
                public void onFailure(Call<List<Operario>> call, Throwable t) {
                    listener.onOperarioCargado("Operario " + idOperario);
                }
            });
        } else {
            // Si el operario no está en el cache y el cache no está vacío, significa que no existe
            listener.onOperarioCargado("Operario " + idOperario);
        }
    }

    private void obtenerDescripcionSupervisor(int idSupervisor, final OnSupervisorCargadoListener listener) {
        if (idSupervisor <= 0) {
            listener.onSupervisorCargado("No especificado");
            return;
        }

        String token = ApiClient.leerToken(requireContext());
        if (token == null) {
            listener.onSupervisorCargado("Supervisor " + idSupervisor);
            return;
        }

        // Primero intentar obtener del cache
        if (supervisoresCache.containsKey(idSupervisor)) {
            listener.onSupervisorCargado(supervisoresCache.get(idSupervisor));
            return;
        }

        // Si el supervisor no está en el cache, obtenerlo individualmente
        ApiClient.getEndPoints().obtenerSupervisor(token, idSupervisor).enqueue(new Callback<Supervisor>() {
            @Override
            public void onResponse(Call<Supervisor> call, Response<Supervisor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Supervisor supervisor = response.body();
                    String nombreCompleto = supervisor.getApellido() + ", " + supervisor.getNombre();
                    supervisoresCache.put(idSupervisor, nombreCompleto);
                    listener.onSupervisorCargado(nombreCompleto);
                } else {
                    listener.onSupervisorCargado("Supervisor " + idSupervisor);
                }
            }

            @Override
            public void onFailure(Call<Supervisor> call, Throwable t) {
                Log.e(TAG, "Error al obtener supervisor: " + t.getMessage());
                listener.onSupervisorCargado("Supervisor " + idSupervisor);
            }
        });
    }

    private interface OnItemBPMCargadoListener {
        void onItemCargado(String descripcionItem);
        void onError(String error);
    }

    private interface OnOperarioCargadoListener {
        void onOperarioCargado(String nombreOperario);
        void onError(String error);
    }

    private interface OnSupervisorCargadoListener {
        void onSupervisorCargado(String nombreSupervisor);
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

    private String obtenerNombreCompletoSupervisor(Supervisor supervisor) {
        if (supervisor == null) return "No especificado";
        return supervisor.getApellido() + ", " + supervisor.getNombre();
    }

    private void checkExportCompletion(CountDownLatch latch, CSVWriter writer, AtomicBoolean writerClosed, 
                                     int procesados, int errores, int total, File file) {
        Log.d(TAG, "Verificando completitud de exportación. Latch count: " + latch.getCount() + 
              ", writerClosed: " + writerClosed.get() + 
              ", procesados: " + procesados + 
              ", errores: " + errores + 
              ", total: " + total);

        if (latch.getCount() == 0 && !writerClosed.get()) {
            Log.d(TAG, "Condiciones cumplidas para cerrar writer");
            synchronized (writer) {
                if (!writerClosed.get()) {
                    try {
                        writer.close();
                        writerClosed.set(true);
                        Log.d(TAG, "Writer cerrado exitosamente");
                        
                        requireActivity().runOnUiThread(() -> {
                            Log.d(TAG, "Ejecutando en UI thread");
                            binding.progressBar.setVisibility(View.GONE);
                            binding.progressText.setVisibility(View.GONE);
                            
                            String mensaje = errores > 0 
                                ? "Exportación completada con " + errores + " errores"
                                : "Archivo CSV exportado exitosamente";
                                
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Intentando compartir archivo: " + file.getAbsolutePath());
                            Log.d(TAG, "¿Archivo existe? " + file.exists() + " ¿Se puede leer? " + file.canRead());
                            
                            shareFile(file);
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error al cerrar writer", e);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), 
                                "Error al finalizar la exportación: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }
        }
    }

    private void shareFile(File file) {
        try {
            Log.d(TAG, "Iniciando proceso de compartir archivo");
            Log.d(TAG, "Archivo a compartir: " + file.getAbsolutePath());
            Log.d(TAG, "¿Archivo existe? " + file.exists());
            Log.d(TAG, "¿Archivo se puede leer? " + file.canRead());
            
            Context context = requireContext();
            String authority = context.getPackageName() + ".provider";
            Log.d(TAG, "Usando authority: " + authority);
            
            Uri uri = FileProvider.getUriForFile(context, authority, file);
            Log.d(TAG, "URI generada: " + uri.toString());

            // Intent para descargar/guardar
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Intent para abrir
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(uri, "text/csv");
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Crear un chooser que combine ambas opciones
            Intent chooserIntent = Intent.createChooser(shareIntent, "Guardar o abrir archivo CSV");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { viewIntent });
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Otorgar permisos a todas las apps que puedan manejar el intent
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            Log.d(TAG, "Lanzando intent chooser");
            startActivity(chooserIntent);
            Log.d(TAG, "Intent chooser lanzado");

        } catch (Exception e) {
            Log.e(TAG, "Error al compartir el archivo", e);
            Toast.makeText(requireContext(),
                "Error al compartir el archivo: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
