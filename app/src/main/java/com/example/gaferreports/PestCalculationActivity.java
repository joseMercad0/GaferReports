package com.example.gaferreports;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PestCalculationActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView txtProgress;
    private Button btnGeneratePDF;
    private String enterpriseCode;
    private DatabaseReference ref;
    private String clientName, ubicacion, fecha, horaIngreso, horaSalida, selectedTechnician, enterpriseName, activity;
    private long trampasSinMovimiento, trampasConConsumo, trampasSinAcceso, cantidad;
    private EditText inputTipoCebo, inputPeso, inputCompuestoActivo, inputVencimiento, inputNroLote;
    private EditText inputTrapMechanic, inputGlueActivated, inputRodentsFound;
    private Button btnIncrementTrap, btnDecrementTrap, btnIncrementGlue, btnDecrementGlue, btnIncrementRodents, btnDecrementRodents;
    private long cantidadInstalada;  // Nueva variable
    private long cebosConsumidos;  // Nueva variable para la cantidad consumida
    private long cebosRepuestos;
    private Button btnGenerateInicioInforme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pest_calculation);

        // Inicializar las vistas
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);
        btnGeneratePDF = findViewById(R.id.btn_generate_pdf);
        btnGenerateInicioInforme = findViewById(R.id.btn_generate_inicio_informe);
        // Obtener el código de la empresa desde el Intent
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");

        if (enterpriseCode != null) {
            ref = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode);
            progressBar.setProgress(10);
            inputTipoCebo = findViewById(R.id.inputTipoCebo);
            inputPeso = findViewById(R.id.inputPeso);
            inputCompuestoActivo = findViewById(R.id.inputCompuestoActivo);
            inputVencimiento = findViewById(R.id.inputVencimiento);
            inputNroLote = findViewById(R.id.inputNroLote);
            fetchControlQuimicoData();
            fetchEnterpriseName();
        } else {
            Toast.makeText(this, "Código de empresa no disponible", Toast.LENGTH_SHORT).show();
        }

        // Inicializar los inputs
        inputTrapMechanic = findViewById(R.id.inputTrapMechanic);
        inputGlueActivated = findViewById(R.id.inputGlueActivated);
        inputRodentsFound = findViewById(R.id.inputRodentsFound);

        // Botones para Trampa Mecánica
        btnIncrementTrap = findViewById(R.id.btnIncrementTrap);
        btnDecrementTrap = findViewById(R.id.btnDecrementTrap);

        btnIncrementTrap.setOnClickListener(v -> incrementValue(inputTrapMechanic));
        btnDecrementTrap.setOnClickListener(v -> decrementValue(inputTrapMechanic));

        // Botones para Goma Adhesiva Activada
        btnIncrementGlue = findViewById(R.id.btnIncrementGlue);
        btnDecrementGlue = findViewById(R.id.btnDecrementGlue);

        btnIncrementGlue.setOnClickListener(v -> incrementValue(inputGlueActivated));
        btnDecrementGlue.setOnClickListener(v -> decrementValue(inputGlueActivated));

        // Botones para Roedores Encontrados
        btnIncrementRodents = findViewById(R.id.btnIncrementRodents);
        btnDecrementRodents = findViewById(R.id.btnDecrementRodents);

        btnIncrementRodents.setOnClickListener(v -> incrementValue(inputRodentsFound));
        btnDecrementRodents.setOnClickListener(v -> decrementValue(inputRodentsFound));




        // Botón para generar PDF después de la carga
        findViewById(R.id.btn_generate_pdf).setOnClickListener(v -> {
            saveControlQuimicoData();
            try {
                generatePDF();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fetchDataFromFirebase();

        btnGenerateInicioInforme.setOnClickListener(v -> {
            try {
                generateInicioInformePDF();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    /////////////////////////////////////////////////


    private void generateInicioInformePDF() throws IOException {
        // Crear el archivo PDF con el nombre de la empresa
        File pdfFile = new File(getExternalFilesDir(null), "inicio_informe_" + enterpriseName + ".pdf");

        // Abrir el PDF existente desde la carpeta assets
        PdfReader reader = new PdfReader(getAssets().open("INICIO_INFORME.pdf"));
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        PdfCanvas pdfCanvas = new PdfCanvas(pdfDoc.getFirstPage());
        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);



        // Colocar los datos extraídos desde Firebase en el PDF en coordenadas específicas
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String codigoEmpresa = dataSnapshot.getKey();  // Obtener el código de empresa de la referencia actual
                String fecha = dataSnapshot.child("fecha").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(150, 675);  // Coordenadas aproximadas para FECHA
                pdfCanvas.showText(fecha);
                pdfCanvas.endText();

                String nombre = dataSnapshot.child("nombre").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(150, 664);  // Coordenadas aproximadas para USUARIO (nombre de la empresa)
                pdfCanvas.showText(nombre);
                pdfCanvas.endText();

                Long rucLong = dataSnapshot.child("ruc").getValue(Long.class); // Cambia a Long
                String ruc = rucLong != null ? String.valueOf(rucLong) : ""; // Convertir a String
                pdfCanvas.beginText();
                pdfCanvas.moveText(150, 653);  // Coordenadas aproximadas para RUC
                pdfCanvas.showText("" + ruc);
                pdfCanvas.endText();

                String ubicacion = dataSnapshot.child("ubicacion").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(150, 642);  // Coordenadas aproximadas para DIRECCION
                pdfCanvas.showText(ubicacion);
                pdfCanvas.endText();

                String rubro = dataSnapshot.child("rubro").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(150, 630);  // Coordenadas aproximadas para RUBRO
                pdfCanvas.showText(rubro);
                pdfCanvas.endText();



                String horaIngreso = dataSnapshot.child("horaIngreso").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(105, 502);  // Coordenadas aproximadas para hora de ingreso
                pdfCanvas.showText("" + formatTime(horaIngreso));
                pdfCanvas.endText();

                String horaSalida = dataSnapshot.child("horaSalida").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(118, 487);  // Coordenadas aproximadas para hora de salida
                pdfCanvas.showText("" + formatTime(horaSalida));
                pdfCanvas.endText();

                List<String> selectedTechnicians = new ArrayList<>();
                for (DataSnapshot techSnapshot : dataSnapshot.child("developed_activities")
                        .child("datosVisit")
                        .child("selectedTechnicians")
                        .getChildren()) {
                    selectedTechnicians.add(techSnapshot.getValue(String.class));
                }


                int posY = 590;  // Coordenada Y inicial donde comenzarán a plasmarse los resultados
                for (DataSnapshot resultSnapshot : dataSnapshot.child("developed_activities").child("datosVisit").child("visitResults").getChildren()) {
                    String result = resultSnapshot.getValue(String.class);

                    pdfCanvas.beginText();
                    pdfCanvas.moveText(80, posY);  // Coordenadas ajustadas para cada resultado de visitResults
                    pdfCanvas.showText("\u2022 " + result);  // Agregar viñeta con un punto
                    pdfCanvas.endText();

                    posY -= 10;  // Ajustar la coordenada Y para la siguiente línea
                }


                // Agregar código de empresa
                pdfCanvas.beginText();
                pdfCanvas.moveText(355, 732);  // Coordenadas aproximadas para el código de empresa
                pdfCanvas.showText("" + codigoEmpresa);
                pdfCanvas.endText();

                // Colocar Técnico y Operador técnico
                int posYtech = 450; // Coordenada Y inicial para listar técnicos
                for (String technician : selectedTechnicians) {
                    String technicianText = "\u2713 " + technician; // Símbolo de viñeta ✓ y nombre del técnico

                    // Verificar si el técnico es uno de los mencionados y agregar el texto correspondiente
                    if ("Wilmer Taipe".equals(technician) || "Isaac Pusari".equals(technician)) {
                        technicianText += " – Operador técnico";
                    } else if ("Alexander Gárate".equals(technician)) {
                        technicianText += " – Supervisor";
                    }

                    // Mostrar el texto en el PDF
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(90, posYtech); // Ajustar la posición para cada técnico
                    pdfCanvas.showText(technicianText); // Mostrar el nombre del técnico con su título
                    pdfCanvas.endText();

                    posYtech -= 15; // Reducir la coordenada Y para la siguiente línea
                }


                // Cerrar el documento después de agregar todos los datos
                document.close();
                Toast.makeText(PestCalculationActivity.this, "PDF generado exitosamente: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar los datos de la empresa", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //////////////////////////////////
    private void fetchControlQuimicoData() {
        ref.child("control_quimico").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String tipoCebo = dataSnapshot.child("tipo_cebo").getValue(String.class);
                    String peso = dataSnapshot.child("peso").getValue(String.class);
                    String compuestoActivo = dataSnapshot.child("compuesto_activo").getValue(String.class);
                    String vencimiento = dataSnapshot.child("vencimiento").getValue(String.class);
                    String nroLote = dataSnapshot.child("nro_lote").getValue(String.class);

                    inputTipoCebo.setText(tipoCebo);
                    inputPeso.setText(peso);
                    inputCompuestoActivo.setText(compuestoActivo);
                    inputVencimiento.setText(vencimiento);
                    inputNroLote.setText(nroLote);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar datos de control químico", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void incrementValue(EditText editText) {
        int value = Integer.parseInt(editText.getText().toString());
        editText.setText(String.valueOf(value + 1));
    }

    private void decrementValue(EditText editText) {
        int value = Integer.parseInt(editText.getText().toString());
        if (value > 0) {
            editText.setText(String.valueOf(value - 1));
        }
    }

    private void updateProgress(int progress, String message) {
        progressBar.setProgress(progress);
        txtProgress.setText(message + " " + progress + "%");
    }

    private void fetchEnterpriseName() {
        updateProgress(20, "Cargando nombre de la empresa...");
        ref.child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                enterpriseName = dataSnapshot.getValue(String.class);
                updateProgress(40, "Cargando datos del cliente...");
                fetchDataFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener el nombre de la empresa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDataFromFirebase() {
        ref.child("developed_activities").child("datosVisit").child("clientName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        clientName = dataSnapshot.getValue(String.class);
                        updateProgress(50, "Cargando ubicación...");
                        fetchUbicacion();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(PestCalculationActivity.this, "Error al obtener el nombre del cliente", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUbicacion() {
        ref.child("ubicacion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ubicacion = dataSnapshot.getValue(String.class);
                updateProgress(60, "Cargando fecha...");
                fetchFecha();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFecha() {

        ref.child("fecha").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fecha = dataSnapshot.getValue(String.class);
                updateProgress(65, "Cargando datos de trampas...");
                fetchCantidad();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener la fecha", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatFecha(String fechaOriginal) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat newFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'del' yyyy", Locale.getDefault());
        try {
            Date date = originalFormat.parse(fechaOriginal);
            return newFormat.format(date); // Retorna la fecha formateada
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fechaOriginal; // Si hay un error, retorna la fecha original
    }

    // Nuevo método para obtener la cantidad
    private void fetchCantidad() {
        ref.child("cantidad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cantidad = dataSnapshot.getValue(Long.class); // Obtener la cantidad de trampas
                updateProgress(70, "Cargando datos de trampas...");
                fetchTrampasSinMovimiento(); // Continuar con el siguiente fetch
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener la cantidad de trampas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Nueva estructura con funciones separadas para obtener cada dato
    private void fetchTrampasSinMovimiento() {
        ref.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long sinMovimientoCount = 0;
                for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                    DataSnapshot historySnapshot = trapSnapshot.child("history");
                    for (DataSnapshot entry : historySnapshot.getChildren()) {
                        Boolean consumption = entry.child("consumption").getValue(Boolean.class);
                        if (consumption != null && !consumption) {
                            sinMovimientoCount++;
                        }
                    }
                }
                trampasSinMovimiento = sinMovimientoCount;
                updateProgress(75, "Trampas sin movimiento cargadas");
                fetchTrampasConConsumo(); // Continuar con el siguiente cálculo
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar trampas sin movimiento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrampasConConsumo() {
        ref.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long conConsumoCount = 0;
                for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                    DataSnapshot historySnapshot = trapSnapshot.child("history");
                    for (DataSnapshot entry : historySnapshot.getChildren()) {
                        Boolean consumption = entry.child("consumption").getValue(Boolean.class);
                        if (consumption != null && consumption) {
                            conConsumoCount++;
                        }
                    }
                }
                trampasConConsumo = conConsumoCount;
                updateProgress(76, "Trampas con consumo cargadas");
                fetchTrampasSinAcceso(); // Continuar con el siguiente cálculo
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar trampas con consumo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrampasSinAcceso() {
        ref.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long sinAccesoCount = 0;
                for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                    DataSnapshot historySnapshot = trapSnapshot.child("history");
                    for (DataSnapshot entry : historySnapshot.getChildren()) {
                        if (!entry.hasChild("trapType")) {
                            sinAccesoCount++;
                        }
                    }
                }
                trampasSinAcceso = sinAccesoCount;
                updateProgress(80, "Trampas sin acceso cargadas");
                fetchActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar trampas sin acceso", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchActivity() {
        ref.child("developed_activities").child("activity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activity = dataSnapshot.getValue(String.class);
                // Actualiza el progreso o continua con la siguiente carga si es necesario
                updateProgress(85, "Cargando actividad...");
                fetchHoraIngreso();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener la actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchHoraIngreso() {
        ref.child("horaIngreso").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                horaIngreso = dataSnapshot.getValue(String.class);
                updateProgress(90, "Cargando hora de salida...");
                fetchHoraSalida();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener la hora de ingreso", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHoraSalida() {
        ref.child("horaSalida").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                horaSalida = dataSnapshot.getValue(String.class);
                updateProgress(90, "Cargando cantidad instalada...");
                calculateCantidadInstalada();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener la hora de salida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTime(String hora) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm", Locale.getDefault()); // Formato 24 horas
        SimpleDateFormat newFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());  // Formato 12 horas AM/PM
        try {
            Date date = originalFormat.parse(hora);
            return newFormat.format(date); // Retorna la hora en formato AM/PM
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hora; // Si hay un error, retorna la hora original
    }

    private void calculateCantidadInstalada() {
        // La cantidad instalada es la suma de trampas sin movimiento y trampas con consumo
        cantidadInstalada = trampasSinMovimiento + trampasConConsumo;
        updateProgress(92, "Cargando cantidad consumida...");
        fetchCantidadConsumida();
    }

    private void fetchCantidadConsumida() {
        ref.child("developed_activities").child("cebos_consumidos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String cebosConsumidosStr = dataSnapshot.getValue(String.class);  // Leer como String
                try {
                    cebosConsumidos = Long.parseLong(cebosConsumidosStr);  // Convertir a long
                } catch (NumberFormatException e) {
                    cebosConsumidos = 0;  // Si hay un error en la conversión, asignar 0
                }
                updateProgress(95, "Cargando cebos repuestos...");
                fetchSelectedTechnician();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener los cebos consumidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSelectedTechnician() {
        ref.child("developed_activities").child("datosVisit").child("selectedTechnician")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        selectedTechnician = dataSnapshot.getValue(String.class);
                        updateProgress(98, "Datos cargados. Generar PDF.");
                        fetchCantidadRepuesta();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(PestCalculationActivity.this, "Error al obtener el técnico", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCantidadRepuesta() {
        ref.child("developed_activities").child("cebos_repuestas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String cebosRepuestosStr = dataSnapshot.getValue(String.class);  // Leer como String
                try {
                    cebosRepuestos = Long.parseLong(cebosRepuestosStr);  // Convertir a long
                } catch (NumberFormatException e) {
                    cebosRepuestos = 0;  // Si hay un error en la conversión, asignar 0
                }
                updateProgress(100, "Datos cargados. Generar PDF.");
                btnGeneratePDF.setVisibility(View.VISIBLE);
                btnGenerateInicioInforme.setVisibility(View.VISIBLE); // Mostrar el botón para generar el PDF
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PestCalculationActivity.this, "Error al obtener los cebos repuestos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatShortDate(String fechaOriginal) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat newFormat = new SimpleDateFormat("dd-MMM-yy", Locale.getDefault());
        try {
            Date date = originalFormat.parse(fechaOriginal);
            return newFormat.format(date);  // Retorna la fecha en el nuevo formato
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fechaOriginal;  // Si hay un error, retorna la fecha original
    }

    private void saveControlQuimicoData() {
        String tipoCebo = inputTipoCebo.getText().toString();
        String peso = inputPeso.getText().toString();
        String compuestoActivo = inputCompuestoActivo.getText().toString();
        String vencimiento = inputVencimiento.getText().toString();
        String nroLote = inputNroLote.getText().toString();

        DatabaseReference controlQuimicoRef = ref.child("control_quimico");

        controlQuimicoRef.child("tipo_cebo").setValue(tipoCebo);
        controlQuimicoRef.child("peso").setValue(peso);
        controlQuimicoRef.child("compuesto_activo").setValue(compuestoActivo);
        controlQuimicoRef.child("vencimiento").setValue(vencimiento);
        controlQuimicoRef.child("nro_lote").setValue(nroLote);
    }


    private void generatePDF() throws IOException {
        // Crear el archivo PDF
        File pdfFile = new File(getExternalFilesDir(null), "Calculo_" + enterpriseName + ".pdf");

        // Abrir el PDF existente desde la carpeta assets
        PdfReader reader = new PdfReader(getAssets().open("PROGRAMA MANEJO.pdf"));
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        PdfCanvas pdfCanvas = new PdfCanvas(pdfDoc.getFirstPage());
        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 7);

        int trapMechanicValue = Integer.parseInt(inputTrapMechanic.getText().toString());
        int glueActivatedValue = Integer.parseInt(inputGlueActivated.getText().toString());
        int rodentsFoundValue = Integer.parseInt(inputRodentsFound.getText().toString());

        String trapMechanicText = (trapMechanicValue == 0) ? "--" : String.valueOf(trapMechanicValue);
        String glueActivatedText = (glueActivatedValue == 0) ? "--" : String.valueOf(glueActivatedValue);
        String rodentsFoundText = (rodentsFoundValue == 0) ? "--" : String.valueOf(rodentsFoundValue);

        // Colocar el nombre del cliente
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 530);  // Coordenadas aproximadas para CLIENTE
        pdfCanvas.showText(clientName);
        pdfCanvas.endText();

        // Colocar la ubicación
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 510);  // Coordenadas aproximadas para DIRECCIÓN
        pdfCanvas.showText(ubicacion);
        pdfCanvas.endText();

        // Colocar la fecha
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 492);  // Coordenadas aproximadas para FECHA
        pdfCanvas.showText(formatFecha(fecha));
        pdfCanvas.endText();

        ref.child("developed_activities")
                .child("datosVisit")
                .child("selectedTechnicians")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();  // Aquí obtienes el DataSnapshot
                        List<String> selectedTechnicians = new ArrayList<>();

                        for (DataSnapshot techSnapshot : dataSnapshot.getChildren()) {
                            String technician = techSnapshot.getValue(String.class);
                            // Excluir "Alexander Gárate" de la lista
                            if (!"Alexander Gárate".equals(technician)) {
                                selectedTechnicians.add(technician);
                            }
                        }

                        String techniciansText = String.join(", ", selectedTechnicians);

                        // Ahora puedes usar 'techniciansText' en el PDF
                        pdfCanvas.beginText();
                        pdfCanvas.moveText(73, 482);  // Coordenadas aproximadas para TECNICO
                        pdfCanvas.showText(techniciansText);
                        pdfCanvas.endText();
                    } else {
                        Toast.makeText(PestCalculationActivity.this, "Error al cargar los técnicos", Toast.LENGTH_SHORT).show();
                    }
                });


        // Colocar el supervisor
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 472);  // Coordenadas aproximadas para SUPERVISOR
        pdfCanvas.showText("Alexander Garate");
        pdfCanvas.endText();

        // Colocar la actividad
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 460);  // Coordenadas aproximadas para la ACTIVIDAD
        pdfCanvas.showText(activity);
        pdfCanvas.endText();

        // Colocar la hora de ingreso
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 449);  // Coordenadas aproximadas para INICIO
        pdfCanvas.showText(formatTime(horaIngreso));
        pdfCanvas.endText();

        // Colocar la hora de término
        pdfCanvas.beginText();
        pdfCanvas.moveText(73, 438);  // Coordenadas aproximadas para TERMINO
        pdfCanvas.showText(formatTime(horaSalida));
        pdfCanvas.endText();

        ///////////////////////////////////////////////

        // Colocar la cantidad
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 530);  // Coordenadas aproximadas para CANTIDAD TOTAL
        pdfCanvas.showText(String.valueOf(cantidad));
        pdfCanvas.endText();

        // Colocar trampas sin movimiento
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 492);  // Coordenadas aproximadas para trampas sin movimiento
        pdfCanvas.showText(String.valueOf(trampasSinMovimiento));
        pdfCanvas.endText();

        // Colocar trampas con consumo de cebo
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 482);  // Coordenadas aproximadas para trampas con consumo de cebo
        pdfCanvas.showText(String.valueOf(trampasConConsumo));
        pdfCanvas.endText();

        // Colocar trampas sin acceso
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 472);  // Coordenadas aproximadas para trampas sin acceso
        pdfCanvas.showText(String.valueOf(trampasSinAcceso));
        pdfCanvas.endText();

        // Colocar Trampa Mecánica
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 460);  // Coordenadas para Trampa mecánica
        pdfCanvas.showText(trapMechanicText);
        pdfCanvas.endText();

        // Colocar Goma adhesiva activada
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 449);  // Coordenadas para Goma adhesiva activada
        pdfCanvas.showText(glueActivatedText);
        pdfCanvas.endText();

        // Colocar Roedores encontrados
        pdfCanvas.beginText();
        pdfCanvas.moveText(350, 438);  // Coordenadas para Roedores encontrados
        pdfCanvas.showText(rodentsFoundText);
        pdfCanvas.endText();

        ///////////////////////////////////////////////

        // Colocar la cantidad instalada
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 530);  // Coordenadas aproximadas para CANTIDAD INSTALADA
        pdfCanvas.showText(String.valueOf(cantidadInstalada));
        pdfCanvas.endText();

        // Colocar la cantidad consumida (cebos_consumidos)
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 510);  // Coordenadas aproximadas para CANTIDAD CONSUMIDA
        pdfCanvas.showText(String.valueOf(cebosConsumidos));
        pdfCanvas.endText();

        // Colocar la cantidad de cebos repuestos (cebos_repuestas)
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 492);  // Coordenadas aproximadas para CANTIDAD REPUESTA
        pdfCanvas.showText(String.valueOf(cebosRepuestos));
        pdfCanvas.endText();

        //////////////////////////////
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 482);  // Coordenadas ajustadas para mostrar el tipo de cebo
        pdfCanvas.showText("" + inputTipoCebo.getText().toString());
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 472);  // Coordenadas ajustadas para mostrar el peso
        pdfCanvas.showText("" + inputPeso.getText().toString() + "");
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 460);  // Coordenadas ajustadas para compuesto activo
        pdfCanvas.showText("" + inputCompuestoActivo.getText().toString());
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 449);  // Coordenadas ajustadas para vencimiento
        pdfCanvas.showText("" + inputVencimiento.getText().toString());
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 438);  // Coordenadas ajustadas para número de lote
        pdfCanvas.showText("" + inputNroLote.getText().toString());
        pdfCanvas.endText();


        // Agregar los datos de las trampas con coordenadas personalizadas
        // Dentro de la función que agrega datos de trampas
        ref.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float posY = 388.0f ;
                int trapsCount = 0;

                for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                    DataSnapshot historySnapshot = trapSnapshot.child("history").getChildren().iterator().next(); // Última entrada del historial

                    String trapType = historySnapshot.child("trapType").getValue(String.class);
                    int poisonAmount = historySnapshot.child("poisonAmount").getValue(Integer.class);
                    int consumptionPercentage = historySnapshot.child("consumptionPercentage").getValue(Integer.class);
                    String date = historySnapshot.child("date").getValue(String.class);
                    int replaceAmount = historySnapshot.child("replaceAmount").getValue(Integer.class);
                    Boolean consumption = historySnapshot.child("consumption").getValue(Boolean.class);
                    Boolean replace = historySnapshot.child("replace").getValue(Boolean.class);
                    Boolean noAccess = historySnapshot.child("noAccess").getValue(Boolean.class);
                    Boolean noChanges = historySnapshot.child("noChanges").getValue(Boolean.class);
                    Boolean perdido = historySnapshot.child("perdido").getValue(Boolean.class);

                    // Verificar condiciones y aplicar colores
                    PdfExtGState transparentState = new PdfExtGState();
                    transparentState.setFillOpacity(0.5f);
                    pdfCanvas.saveState();
                    pdfCanvas.setExtGState(transparentState);

                    if (Boolean.TRUE.equals(noAccess) && Boolean.FALSE.equals(noChanges) && Boolean.FALSE.equals(perdido) && Boolean.FALSE.equals(replace) && Boolean.FALSE.equals(consumption)) {
                        pdfCanvas.setColor(new DeviceRgb(255, 255, 0), true); // Amarillo
                    } else if (Boolean.FALSE.equals(noAccess) && Boolean.FALSE.equals(noChanges) && Boolean.FALSE.equals(perdido) && Boolean.TRUE.equals(replace) && Boolean.FALSE.equals(consumption)) {
                        pdfCanvas.setColor(new DeviceRgb(0, 255, 255), true); // Celeste
                    } else if (Boolean.FALSE.equals(noAccess) && Boolean.FALSE.equals(noChanges) && Boolean.FALSE.equals(perdido) && Boolean.TRUE.equals(replace) && Boolean.TRUE.equals(consumption)) {
                        pdfCanvas.setColor(new DeviceRgb(255, 0, 0), true); // Rojo
                    } else if (Boolean.FALSE.equals(noAccess) && Boolean.TRUE.equals(noChanges) && Boolean.FALSE.equals(perdido)) {
                        pdfCanvas.setColor(new DeviceRgb(255, 255, 255), true); // Blanco
                    } else if (Boolean.FALSE.equals(noAccess) && Boolean.FALSE.equals(noChanges) && Boolean.TRUE.equals(perdido)) {
                        pdfCanvas.setColor(new DeviceRgb(128, 128, 128), true); // Plomo
                    } else {
                        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true); // Negro (por defecto)
                    }

                    pdfCanvas.rectangle(70, posY - 2, 226, 9); // Dibujar rectángulo
                    pdfCanvas.fill();
                    pdfCanvas.restoreState();
                    pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true); // Color texto

                    // Mostrar datos
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(73, posY);
                    pdfCanvas.showText(trapType == null ? "Inaccesible" : trapType);
                    pdfCanvas.endText();

                    pdfCanvas.beginText();
                    pdfCanvas.moveText(130, posY);
                    pdfCanvas.showText(String.valueOf(poisonAmount));
                    pdfCanvas.endText();

                    pdfCanvas.beginText();
                    pdfCanvas.moveText(180, posY);
                    pdfCanvas.showText(consumptionPercentage + "%");
                    pdfCanvas.endText();

                    pdfCanvas.beginText();
                    pdfCanvas.moveText(240, posY);
                    pdfCanvas.showText(replaceAmount == 0 ? "--" : String.valueOf(replaceAmount));
                    pdfCanvas.endText();

                    pdfCanvas.beginText();
                    pdfCanvas.moveText(260, posY);
                    pdfCanvas.showText(formatShortDate(date));
                    pdfCanvas.endText();

                    posY -= 10.5f;

                    if (trapsCount == 24) {
                        posY = 388; // Reiniciar posición Y
                    }
                    trapsCount++;
                }

                document.close();
                Toast.makeText(PestCalculationActivity.this, "PDF generado exitosamente.", Toast.LENGTH_LONG).show();
                // Navegar de regreso al menú principal
                Intent intent = new Intent(PestCalculationActivity.this, MenuActivity.class);
                intent.putExtra("enterpriseCode", enterpriseCode);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar trampas.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Dejar este método vacío o mostrar un mensaje si es necesario.
        // No llamar a super.onBackPressed() para deshabilitar el comportamiento predeterminado.
    }
}