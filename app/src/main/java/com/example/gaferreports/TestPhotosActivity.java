package com.example.gaferreports;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TestPhotosActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView[] imageViews = new ImageView[8];
    private Uri[] imageUris = new Uri[8];
    private int currentImageIndex = 0;
    private String enterpriseName = "EmpresaDemo"; // Reemplazar con el nombre real de la empresa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_photos);

        // Inicializar los ImageViews
        imageViews[0] = findViewById(R.id.imageView1);
        imageViews[1] = findViewById(R.id.imageView2);
        imageViews[2] = findViewById(R.id.imageView3);
        imageViews[3] = findViewById(R.id.imageView4);
        imageViews[4] = findViewById(R.id.imageView5);
        imageViews[5] = findViewById(R.id.imageView6);
        imageViews[6] = findViewById(R.id.imageView7);
        imageViews[7] = findViewById(R.id.imageView8);

        // Configurar click listener para seleccionar imágenes
        for (int i = 0; i < imageViews.length; i++) {
            int index = i;
            imageViews[i].setOnClickListener(v -> {
                currentImageIndex = index;
                openFileChooser();
            });
        }

        // Botón para generar el PDF
        Button generatePdfButton = findViewById(R.id.btn_generate_pdf);
        generatePdfButton.setOnClickListener(v -> {
            if (areImagesSelected()) {
                generatePdfWithImages();
            } else {
                Toast.makeText(this, "Por favor selecciona al menos una imagen", Toast.LENGTH_SHORT).show();
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
            // Cargar el PDF existente desde assets
            InputStream inputStream = getAssets().open("ESPACIO FOTOS.pdf");
            PdfReader pdfReader = new PdfReader(inputStream);

            // Crear el archivo PDF con el nombre de la empresa
            String outputFilePath = getExternalFilesDir(null) + "/ESPACIO_FOTOS_" + enterpriseName + ".pdf";
            PdfWriter pdfWriter = new PdfWriter(outputFilePath);
            PdfDocument pdfDocument = new PdfDocument(pdfReader, pdfWriter);

            // Crear objeto Document
            Document document = new Document(pdfDocument);

            // Coordenadas ajustadas para centrar las imágenes en ambas páginas
            float[][] imageCoordinates = {
                    {150, 550}, {350, 550}, {150, 350}, {350, 350}, // Página 1
                    {150, 550}, {350, 550}, {150, 350}, {350, 350}  // Página 2
            };

            // Insertar las imágenes en las posiciones correspondientes
            for (int i = 0; i < imageUris.length; i++) {
                if (imageUris[i] != null) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUris[i]);
                    ImageData imageData = ImageDataFactory.create(bitmapToBytes(bitmap));
                    Image image = new Image(imageData);

                    // Ajustar imagen a la posición
                    int pageNumber = (i < 4) ? 1 : 2;
                    float[] coords = imageCoordinates[i];
                    image.setFixedPosition(pageNumber, coords[0], coords[1]);
                    image.scaleToFit(150, 150); // Escalar imagen

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
