package baigorriap.auditoriabpm.ui.cerrarSesion;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.navigation.NavOptions;

import baigorriap.auditoriabpm.LoginActivity;
import baigorriap.auditoriabpm.MenuActivity;
import baigorriap.auditoriabpm.R;
import baigorriap.auditoriabpm.request.ApiClient;

public class SlideshowViewModel extends AndroidViewModel {

    public SlideshowViewModel(@NonNull Application application) {
        super(application);
    }

    public static void mostrarDialogo(Context context, View root) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmar salida");
        builder.setMessage("¿Está seguro que desea salir?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("Desloguear", ApiClient.leerToken(context.getApplicationContext()));

                // Eliminar el token
                ApiClient.eliminarToken(context.getApplicationContext());

                // Crear un Intent para iniciar la actividad de inicio de sesión
                Intent intent = new Intent(context, LoginActivity.class);

                // Establecer los flags para limpiar la pila de actividades
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                // Iniciar la actividad de inicio de sesión
                context.startActivity(intent);

                // Finalizar la actividad actual
                ((MenuActivity) context).finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Navegar al HomeFragment
                androidx.navigation.Navigation.findNavController(root).navigate(
                    R.id.action_nav_slideshow_to_nav_home,
                    null,
                    new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, true)
                        .build()
                );
            }
        });
        builder.show();
    }
}