package baigorriap.auditoriabpm.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import baigorriap.auditoriabpm.LoginActivity;
import baigorriap.auditoriabpm.R;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
