package baigorriap.auditoriabpm.request;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import baigorriap.auditoriabpm.model.Actividad;
import baigorriap.auditoriabpm.model.AltaAuditoriaRequest;
import baigorriap.auditoriabpm.model.Auditoria;
import baigorriap.auditoriabpm.model.AuditoriaItemBPM;
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
 * Created by etsda on 24/01/2018.
 */

public class ApiClient {
    public static final String URL = "http://192.168.100.3:5000/";
    private static MisEndPoints mep;

    public static MisEndPoints getEndPoints(){
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mep = retrofit.create(MisEndPoints.class);
        return mep;
    }

    public interface MisEndPoints {
        @FormUrlEncoded
        @POST("Supervisores/login")
        Call<String> login(@Field("Legajo") String u, @Field("Clave") String c);

        @GET("Supervisores")
        Call<Supervisor> miPerfil(@Header("Authorization") String token);

        @GET("Actividades/byLegajo")
        Call<List<Actividad>> obtenerActividades(@Header("Authorization") String token, @Query("legajo") int legajo);

        @GET("Lineas/byLegajo")
        Call<List<Linea>> obtenerLineas(@Header("Authorization") String token, @Query("legajo") int legajo);

        @GET("Operarios/byLegajo")
        Call<Operario> obtenerOperario(@Header("Authorization") String token, @Query("legajo") int legajo);

        @GET("Actividades/todas")
        Call<List<Actividad>> obtenerTodasLasActividades(@Header("Authorization") String token);

        @GET("Lineas/todas")
        Call<List<Linea>> obtenerTodasLasLineas(@Header("Authorization") String token);

        @GET("Actividades")
        Call<List<Actividad>> obtenerTodasActividades(@Header("Authorization") String token);

        @GET("Lineas")
        Call<List<Linea>> obtenerTodasLineas(@Header("Authorization") String token);

        @GET("Operarios/validar-legajo/{legajo}")
        Call<Boolean> verificarLegajo(@Header("Authorization") String token, @Path("legajo") int legajo);

        @POST("Auditorias/alta-auditoria-completa")
        Call<ResponseBody> altaAuditoriaCompleta(@Header("Authorization") String token, @Body AltaAuditoriaRequest auditoria);

        @POST("FirmaPatron/alta")
        Call<ResponseBody> guardarFirmaPatron(@Header("Authorization") String token, @Body FirmaPatron firmaPatron);

        @POST("FirmaPatron/verificar")
        Call<Boolean> verificarFirma(@Header("Authorization") String token, @Body FirmaPatron firma);

        @GET("FirmaPatron/operario/{idOperario}")
        Call<FirmaPatron> obtenerFirmaPatron(@Header("Authorization") String token, @Path("idOperario") int idOperario);

        @GET("Auditorias/auditorias-operario")
        Call<List<OperarioSinAuditoria>> obtenerOperariosSinAuditorias(@Header("Authorization") String token);

        @GET("Auditorias/estadisticas")
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
    }

    public static void guardarToken(String token, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static String leerToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        return token != null ? "Bearer " + token : null;
    }

    public static void eliminarToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", "");
        editor.apply();
    }
}
