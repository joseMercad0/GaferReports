package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Spinner selectEnterprise;
    private Button buttonGoToMenu;
    private String selectedEnterprise, selectedEnterpriseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectEnterprise = findViewById(R.id.selectEnterprise);
        buttonGoToMenu = findViewById(R.id.buttonGoToMenu);

        // Configurar el selector de empresas
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.enterprise, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectEnterprise.setAdapter(adapter);

        // Acción al seleccionar una empresa
        selectEnterprise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEnterprise = parent.getItemAtPosition(position).toString();
                String[] enterpriseCodes = getResources().getStringArray(R.array.enterprise_codes);
                selectedEnterpriseCode = enterpriseCodes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Acción cuando no se selecciona ninguna empresa
            }
        });

        // Acción al hacer clic en el botón para ir al menú
        buttonGoToMenu.setOnClickListener(v -> {
            if (selectedEnterprise != null && !selectedEnterprise.isEmpty()) {
                // Redirigir a LoginActivity y pasar el nombre de la empresa seleccionada y su código
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("enterpriseName", selectedEnterprise);
                intent.putExtra("enterpriseCode", selectedEnterpriseCode);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Por favor selecciona una empresa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Dejar este método vacío o mostrar un mensaje si es necesario.
        // No llamar a super.onBackPressed() para deshabilitar el comportamiento predeterminado.
    }

}
