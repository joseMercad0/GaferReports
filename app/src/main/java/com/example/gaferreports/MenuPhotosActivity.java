package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuPhotosActivity extends AppCompatActivity {

    private String enterpriseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_photos);

        // Obtener el código de la empresa
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");

        // Botón "Fotos Reporte" para abrir TestPhotosActivity
        Button buttonFotosReporte = findViewById(R.id.buttonFotosReporte);
        buttonFotosReporte.setOnClickListener(v -> {
            Intent intent = new Intent(MenuPhotosActivity.this, TestPhotosActivity.class);
            intent.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intent);
        });

        // Botón "Fotos Informe" (se implementará más tarde)
        Button buttonFotosInforme = findViewById(R.id.buttonFotosInforme);
        buttonFotosInforme.setOnClickListener(v -> {
            Intent intent = new Intent(MenuPhotosActivity.this, PhotosReportActivity.class); // Placeholder para la nueva actividad
            intent.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intent);
        });
    }
}
