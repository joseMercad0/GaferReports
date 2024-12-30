package com.example.gaferreports;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private Button buttonTrapStatus, buttonServiceReport, buttonTestPhotos, buttonHeatMap,buttonCalculatePests, buttonGeneratePDF;
    private String selectedEnterprise, enterpriseCode;
    private TextView welcomeTextView, rucTextView, ubicacionTextView;
    private SharedPreferences sharedPreferences;
    private DatabaseReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Inicializar vistas
        welcomeTextView = findViewById(R.id.welcomeTextView);
        rucTextView = findViewById(R.id.rucTextView);
        ubicacionTextView = findViewById(R.id.ubicacionTextView);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("EnterpriseData", MODE_PRIVATE);

        // Obtener los datos de la empresa del Intent o de SharedPreferences
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra("enterpriseName")) {
            selectedEnterprise = receivedIntent.getStringExtra("enterpriseName");
            String enterpriseRUC = receivedIntent.getStringExtra("enterpriseRUC");
            String enterpriseLocation = receivedIntent.getStringExtra("enterpriseLocation");
            enterpriseCode = receivedIntent.getStringExtra("enterpriseCode");

            // Guardar datos en SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("enterpriseName", selectedEnterprise);
            editor.putString("enterpriseRUC", enterpriseRUC);
            editor.putString("enterpriseLocation", enterpriseLocation);
            editor.putString("enterpriseCode", enterpriseCode);
            editor.apply();
        } else {
            // Recuperar datos de SharedPreferences
            selectedEnterprise = sharedPreferences.getString("enterpriseName", null);
            String enterpriseRUC = sharedPreferences.getString("enterpriseRUC", null);
            String enterpriseLocation = sharedPreferences.getString("enterpriseLocation", null);
            enterpriseCode = sharedPreferences.getString("enterpriseCode", null);

            if (selectedEnterprise == null) {
                Toast.makeText(this, "Error: No se recibieron datos de la empresa", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Mostrar los datos de la empresa
        welcomeTextView.setText("Bienvenido a " + selectedEnterprise);
        rucTextView.setText("RUC: " + sharedPreferences.getString("enterpriseRUC", ""));
        ubicacionTextView.setText("Ubicación: " + sharedPreferences.getString("enterpriseLocation", ""));

        // Configurar los botones del menú
        buttonTrapStatus = findViewById(R.id.buttonTrapStatus);
        buttonTrapStatus.setOnClickListener(v -> {
            Intent intentToTrapStatus = new Intent(MenuActivity.this, CompanyInfoFormActivity.class);
            intentToTrapStatus.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intentToTrapStatus);
        });



        buttonServiceReport = findViewById(R.id.buttonServiceReport);
        buttonServiceReport.setOnClickListener(v -> {
            Intent intentToServiceReport = new Intent(MenuActivity.this, ServiceReportActivity.class);
            intentToServiceReport.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intentToServiceReport);
        });

        buttonCalculatePests = findViewById(R.id.btnPestCalculation);
        buttonCalculatePests.setOnClickListener(v -> {
            String enterpriseCode = getIntent().getStringExtra("enterpriseCode");  // Obtener el código de empresa
            Intent intent = new Intent(MenuActivity.this, PestCalculationActivity.class);
            intent.putExtra("enterpriseCode", enterpriseCode);  // Pasar el código de empresa a PestCalculationActivity
            startActivity(intent);
        });

        buttonTestPhotos = findViewById(R.id.buttonTestPhotos);
        buttonTestPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, TestPhotosActivity.class);
            intent.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intent);
        });

        buttonGeneratePDF = findViewById(R.id.buttonGeneratePDF);
        buttonGeneratePDF.setOnClickListener(v -> {
            fetchEnterpriseNameAndGeneratePDF();
        });
    }

    // Función para obtener el nombre de la empresa desde Firebase
    private void fetchEnterpriseNameAndGeneratePDF() {
        if (enterpriseCode != null) {
            ref = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode).child("nombre");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String enterpriseName = dataSnapshot.getValue(String.class);
                    if (enterpriseName != null) {
                        mergePDFs(enterpriseName);  // Llamar a la función para unir los PDFs
                    } else {
                        Toast.makeText(MenuActivity.this, "No se encontró el nombre de la empresa", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MenuActivity.this, "Error al obtener el nombre de la empresa", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Función para unir los PDFs generados
    private void mergePDFs(String enterpriseName) {
        try {
            // Ruta donde se guardarán los PDFs combinados en la carpeta Downloads
            String combinedPDFPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/Reporte Final " + enterpriseName + ".pdf";

            // Crear el archivo final PDF
            PdfWriter writer = new PdfWriter(new FileOutputStream(combinedPDFPath));
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Lista de los archivos PDF que vamos a unir
            ArrayList<String> pdfFiles = new ArrayList<>();
            pdfFiles.add(getExternalFilesDir(null) + "/inicio_informe_" + enterpriseName + ".pdf");
            pdfFiles.add(getExternalFilesDir(null) + "/Estacion_" + enterpriseName + ".pdf");
            pdfFiles.add(getExternalFilesDir(null) + "/Reporte_" + enterpriseName + ".pdf");
            pdfFiles.add(getExternalFilesDir(null) + "/Calculo_" + enterpriseName + ".pdf");
            pdfFiles.add(getExternalFilesDir(null) + "/ESPACIO_FOTOS_" + enterpriseName + ".pdf"); 

            for (String pdfFilePath : pdfFiles) {
                // Leer cada PDF y añadirlo al PDF final
                PdfDocument sourcePDF = new PdfDocument(new PdfReader(pdfFilePath));
                sourcePDF.copyPagesTo(1, sourcePDF.getNumberOfPages(), pdfDoc);
                sourcePDF.close();
            }

            // Cerrar el PDF final
            pdfDoc.close();

            // Mostrar mensaje de éxito
            Toast.makeText(this, "PDF combinado guardado en la carpeta Downloads", Toast.LENGTH_LONG).show();

            // Abrir el archivo PDF
            openGeneratedPDF(combinedPDFPath);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al combinar los PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    // Función para abrir el PDF generado
    private void openGeneratedPDF(String filePath) {
        File file = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Dejar este método vacío o mostrar un mensaje si es necesario.
        // No llamar a super.onBackPressed() para deshabilitar el comportamiento predeterminado.
    }
}
