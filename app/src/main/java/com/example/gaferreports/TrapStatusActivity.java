package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
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
import java.util.List;

public class TrapStatusActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private Button buttonSaveCurrentStation;
    private String enterpriseCode;
    private String currentDate;
    private DatabaseReference empresaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trap_status);

        gridLayout = findViewById(R.id.gridLayout);
        buttonSaveCurrentStation = findViewById(R.id.buttonSaveCurrentStation);

        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        currentDate = getIntent().getStringExtra("date");

        empresaRef = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode);

        loadTrapButtons();

        buttonSaveCurrentStation.setOnClickListener(v -> showConfirmationDialog());
    }

    private void loadTrapButtons() {
        empresaRef.child("cantidad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int trapCount = snapshot.getValue(Integer.class);
                    for (int i = 1; i <= trapCount; i++) {
                        addButtonForTrap(i);
                    }
                } else {
                    Toast.makeText(TrapStatusActivity.this, "No se encontró la cantidad de trampas para esta empresa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrapStatusActivity.this, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addButtonForTrap(int trapNumber) {
        Button button = new Button(this);
        button.setText("Trampa " + trapNumber);
        button.setOnClickListener(v -> openTrapForm(trapNumber));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);

        gridLayout.addView(button);

        checkTrapStatus(trapNumber, button);
    }

    private void checkTrapStatus(int trapNumber, Button button) {
        DatabaseReference trapRef = empresaRef.child("trampas").child(String.valueOf(trapNumber));
        trapRef.child("history").orderByChild("date").equalTo(currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                        HistoryEntry entry = entrySnapshot.getValue(HistoryEntry.class);
                        if (entry != null) {
                            if (entry.isNoAccess()) {
                                // Cambiar el estilo para trampas inaccesibles
                                button.setBackgroundResource(R.drawable.button_background_inaccessible);
                                button.setText("✗ Trampa " + trapNumber);
                            } else if (entry.isNoChanges()) {
                                // Cambiar el estilo para trampas sin cambios
                                button.setBackgroundResource(R.drawable.button_background_no_changes);
                                button.setText("⟳ Trampa " + trapNumber); // Símbolo para 'Sin Cambios'
                            } else {
                                // Cambiar el estilo para trampas registradas normalmente
                                button.setBackgroundResource(R.drawable.button_background_registered);
                                button.setText("✓ Trampa " + trapNumber);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrapStatusActivity.this, "Error al verificar la trampa", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void openTrapForm(int trapNumber) {
        Intent intent = new Intent(TrapStatusActivity.this, FormActivity.class);
        intent.putExtra("enterpriseCode", enterpriseCode);
        intent.putExtra("trapNumber", trapNumber);
        intent.putExtra("date", currentDate);
        startActivity(intent);
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("Se guardará la estación actual, esto será irreversible.\n\n" +
                        "Contacte con el programador si hubo un error.")
                .setPositiveButton("Sí", (dialog, which) -> saveCurrentStation())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveCurrentStation() {
        empresaRef.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean allTrapsUpdated = true;
                List<TrapEntry> traps = new ArrayList<>();

                for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                    String trapKey = trapSnapshot.getKey();
                    DataSnapshot historySnapshot = trapSnapshot.child("history");

                    HistoryEntry lastEntry = null;
                    for (DataSnapshot entrySnapshot : historySnapshot.getChildren()) {
                        HistoryEntry entry = entrySnapshot.getValue(HistoryEntry.class);
                        if (entry != null && currentDate != null && currentDate.equals(entry.getDate())) {
                            lastEntry = entry;
                            break;
                        }
                    }

                    if (lastEntry != null) {
                        if (lastEntry.isNoAccess()) {
                            // Manejar trampas inaccesibles
                            traps.add(new TrapEntry(
                                    "Inaccesible",
                                    "",
                                    0,
                                    false,
                                    0,
                                    false,
                                    0,
                                    "",
                                    true
                            ));
                        } else if (lastEntry.isNoChanges()) {
                            // Manejar trampas sin cambios
                            traps.add(new TrapEntry(
                                    lastEntry.getTrapType(),
                                    lastEntry.getPoisonType(),
                                    lastEntry.getPoisonAmount(),
                                    lastEntry.isConsumption(),
                                    lastEntry.getConsumptionPercentage(),
                                    lastEntry.isReplace(),
                                    lastEntry.getReplaceAmount(),
                                    lastEntry.getReplacePoisonType(),
                                    false
                            ));
                        } else {
                            // Manejar trampas registradas normalmente
                            traps.add(new TrapEntry(
                                    lastEntry.getTrapType(),
                                    lastEntry.getPoisonType(),
                                    lastEntry.getPoisonAmount(),
                                    lastEntry.isConsumption(),
                                    lastEntry.getConsumptionPercentage(),
                                    lastEntry.isReplace(),
                                    lastEntry.getReplaceAmount(),
                                    lastEntry.getReplacePoisonType(),
                                    false
                            ));
                        }
                    } else {
                        allTrapsUpdated = false;
                        break;
                    }
                }

                if (allTrapsUpdated) {
                    Intent intent = new Intent(TrapStatusActivity.this, CompanyInfoFormActivity.class);
                    intent.putExtra("enterpriseCode", enterpriseCode);
                    intent.putParcelableArrayListExtra("traps", (ArrayList<TrapEntry>) traps);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(TrapStatusActivity.this, "Algunas trampas no han sido actualizadas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrapStatusActivity.this, "Error al obtener los datos de las trampas", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
