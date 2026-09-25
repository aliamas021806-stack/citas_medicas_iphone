package com.tuempresa.citasmedicas.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.data.firebase.FirebaseManager;
import com.tuempresa.citasmedicas.data.firebase.FirestoreSeeder;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.User;
import com.tuempresa.citasmedicas.util.Resource;
import com.tuempresa.citasmedicas.util.SessionManager;
import com.tuempresa.citasmedicas.view.main.MainActivity;
import com.tuempresa.citasmedicas.viewmodel.AuthViewModel;

/**
 * Pantalla de Login / Registro simulado (View). Incluye la EDAD del paciente
 * en el registro.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilAge;
    private TextInputEditText etName, etAge, etEmail, etPassword;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private TextView tvToggle, tvError;

    private boolean isRegisterMode = false;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnSubmit.setOnClickListener(v -> submit());
        tvToggle.setOnClickListener(v -> toggleMode());

        observeViewModel();
    }

    private void bindViews() {
        tilName = findViewById(R.id.tilName);
        tilAge = findViewById(R.id.tilAge);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        tvToggle = findViewById(R.id.tvToggle);
        tvError = findViewById(R.id.tvError);
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            tilName.setVisibility(View.VISIBLE);
            tilAge.setVisibility(View.VISIBLE);
            btnSubmit.setText(R.string.register_button);
            tvToggle.setText(R.string.toggle_login);
        } else {
            tilName.setVisibility(View.GONE);
            tilAge.setVisibility(View.GONE);
            btnSubmit.setText(R.string.login_button);
            tvToggle.setText(R.string.toggle_register);
        }
        hideError();
    }

    private void submit() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        hideError();

        if (isRegisterMode) {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                showError("Por favor ingresa tu nombre completo");
                return;
            }
            String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
            int age = 0;
            if (!TextUtils.isEmpty(ageStr)) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException ignored) {
                    age = 0;
                }
            }
            if (ageStr.isEmpty() || age < 1 || age > 120) {
                showError("Ingresa una edad válida (entre 1 y 120 años)");
                return;
            }
            viewModel.register(name, email, password, age);
        } else {
            viewModel.login(email, password);
        }
    }

    private void observeViewModel() {
        viewModel.getAuthState().observe(this, resource -> {
            if (resource == null) {
                return;
            }
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    hideError();
                    break;
                case SUCCESS:
                    setLoading(false);
                    onAuthSuccess(resource.data);
                    break;
                case ERROR:
                    setLoading(false);
                    showError(resource.message);
                    break;
            }
        });
    }

    private void onAuthSuccess(User user) {
        SessionManager.getInstance(this).saveSession(user);

        // Si Firestore esta disponible, siembra los datos sinteticos UNA sola vez
        // (idempotente: 1 lectura si ya existen). No bloquea la navegacion.
        if (FirebaseManager.isAvailable()) {
            FirestoreSeeder.seedIfNeeded(new RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                }

                @Override
                public void onError(String message) {
                }
            });
        }

        Toast.makeText(this,
                isRegisterMode ? "Cuenta creada. ¡Bienvenido!" : "¡Bienvenido de nuevo!",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    private void showError(String message) {
        if (message == null) {
            return;
        }
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}
