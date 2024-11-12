package com.example.gaferreports;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DevelopedActivitiesActivity extends AppCompatActivity {

    private Spinner spinnerActivities, spinnerTrampasActivadas;
    private TextView textViewCebos, textViewTrampas, textViewRoedor;
    private EditText editTextEstaciones, editTextCebosConsumidos, editTextCebosRepuestas;
    private Button buttonSave;
    private String enterpriseCode;

    private boolean[] selectedCebos, selectedTrampas, selectedRoedores;
    private ArrayList<Integer> cebosList = new ArrayList<>();
    private ArrayList<Integer> trampasList = new ArrayList<>();
    private ArrayList<Integer> roedoresList = new ArrayList<>();

    private String[] cebosArray, trampasArray, roedoresArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developed_activities);

        // Inicializar vistas
        spinnerActivities = findViewById(R.id.spinnerActivities);
        textViewCebos = findViewById(R.id.textViewCebos);
        textViewTrampas = findViewById(R.id.textViewTrampas);
        textViewRoedor = findViewById(R.id.textViewRoedor);
        editTextEstaciones = findViewById(R.id.editTextEstaciones);
        editTextCebosConsumidos = findViewById(R.id.editTextCebosConsumidos);
        editTextCebosRepuestas = findViewById(R.id.editTextCebosRepuestas);
        spinnerTrampasActivadas = findViewById(R.id.spinnerTrampasActivadas);
        buttonSave = findViewById(R.id.buttonSave);

        // Configurar Spinner de trampas activadas
        ArrayAdapter<Integer> trampasActivadasAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getNumberList(1, 10));
        trampasActivadasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrampasActivadas.setAdapter(trampasActivadasAdapter);

        // Obtener enterpriseCode del Intent
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");


        // Inicializar las selecciones booleanas
        cebosArray = getResources().getStringArray(R.array.cebos_array);
        trampasArray = getResources().getStringArray(R.array.trampas_array);
        roedoresArray = getResources().getStringArray(R.array.roedor_array);

        selectedCebos = new boolean[cebosArray.length];
        selectedTrampas = new boolean[trampasArray.length];
        selectedRoedores = new boolean[roedoresArray.length];

        // Configurar Spinner para actividades
        ArrayAdapter<CharSequence> activitiesAdapter = ArrayAdapter.createFromResource(this, R.array.activities_array, android.R.layout.simple_spinner_item);
        activitiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivities.setAdapter(activitiesAdapter);

        // Configurar TextViews para mostrar diálogos de selección múltiple
        textViewCebos.setOnClickListener(v -> showMultiSelectDialog("Seleccionar Cebos", cebosArray, selectedCebos, cebosList));
        textViewTrampas.setOnClickListener(v -> showMultiSelectDialog("Seleccionar Trampas", trampasArray, selectedTrampas, trampasList));
        textViewRoedor.setOnClickListener(v -> showMultiSelectDialog("Seleccionar Tipo de Roedor", roedoresArray, selectedRoedores, roedoresList));

        // Cargar datos desde Firebase
        loadEnterpriseData();

        // Configurar botón para guardar los datos
        buttonSave.setOnClickListener(v -> saveDataAndRedirect());
    }

    private ArrayList<Integer> getNumberList(int min, int max) {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    private void showMultiSelectDialog(String title, String[] items, boolean[] selectedItems, ArrayList<Integer> selectedList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, selectedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (items[which].equals("Ninguno")) {
                    for (int i = 0; i < selectedItems.length; i++) {
                        selectedItems[i] = false;
                        if (selectedList.contains(i)) {
                            selectedList.remove(Integer.valueOf(i));
                        }
                    }
                    selectedItems[which] = true;
                    selectedList.clear();
                    selectedList.add(which);
                } else {
                    selectedList.add(which);
                    if (selectedList.contains(items.length - 1)) {
                        selectedItems[items.length - 1] = false;
                        selectedList.remove(Integer.valueOf(items.length - 1));
                    }
                }
            } else {
                selectedList.remove(Integer.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Do something when OK is pressed
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Do something when Cancel is pressed
        });

        builder.show();
    }

    private void loadEnterpriseData() {
        DatabaseReference enterpriseRef = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode);

        // Cargar cantidad de trampas
        enterpriseRef.child("cantidad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int trapCount = snapshot.getValue(Integer.class);
                    editTextEstaciones.setText(String.valueOf(trapCount));
                } else {
                    editTextEstaciones.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DevelopedActivitiesActivity.this, "Error al cargar la cantidad de trampas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar la fecha de la empresa
        enterpriseRef.child("fecha").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String enterpriseDate = snapshot.getValue(String.class);

                if (enterpriseDate != null) {
                    // Cargar datos históricos de trampas
                    enterpriseRef.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int cebosConsumidosTotal = 0;
                            int cebosRepuestosTotal = 0;

                            for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot historySnapshot : trapSnapshot.child("history").getChildren()) {
                                    HistoryEntry entry = historySnapshot.getValue(HistoryEntry.class);
                                    if (entry != null) {
                                        // Verificar si la fecha del historial coincide con la fecha de la empresa
                                        if (enterpriseDate.equals(entry.getDate())) {
                                            int poisonAmount = entry.getPoisonAmount();
                                            int replaceAmount = entry.getReplaceAmount();

                                            // Sumar solo si los valores son mayores a 0
                                            if (poisonAmount > 0) {
                                                cebosConsumidosTotal += poisonAmount;
                                            }
                                            if (replaceAmount > 0) {
                                                cebosRepuestosTotal += replaceAmount;
                                            }
                                        }
                                    }
                                }
                            }

                            editTextCebosConsumidos.setText(String.valueOf(cebosConsumidosTotal));
                            editTextCebosRepuestas.setText(String.valueOf(cebosRepuestosTotal));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(DevelopedActivitiesActivity.this, "Error loading historical data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DevelopedActivitiesActivity.this, "Error loading enterprise date: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void saveDataAndRedirect() {
        String activity = spinnerActivities.getSelectedItem().toString();
        String estaciones = editTextEstaciones.getText().toString();
        String cebosConsumidos = editTextCebosConsumidos.getText().toString();
        String cebosRepuestas = editTextCebosRepuestas.getText().toString();
        int trampasActivadas = (int) spinnerTrampasActivadas.getSelectedItem();

        ArrayList<String> selectedCebosList = getSelectedItems(cebosArray, cebosList);
        ArrayList<String> selectedTrampasList = getSelectedItems(trampasArray, trampasList);
        ArrayList<String> selectedRoedoresList = getSelectedItems(roedoresArray, roedoresList);

        // Guardar los datos en Firebase
        DatabaseReference developedActivitiesRef = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode).child("developed_activities");
        developedActivitiesRef.child("activity").setValue(activity);
        developedActivitiesRef.child("estaciones").setValue(estaciones);
        developedActivitiesRef.child("cebos_consumidos").setValue(cebosConsumidos);
        developedActivitiesRef.child("cebos_repuestas").setValue(cebosRepuestas);
        developedActivitiesRef.child("trampas_activadas").setValue(trampasActivadas);
        developedActivitiesRef.child("selected_cebos").setValue(selectedCebosList);
        developedActivitiesRef.child("selected_trampas").setValue(selectedTrampasList);
        developedActivitiesRef.child("selected_roedores").setValue(selectedRoedoresList);


        // Redirigir a ServiceReportActivity con los datos seleccionados
        Intent intent = new Intent(DevelopedActivitiesActivity.this, ServiceReportActivity.class);
        intent.putExtra("enterpriseCode", enterpriseCode);
        intent.putExtra("activity", activity);
        intent.putExtra("estaciones", estaciones);
        intent.putExtra("cebosConsumidos", cebosConsumidos);
        intent.putExtra("cebosRepuestas", cebosRepuestas);
        intent.putExtra("trampasActivadas", trampasActivadas);
        intent.putStringArrayListExtra("selectedCebos", selectedCebosList);
        intent.putStringArrayListExtra("selectedTrampas", selectedTrampasList);
        intent.putStringArrayListExtra("selectedRoedores", selectedRoedoresList);


        startActivity(intent);
        finish();
    }

    private ArrayList<String> getSelectedItems(String[] itemsArray, ArrayList<Integer> selectedItemsList) {
        ArrayList<String> selectedItems = new ArrayList<>();
        for (int index : selectedItemsList) {
            selectedItems.add(itemsArray[index]);
        }
        return selectedItems;
    }
}
