package baigorriap.auditoriabpm.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.AltaAuditoriaRequest;
import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM.EstadoEnum;
import baigorriap.auditoriabpm.model.EstadisticasAuditoria;
import baigorriap.auditoriabpm.model.FirmaPatron;
import baigorriap.auditoriabpm.model.ItemBPM;
import baigorriap.auditoriabpm.model.ItemNoOk;
import baigorriap.auditoriabpm.model.Linea;
import baigorriap.auditoriabpm.model.Operario;
import baigorriap.auditoriabpm.model.OperarioSinAuditoria;
import baigorriap.auditoriabpm.model.ReporteAuditorias;
import baigorriap.auditoriabpm.model.Supervisor;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Clase que maneja la configuración y creación de la API de Retrofit.
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    public static final String URL = "http://192.168.100.60:5000/";
    private static MisEndPoints mep;

    /**
     * Método que devuelve una instancia de la interfaz de la API.
     * @return Instancia de la interfaz de la API.
     */
    public static MisEndPoints getEndPoints(){
        Gson gson = new GsonBuilder()
            .setLenient()
            .registerTypeAdapter(EstadoEnum.class, new JsonDeserializer<EstadoEnum>() {
                @Override
                public EstadoEnum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    try {
                        int value = json.getAsInt();
                        Log.d(TAG, "Deserializando EstadoEnum desde valor: " + value);
                        EstadoEnum estado = EstadoEnum.fromValue(value);
                        Log.d(TAG, "Estado deserializado: " + estado);
                        return estado;
                    } catch (Exception e) {
                        Log.e(TAG, "Error deserializando EstadoEnum: " + e.getMessage());
                        return null;
                    }
                }
            })
            .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mep = retrofit.create(MisEndPoints.class);
        return mep;
    }

    /**
     * Interfaz que define los endpoints de la API.
     */
    public interface MisEndPoints {
        // Autenticación
        @FormUrlEncoded
        @POST("Supervisores/login")
        Call<String> login(@Field("Legajo") String u, @Field("Clave") String c);

        // Perfil
        @GET("Supervisores")
        Call<Supervisor> miPerfil(@Header("Authorization") String token);

        @GET("Supervisores/{id}")
        Call<Supervisor> obtenerSupervisor(@Header("Authorization") String token, @Path("id") int idSupervisor);

        // Actividades
        @GET("Actividades/byLegajo")
        Call<List<Actividad>> obtenerActividades(@Header("Authorization") String token, @Query("legajo") int legajo);

        @GET("Actividades/{id}")
        Call<Actividad> obtenerActividadPorId(@Header("Authorization") String token, @Path("id") int idActividad);

        @GET("Actividades/todas")
        Call<List<Actividad>> obtenerTodasLasActividades(@Header("Authorization") String token);

        // Líneas
        @GET("Lineas/byLegajo")
        Call<List<Linea>> obtenerLineas(@Header("Authorization") String token, @Query("legajo") int legajo);

        @GET("Lineas/{id}")
        Call<Linea> obtenerLineaPorId(@Header("Authorization") String token, @Path("id") int idLinea);

        @GET("Lineas/todas")
        Call<List<Linea>> obtenerTodasLasLineas(@Header("Authorization") String token);

        // Operarios
        @GET("Operarios/byLegajo")
        Call<Operario> obtenerOperario(@Header("Authorization") String token, @Query("legajo") int legajo);

        @GET("Operarios/{id}")
        Call<Operario> obtenerOperarioPorId(@Header("Authorization") String token, @Path("id") int idOperario);

        @GET("Operarios/validar-legajo/{legajo}")
        Call<Boolean> verificarLegajo(@Header("Authorization") String token, @Path("legajo") int legajo);

        // Items BPM
        @GET("ItemsBPM/{id}")
        Call<ItemBPM> obtenerItemBPMPorId(@Header("Authorization") String token, @Path("id") int idItemBPM);

        // Auditorías
        @POST("Auditorias/alta-auditoria-completa")
        Call<ResponseBody> altaAuditoriaCompleta(@Header("Authorization") String token, @Body AltaAuditoriaRequest auditoria);

        @GET("Auditorias/auditorias-operario")
        Call<List<OperarioSinAuditoria>> obtenerOperariosSinAuditorias(@Header("Authorization") String token);

        @GET("Auditorias/por-fecha")
        Call<List<Auditoria>> obtenerAuditoriasPorFecha(
            @Header("Authorization") String token,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate,
            @Query("supervisorId") int supervisorId
        );

        @GET("Auditorias/cantidad-auditorias-mes-a-mes")
        Call<Map<String, EstadisticasAuditoria>> obtenerEstadisticasAuditoria(
                @Header("Authorization") String token, 
                @Query("anioInicio") int anioInicio,
                @Query("anioFin") int anioFin
        );

        @GET("AuditoriasItemBPM/estado-nook-por-operario")
        Call<List<ItemNoOk>> obtenerAuditoriasNookPorOperario(
            @Header("Authorization") String token, 
            @Query("legajo") int legajo
        );

        // Firma patrón
        @POST("FirmaPatron/alta")
        Call<ResponseBody> guardarFirmaPatron(@Header("Authorization") String token, @Body FirmaPatron firmaPatron);

        @POST("FirmaPatron/verificar")
        Call<Boolean> verificarFirma(@Header("Authorization") String token, @Body FirmaPatron firma);

        @GET("FirmaPatron/operario/{idOperario}")
        Call<FirmaPatron> obtenerFirmaPatron(@Header("Authorization") String token, @Path("idOperario") int idOperario);
    }

    /**
     * Método que guarda el token de autenticación en las preferencias compartidas.
     * @param token Token de autenticación.
     * @param context Contexto de la aplicación.
     */
    public static void guardarToken(String token, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    /**
     * Método que lee el token de autenticación desde las preferencias compartidas.
     * @param context Contexto de la aplicación.
     * @return Token de autenticación.
     */
    public static String leerToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Método que elimina el token de autenticación desde las preferencias compartidas.
     * @param context Contexto de la aplicación.
     */
    public static void eliminarToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", "");
        editor.apply();
    }
}
