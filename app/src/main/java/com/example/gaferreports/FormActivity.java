package com.example.gaferreports;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FormActivity extends AppCompatActivity {

    private Spinner spinnerTrapType, spinnerPoisonType, spinnerPoisonAmount, spinnerReplaceAmount, spinnerReplacePoisonType;
    private Spinner spinnerConsumptionPercentage;
    private RadioGroup radioGroupConsumption, radioGroupReplace;
    private Button buttonSave;
    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<HistoryEntry> historyEntries;
    private DatabaseReference trapRef;
    private String enterpriseCode;
    private int trapNumber;
    private String currentDate;
    private CheckBox checkBoxNoAccess;
    private CheckBox checkboxNoChanges;
    private TrapEntry currentTrapEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        spinnerTrapType = findViewById(R.id.spinnerTrapType);
        spinnerPoisonType = findViewById(R.id.spinnerPoisonType);
        spinnerPoisonAmount = findViewById(R.id.spinnerPoisonAmount);
        radioGroupConsumption = findViewById(R.id.radioGroupConsumption);
        spinnerConsumptionPercentage = findViewById(R.id.spinnerConsumptionPercentage);
        radioGroupReplace = findViewById(R.id.radioGroupReplace);
        spinnerReplaceAmount = findViewById(R.id.spinnerReplaceAmount);
        spinnerReplacePoisonType = findViewById(R.id.selectReplacePoisonType);
        buttonSave = findViewById(R.id.buttonSave);
        checkBoxNoAccess = findViewById(R.id.checkBoxNoAccess);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        checkboxNoChanges = findViewById(R.id.checkbox_no_changes);
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        trapNumber = getIntent().getIntExtra("trapNumber", -1);
        currentDate = getIntent().getStringExtra("currentDate");

        currentDate = getIntent().getStringExtra("date");

        trapRef = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode).child("trampas").child(String.valueOf(trapNumber));

        setupSpinners();
        setupRadioGroups();
        setupRecyclerView();
        setupCheckBox();
        loadHistoryEntries();

        checkBoxNoAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setFormEnabled(!isChecked);
        });

        buttonSave.setOnClickListener(v -> showConfirmationDialog());
    }



    private void setupSpinners() {
        ArrayAdapter<CharSequence> trapTypeAdapter = ArrayAdapter.createFromResource(this, R.array.trap_types, android.R.layout.simple_spinner_item);
        trapTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrapType.setAdapter(trapTypeAdapter);

        ArrayAdapter<CharSequence> poisonTypeAdapter = ArrayAdapter.createFromResource(this, R.array.poison_types, android.R.layout.simple_spinner_item);
        poisonTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoisonType.setAdapter(poisonTypeAdapter);

        ArrayAdapter<CharSequence> poisonAmountAdapter = ArrayAdapter.createFromResource(this, R.array.poison_amount_options, android.R.layout.simple_spinner_item);
        poisonAmountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoisonAmount.setAdapter(poisonAmountAdapter);

        ArrayAdapter<CharSequence> replaceAmountAdapter = ArrayAdapter.createFromResource(this, R.array.replace_amount_options, android.R.layout.simple_spinner_item);
        replaceAmountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReplaceAmount.setAdapter(replaceAmountAdapter);

        ArrayAdapter<CharSequence> consumptionPercentageAdapter = ArrayAdapter.createFromResource(this, R.array.percentage_values, android.R.layout.simple_spinner_item);
        consumptionPercentageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConsumptionPercentage.setAdapter(consumptionPercentageAdapter);
    }

    private void setupRadioGroups() {
        radioGroupConsumption.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonYesConsumption) {
                findViewById(R.id.textViewConsumptionPercentage).setVisibility(View.VISIBLE);
                spinnerConsumptionPercentage.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.textViewConsumptionPercentage).setVisibility(View.GONE);
                spinnerConsumptionPercentage.setVisibility(View.GONE);
            }
        });

        radioGroupReplace.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonYesReplace) {
                spinnerReplaceAmount.setVisibility(View.VISIBLE);
                spinnerReplacePoisonType.setVisibility(View.VISIBLE);
            } else {
                spinnerReplaceAmount.setVisibility(View.GONE);
                spinnerReplacePoisonType.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        historyEntries = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyEntries);
        recyclerViewHistory.setAdapter(historyAdapter);
    }

    private void loadHistoryEntries() {
        trapRef.child("history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyEntries.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HistoryEntry entry = dataSnapshot.getValue(HistoryEntry.class);
                    historyEntries.add(entry);
                }
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FormActivity.this, "Error al cargar el historial", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("Se guardará en el historial, esto será irreversible. ¿Desea continuar?")
                .setPositiveButton("Sí", (dialog, which) -> saveTrapData())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupCheckBox() {
        checkBoxNoAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setFormEnabled(!isChecked);
        });
    }

    private void setFormEnabled(boolean enabled) {
        spinnerTrapType.setEnabled(enabled);
        spinnerPoisonType.setEnabled(enabled);
        spinnerPoisonAmount.setEnabled(enabled);
        radioGroupConsumption.setEnabled(enabled);
        radioGroupReplace.setEnabled(enabled);
        if (enabled) {
            // Habilitar o deshabilitar los elementos dentro de los RadioGroups
            radioGroupConsumption.getChildAt(0).setEnabled(true); // Yes
            radioGroupConsumption.getChildAt(1).setEnabled(true); // No
            radioGroupReplace.getChildAt(0).setEnabled(true); // Yes
            radioGroupReplace.getChildAt(1).setEnabled(true); // No
        } else {
            radioGroupConsumption.check(R.id.radioButtonNoConsumption); // Desmarcar y seleccionar "No"
            radioGroupReplace.check(R.id.radioButtonNoReplace); // Desmarcar y seleccionar "No"
        }
        spinnerConsumptionPercentage.setEnabled(enabled);
        spinnerReplaceAmount.setEnabled(enabled);
        spinnerReplacePoisonType.setEnabled(enabled);
    }


    ///BUG PARA QUE VUELVA A TRAP STATUS ACTIVITY

    private void saveTrapData() {
        boolean isNoAccessChecked = checkBoxNoAccess.isChecked();
        Log.d("FormActivity", "Checkbox No Access marcado: " + isNoAccessChecked);

        if (isNoAccessChecked) {
            // Si 'no access' está marcado, buscar la trampa anterior en el historial
            trapRef.child("history").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Obtener la fecha de la trampa anterior y guardarla como 'previousDate'
                        for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                            String previousDate = trapSnapshot.child("date").getValue(String.class);
                            saveNoAccessTrapData(previousDate);  // Guardar los datos con la fecha de la trampa anterior
                        }
                    } else {
                        // No hay trampas anteriores, no se guarda 'previousDate'
                        saveNoAccessTrapData(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FormActivity.this, "Error al obtener la trampa anterior.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Si 'no access' no está marcado, guardar sin 'previousDate'
            saveNormalTrapData();
        }
    }


    private void saveNoAccessTrapData(String previousDate) {
        // Guardar la entrada con los datos de no access, incluyendo 'previousDate'
        HistoryEntry entry = new HistoryEntry(
                currentDate,
                null,
                null,
                0,
                false,
                0,
                false,
                0,
                null,
                true,  // Aquí se marca 'noAccess' como true
                previousDate
        );

        // Guardar la entrada en Firebase
        trapRef.child("history").push().setValue(entry).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FormActivity.this, "Datos de la trampa guardados correctamente.", Toast.LENGTH_SHORT).show();
                redirectToTrapStatus();
            } else {
                Toast.makeText(FormActivity.this, "Error al guardar los datos de la trampa.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void saveNormalTrapData() {
        // Guardar la entrada normal, si no está marcado 'no access'
        String trapType = spinnerTrapType.getSelectedItem().toString();
        String poisonType = spinnerPoisonType.getSelectedItem().toString();
        int poisonAmount = Integer.parseInt(spinnerPoisonAmount.getSelectedItem().toString());
        boolean consumption = radioGroupConsumption.getCheckedRadioButtonId() == R.id.radioButtonYesConsumption;
        int consumptionPercentage = consumption ? Integer.parseInt(spinnerConsumptionPercentage.getSelectedItem().toString()) : 0;
        boolean replace = radioGroupReplace.getCheckedRadioButtonId() == R.id.radioButtonYesReplace;
        int replaceAmount = replace ? Integer.parseInt(spinnerReplaceAmount.getSelectedItem().toString()) : 0;
        String replacePoisonType = replace ? spinnerReplacePoisonType.getSelectedItem().toString() : "";

        // Crear un nuevo objeto HistoryEntry con todos los datos llenos
        HistoryEntry entry = new HistoryEntry(
                currentDate, trapType, poisonType, poisonAmount,
                consumption, consumptionPercentage, replace, replaceAmount,
                replacePoisonType, false, null  // No hay 'previousDate' cuando 'no access' no está marcado
        );

        // Guardar la entrada en Firebase
        trapRef.child("history").push().setValue(entry).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FormActivity.this, "Datos de la trampa guardados correctamente.", Toast.LENGTH_SHORT).show();
                redirectToTrapStatus();
            } else {
                Toast.makeText(FormActivity.this, "Error al guardar los datos de la trampa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToTrapStatus() {
        Intent intent = new Intent(FormActivity.this, TrapStatusActivity.class);
        intent.putExtra("enterpriseCode", enterpriseCode);
        intent.putExtra("date", getIntent().getStringExtra("date"));
        startActivity(intent);
        finish();  // Cerrar la actividad actual
    }

}
