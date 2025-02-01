package baigorriap.auditoriabpm.ui.reporte;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.List;

import baigorriap.auditoriabpm.model.ItemNoOk;
import baigorriap.auditoriabpm.request.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemsNoOkViewModel extends ViewModel {
    private static final String TAG = "ItemsNoOkViewModel";
    private final MutableLiveData<List<ItemNoOk>> itemsNoOk = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<ItemNoOk>> getItemsNoOk() {
        return itemsNoOk;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadItemsNoOk(int legajo, Context context) {
        SharedPreferences sp = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        
        if (token.isEmpty()) {
            String errorMsg = "Error: No hay token de autenticación";
            Log.e(TAG, errorMsg);
            error.setValue(errorMsg);
            return;
        }

        String authToken = "Bearer " + token;
        
        Call<List<ItemNoOk>> call = ApiClient.getEndPoints().obtenerAuditoriasNookPorOperario(authToken, legajo);
        Log.d(TAG, "Llamando a URL: " + call.request().url());
        
        call.enqueue(new Callback<List<ItemNoOk>>() {
            @Override
            public void onResponse(Call<List<ItemNoOk>> call, Response<List<ItemNoOk>> response) {
                Log.d(TAG, "Código de respuesta: " + response.code());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<ItemNoOk> items = response.body();
                        if (items.isEmpty()) {
                            String msg = "No se encontraron items NO OK para este operario";
                            Log.d(TAG, msg);
                            error.setValue(msg);
                        } else {
                            Log.d(TAG, "Items recibidos: " + items.size());
                            itemsNoOk.setValue(items);
                        }
                    } else {
                        String msg = "Error: La respuesta del servidor está vacía";
                        Log.e(TAG, msg);
                        error.setValue(msg);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                            response.errorBody().string() : "Error desconocido";
                        String msg = "Error " + response.code() + ": " + errorBody;
                        Log.e(TAG, msg);
                        Log.e(TAG, "URL llamada: " + call.request().url());
                        error.setValue(msg);
                    } catch (IOException e) {
                        String msg = "Error " + response.code() + " al cargar los items NO OK";
                        Log.e(TAG, msg, e);
                        error.setValue(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ItemNoOk>> call, Throwable t) {
                String msg = "Error de conexión: " + t.getMessage() + "\nURL: " + call.request().url();
                Log.e(TAG, msg, t);
                error.setValue(msg);
            }
        });
    }
}
