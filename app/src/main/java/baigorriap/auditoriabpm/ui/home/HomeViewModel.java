package baigorriap.auditoriabpm.ui.home;

import android.app.Application;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.Linea;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.request.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText;
    private MutableLiveData<String> mLegajo;
    private MutableLiveData<List<Actividad>> mListaActividad;
    private MutableLiveData<List<Linea>> mListaLinea;
    private MutableLiveData<Operario> mOperario;
    private MutableLiveData<String> mErrorMessage;
    private final Application application;


    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.application = application; // Inicializa la variable
        mText = new MutableLiveData<>();
        mText.setValue("Datos del Operario");
        mListaActividad = new MutableLiveData<>();
        mListaLinea = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>(); // Inicializa el MutableLiveData para errores
    }

    public LiveData<String> getErrorMessage() {
        if (mErrorMessage == null) {
            mErrorMessage = new MutableLiveData<>();
        }
        return mErrorMessage;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getMLegajo() {
        if (mLegajo == null) {
            mLegajo = new MutableLiveData<>();
        }
        return mLegajo;
    }

    public LiveData<List<Actividad>> getMListaActividad() {
        if (mListaActividad == null) {
            mListaActividad = new MutableLiveData<>();
        }
        return mListaActividad;
    }

    public LiveData<List<Linea>> getMListaLinea() {
        if (mListaLinea == null) {
            mListaLinea = new MutableLiveData<>();
        }
        return mListaLinea;
    }

    public LiveData<Operario> getMOperario() {
        if (mOperario == null) {
            mOperario = new MutableLiveData<>();
        }
        return mOperario;
    }

    public void cargarDatosPorLegajo(int legajo) {
        if (legajo <= 0) { // Validación simple para legajo
            mErrorMessage.setValue("Ingrese un Legajo Válido");
            return; // Salir del método si el legajo no es válido
        }

        String token = "Bearer " + ApiClient.leerToken(application);

        // Obtener actividades filtradas por legajo
        Call<List<Actividad>> callActividades = ApiClient.getEndPoints().obtenerActividades(token, legajo);
        callActividades.enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Establecer la actividad del operario
                    mListaActividad.setValue(response.body());
                    // Obtener todas las actividades disponibles
                    cargarTodasLasActividades();
                } else {
                    Log.e("HomeViewModel", "Error al obtener actividades por legajo: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Toast.makeText(getApplication(), "Error en la obtención de actividades por legajo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Obtener líneas filtradas por legajo
        Call<List<Linea>> callLineas = ApiClient.getEndPoints().obtenerLineas(token, legajo);
        callLineas.enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Establecer la línea del operario
                    mListaLinea.setValue(response.body());

                    // Obtener todas las líneas disponibles
                    cargarTodasLasLineas();
                } else {
                    Log.e("HomeViewModel", "Error al obtener líneas por legajo: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Toast.makeText(getApplication(), "Error en la obtención de líneas por legajo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void cargarOperarioPorLegajo(int legajo) {
        if (legajo <= 0) {
            mErrorMessage.setValue("Ingrese un Legajo Válido");
            return;
        }

        String token = "Bearer " + ApiClient.leerToken(application);
        Log.d("HomeViewModel", "Obteniendo operario con legajo: " + legajo);

        // Realizar la llamada para obtener el Operario por legajo
        Call<Operario> callOperario = ApiClient.getEndPoints().obtenerOperario(token, legajo);
        callOperario.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Establecer el operario
                    mOperario.setValue(response.body());
                    Log.d("HomeViewModel", "Operario obtenido: " + response.body().toString());
                } else {
                    // Si no se encuentra el operario, establecer null
                    mOperario.setValue(null);
                    Log.e("HomeViewModel", "Operario no encontrado para legajo: " + legajo);
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                mErrorMessage.setValue("Error en la obtención del operario: " + t.getMessage());
                Log.e("HomeViewModel", "Error en la obtención del operario: " + t.getMessage());
            }
        });
    }

    // Obtener todas las actividades
    private void cargarTodasLasActividades() {
        String token = "Bearer " + ApiClient.leerToken(application);
        Call<List<Actividad>> callTodasActividades = ApiClient.getEndPoints().obtenerTodasLasActividades(token);
        callTodasActividades.enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Añadir todas las actividades al spinner
                    mListaActividad.getValue().addAll(response.body());
                } else {
                    Log.e("HomeViewModel", "Error al obtener todas las actividades: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Toast.makeText(getApplication(), "Error en la obtención de todas las actividades: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Obtener todas las líneas
    private void cargarTodasLasLineas() {
        String token = "Bearer " + ApiClient.leerToken(application);
        Call<List<Linea>> callTodasLineas = ApiClient.getEndPoints().obtenerTodasLasLineas(token);
        callTodasLineas.enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Añadir todas las líneas al spinner
                    mListaLinea.getValue().addAll(response.body());
                } else {
                    Log.e("HomeViewModel", "Error al obtener todas las líneas: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Toast.makeText(getApplication(), "Error en la obtención de todas las líneas: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void limpiarSpinnersConHint() {
        // Crear una lista con solo el hint
        List<Actividad> hintActividad = new ArrayList<>();
        hintActividad.add(new Actividad(0, "Actividad")); // Puedes usar un id de 0 para el hint

        List<Linea> hintLinea = new ArrayList<>();
        hintLinea.add(new Linea(0, "Línea")); // Puedes usar un id de 0 para el hint

        // Actualiza los LiveData para los spinners
        mListaActividad.setValue(hintActividad);
        mListaLinea.setValue(hintLinea);
    }
}