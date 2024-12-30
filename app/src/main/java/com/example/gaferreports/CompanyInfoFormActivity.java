package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CompanyInfoFormActivity extends AppCompatActivity {

    private EditText editTextEnterpriseName, editTextDate;
    private TimePicker timePickerStart;
    private Button buttonSave, buttonGoToTraps, buttonGeneratePDF;
    private String enterpriseCode;
    private DatabaseReference databaseRef;
    private ArrayList<TrapEntry> traps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_info_form);

        // Inicializar vistas
        editTextEnterpriseName = findViewById(R.id.editTextEnterpriseName);
        editTextDate = findViewById(R.id.editTextDate);
        timePickerStart = findViewById(R.id.timePickerStart);
        buttonSave = findViewById(R.id.buttonSave);
        buttonGoToTraps = findViewById(R.id.buttonGoToTraps);
        buttonGeneratePDF = findViewById(R.id.buttonGeneratePDF);

        // Obtener enterpriseCode del Intent
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        traps = getIntent().getParcelableArrayListExtra("traps");

        // Inicializar referencia a la base de datos de Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode);

        // Cargar datos de la empresa desde Firebase
        loadCompanyData();

        // Generar y mostrar la fecha actual automáticamente
        generateCurrentDate();

        // Configurar botón de guardar
        buttonSave.setOnClickListener(v -> {
            saveCompanyInfo();
        });

        // Configurar botón de ir a trampas
        buttonGoToTraps.setOnClickListener(v -> {
            Intent intent = new Intent(CompanyInfoFormActivity.this, TrapStatusActivity.class);
            intent.putExtra("enterpriseCode", enterpriseCode);
            intent.putExtra("date", editTextDate.getText().toString().trim());
            startActivity(intent);
        });

        // Configurar botón de generar PDF
        buttonGeneratePDF.setOnClickListener(v -> {
            generatePDF(traps);
        });
    }

    private void loadCompanyData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String enterpriseName = snapshot.child("nombre").getValue(String.class);
                    editTextEnterpriseName.setText(enterpriseName);
                } else {
                    Toast.makeText(CompanyInfoFormActivity.this, "No se encontraron datos de la empresa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CompanyInfoFormActivity.this, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        editTextDate.setText(currentDate);
    }

    private void saveCompanyInfo() {
        String date = editTextDate.getText().toString().trim();
        int hour = timePickerStart.getCurrentHour();
        int minute = timePickerStart.getCurrentMinute();

        if (date.isEmpty()) {
            editTextDate.setError("Ingrese la fecha");
            editTextDate.requestFocus();
            return;
        }

        Map<String, Object> companyData = new HashMap<>();
        companyData.put("fecha", date);
        companyData.put("horaIngreso", String.format("%02d:%02d", hour, minute));

        databaseRef.updateChildren(companyData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CompanyInfoFormActivity.this, "Información guardada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CompanyInfoFormActivity.this, "Error al guardar la información", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePDF(List<TrapEntry> traps) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String enterpriseName = snapshot.child("nombre").getValue(String.class);
                    String date = snapshot.child("fecha").getValue(String.class);
                    String startTime = snapshot.child("horaIngreso").getValue(String.class);
                    String endTime = snapshot.child("horaSalida").getValue(String.class);

                    // Generar el PDF con los datos obtenidos
                    try {
                        File pdfFile = new File(getExternalFilesDir(null), "Estacion_" + enterpriseName + ".pdf");
                        PDFUtils.createPDF(CompanyInfoFormActivity.this, pdfFile, enterpriseName, date, startTime, endTime, traps);
                        Toast.makeText(CompanyInfoFormActivity.this, "PDF generado: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

                        // Redirigir a MenuActivity después de generar el PDF
                        Intent intent = new Intent(CompanyInfoFormActivity.this, MenuActivity.class);
                        intent.putExtra("enterpriseCode", enterpriseCode);
                        startActivity(intent);
                        finish(); // Finalizar la actividad actual
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CompanyInfoFormActivity.this, "Error al generar el PDF", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CompanyInfoFormActivity.this, "No se encontraron datos de la empresa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CompanyInfoFormActivity.this, "Error al obtener los datos de la empresa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Dejar este método vacío o mostrar un mensaje si es necesario.
        // No llamar a super.onBackPressed() para deshabilitar el comportamiento predeterminado.
    }
}
