package baigorriap.auditoriabpm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import baigorriap.auditoriabpm.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar como ActionBar
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Definir los destinos en los que no se mostrará la flecha de retroceso
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_auditoria)
                .setOpenableLayout(drawer)
                .build();

        // Configurar el NavController y la ActionBar
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Actualizar el encabezado del NavigationView
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String nombre = sharedPreferences.getString("nombre completo", "example");
        int legajo = sharedPreferences.getInt("legajo", 0);
        
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        TextView navHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);
        
        navHeaderTitle.setText("Supervisor " + nombre);
        navHeaderSubtitle.setText("Legajo: " + legajo);

        // Manejar la navegación a auditoría si viene del adaptador
        if (getIntent().getBooleanExtra("iniciarAuditoria", false)) {
            int destino = getIntent().getIntExtra("destino", R.id.nav_home);
            int idOperario = getIntent().getIntExtra("idOperario", -1);
            String nombreOperario = getIntent().getStringExtra("nombreOperario");
            int legajoOperario = getIntent().getIntExtra("legajoOperario", -1);
            
            // Guardar los datos del operario en SharedPreferences para que el fragmento los use
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("idOperario", idOperario);
            editor.putString("nombreOperarioAuditoria", nombreOperario);
            editor.putInt("legajoOperarioAuditoria", legajoOperario);
            editor.apply();
            
            // Crear Bundle con los datos
            Bundle bundle = new Bundle();
            bundle.putInt("idOperario", idOperario);
            bundle.putString("nombreOperario", nombreOperario);
            bundle.putInt("legajoOperario", legajoOperario);
            
            // Navegar al fragmento de auditoría con los datos
            navController.navigate(destino, bundle);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
