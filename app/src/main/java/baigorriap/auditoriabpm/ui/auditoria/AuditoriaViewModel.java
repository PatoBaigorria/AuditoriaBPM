package baigorriap.auditoriabpm.ui.auditoria;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.model.AltaAuditoriaRequest;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM.EstadoEnum;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.Firma;
import baigorriap.auditoriabpm.model.FirmaPatron;
import baigorriap.auditoriabpm.model.ItemAuditoriaRequest;
import baigorriap.auditoriabpm.model.Linea;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.request.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuditoriaViewModel extends AndroidViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAuditoriaGuardada = new MutableLiveData<>();
    private final MutableLiveData<List<AuditoriaItemBPM>> mListaItemsSeleccionados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> comentario = new MutableLiveData<>("");
    private final MutableLiveData<Operario> operario = new MutableLiveData<>();
    private final MutableLiveData<String> firma = new MutableLiveData<>();
    private final MutableLiveData<Boolean> noConforme = new MutableLiveData<>();
    private final MutableLiveData<Integer> mTotalItems = new MutableLiveData<>(0);
    private final Application application;
    private static final String TAG = "AuditoriaViewModel";

    // Variables para mantener el estado
    private int ultimoLegajo = 0;
    private boolean datosOperarioCargados = false;

    public AuditoriaViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getMAuditoriaGuardada() {
        return mAuditoriaGuardada;
    }

    public void setMAuditoriaGuardada(Boolean value) {
        mAuditoriaGuardada.setValue(value);
    }

    public LiveData<List<AuditoriaItemBPM>> getMListaItemsSeleccionados() {
        return mListaItemsSeleccionados;
    }

    public LiveData<String> getComentario() {
        return comentario;
    }

    public void setComentario(String value) {
        comentario.setValue(value);
    }

    public LiveData<Operario> getOperario() {
        return operario;
    }

    public void setOperario(Operario op) {
        if (operario.getValue() == null || !operario.getValue().equals(op)) {
            operario.setValue(op);
        }
    }

    public void limpiarItemsSeleccionados() {
        mListaItemsSeleccionados.setValue(new ArrayList<>());
    }

    public void limpiarDatosOperario() {
        operario.setValue(null);
        datosOperarioCargados = false;
        ultimoLegajo = 0;
    }

    // Métodos para seleccionar y actualizar ítems
    public void seleccionarEstado(int idItem, EstadoEnum estado) {
        List<AuditoriaItemBPM> items = mListaItemsSeleccionados.getValue();

        if (items != null) {
            boolean itemActualizado = false;

            for (AuditoriaItemBPM item : items) {
                if (item.getIdItemBPM() == idItem) {
                    item.setEstado(estado);
                    itemActualizado = true;
                    break;
                }
            }

            if (!itemActualizado) {
                AuditoriaItemBPM nuevoItem = new AuditoriaItemBPM();
                nuevoItem.setIdItemBPM(idItem);
                nuevoItem.setEstado(estado);
                items.add(nuevoItem);
            }

            mListaItemsSeleccionados.setValue(items);
        }
    }

    public boolean todosLosItemsTienenEstado() {
        // Obtener el total de items que debería haber
        Integer totalItems = mTotalItems.getValue();
        if (totalItems == null || totalItems == 0) {
            return false;
        }
        
        // Obtener los items que tienen estado seleccionado
        List<AuditoriaItemBPM> itemsSeleccionados = mListaItemsSeleccionados.getValue();
        if (itemsSeleccionados == null) {
            return false;
        }
        
        // Verificar que la cantidad de items seleccionados sea igual al total de items
        return itemsSeleccionados.size() == totalItems;
    }

    public boolean tieneItemsSeleccionados() {
        List<AuditoriaItemBPM> items = mListaItemsSeleccionados.getValue();
        return items != null && !items.isEmpty();
    }

    // Método para guardar auditoría
    public void guardarAuditoria() {
        // Validar que haya un operario seleccionado
        if (operario.getValue() == null || operario.getValue().getIdOperario() == 0) {
            errorMessage.setValue("Debe cargar un operario");
            return;
        }

        // Validar que haya items seleccionados
        if (!tieneItemsSeleccionados()) {
            errorMessage.setValue("Debe seleccionar al menos un ítem");
            return;
        }

        // Validar que todos los items tengan un estado seleccionado
        if (!todosLosItemsTienenEstado()) {
            errorMessage.setValue("Debe seleccionar un estado para todos los ítems");
            return;
        }

        // Validar que haya firma
        if (!tieneFirma()) {
            errorMessage.setValue("Se requiere la firma del operario");
            return;
        }

        // Obtener el token
        String token = "Bearer " + ApiClient.leerToken(application);
        Log.d("FirmaPatron", "Token: " + token);
        Log.d("FirmaPatron", "ID Operario: " + operario.getValue().getIdOperario());

        // Primero verificar si el operario ya tiene una firma patrón
        Call<FirmaPatron> firmaPatronCall = ApiClient.getEndPoints().obtenerFirmaPatron(token, operario.getValue().getIdOperario());
        firmaPatronCall.enqueue(new Callback<FirmaPatron>() {
            @Override
            public void onResponse(Call<FirmaPatron> call, Response<FirmaPatron> response) {
                Log.d("FirmaPatron", "Código de respuesta: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("FirmaPatron", "Firma patrón encontrada");
                    // Ya tiene firma patrón, verificar la firma actual
                    verificarFirmaYGuardarAuditoria(token);
                } else if (response.code() == 404) {
                    Log.d("FirmaPatron", "No se encontró firma patrón, creando nueva");
                    // No tiene firma patrón, guardar la firma actual como patrón
                    guardarFirmaPatronYContinuar(token);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("FirmaPatron", "Error al obtener firma patrón: " + errorBody);
                        Log.e("FirmaPatron", "URL llamada: " + call.request().url());
                        errorMessage.setValue("Error al obtener firma patrón: " + errorBody);
                    } catch (IOException e) {
                        Log.e("FirmaPatron", "Error al leer error body", e);
                        errorMessage.setValue("Error al obtener firma patrón");
                    }
                }
            }

            @Override
            public void onFailure(Call<FirmaPatron> call, Throwable t) {
                Log.e("FirmaPatron", "Error de conexión al obtener firma patrón: " + t.getMessage(), t);
                Log.e("FirmaPatron", "URL llamada: " + call.request().url());
                errorMessage.setValue("Error de conexión al obtener firma patrón: " + t.getMessage());
            }
        });
    }

    private void guardarFirmaPatronYContinuar(String token) {
        // Crear objeto firma patrón
        FirmaPatron firmaPatron = new FirmaPatron();
        firmaPatron.setIdOperario(operario.getValue().getIdOperario());
        firmaPatron.setFirma(firma.getValue());
        
        // Calcular métricas de la firma SVG
        List<PointF> points = extraerPuntosDelSVG(firma.getValue());
        firmaPatron.setPuntosTotales(points.size());
        firmaPatron.setVelocidadMedia(calcularVelocidadMedia(points));
        firmaPatron.setPresionMedia(1.0f); // Por defecto en firmas vectoriales
        firmaPatron.setHash(generarHash(firma.getValue()));
        
        // Formatear la fecha en ISO 8601
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
        firmaPatron.setFechaCreacion(iso8601Format.format(new Date()));
        
        firmaPatron.setActiva(true);

        // Guardar firma patrón
        Call<ResponseBody> call = ApiClient.getEndPoints().guardarFirmaPatron(token, firmaPatron);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("FirmaPatron", "Firma patrón guardada exitosamente");
                    // Firma patrón guardada, continuar con la auditoría
                    procederConGuardadoAuditoria(token);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("FirmaPatron", "Error al guardar firma patrón: " + errorBody);
                        errorMessage.setValue("Error al guardar firma patrón: " + errorBody);
                    } catch (IOException e) {
                        Log.e("FirmaPatron", "Error al leer error body", e);
                        errorMessage.setValue("Error al guardar firma patrón");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("FirmaPatron", "Error de conexión al guardar firma patrón: " + t.getMessage(), t);
                errorMessage.setValue("Error de conexión al guardar firma patrón: " + t.getMessage());
            }
        });
    }

    private void verificarFirmaYGuardarAuditoria(String token) {
        // Crear objeto firma para verificar
        FirmaPatron firmaVerificar = new FirmaPatron();
        firmaVerificar.setIdOperario(operario.getValue().getIdOperario());
        firmaVerificar.setFirma(firma.getValue());
        
        // Calcular métricas de la firma SVG
        List<PointF> points = extraerPuntosDelSVG(firma.getValue());
        firmaVerificar.setPuntosTotales(points.size());
        firmaVerificar.setVelocidadMedia(calcularVelocidadMedia(points));
        firmaVerificar.setPresionMedia(1.0f); // Por defecto en firmas vectoriales
        firmaVerificar.setHash(generarHash(firma.getValue()));

        // Verificar firma
        Call<Boolean> call = ApiClient.getEndPoints().verificarFirma(token, firmaVerificar);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null && response.body()) {
                    // Firma verificada, proceder con la auditoría
                    procederConGuardadoAuditoria(token);
                } else {
                    errorMessage.setValue("La firma no coincide con el patrón del operario");
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error de conexión al verificar firma", t);
                errorMessage.setValue("Error de conexión al verificar firma: " + t.getMessage());
            }
        });
    }

    private List<PointF> extraerPuntosDelSVG(String svgPath) {
        List<PointF> points = new ArrayList<>();
        Pattern pattern = Pattern.compile("[MmLlHhVvCcSsQqTtAaZz]|[-+]?[0-9]*\\.?[0-9]+");
        Matcher matcher = pattern.matcher(svgPath);
        
        char comando = 'M';
        float x = 0, y = 0;
        
        while (matcher.find()) {
            String token = matcher.group();
            
            if (token.matches("[MmLlHhVvCcSsQqTtAaZz]")) {
                comando = token.charAt(0);
            } else {
                float valor = Float.parseFloat(token);
                
                switch (comando) {
                    case 'M':
                    case 'm':
                        if (matcher.find()) {
                            float y1 = Float.parseFloat(matcher.group());
                            x = comando == 'M' ? valor : x + valor;
                            y = comando == 'M' ? y1 : y + y1;
                            points.add(new PointF(x, y));
                        }
                        break;
                    case 'L':
                    case 'l':
                        if (matcher.find()) {
                            float y1 = Float.parseFloat(matcher.group());
                            x = comando == 'L' ? valor : x + valor;
                            y = comando == 'L' ? y1 : y + y1;
                            points.add(new PointF(x, y));
                        }
                        break;
                    case 'H':
                    case 'h':
                        x = comando == 'H' ? valor : x + valor;
                        points.add(new PointF(x, y));
                        break;
                    case 'V':
                    case 'v':
                        y = comando == 'V' ? valor : y + valor;
                        points.add(new PointF(x, y));
                        break;
                }
            }
        }
        
        return points;
    }

    private float calcularVelocidadMedia(List<PointF> points) {
        if (points.size() < 2) return 0;
        
        float distanciaTotal = 0;
        
        for (int i = 1; i < points.size(); i++) {
            PointF p1 = points.get(i-1);
            PointF p2 = points.get(i);
            
            float dx = p2.x - p1.x;
            float dy = p2.y - p1.y;
            distanciaTotal += (float)Math.sqrt(dx*dx + dy*dy);
        }
        
        // Asumimos una velocidad constante, así que la velocidad media será
        // la distancia total dividida por el número de segmentos
        return distanciaTotal / (points.size() - 1);
    }

    private String generarHash(String firma) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(firma.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(hash, android.util.Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error al generar hash", e);
            return "";
        }
    }

    private void procederConGuardadoAuditoria(String token) {
        // Obtener el ID del supervisor de SharedPreferences
        SharedPreferences sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int idSupervisor = sharedPreferences.getInt("idSupervisor", 0);

        // Convertir los ítems seleccionados al formato requerido por la API
        List<ItemAuditoriaRequest> items = new ArrayList<>();
        List<AuditoriaItemBPM> itemsSeleccionados = mListaItemsSeleccionados.getValue();
        if (itemsSeleccionados != null) {
            for (AuditoriaItemBPM item : itemsSeleccionados) {
                items.add(new ItemAuditoriaRequest(item.getIdItemBPM(), item.getEstado().toString()));
            }
        }

        // Obtener el valor de noConforme
        Boolean noConformeValue = noConforme.getValue();
        boolean isNoConforme = noConformeValue != null && noConformeValue;

        // Crear el objeto AltaAuditoriaRequest
        AltaAuditoriaRequest auditoria = new AltaAuditoriaRequest(
            operario.getValue().getIdOperario(),
            idSupervisor,
            operario.getValue().getIdActividad(),
            operario.getValue().getIdLinea(),
            comentario.getValue() != null ? comentario.getValue() : "",
            items,
            firma.getValue(),  // Incluir la firma al crear la auditoría
            isNoConforme
        );

        // Llamar a la API para guardar la auditoría
        Call<ResponseBody> call = ApiClient.getEndPoints().darDeAltaAuditoria(token, auditoria);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // Obtener el JSON de la respuesta
                        String responseBody = response.body().string();
                        
                        // Parsear el JSON para obtener el ID de la auditoría
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject auditoriaJson = jsonResponse.getJSONObject("auditoria");
                        int idAuditoria = auditoriaJson.getInt("idAuditoria");
                        
                        Log.d(TAG, "Auditoría guardada correctamente con ID: " + idAuditoria);
                        mAuditoriaGuardada.setValue(true);
                        limpiarDatos();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear respuesta JSON", e);
                        errorMessage.setValue("Error al procesar la respuesta del servidor");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e(TAG, "Error al guardar auditoría: " + errorBody);
                        errorMessage.setValue("Error al guardar auditoría: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer error body", e);
                        errorMessage.setValue("Error al guardar auditoría");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error de conexión al guardar auditoría", t);
                errorMessage.setValue("Error de conexión al guardar auditoría: " + t.getMessage());
            }
        });
    }

    public void setFirma(String firmaSvg, boolean noConformeValue) {
        firma.setValue(firmaSvg);
        noConforme.setValue(noConformeValue);
        Log.d(TAG, "Firma guardada. noConforme: " + noConformeValue);
    }

    public LiveData<Boolean> getNoConforme() {
        return noConforme;
    }

    public boolean tieneFirma() {
        return firma.getValue() != null && !firma.getValue().isEmpty();
    }

    private void limpiarDatos() {
        firma.setValue(null);
        noConforme.setValue(null);
        mListaItemsSeleccionados.setValue(new ArrayList<>());
        comentario.setValue("");
        limpiarDatosOperario();
    }

    public void cargarOperario(int legajo) {
        // Si ya tenemos los datos del operario y es el mismo legajo, no volvemos a cargar
        if (datosOperarioCargados && ultimoLegajo == legajo) {
            Log.d(TAG, "Usando datos en caché del operario: " + legajo);
            return;
        }

        Log.d(TAG, "Cargando datos del operario: " + legajo);
        String token = ApiClient.leerToken(getApplication());
        if (token == null) {
            manejarError("No se encontró el token de autenticación");
            return;
        }

        ApiClient.getEndPoints().obtenerOperario("Bearer " + token, legajo)
                .enqueue(new Callback<Operario>() {
                    @Override
                    public void onResponse(Call<Operario> call, Response<Operario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Operario op = response.body();
                            operario.setValue(op);
                            ultimoLegajo = legajo;
                            datosOperarioCargados = true;
                            Log.d(TAG, "Datos del operario cargados correctamente");
                        } else {
                            manejarError("Error al cargar el operario: " + response.code());
                            datosOperarioCargados = false;
                        }
                    }

                    @Override
                    public void onFailure(Call<Operario> call, Throwable t) {
                        manejarError("Error de conexión al cargar el operario: " + t.getMessage());
                        datosOperarioCargados = false;
                    }
                });
    }

    public void setTotalItems(int total) {
        mTotalItems.setValue(total);
    }

    public LiveData<Integer> getTotalItems() {
        return mTotalItems;
    }

    // Manejo de errores
    private void manejarError(String mensaje) {
        Log.e("API Error", mensaje);
        errorMessage.postValue(mensaje);
    }
}
