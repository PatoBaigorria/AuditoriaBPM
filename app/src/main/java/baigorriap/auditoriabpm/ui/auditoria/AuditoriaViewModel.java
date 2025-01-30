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

import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.ItemAuditoriaRequest;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.request.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuditoriaViewModel extends AndroidViewModel {
    private MutableLiveData<String> mErrorMessage;
    private MutableLiveData<List<AuditoriaItemBPM>> mListaItemsSeleccionados;
    private MutableLiveData<Boolean> mAuditoriaGuardada;
    private MutableLiveData<String> mComentario;
    private MutableLiveData<Operario> operario;
    private final Application application;
    private static final String TAG = "AuditoriaViewModel"; // Definimos un TAG para los logs


    public AuditoriaViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mErrorMessage = new MutableLiveData<>();
        mListaItemsSeleccionados = new MutableLiveData<>(new ArrayList<>());
        operario = new MutableLiveData<>();
    }

    // Métodos de LiveData

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }

    public LiveData<List<AuditoriaItemBPM>> getMListaItemsSeleccionados() {
        if (mListaItemsSeleccionados == null) {
            mListaItemsSeleccionados = new MutableLiveData<>();
        }
        return mListaItemsSeleccionados;
    }

    public LiveData<Boolean> getMAuditoriaGuardada() {
        if (mAuditoriaGuardada == null) {
            mAuditoriaGuardada = new MutableLiveData<>();
        }
        return mAuditoriaGuardada;
    }
    public void setMAuditoriaGuardada(Boolean value) {
        if (mAuditoriaGuardada == null) {
            mAuditoriaGuardada = new MutableLiveData<>();
        }
        mAuditoriaGuardada.setValue(value);
    }

    public LiveData<String> getComentario() {
        if (mComentario == null) {
            mComentario = new MutableLiveData<>();
        }
        return mComentario;
    }
    public void setComentario(String comentario) {
        if (mComentario == null) {
            mComentario = new MutableLiveData<>();
        }
        mComentario.setValue(comentario);
    }

    public LiveData<Operario> getOperario() {
        return operario;
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
        String token = ApiClient.leerToken(getApplication());
        if (token == null) return;

        ApiClient.getEndPoints().obtenerOperario("Bearer " + token, legajo)
                .enqueue(new Callback<Operario>() {
                    @Override
                    public void onResponse(Call<Operario> call, Response<Operario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            operario.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Operario> call, Throwable t) {
                        // Manejar el error si es necesario
                    }
                });
    }

    // Manejo de errores
    private void manejarError(String mensaje) {
        Log.e("API Error", mensaje);
        mErrorMessage.postValue(mensaje);
    }
}
