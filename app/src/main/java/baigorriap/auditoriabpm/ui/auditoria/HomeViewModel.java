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


    public void cargarOperarioPorLegajo(int legajo) {
        if (legajo <= 0) {
            mErrorMessage.setValue("Ingrese un Legajo Válido");
            return;
        }

        String token = "Bearer " + ApiClient.leerToken(application);
        Log.d("HomeViewModel", "Obteniendo operario con legajo: " + legajo);

        Call<Operario> callOperario = ApiClient.getEndPoints().obtenerOperario(token, legajo);
        callOperario.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();
                    mOperario.setValue(operario);
                    mIdOperario.setValue(operario.getIdOperario());
                    Log.d("HomeViewModel", "Operario obtenido: " + operario.toString());
                } else {
                    mOperario.setValue(null);
                    mIdOperario.setValue(null);
                    Log.e("HomeViewModel", "Operario no encontrado para legajo: " + legajo);
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                mOperario.setValue(null);
                mIdOperario.setValue(null);
                mErrorMessage.setValue("Error en la obtención del operario: " + t.getMessage());
                Log.e("HomeViewModel", "Error en la obtención del operario: " + t.getMessage());
            }
        });
    }

    public void cargarDatosPorLegajo(int legajo) {
        if (legajo <= 0) {
            mErrorMessage.setValue("Ingrese un Legajo Válido");
            return;
        }

        String token = "Bearer " + ApiClient.leerToken(application);
        
        // Primero obtener la actividad y línea específica del operario
        Call<List<Actividad>> callActividadOperario = ApiClient.getEndPoints().obtenerActividades(token, legajo);
        callActividadOperario.enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Actividad actividadOperario = response.body().get(0);
                    mIdActividad.setValue(actividadOperario.getIdActividad());
                    
                    // Después de obtener la actividad del operario, cargar todas las actividades
                    cargarTodasLasActividades(token, actividadOperario);
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al obtener actividad del operario: " + t.getMessage());
                cargarTodasLasActividades(token, null);
            }
        });

        // Obtener la línea específica del operario
        Call<List<Linea>> callLineaOperario = ApiClient.getEndPoints().obtenerLineas(token, legajo);
        callLineaOperario.enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Linea lineaOperario = response.body().get(0);
                    mIdLinea.setValue(lineaOperario.getIdLinea());
                    
                    // Después de obtener la línea del operario, cargar todas las líneas
                    cargarTodasLasLineas(token, lineaOperario);
                }
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al obtener línea del operario: " + t.getMessage());
                cargarTodasLasLineas(token, null);
            }
        });
    }

    private void cargarTodasLasActividades(String token, Actividad actividadSeleccionada) {
        Call<List<Actividad>> callTodasActividades = ApiClient.getEndPoints().obtenerTodasLasActividades(token);
        callTodasActividades.enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Actividad> todasActividades = new ArrayList<>();
                    // Agregar el placeholder
                    todasActividades.add(new Actividad(-1, "Actividad"));
                    // Agregar todas las actividades
                    todasActividades.addAll(response.body());
                    mListaActividad.setValue(todasActividades);
                    
                    // Si hay una actividad seleccionada, asegurarnos de que esté en la lista
                    if (actividadSeleccionada != null) {
                        mIdActividad.setValue(actividadSeleccionada.getIdActividad());
                    }
                } else {
                    limpiarSpinnersConHint();
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al obtener todas las actividades: " + t.getMessage());
                limpiarSpinnersConHint();
            }
        });
    }

    private void cargarTodasLasLineas(String token, Linea lineaSeleccionada) {
        Call<List<Linea>> callTodasLineas = ApiClient.getEndPoints().obtenerTodasLasLineas(token);
        callTodasLineas.enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Linea> todasLineas = new ArrayList<>();
                    // Agregar el placeholder
                    todasLineas.add(new Linea(-1, "Línea"));
                    // Agregar todas las líneas
                    todasLineas.addAll(response.body());
                    mListaLinea.setValue(todasLineas);
                    
                    // Si hay una línea seleccionada, asegurarnos de que esté en la lista
                    if (lineaSeleccionada != null) {
                        mIdLinea.setValue(lineaSeleccionada.getIdLinea());
                    }
                } else {
                    limpiarSpinnersConHint();
                }
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al obtener todas las líneas: " + t.getMessage());
                limpiarSpinnersConHint();
            }
        });
    }

    public void limpiarSpinnersConHint() {
        List<Actividad> actividadesVacias = new ArrayList<>();
        actividadesVacias.add(new Actividad(-1, "Actividad"));
        mListaActividad.setValue(actividadesVacias);

        List<Linea> lineasVacias = new ArrayList<>();
        lineasVacias.add(new Linea(-1, "Línea"));
        mListaLinea.setValue(lineasVacias);
    }

    public void limpiarTodosDatos() {
        mOperario.setValue(null);
        mIdSupervisor.setValue(null);
        mIdOperario.setValue(null);
        mIdActividad.setValue(null);
        mIdLinea.setValue(null);
        limpiarSpinnersConHint();
        mLegajo.setValue("");
    }

    public void buscarOperario(int legajo) {
        String token = ApiClient.leerToken(application);
        if (token == null) {
            mErrorMessage.setValue("Error: No se encontró el token");
            return;
        }

        ApiClient.getEndPoints().obtenerOperario("Bearer " + token, legajo).enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();
                    mOperario.setValue(operario);
                    mIdOperario.setValue(operario.getIdOperario());
                    mIdActividad.setValue(operario.getIdActividad());
                    mIdLinea.setValue(operario.getIdLinea());

                    // Cargar actividades y líneas asociadas
                    cargarDatosPorLegajo(legajo);
                } else {
                    mOperario.setValue(null);
                    mErrorMessage.setValue("No se encontró ningún operario con ese legajo");
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Log.e("HomeViewModel", "Error al buscar operario", t);
                mOperario.setValue(null);
                mErrorMessage.setValue("Error al buscar operario: " + t.getMessage());
            }
        });
    }

    private void cargarActividades() {
        String token = ApiClient.leerToken(application);
        if (token == null) return;

        ApiClient.getEndPoints().obtenerTodasLasActividades("Bearer " + token).enqueue(new Callback<List<Actividad>>() {
            @Override
            public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Actividad> actividades = new ArrayList<>();
                    actividades.add(new Actividad(-1, "Actividad")); // Placeholder
                    actividades.addAll(response.body());
                    mListaActividad.setValue(actividades);
                }
            }

            @Override
            public void onFailure(Call<List<Actividad>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al cargar actividades", t);
                mErrorMessage.setValue("Error al cargar actividades: " + t.getMessage());
            }
        });
    }

    private void cargarLineas() {
        String token = ApiClient.leerToken(application);
        if (token == null) return;

        ApiClient.getEndPoints().obtenerTodasLasLineas("Bearer " + token).enqueue(new Callback<List<Linea>>() {
            @Override
            public void onResponse(Call<List<Linea>> call, Response<List<Linea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Linea> lineas = new ArrayList<>();
                    lineas.add(new Linea(-1, "Línea")); // Placeholder
                    lineas.addAll(response.body());
                    mListaLinea.setValue(lineas);
                }
            }

            @Override
            public void onFailure(Call<List<Linea>> call, Throwable t) {
                Log.e("HomeViewModel", "Error al cargar líneas", t);
                mErrorMessage.setValue("Error al cargar líneas: " + t.getMessage());
            }
        });
    }
}