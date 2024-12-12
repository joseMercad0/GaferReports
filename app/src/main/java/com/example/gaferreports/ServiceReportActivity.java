package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceReportActivity extends AppCompatActivity {

    private static final int REQUEST_VISIT_RESULTS = 1;

    private TextView textViewEnterpriseName, textViewLocation, textViewDate, textViewStartTime;
    private EditText editTextArea;
    private TimePicker timePickerEnd;
    private Button buttonSaveEndTime, buttonDevelopedActivities, buttonGeneratePDF, buttonVisitResults;
    private String enterpriseCode;
    private DatabaseReference databaseRef;
    private String activity, estaciones, cebosConsumidos, cebosRepuestas ;
    private ArrayList<String> selectedCebos, selectedTrampas, selectedRoedores;
    private String visitResults, selectedTechnician;
    private String endTime;
    private String clientName;
    private String observations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_report);

        // Inicializar vistas
        textViewEnterpriseName = findViewById(R.id.textViewEnterpriseName);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewDate = findViewById(R.id.textViewDate);
        textViewStartTime = findViewById(R.id.textViewStartTime);
        editTextArea = findViewById(R.id.editTextArea);
        timePickerEnd = findViewById(R.id.timePickerEnd);
        buttonSaveEndTime = findViewById(R.id.buttonSaveEndTime);
        buttonDevelopedActivities = findViewById(R.id.buttonDevelopedActivities);
        buttonGeneratePDF = findViewById(R.id.buttonGeneratePDF);
        buttonVisitResults = findViewById(R.id.buttonVisitResults);

        // Obtener enterpriseCode del Intent
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");

        // Inicializar referencia a la base de datos de Firebase
        if (enterpriseCode != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode);
        } else {
            Toast.makeText(this, "Error: No se recibió el código de la empresa.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener datos pasados desde DevelopedActivitiesActivity
        Intent intent = getIntent();
        activity = intent.getStringExtra("activity");
        estaciones = intent.getStringExtra("estaciones");
        cebosConsumidos = intent.getStringExtra("cebosConsumidos");
        cebosRepuestas = intent.getStringExtra("cebosRepuestas");
        selectedCebos = intent.getStringArrayListExtra("selectedCebos");
        selectedTrampas = intent.getStringArrayListExtra("selectedTrampas");
        selectedRoedores = intent.getStringArrayListExtra("selectedRoedores");

        // Cargar datos de la empresa desde Firebase
        loadCompanyData();

        // Configurar botón para guardar la hora de término
        buttonSaveEndTime.setOnClickListener(v -> saveEndTime());

        // Configurar botón para redirigir a la actividad de actividades desarrolladas
        buttonDevelopedActivities.setOnClickListener(v -> {
            Intent intentToDevelopedActivities = new Intent(ServiceReportActivity.this, DevelopedActivitiesActivity.class);
            intentToDevelopedActivities.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intentToDevelopedActivities);
        });

        buttonVisitResults.setOnClickListener(v -> {
            if (enterpriseCode != null) {
                Intent intentToVisitResults = new Intent(ServiceReportActivity.this, VisitResultsActivity.class);
                intentToVisitResults.putExtra("enterpriseCode", enterpriseCode); // Pasar el enterpriseCode
                startActivityForResult(intentToVisitResults, REQUEST_VISIT_RESULTS);
            } else {
                Toast.makeText(ServiceReportActivity.this, "Error: No se recibió el código de la empresa.", Toast.LENGTH_SHORT).show();
            }
        });



        // Configurar botón para generar PDF
        buttonGeneratePDF.setOnClickListener(v -> {
            try {
                generatePDF();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ServiceReportActivity.this, "Error al generar el PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VISIT_RESULTS && resultCode == RESULT_OK) {
            ArrayList<String> visitResultsList = data.getStringArrayListExtra("visitResults");
            visitResults = formatVisitResults(visitResultsList);
            selectedTechnician = data.getStringExtra("selectedTechnician");
            clientName = data.getStringExtra("clientName");
            observations = data.getStringExtra("observations");

        }
    }

    private String formatVisitResults(List<String> visitResultsList) {
        StringBuilder formattedResults = new StringBuilder();
        for (String result : visitResultsList) {
            formattedResults.append("• ").append(formatTextWithLineBreaks(result)).append("\n\n");
        }
        return formattedResults.toString();
    }

    private String formatTextWithLineBreaks(String text) {
        StringBuilder formattedText = new StringBuilder();
        int lineLength = 65; // Longitud máxima por línea, ajusta este valor según el ancho de tu PDF
        String[] words = text.split(" ");
        int currentLineLength = 0;

        for (String word : words) {
            if (currentLineLength + word.length() + 1 > lineLength) {
                formattedText.append("\n"); // Salto de línea
                currentLineLength = 0;
            }
            formattedText.append(word).append(" ");
            currentLineLength += word.length() + 1;
        }
        return formattedText.toString().trim();
    }

    private void loadCompanyData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String enterpriseName = snapshot.child("nombre").getValue(String.class);
                    String location = snapshot.child("ubicacion").getValue(String.class);
                    String date = snapshot.child("fecha").getValue(String.class);
                    String startTime = snapshot.child("horaIngreso").getValue(String.class);
                    endTime = snapshot.child("horaSalida").getValue(String.class);
                    String area = snapshot.child("area").getValue(String.class);  // Obtener el área

                    textViewEnterpriseName.setText(enterpriseName);
                    textViewLocation.setText(location);
                    textViewDate.setText(date);
                    textViewStartTime.setText(startTime);
                    if (area != null) {
                        editTextArea.setText(area);  // Mostrar el área guardada
                    }
                } else {
                    Toast.makeText(ServiceReportActivity.this, "No se encontraron datos de la empresa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServiceReportActivity.this, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEndTime() {
        int endHour = timePickerEnd.getCurrentHour();
        int endMinute = timePickerEnd.getCurrentMinute();
        String formattedEndTime = String.format("%02d:%02d", endHour, endMinute);

        // Obtener el área del EditText
        String area = editTextArea.getText().toString();

        Map<String, Object> serviceReportData = new HashMap<>();
        serviceReportData.put("horaSalida", formattedEndTime);
        serviceReportData.put("area", area); // Agregar el área

        // Actualizar Firebase con los datos de "horaSalida"
        databaseRef.updateChildren(serviceReportData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Modificar el PDF después de guardar en Firebase
                try {
                    modifyPDFWithEndTime(formattedEndTime);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ServiceReportActivity.this, "Error al modificar el PDF.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(ServiceReportActivity.this, "Hora de salida guardada y archivo modificado correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Desconocido";
                Toast.makeText(ServiceReportActivity.this, "Error al guardar la hora de salida: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void modifyPDFWithEndTime(String endTime) throws IOException {
        String enterpriseName = textViewEnterpriseName.getText().toString();
        if (enterpriseName.isEmpty()) {
            Toast.makeText(this, "El nombre de la empresa no está disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ruta del PDF existente
        File existingPDF = new File(getExternalFilesDir(null), "/Estacion_" + enterpriseName + ".pdf");

        if (!existingPDF.exists()) {
            Toast.makeText(this, "El archivo PDF no existe.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ruta del nuevo PDF modificado
        File modifiedPDF = new File(getExternalFilesDir(null), "/Estacion_Modificada_" + enterpriseName + ".pdf");

        // Leer el PDF existente y crear uno nuevo
        PdfReader reader = new PdfReader(existingPDF.getAbsolutePath());
        PdfWriter writer = new PdfWriter(modifiedPDF.getAbsolutePath());
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Modificar el primer (o un específico) página del PDF
        com.itextpdf.kernel.pdf.canvas.PdfCanvas pdfCanvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(pdfDoc.getFirstPage());
        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);

        // Posición donde se escribirá la hora de salida (ajustar según el diseño del PDF)
        pdfCanvas.beginText();
        pdfCanvas.moveText(500, 663); // Ajusta las coordenadas según sea necesario
        pdfCanvas.showText(endTime);
        pdfCanvas.endText();

        document.close();

        // Reemplazar el archivo original con el modificado
        if (existingPDF.delete()) {
            modifiedPDF.renameTo(existingPDF);
            Toast.makeText(this, "El archivo 'Estacion' ha sido modificado.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al reemplazar el archivo PDF.", Toast.LENGTH_SHORT).show();
        }
    }


    private void generatePDF() throws IOException {
        String enterpriseName = textViewEnterpriseName.getText().toString();
        String location = textViewLocation.getText().toString();
        String date = textViewDate.getText().toString();

        String startTime = textViewStartTime.getText().toString();
        String endTimeToUse = endTime != null ? endTime : "00:00";

        String area = editTextArea.getText().toString();
        String estaciones = getIntent().getStringExtra("estaciones");
        String cebosConsumidos = getIntent().getStringExtra("cebosConsumidos");
        String cebosRepuestas = getIntent().getStringExtra("cebosRepuestas");
        int trampasActivadas = getIntent().getIntExtra("trampasActivadas", 0);

        File enterpriseDir = new File(getExternalFilesDir(null), enterpriseName);
        File dateDir = new File(enterpriseDir, date);

        File pdfFile = new File(getExternalFilesDir(null), "Reporte_" + enterpriseName + ".pdf");

        PdfReader reader = new PdfReader(getAssets().open("EJECUCION DE SERVICIOS.pdf"));
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Escribir datos en el PDF
        com.itextpdf.kernel.pdf.canvas.PdfCanvas pdfCanvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(pdfDoc.getFirstPage());
        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);

        // Cliente
        pdfCanvas.beginText();
        pdfCanvas.moveText(275, 680);
        pdfCanvas.showText(enterpriseName);
        pdfCanvas.endText();

        // Dirección
        pdfCanvas.beginText();
        pdfCanvas.moveText(275, 654);
        pdfCanvas.showText(location);
        pdfCanvas.endText();

        // Fecha
        pdfCanvas.beginText();
        pdfCanvas.moveText(275, 635);
        pdfCanvas.showText(date);
        pdfCanvas.endText();

        // Hora de inicio
        pdfCanvas.beginText();
        pdfCanvas.moveText(275, 613);
        pdfCanvas.showText(startTime);
        pdfCanvas.endText();


        pdfCanvas.beginText();
        pdfCanvas.moveText(440, 635);
        pdfCanvas.showText(area);
        pdfCanvas.endText();

        // Hora de término
        pdfCanvas.beginText();
        pdfCanvas.moveText(440, 613);
        pdfCanvas.showText(endTimeToUse);
        pdfCanvas.endText();

        // Número de estaciones
        pdfCanvas.beginText();
        pdfCanvas.moveText(110, 480);
        pdfCanvas.showText(estaciones);
        pdfCanvas.endText();

        // Cebos consumidos
        pdfCanvas.beginText();
        pdfCanvas.moveText(110, 310);
        pdfCanvas.showText(cebosConsumidos);
        pdfCanvas.endText();

        // Cebos repuestos
        pdfCanvas.beginText();
        pdfCanvas.moveText(110, 265);
        pdfCanvas.showText(cebosRepuestas);
        pdfCanvas.endText();

        // Trampas activadas
        pdfCanvas.beginText();
        pdfCanvas.moveText(110, 235);
        pdfCanvas.showText(String.valueOf(trampasActivadas));
        pdfCanvas.endText();

        // Agregar resultados de la visita, observaciones, recomendaciones y firma al PDF
        if (visitResults != null) {
            // Dividir los resultados en líneas o ideas individuales si están en una sola cadena
            String[] resultsArray = visitResults.split("\n"); // Suponiendo que las ideas estén separadas por saltos de línea

            // Crear un StringBuilder para construir el texto formateado
            StringBuilder formattedResults = new StringBuilder();

            for (String result : resultsArray) {
                formattedResults.append("").append(result.trim()).append("\n"); // Añadir viñeta y texto
            }

            // Convertir el StringBuilder a una cadena final
            String resultsWithBullets = formattedResults.toString();

            // Dibujar el texto con viñetas en el PDF
            pdfCanvas.beginText();
            pdfCanvas.moveText(230, 580); // Ajustar la posición según el layout

            // Añadir lógica para manejar el salto de línea si el texto es largo
            String[] lines = resultsWithBullets.split("\n");
            for (String line : lines) {
                pdfCanvas.showText(line); // Mostrar cada línea con viñeta
                pdfCanvas.moveText(0, -15); // Mover hacia abajo para la siguiente línea (ajusta este valor según sea necesario)
            }

            pdfCanvas.endText();
        }


        pdfCanvas.beginText();
        pdfCanvas.moveText(280, 120); // Ajustar posición según el layout
        pdfCanvas.showText(selectedTechnician);
        pdfCanvas.endText();

        // Agregar nombre del cliente
        pdfCanvas.beginText();
        pdfCanvas.moveText(450, 120); // Ajusta la posición según el layout
        pdfCanvas.showText(clientName);
        pdfCanvas.endText();

        Paragraph observationsParagraph = new Paragraph(observations)
                .setTextAlignment(TextAlignment.LEFT)  // Alineación del texto
                .setFontSize(10)  // Tamaño de la fuente
                .setFixedPosition(220, 210, 300);  // Establece la posición y el ancho máximo del texto

        // Añadir el Paragraph al documento
        document.add(observationsParagraph);

        // Plasmar actividades desarrolladas
        markSelectedActivity(pdfCanvas, activity);

        // Cebo
        markSelectedOptions(pdfCanvas, selectedCebos, 110, 456, R.array.cebos_array);

        // Trampa
        markSelectedOptions(pdfCanvas, selectedTrampas, 110, 214, R.array.trampas_array);

        // Roedor
        markSelectedOptions(pdfCanvas, selectedRoedores, 110, 118, R.array.roedor_array);

        document.close();
        Toast.makeText(this, "PDF generado: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        // Redirigir a MenuActivity después de generar el PDF
        Intent intent = new Intent(ServiceReportActivity.this, MenuActivity.class);
        intent.putExtra("enterpriseCode", enterpriseCode);
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }


    private void markSelectedActivity(com.itextpdf.kernel.pdf.canvas.PdfCanvas pdfCanvas, String selectedActivity) {
        int yCoordinate = 0;
        switch (selectedActivity) {
            case "Evaluación":
                yCoordinate = 590;
                break;
            case "Instalación":
                yCoordinate = 565;
                break;
            case "Monitoreo":
                yCoordinate = 540;
                break;
            case "Emergencia":
                yCoordinate = 515;
                break;
        }
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, yCoordinate);  // Ajustar las coordenadas según sea necesario
        pdfCanvas.showText("X");
        pdfCanvas.endText();
    }

    private void markSelectedOptions(com.itextpdf.kernel.pdf.canvas.PdfCanvas pdfCanvas, ArrayList<String> selectedOptions, int startX, int startY, int arrayResourceId) {
        String[] optionsArray = getResources().getStringArray(arrayResourceId);
        if (selectedOptions.contains("Ninguno")) {
            return; // No marcar ninguna opción si "Ninguno" está seleccionada
        }
        for (int i = 0; i < optionsArray.length; i++) {
            if (selectedOptions.contains(optionsArray[i])) {
                pdfCanvas.beginText();
                pdfCanvas.moveText(startX, startY - (i * 23));  // Ajustar las coordenadas según sea necesario
                pdfCanvas.showText("X");
                pdfCanvas.endText();
            }
        }
    }
}
