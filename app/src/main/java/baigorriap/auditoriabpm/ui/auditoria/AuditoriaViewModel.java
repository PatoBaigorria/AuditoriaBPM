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
    private final MutableLiveData<Boolean> mAuditoriaGuardada = new MutableLiveData<>(false);
    private final MutableLiveData<List<AuditoriaItemBPM>> mListaItemsSeleccionados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> comentario = new MutableLiveData<>("");
    private final MutableLiveData<Operario> operario = new MutableLiveData<>();
    private final MutableLiveData<String> firma = new MutableLiveData<>();
    private final MutableLiveData<Boolean> noConforme = new MutableLiveData<>();
    private final MutableLiveData<Integer> mTotalItems = new MutableLiveData<>(0);
    private boolean isNoConforme = false; // Variable para guardar el estado de noConforme
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
        Log.d(TAG, "setMAuditoriaGuardada llamado con valor: " + value);
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
    public interface GuardarAuditoriaCallback {
        void onAuditoriaGuardada(boolean exitoso);
    }

    public void guardarAuditoria(GuardarAuditoriaCallback callback) {
        Log.d(TAG, "Iniciando guardarAuditoria()");
        
        if (firma.getValue() == null || firma.getValue().isEmpty()) {
            Log.e(TAG, "Error: Firma vacía o nula");
            errorMessage.setValue("Se requiere una firma");
            callback.onAuditoriaGuardada(false);
            return;
        }

        List<AuditoriaItemBPM> itemsSeleccionados = mListaItemsSeleccionados.getValue();
        if (itemsSeleccionados == null || itemsSeleccionados.isEmpty()) {
            Log.e(TAG, "Error: No hay items seleccionados");
            errorMessage.setValue("Debe seleccionar al menos un ítem");
            callback.onAuditoriaGuardada(false);
            return;
        }

        // Obtener el token
        String token = "Bearer " + ApiClient.leerToken(application);
        Log.d(TAG, "Token obtenido: " + (token != null ? "Sí" : "No"));

        // Obtener el ID del supervisor
        SharedPreferences sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int idSupervisor = sharedPreferences.getInt("idSupervisor", 0);
        Log.d(TAG, "ID Supervisor: " + idSupervisor);

        // Crear la lista de items
        List<ItemAuditoriaRequest> items = new ArrayList<>();
        for (AuditoriaItemBPM item : itemsSeleccionados) {
            items.add(new ItemAuditoriaRequest(item.getIdItemBPM(), item.getEstado().toString()));
        }

        // Verificar que el operario existe
        if (operario.getValue() == null) {
            Log.e(TAG, "Error: Operario es nulo");
            errorMessage.setValue("No hay operario seleccionado");
            callback.onAuditoriaGuardada(false);
            return;
        }

        // Log de datos antes de crear la auditoría
        Log.d(TAG, "Datos de la auditoría a enviar:");
        Log.d(TAG, "ID Operario: " + operario.getValue().getIdOperario());
        Log.d(TAG, "ID Supervisor: " + idSupervisor);
        Log.d(TAG, "ID Actividad: " + operario.getValue().getIdActividad());
        Log.d(TAG, "ID Línea: " + operario.getValue().getIdLinea());
        Log.d(TAG, "Comentario: " + (comentario.getValue() != null ? comentario.getValue() : ""));
        Log.d(TAG, "Cantidad de items: " + items.size());
        Log.d(TAG, "Firma length: " + (firma.getValue() != null ? firma.getValue().length() : 0));
        Log.d(TAG, "No Conforme: " + isNoConforme);

        // Crear el objeto AltaAuditoriaRequest
        AltaAuditoriaRequest auditoria = new AltaAuditoriaRequest(
            operario.getValue().getIdOperario(),
            idSupervisor,
            operario.getValue().getIdActividad(),
            operario.getValue().getIdLinea(),
            comentario.getValue() != null ? comentario.getValue() : "",
            items,
            firma.getValue(),
            isNoConforme
        );

        // Llamar a la API
        Call<ResponseBody> call = ApiClient.getEndPoints().altaAuditoriaCompleta(token, auditoria);
        Log.d(TAG, "Llamando a API altaAuditoriaCompleta");
        Log.d(TAG, "URL de la llamada: " + call.request().url());
        
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Respuesta recibida de la API");
                Log.d(TAG, "Código de respuesta: " + response.code());
                
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Respuesta exitosa del servidor: " + responseBody);
                        
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject auditoriaJson = jsonResponse.getJSONObject("auditoria");
                        int idAuditoria = auditoriaJson.getInt("idAuditoria");
                        
                        Log.d(TAG, "Auditoría guardada correctamente con ID: " + idAuditoria);
                        
                        // Limpiar datos
                        operario.postValue(null);
                        firma.postValue("");
                        comentario.postValue("");
                        mListaItemsSeleccionados.postValue(new ArrayList<>());
                        isNoConforme = false;
                        
                        // Notificar éxito
                        callback.onAuditoriaGuardada(true);
                        
                    } catch (JSONException | IOException e) {
                        Log.e(TAG, "Error al procesar respuesta JSON", e);
                        Log.e(TAG, "Stack trace: ", e);
                        errorMessage.postValue("Error al procesar la respuesta del servidor");
                        callback.onAuditoriaGuardada(false);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e(TAG, "Error al guardar auditoría. Código: " + response.code());
                        Log.e(TAG, "Error body: " + errorBody);
                        Log.e(TAG, "Headers de la respuesta: " + response.headers());
                        Log.e(TAG, "URL llamada: " + call.request().url());
                        errorMessage.postValue("Error al guardar auditoría: " + errorBody);
                        callback.onAuditoriaGuardada(false);
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer error body", e);
                        Log.e(TAG, "Stack trace: ", e);
                        errorMessage.postValue("Error al guardar auditoría");
                        callback.onAuditoriaGuardada(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error de conexión al guardar auditoría", t);
                Log.e(TAG, "URL llamada: " + call.request().url());
                Log.e(TAG, "Stack trace: ", t);
                errorMessage.postValue("Error de conexión al guardar auditoría: " + t.getMessage());
                callback.onAuditoriaGuardada(false);
            }
        });
    }

    public void setFirma(String firmaSvg, boolean noConforme) {
        Log.d(TAG, "setFirma - noConforme: " + noConforme);
        firma.setValue(firmaSvg);
        isNoConforme = noConforme;
    }

    public LiveData<Boolean> getNoConforme() {
        return noConforme;
    }

    public boolean tieneFirma() {
        return firma.getValue() != null && !firma.getValue().isEmpty();
    }

    private void limpiarDatos() {
        Log.d(TAG, "Limpiando datos después de guardar");
        operario.setValue(null);
        firma.setValue("");
        comentario.setValue("");
        mListaItemsSeleccionados.setValue(new ArrayList<>());
        isNoConforme = false;
        // No reseteamos mAuditoriaGuardada aquí, lo haremos después de que el diálogo se cierre
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
