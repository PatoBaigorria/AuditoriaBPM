package baigorriap.auditoriabpm.ui.estadisticas;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import baigorriap.auditoriabpm.model.EstadisticasAuditoria;
import baigorriap.auditoriabpm.request.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasViewModel extends AndroidViewModel {
    private MutableLiveData<Map<String, EstadisticasAuditoria>> estadisticas;
    private MutableLiveData<String> error;
    private Context context;

    public EstadisticasViewModel(Application application) {
        super(application);
        context = application.getApplicationContext();
        estadisticas = new MutableLiveData<>();
        error = new MutableLiveData<>();
    }

    public void cargarEstadisticas(int anioInicio, int anioFin) {
        String token = ApiClient.leerToken(context);
        
        if (token == null) {
            error.setValue("No hay token disponible");
            Log.e("EstadisticasViewModel", "Token no encontrado");
            return;
        }

        Log.d("EstadisticasViewModel", "Token obtenido correctamente");
        Log.d("EstadisticasViewModel", "Cargando estadísticas para años: " + anioInicio + " - " + anioFin);

        Call<Map<String, EstadisticasAuditoria>> call = ApiClient.getEndPoints().obtenerEstadisticasAuditoria(token, anioInicio, anioFin);
        call.enqueue(new Callback<Map<String, EstadisticasAuditoria>>() {
            @Override
            public void onResponse(Call<Map<String, EstadisticasAuditoria>> call, Response<Map<String, EstadisticasAuditoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("EstadisticasViewModel", "Estadísticas recibidas correctamente");
                    estadisticas.setValue(response.body());
                } else {
                    String errorMsg = "Error al cargar las estadísticas. Código: " + response.code();
                    Log.e("EstadisticasViewModel", errorMsg);
                    error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Map<String, EstadisticasAuditoria>> call, Throwable t) {
                String errorMsg = "Error de conexión: " + t.getMessage();
                Log.e("EstadisticasViewModel", errorMsg, t);
                error.setValue(errorMsg);
            }
        });
    }

    public LiveData<Map<String, EstadisticasAuditoria>> getEstadisticas() {
        return estadisticas;
    }

    public LiveData<String> getError() {
        return error;
    }
}
