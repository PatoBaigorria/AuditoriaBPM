package baigorriap.auditoriabpm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import baigorriap.auditoriabpm.model.Supervisor;
import baigorriap.auditoriabpm.request.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class LoginActivityViewModel extends AndroidViewModel {
    private SharedPreferences sharedPreferences;

    public LoginActivityViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    public void logueo(String legajo, String clave, LoginCallback callback) {
        Log.d("Login", "Intentando login con legajo: " + legajo);
        ApiClient.MisEndPoints api = ApiClient.getEndPoints();
        Call<String> call = api.login(legajo, clave);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("Login", "Código de respuesta: " + response.code());
                if (response.isSuccessful()) {
                    String token = response.body();
                    Log.d("Login", "Token recibido: " + token);
                    guardarToken(token);
                    Log.d("Login", "Token guardado, iniciando MainActivity");
                    iniciarMainActivity(callback);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("Login", "Error en login: " + errorBody);
                        Toast.makeText(getApplication(), "Legajo o contraseña incorrecta", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("Login", "Error al leer error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                Log.e("Login", "Error de conexión: " + throwable.getMessage(), throwable);
                Toast.makeText(getApplication(), "Falla en el inicio de sesión: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarToken(String token) {

        ApiClient.guardarToken(token, getApplication());
    }

    private void iniciarMainActivity(LoginCallback callback) {
        sharedPreferences = getApplication().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ApiClient.MisEndPoints api = ApiClient.getEndPoints();
        String token = ApiClient.leerToken(getApplication());
        Log.d("salida", token);
        Call<Supervisor> call = api.miPerfil(token);
        call.enqueue(new Callback<Supervisor>() {
            @Override
            public void onResponse(Call<Supervisor> call, Response<Supervisor> response) {
                if (response.isSuccessful()) {
                    Supervisor supervisor = response.body();
                    if (supervisor != null) {
                        Log.d("SupervisorData", "ID: " + supervisor.getIdSupervisor());
                        Log.d("SupervisorData", "Nombre: " + supervisor.getNombre() + " " + supervisor.getApellido());
                        // Guardar los datos
                        editor.putInt("idSupervisor", supervisor.getIdSupervisor());
                        editor.putString("nombre completo", supervisor.getNombre() + " " + supervisor.getApellido());
                        editor.putInt("legajo", supervisor.getLegajo());
                        editor.apply();

                        // Navegar a MenuActivity aquí
                        Intent intent = new Intent(getApplication(), MenuActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getApplication().startActivity(intent);
                    } else {
                        Log.d("SupervisorData", "Respuesta nula del supervisor.");
                    }
                } else {
                    Log.d("salida", response.message());
                    Toast.makeText(getApplication(), "Error al obtener perfil", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<Supervisor> call, Throwable throwable) {
                Log.d("salida", throwable.getMessage());
                Toast.makeText(getApplication(), "Falla en la obtención de perfil", Toast.LENGTH_LONG).show();
            }
        });
    }
    public interface LoginCallback {
        void onSuccess();
    }
}
