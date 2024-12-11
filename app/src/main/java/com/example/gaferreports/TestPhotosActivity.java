package com.example.gaferreports;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestPhotosActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView[] imageViews = new ImageView[8];
    private Uri[] imageUris = new Uri[8];
    private int currentImageIndex = 0;
    private String enterpriseCode;
    private String enterpriseName;  // Dynamically fetched company name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_photos);

        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        if (enterpriseCode == null) {
            Toast.makeText(this, "Enterprise code not found", Toast.LENGTH_SHORT).show();
            finish(); // Optional: Close the activity if no code is found
        }


        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        fetchCompanyName();  // Fetch the company name dynamically

        // Initialize ImageViews
        imageViews[0] = findViewById(R.id.imageView1);
        imageViews[1] = findViewById(R.id.imageView2);
        imageViews[2] = findViewById(R.id.imageView3);
        imageViews[3] = findViewById(R.id.imageView4);
        imageViews[4] = findViewById(R.id.imageView5);
        imageViews[5] = findViewById(R.id.imageView6);
        imageViews[6] = findViewById(R.id.imageView7);
        imageViews[7] = findViewById(R.id.imageView8);

        // Set up click listener for image selection
        for (int i = 0; i < imageViews.length; i++) {
            int index = i;
            imageViews[i].setOnClickListener(v -> {
                currentImageIndex = index;
                openFileChooser();
            });
        }

        // Generate PDF button
        Button generatePdfButton = findViewById(R.id.btn_generate_pdf);
        generatePdfButton.setOnClickListener(v -> {
            if (areImagesSelected()) {
                generatePdfWithImages();  // Genera el PDF
                // Regresar a la actividad anterior (MenuActivity)
                Intent intent = new Intent(TestPhotosActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();  // Termina la actividad actual (TestPhotosActivity)
            } else {
                Toast.makeText(this, "Por favor selecciona al menos una imagen", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchCompanyName() {
        if (enterpriseCode == null) {
            Toast.makeText(TestPhotosActivity.this, "Enterprise code is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("empresas").child(enterpriseCode);
        ref.child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                enterpriseName = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TestPhotosActivity.this, "Error retrieving company name", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUris[currentImageIndex] = data.getData();
            imageViews[currentImageIndex].setImageURI(imageUris[currentImageIndex]);
        }
    }

    private boolean areImagesSelected() {
        for (Uri uri : imageUris) {
            if (uri != null) {
                return true;
            }
        }
        return false;
    }

    private void generatePdfWithImages() {
        try {
            InputStream inputStream = getAssets().open("ESPACIO FOTOS.pdf");
            PdfReader pdfReader = new PdfReader(inputStream);

            String outputFilePath = getExternalFilesDir(null) + "/ESPACIO_FOTOS_" + enterpriseName + ".pdf";
            PdfWriter pdfWriter = new PdfWriter(outputFilePath);
            PdfDocument pdfDocument = new PdfDocument(pdfReader, pdfWriter);
            Document document = new Document(pdfDocument);

            // Coordenadas ajustadas
            float[][] imageCoordinates = {
                    {100, 500}, {350, 500}, {100, 250}, {350, 250}, // Página 1
                    {100, 500}, {350, 500}, {100, 250}, {350, 250}  // Página 2
            };

            for (int i = 0; i < imageUris.length; i++) {
                if (imageUris[i] != null) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUris[i]);
                    ImageData imageData = ImageDataFactory.create(bitmapToBytes(bitmap));
                    Image image = new Image(imageData);

                    int pageNumber = (i < 4) ? 1 : 2;
                    float[] coords = imageCoordinates[i];
                    image.setFixedPosition(pageNumber, coords[0], coords[1]);
                    image.scaleToFit(200, 200); // Escalar la imagen

                    document.add(image);
                }
            }

            document.close();
            pdfReader.close();
            pdfWriter.close();

            Toast.makeText(this, "PDF generado exitosamente: " + outputFilePath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar el PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }
}
