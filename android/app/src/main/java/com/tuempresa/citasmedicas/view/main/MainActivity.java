package com.tuempresa.citasmedicas.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.util.SessionManager;
import com.tuempresa.citasmedicas.view.appointment.CitasFragment;
import com.tuempresa.citasmedicas.view.auth.LoginActivity;

/**
 * Actividad contenedora principal (Doctores, Mis Citas, Perfil).
 */
public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_doctors);
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        int id = item.getItemId();
        if (id == R.id.nav_appointments) {
            fragment = new CitasFragment();
            toolbar.setTitle(R.string.tab_appointments);
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            toolbar.setTitle(R.string.tab_profile);
        } else {
            fragment = new DoctorsFragment();
            toolbar.setTitle(R.string.tab_doctors);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        return true;
    }

    public void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
