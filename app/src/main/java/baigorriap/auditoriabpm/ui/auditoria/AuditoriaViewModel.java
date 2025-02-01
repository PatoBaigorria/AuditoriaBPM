package baigorriap.auditoriabpm.ui.auditoria;

import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.model.AltaAuditoriaRequest;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM.EstadoEnum;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.ItemAuditoriaRequest;
import baigorriap.auditoriabpm.model.Linea;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.request.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuditoriaViewModel extends AndroidViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAuditoriaGuardada = new MutableLiveData<>();
    private final MutableLiveData<List<AuditoriaItemBPM>> mListaItemsSeleccionados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> comentario = new MutableLiveData<>("");
    private final MutableLiveData<Operario> operario = new MutableLiveData<>();
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

    // Método para guardar auditoría
    public void guardarAuditoria(int idOperario, int idSupervisor, int idActividad, int idLinea, String comentario, List<ItemAuditoriaRequest> items) {
        String token = ApiClient.leerToken(application);
        AltaAuditoriaRequest request = new AltaAuditoriaRequest(idOperario, idSupervisor, idActividad, idLinea, comentario, items);

        Call<ResponseBody> callAuditoria = ApiClient.getEndPoints().darDeAltaAuditoria(token, request);
        callAuditoria.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("API Response", "Código: " + response.code() + ", Cuerpo: " + response.body());
                if (response.isSuccessful()) {
                    mAuditoriaGuardada.postValue(true);
                    // Limpiar datos después de guardar exitosamente
                    limpiarDatosOperario();
                } else {
                    manejarError("Error al guardar auditoría. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                manejarError("Error en la conexión: " + t.getMessage());
            }
        });
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

    // Manejo de errores
    private void manejarError(String mensaje) {
        Log.e("API Error", mensaje);
        errorMessage.postValue(mensaje);
    }
}
