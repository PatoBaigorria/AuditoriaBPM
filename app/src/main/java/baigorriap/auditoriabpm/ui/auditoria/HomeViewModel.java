package baigorriap.auditoriabpm.ui.auditoria;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
    private MutableLiveData<Integer> mIdSupervisor;
    private MutableLiveData<Integer> mIdOperario;
    private MutableLiveData<Integer> mIdActividad;
    private MutableLiveData<Integer> mIdLinea;
    private MutableLiveData<String> mErrorMessage;
    private final Application application;


    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.application = application; // Inicializa la variable
        
        // Inicializar todas las MutableLiveData
        mText = new MutableLiveData<>();
        mLegajo = new MutableLiveData<>();
        mListaActividad = new MutableLiveData<>();
        mListaLinea = new MutableLiveData<>();
        mOperario = new MutableLiveData<>();
        mIdSupervisor = new MutableLiveData<>();
        mIdOperario = new MutableLiveData<>();
        mIdActividad = new MutableLiveData<>();
        mIdLinea = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        
        // Establecer valor inicial del texto
        mText.setValue("Datos del Operario");
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getMLegajo() {
        return mLegajo;
    }

    public LiveData<List<Actividad>> getMListaActividad() {
        return mListaActividad;
    }

    public LiveData<List<Linea>> getMListaLinea() {
        return mListaLinea;
    }

    public LiveData<Operario> getMOperario() {
        return mOperario;
    }
    public LiveData<Integer> getMIdSupervisor() {
        return mIdSupervisor;
    }
    public LiveData<Integer> getMIdOperario() {
        return mIdOperario;
    }
    public LiveData<Integer> getMIdActividad() {
        return mIdActividad;
    }
    public LiveData<Integer> getMIdLinea() {
        return mIdLinea;
    }

    // Método en el ViewModel para cargar el supervisor
    public void cargarSupervisorPorId(int idSupervisor) {
        if (mIdSupervisor != null) {
            mIdSupervisor.setValue(idSupervisor);
        }
    }


    public void cargarDatosPorLegajo(int legajo) {
        if (legajo <= 0) {
            mErrorMessage.setValue("Ingrese un Legajo Válido");
            return;
        }

        String token = "Bearer " + ApiClient.leerToken(application);
        Log.d("HomeViewModel", "Cargando datos para legajo: " + legajo);

        // Obtener actividades filtradas por legajo
        Call<List<Actividad>> callActividades = ApiClient.getEndPoints().obtenerActividades(token, legajo);
        callActividades.enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Log.d("HomeViewModel", "Actividades recibidas: " + response.body().size());
                    mListaActividad.setValue(response.body());
                } else {
                    Log.e("HomeViewModel", "Error al obtener actividades o lista vacía: " + 
                          (response.isSuccessful() ? "Lista vacía" : "Código: " + response.code()));
                    mListaActividad.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al obtener actividades: " + t.getMessage());
                mListaActividad.setValue(new ArrayList<>());
                mErrorMessage.setValue("Error al cargar actividades: " + t.getMessage());
            }
        });

        // Obtener líneas filtradas por legajo
        Call<List<Linea>> callLineas = ApiClient.getEndPoints().obtenerLineas(token, legajo);
        callLineas.enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Log.d("HomeViewModel", "Líneas recibidas: " + response.body().size());
                    mListaLinea.setValue(response.body());
                } else {
                    Log.e("HomeViewModel", "Error al obtener líneas o lista vacía: " + 
                          (response.isSuccessful() ? "Lista vacía" : "Código: " + response.code()));
                    mListaLinea.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al obtener líneas: " + t.getMessage());
                mListaLinea.setValue(new ArrayList<>());
                mErrorMessage.setValue("Error al cargar líneas: " + t.getMessage());
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
                    List<Actividad> actividadesActuales = mListaActividad.getValue();
                    List<Actividad> actividadesTodas = response.body();

                    // Filtrar actividades duplicadas
                    for (Actividad nuevaActividad : actividadesTodas) {
                        if (!actividadesActuales.contains(nuevaActividad)) {
                            actividadesActuales.add(nuevaActividad);
                        }
                    }

                    // Actualizar la lista de actividades en LiveData
                    mListaActividad.setValue(actividadesActuales);
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
                    List<Linea> lineasActuales = mListaLinea.getValue();
                    List<Linea> lineasTodas = response.body();

                    // Filtrar lineas duplicadas
                    for (Linea nuevaLinea : lineasTodas) {
                        if (!lineasActuales.contains(nuevaLinea)) {
                            lineasActuales.add(nuevaLinea);
                        }
                    }
                    // Actualizar la lista de actividades en LiveData
                    mListaLinea.setValue(lineasActuales);
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

    public void limpiarTodosDatos() {
        mOperario.setValue(null);
        mIdSupervisor.setValue(null);
        mIdOperario.setValue(null);
        mIdActividad.setValue(null);
        mIdLinea.setValue(null);
        mListaActividad.setValue(new ArrayList<>());
        mListaLinea.setValue(new ArrayList<>());
        mLegajo.setValue("");
    }
}