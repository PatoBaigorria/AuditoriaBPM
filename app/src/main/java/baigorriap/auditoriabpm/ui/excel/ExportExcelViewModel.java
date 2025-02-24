package baigorriap.auditoriabpm.ui.excel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.request.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExportExcelViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Auditoria>> auditorias = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private static final String TAG = "ExportExcelViewModel";

    public ExportExcelViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Auditoria>> getAuditorias() {
        return auditorias;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void fetchAuditorias(String fromDate, String toDate) {
        isLoading.setValue(true);
        error.setValue(null);

        // Get supervisor ID from SharedPreferences
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int supervisorId = sharedPreferences.getInt("idSupervisor", -1);
        String token = ApiClient.leerToken(getApplication());

        Log.d(TAG, "Fetching auditorias with params: " +
              "fromDate=" + fromDate + 
              ", toDate=" + toDate + 
              ", supervisorId=" + supervisorId +
              ", token=" + (token != null ? "present" : "null"));

        if (supervisorId == -1) {
            error.setValue("Error: No se encontró el ID del supervisor");
            isLoading.setValue(false);
            return;
        }

        if (token == null) {
            error.setValue("Error: No se encontró el token de autenticación");
            isLoading.setValue(false);
            return;
        }

        ApiClient.getEndPoints().obtenerAuditoriasPorFecha(token, fromDate, toDate, supervisorId)
            .enqueue(new Callback<List<Auditoria>>() {
                @Override
                public void onResponse(Call<List<Auditoria>> call, Response<List<Auditoria>> response) {
                    isLoading.setValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Success! Got " + response.body().size() + " auditorias");
                        auditorias.setValue(response.body());
                    } else {
                        String errorMsg = "Error al obtener las auditorías: " + 
                            (response.errorBody() != null ? 
                             "\nCódigo: " + response.code() + 
                             "\nMensaje: " + response.message() : 
                             "Sin detalles del error");
                        Log.e(TAG, errorMsg);
                        error.setValue(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<List<Auditoria>> call, Throwable t) {
                    isLoading.setValue(false);
                    String errorMsg = "Error de conexión: " + t.getMessage();
                    Log.e(TAG, errorMsg, t);
                    error.setValue(errorMsg);
                }
            });
    }
}
