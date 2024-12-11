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
import java.util.Date;
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
                pdfCanvas.moveText(90, 702);  // Coordenadas aproximadas para FECHA
                pdfCanvas.showText(fecha);
                pdfCanvas.endText();

                String nombre = dataSnapshot.child("nombre").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(100, 690);  // Coordenadas aproximadas para USUARIO (nombre de la empresa)
                pdfCanvas.showText(nombre);
                pdfCanvas.endText();

                String ubicacion = dataSnapshot.child("ubicacion").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(106, 668);  // Coordenadas aproximadas para DIRECCION
                pdfCanvas.showText(ubicacion);
                pdfCanvas.endText();

                String rubro = dataSnapshot.child("rubro").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(93, 655);  // Coordenadas aproximadas para RUBRO
                pdfCanvas.showText(rubro);
                pdfCanvas.endText();

                Long rucLong = dataSnapshot.child("ruc").getValue(Long.class); // Cambia a Long
                String ruc = rucLong != null ? String.valueOf(rucLong) : ""; // Convertir a String
                pdfCanvas.beginText();
                pdfCanvas.moveText(78, 679);  // Coordenadas aproximadas para RUC
                pdfCanvas.showText("" + ruc);
                pdfCanvas.endText();

                String horaIngreso = dataSnapshot.child("horaIngreso").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(100, 516);  // Coordenadas aproximadas para hora de ingreso
                pdfCanvas.showText("" + formatTime(horaIngreso));
                pdfCanvas.endText();

                String horaSalida = dataSnapshot.child("horaSalida").getValue(String.class);
                pdfCanvas.beginText();
                pdfCanvas.moveText(115, 504);  // Coordenadas aproximadas para hora de salida
                pdfCanvas.showText("" + formatTime(horaSalida));
                pdfCanvas.endText();

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
                pdfCanvas.moveText(309, 725);  // Coordenadas aproximadas para el código de empresa
                pdfCanvas.showText("" + codigoEmpresa);
                pdfCanvas.endText();

                // Colocar Técnico y Operador técnico
                if (selectedTechnician != null) {
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(109, 450); // Coordenadas ajustadas para el técnico
                    pdfCanvas.showText(selectedTechnician + " – Operador técnico");
                    pdfCanvas.endText();
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
                btnGeneratePDF.setVisibility(View.VISIBLE);  // Mostrar el botón para generar el PDF
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
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);

        int trapMechanicValue = Integer.parseInt(inputTrapMechanic.getText().toString());
        int glueActivatedValue = Integer.parseInt(inputGlueActivated.getText().toString());
        int rodentsFoundValue = Integer.parseInt(inputRodentsFound.getText().toString());

        String trapMechanicText = (trapMechanicValue == 0) ? "--" : String.valueOf(trapMechanicValue);
        String glueActivatedText = (glueActivatedValue == 0) ? "--" : String.valueOf(glueActivatedValue);
        String rodentsFoundText = (rodentsFoundValue == 0) ? "--" : String.valueOf(rodentsFoundValue);

        // Colocar el nombre del cliente
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 836);  // Coordenadas aproximadas para CLIENTE
        pdfCanvas.showText(clientName);
        pdfCanvas.endText();

        // Colocar la ubicación
        pdfCanvas.beginText();
        pdfCanvas.moveText(90, 808);  // Coordenadas aproximadas para DIRECCIÓN
        pdfCanvas.showText(ubicacion);
        pdfCanvas.endText();

        // Colocar la fecha
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 787);  // Coordenadas aproximadas para FECHA
        pdfCanvas.showText(formatFecha(fecha));
        pdfCanvas.endText();

        // Colocar el técnico
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 770);  // Coordenadas aproximadas para TECNICO
        pdfCanvas.showText(selectedTechnician);
        pdfCanvas.endText();

        // Colocar el supervisor
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 755);  // Coordenadas aproximadas para SUPERVISOR
        pdfCanvas.showText("ALEXANDER GARATE");
        pdfCanvas.endText();

        // Colocar la actividad
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 738);  // Coordenadas aproximadas para la ACTIVIDAD
        pdfCanvas.showText(activity);
        pdfCanvas.endText();

        // Colocar la hora de ingreso
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 723);  // Coordenadas aproximadas para INICIO
        pdfCanvas.showText(formatTime(horaIngreso));
        pdfCanvas.endText();

        // Colocar la hora de término
        pdfCanvas.beginText();
        pdfCanvas.moveText(100, 710);  // Coordenadas aproximadas para TERMINO
        pdfCanvas.showText(formatTime(horaSalida));
        pdfCanvas.endText();

        // Colocar la cantidad
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 836);  // Coordenadas aproximadas para CANTIDAD TOTAL
        pdfCanvas.showText(String.valueOf(cantidad));
        pdfCanvas.endText();

        // Colocar trampas sin movimiento
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 787);  // Coordenadas aproximadas para trampas sin movimiento
        pdfCanvas.showText(String.valueOf(trampasSinMovimiento));
        pdfCanvas.endText();

        // Colocar trampas con consumo de cebo
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 770);  // Coordenadas aproximadas para trampas con consumo de cebo
        pdfCanvas.showText(String.valueOf(trampasConConsumo));
        pdfCanvas.endText();

        // Colocar trampas sin acceso
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 755);  // Coordenadas aproximadas para trampas sin acceso
        pdfCanvas.showText(String.valueOf(trampasSinAcceso));
        pdfCanvas.endText();

        // Colocar Trampa Mecánica
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 738);  // Coordenadas para Trampa mecánica
        pdfCanvas.showText(trapMechanicText);
        pdfCanvas.endText();

        // Colocar Goma adhesiva activada
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 723);  // Coordenadas para Goma adhesiva activada
        pdfCanvas.showText(glueActivatedText);
        pdfCanvas.endText();

        // Colocar Roedores encontrados
        pdfCanvas.beginText();
        pdfCanvas.moveText(510, 710);  // Coordenadas para Roedores encontrados
        pdfCanvas.showText(rodentsFoundText);
        pdfCanvas.endText();

        // Colocar la cantidad instalada
        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 836);  // Coordenadas aproximadas para CANTIDAD INSTALADA
        pdfCanvas.showText(String.valueOf(cantidadInstalada));
        pdfCanvas.endText();

        // Colocar la cantidad consumida (cebos_consumidos)
        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 808);  // Coordenadas aproximadas para CANTIDAD CONSUMIDA
        pdfCanvas.showText(String.valueOf(cebosConsumidos));
        pdfCanvas.endText();

        // Colocar la cantidad de cebos repuestos (cebos_repuestas)
        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 878);  // Coordenadas aproximadas para CANTIDAD REPUESTA
        pdfCanvas.showText(String.valueOf(cebosRepuestos));
        pdfCanvas.endText();

        //////////////////////////////
        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 770);  // Coordenadas ajustadas para mostrar el tipo de cebo
        pdfCanvas.showText("" + inputTipoCebo.getText().toString());
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 755);  // Coordenadas ajustadas para mostrar el peso
        pdfCanvas.showText("" + inputPeso.getText().toString() + "");
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 738);  // Coordenadas ajustadas para compuesto activo
        pdfCanvas.showText("" + inputCompuestoActivo.getText().toString());
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 723);  // Coordenadas ajustadas para vencimiento
        pdfCanvas.showText("" + inputVencimiento.getText().toString());
        pdfCanvas.endText();

        pdfCanvas.beginText();
        pdfCanvas.moveText(710, 710);  // Coordenadas ajustadas para número de lote
        pdfCanvas.showText("" + inputNroLote.getText().toString());
        pdfCanvas.endText();


        // Agregar los datos de las trampas con coordenadas personalizadas
        // Dentro de la función que agrega datos de trampas
        ref.child("trampas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int posY = 638; // Coordenada Y para la posición del texto de las trampas
                int trapsCount = 0;
                for (DataSnapshot trapSnapshot : snapshot.getChildren()) {
                    DataSnapshot historySnapshot = trapSnapshot.child("history").getChildren().iterator().next(); // Obtener la última entrada del historial

                    String trapType = historySnapshot.child("trapType").getValue(String.class);
                    int poisonAmount = historySnapshot.child("poisonAmount").getValue(Integer.class);
                    int consumptionPercentage = historySnapshot.child("consumptionPercentage").getValue(Integer.class);
                    String date = historySnapshot.child("date").getValue(String.class);
                    int replaceAmount = historySnapshot.child("replaceAmount").getValue(Integer.class);
                    Boolean consumption = historySnapshot.child("consumption").getValue(Boolean.class);
                    Boolean replace = historySnapshot.child("replace").getValue(Boolean.class);

                    // Verificar si trapType es nulo
                    if (trapType == null) {
                        trapType = "Inaccesible";

                        // Crear un estado de transparencia con opacidad (por ejemplo, 0.5 para 50%)
                        PdfExtGState transparentState = new PdfExtGState();
                        transparentState.setFillOpacity(0.5f); // Ajusta la opacidad entre 0 (transparente) y 1 (opaco)

                        // Aplicar el estado de transparencia al canvas
                        pdfCanvas.saveState();
                        pdfCanvas.setExtGState(transparentState);

                        // Dibuja un rectángulo amarillo transparente detrás del texto
                        pdfCanvas.setColor(new DeviceRgb(255, 255, 0), true); // Color amarillo
                        pdfCanvas.rectangle(95, posY - 5, 321, 13); // Rectángulo (x, y, ancho, alto)
                        pdfCanvas.fill(); // Rellenar el rectángulo con color

                        pdfCanvas.restoreState();
                        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true); // Color negro para el texto
                    } else {
                        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true); // Color negro para otros tipos
                    }

                    if (consumption != null && consumption) {
                        PdfExtGState transparentState = new PdfExtGState();
                        transparentState.setFillOpacity(0.5f);
                        pdfCanvas.saveState();
                        pdfCanvas.setExtGState(transparentState);
                        pdfCanvas.setColor(new DeviceRgb(255, 0, 0), true); // Rojo
                        pdfCanvas.rectangle(95, posY - 5, 321, 13);
                        pdfCanvas.fill();
                        pdfCanvas.restoreState();
                        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);
                    }

                    // Condicional para replace:true - Dibuja un rectángulo celeste transparente
                    if (replace != null && replace) {
                        PdfExtGState transparentState = new PdfExtGState();
                        transparentState.setFillOpacity(0.5f);
                        pdfCanvas.saveState();
                        pdfCanvas.setExtGState(transparentState);
                        pdfCanvas.setColor(new DeviceRgb(0, 255, 255), true); // Celeste
                        pdfCanvas.rectangle(95, posY - 5, 321, 13);
                        pdfCanvas.fill();
                        pdfCanvas.restoreState();
                        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);
                    }

                    // Mostrar Tipo de Trampa
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(95, posY);  // Ajustar coordenadas para la posición del texto
                    pdfCanvas.showText(trapType);
                    pdfCanvas.endText();

                    // Mostrar Cantidad de Cebo
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(200, posY);  // Coordenadas ajustadas
                    pdfCanvas.showText("" + poisonAmount + "");
                    pdfCanvas.endText();

                    // Mostrar Actividad (en porcentaje)
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(250, posY);  // Coordenadas ajustadas
                    pdfCanvas.showText("" + consumptionPercentage + "%");
                    pdfCanvas.endText();

                    pdfCanvas.beginText();
                    pdfCanvas.moveText(300, posY);  // Coordenadas ajustadas para ReplaceAmount
                    pdfCanvas.showText(replaceAmount == 0 ? "--" : String.valueOf(replaceAmount));
                    pdfCanvas.endText();

                    // Mostrar Fecha
                    pdfCanvas.beginText();
                    pdfCanvas.moveText(350, posY);  // Coordenadas ajustadas
                    pdfCanvas.showText(formatShortDate(date));
                    pdfCanvas.endText();

                    // Ajustar la coordenada Y para la siguiente línea
                    posY -= 15;

                    if (trapsCount == 24) {
                        posY = 950;  // Reiniciar Y para la segunda columna
                    }
                    trapsCount++;
                }

                // Cerrar el documento después de agregar todos los datos
                document.close();

                Toast.makeText(PestCalculationActivity.this, "PDF generado exitosamente: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

                // Navegar de regreso al menú principal
                Intent intent = new Intent(PestCalculationActivity.this, MenuActivity.class);
                intent.putExtra("enterpriseCode", enterpriseCode);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PestCalculationActivity.this, "Error al cargar las trampas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
