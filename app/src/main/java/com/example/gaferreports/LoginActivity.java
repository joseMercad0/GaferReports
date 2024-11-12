package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, togglePasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference empresaRef;
    private String enterpriseCode;
    private Button goToRegisterButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Obtener enterpriseCode del Intent
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");

        // Inicializar vistas
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);
        togglePasswordButton = findViewById(R.id.togglePasswordButton);
        progressBar = findViewById(R.id.progressBar);

        // Acción al hacer clic en el botón de login
        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            loginUser();
        });

        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Acción al hacer clic en el botón para ver/ocultar la contraseña
        togglePasswordButton.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordButton.setText("Mostrar Contraseña");
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordButton.setText("Ocultar Contraseña");
            }
            passwordEditText.setSelection(passwordEditText.length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Ingrese su correo electrónico");
            emailEditText.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Ingrese su contraseña");
            passwordEditText.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Autenticar al usuario usando Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Usuario autenticado exitosamente
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Obtener los datos de la empresa desde Firebase Realtime Database
                            empresaRef = FirebaseDatabase.getInstance().getReference().child("empresas").child(enterpriseCode);
                            empresaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String nombre = String.valueOf(snapshot.child("nombre").getValue());
                                        String ruc = String.valueOf(snapshot.child("ruc").getValue());
                                        String ubicacion = String.valueOf(snapshot.child("ubicacion").getValue());

                                        // Redirigir a MenuActivity y pasar los datos de la empresa
                                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                        intent.putExtra("enterpriseName", nombre);
                                        intent.putExtra("enterpriseRUC", ruc);
                                        intent.putExtra("enterpriseLocation", ubicacion);
                                        intent.putExtra("enterpriseCode", enterpriseCode);
                                        startActivity(intent);
                                        finish(); // Finalizar LoginActivity
                                    } else {
                                        Toast.makeText(LoginActivity.this, "No se encontraron datos de la empresa", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, "Error al obtener datos de la empresa: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "No se pudo obtener información del usuario", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Fallo la autenticación
                        Toast.makeText(LoginActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
