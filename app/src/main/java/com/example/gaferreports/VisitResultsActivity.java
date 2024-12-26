package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class VisitResultsActivity extends AppCompatActivity {

    private LinearLayout layoutVisitResults;
    private Spinner spinnerTechnician;
    private EditText editTextClientName;
    private EditText editTextObservations;
    private Button buttonAddVisitResult;
    private Button buttonSaveVisitResults;
    private List<EditText> visitResultFields;
    private String enterpriseCode;
    private CheckBox checkBoxAllTraps;
    private EditText editTextAllTraps;
    private CheckBox checkBoxInaccessibleTraps, checkBoxReplacedBait, checkBoxChangedTraps;
    private EditText editTextInaccessibleTraps, editTextReplacedBait, editTextChangedTraps;
    private CheckBox checkBoxTechnician1, checkBoxTechnician2, checkBoxTechnician3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_results);

        // Obtener enterpriseCode del Intent
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");

        // Verificar que enterpriseCode no sea nulo
        if (enterpriseCode == null || enterpriseCode.isEmpty()) {
            Toast.makeText(this, "Error: enterpriseCode no recibido", Toast.LENGTH_SHORT).show();
            finish(); // Finaliza la actividad si no se recibe enterpriseCode
            return;
        }

        layoutVisitResults = findViewById(R.id.layoutVisitResults);
        checkBoxTechnician1 = findViewById(R.id.checkBoxTechnician1);
        checkBoxTechnician2 = findViewById(R.id.checkBoxTechnician2);
        checkBoxTechnician3 = findViewById(R.id.checkBoxTechnician3);
        editTextClientName = findViewById(R.id.editTextClientName);
        editTextObservations = findViewById(R.id.editTextObservations);
        buttonAddVisitResult = findViewById(R.id.buttonAddVisitResult);
        buttonSaveVisitResults = findViewById(R.id.buttonSaveVisitResults);
        visitResultFields = new ArrayList<>();

        // Listener para añadir nuevos campos de resultado de visita
        buttonAddVisitResult.setOnClickListener(v -> {
            if (visitResultFields.size() < 7) {
                addVisitResultField();
            } else {
                Toast.makeText(this, "Máximo 7 resultados permitidos.", Toast.LENGTH_SHORT).show();
            }
        });


        // Inicializar los CheckBoxes y EditTexts
        checkBoxInaccessibleTraps = findViewById(R.id.checkBoxInaccessibleTraps);
        editTextInaccessibleTraps = findViewById(R.id.editTextInaccessibleTraps);

        checkBoxReplacedBait = findViewById(R.id.checkBoxReplacedBait);
        editTextReplacedBait = findViewById(R.id.editTextReplacedBait);

        checkBoxChangedTraps = findViewById(R.id.checkBoxChangedTraps);
        editTextChangedTraps = findViewById(R.id.editTextChangedTraps);

        checkBoxAllTraps = findViewById(R.id.checkBoxAllTraps);
        editTextAllTraps = findViewById(R.id.editTextAllTraps);
        // Configura el texto predeterminado en el EditText
        editTextAllTraps.setText("Se registraron todas las trampas correctamente");

        checkBoxAllTraps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Muestra el EditText con el texto configurado
                editTextAllTraps.setVisibility(View.VISIBLE);
            } else {
                // Oculta el EditText
                editTextAllTraps.setVisibility(View.GONE);
            }
        });

        // Listener para el CheckBox de trampas inaccesibles
        checkBoxInaccessibleTraps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextInaccessibleTraps.setVisibility(View.VISIBLE);
                editTextInaccessibleTraps.setText("Hay trampas que no son registradas por inaccesibilidad");
            } else {
                editTextInaccessibleTraps.setVisibility(View.GONE);
            }
        });

        // Listener para el CheckBox de cebos reemplazados
        checkBoxReplacedBait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextReplacedBait.setVisibility(View.VISIBLE);
                editTextReplacedBait.setText("Se reemplazaron los cebos que no fueron consumidos");
            } else {
                editTextReplacedBait.setVisibility(View.GONE);
            }
        });

        // Listener para el CheckBox de cambio de trampas
        checkBoxChangedTraps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextChangedTraps.setVisibility(View.VISIBLE);
                editTextChangedTraps.setText("Se cambiaron los tipos de trampas");
            } else {
                editTextChangedTraps.setVisibility(View.GONE);
            }
        });

        // Listener para guardar los resultados de visita
        buttonSaveVisitResults.setOnClickListener(v -> saveVisitResults());
    }

    private void addVisitResultField() {
        EditText editText = new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Escriba un resultado de visita...");
        editText.setBackgroundResource(R.drawable.edittext_background);
        layoutVisitResults.addView(editText);
        visitResultFields.add(editText);
    }

    private void saveVisitResults() {
        List<String> visitResults = new ArrayList<>();

        // Guardar los resultados de los EditText generados por el botón
        for (EditText editText : visitResultFields) {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {
                visitResults.add(text);
            }
        }

        // Agregar los textos de los EditText de los CheckBoxes si están visibles (es decir, si fueron marcados)
        if (checkBoxAllTraps.isChecked() && editTextAllTraps.getVisibility() == View.VISIBLE) {
            String allTrapsText = editTextAllTraps.getText().toString().trim();
            if (!allTrapsText.isEmpty()) {
                visitResults.add(allTrapsText);
            }
        }

        if (checkBoxInaccessibleTraps.isChecked() && editTextInaccessibleTraps.getVisibility() == View.VISIBLE) {
            String inaccessibleTrapsText = editTextInaccessibleTraps.getText().toString().trim();
            if (!inaccessibleTrapsText.isEmpty()) {
                visitResults.add(inaccessibleTrapsText);
            }
        }

        if (checkBoxReplacedBait.isChecked() && editTextReplacedBait.getVisibility() == View.VISIBLE) {
            String replacedBaitText = editTextReplacedBait.getText().toString().trim();
            if (!replacedBaitText.isEmpty()) {
                visitResults.add(replacedBaitText);
            }
        }

        if (checkBoxChangedTraps.isChecked() && editTextChangedTraps.getVisibility() == View.VISIBLE) {
            String changedTrapsText = editTextChangedTraps.getText().toString().trim();
            if (!changedTrapsText.isEmpty()) {
                visitResults.add(changedTrapsText);
            }
        }

        // Obtener el técnico seleccionado, el nombre del cliente y las observaciones
        // Capturar técnicos seleccionados
        List<String> selectedTechnicians = new ArrayList<>();
        if (checkBoxTechnician1.isChecked()) selectedTechnicians.add("Alexander Gárate");
        if (checkBoxTechnician2.isChecked()) selectedTechnicians.add("Wilmer Taipe");
        if (checkBoxTechnician3.isChecked()) selectedTechnicians.add("Isaac Pusari");

        String clientName = editTextClientName.getText().toString();
        String observations = editTextObservations.getText().toString();

        // Guardar los datos en Firebase bajo el child "developed_activities" usando enterpriseCode
        DatabaseReference developedActivitiesRef = FirebaseDatabase.getInstance()
                .getReference("empresas")
                .child(enterpriseCode)
                .child("developed_activities");

        // Crear un objeto DevelopedActivity con los datos obtenidos
        DevelopedActivity developedActivity = new DevelopedActivity(clientName, selectedTechnicians, observations, visitResults);

        // Guardar los datos en Firebase
        developedActivitiesRef.child("datosVisit").setValue(developedActivity)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VisitResultsActivity.this, "Resultados guardados exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VisitResultsActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    }
                });

        // Guardar datos en el Intent para regresar a ServiceReportActivity
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("visitResults", new ArrayList<>(visitResults));
        resultIntent.putExtra("selectedTechnicians", new ArrayList<>(selectedTechnicians));
        resultIntent.putExtra("clientName", clientName);
        resultIntent.putExtra("observations", observations);
        setResult(RESULT_OK, resultIntent);

        finish(); // Finalizar la actividad
    }


    // Clase DevelopedActivity para organizar los datos que se guardarán en Firebase
    public static class DevelopedActivity {
        public String clientName;
        public List<String> selectedTechnicians;
        public String observations;
        public List<String> visitResults;

        public DevelopedActivity(String clientName, List<String> selectedTechnicians, String observations, List<String> visitResults) {
            this.clientName = clientName;
            this.selectedTechnicians = selectedTechnicians;
            this.observations = observations;
            this.visitResults = visitResults;
        }
    }
}
