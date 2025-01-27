package com.example.gaferreports;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotosReportActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private LinearLayout layoutImagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_report);

        layoutImagePreview = findViewById(R.id.layout_image_preview);
        Button btnSelectImages = findViewById(R.id.btn_select_images);
        Button btnGeneratePdf = findViewById(R.id.btn_generate_pdf);

        // Seleccionar imágenes
        btnSelectImages.setOnClickListener(v -> openFileChooser());

        // Generar PDF
        btnGeneratePdf.setOnClickListener(v -> {
            if (!imageUris.isEmpty()) {
                generatePdfWithImages();
            } else {
                Toast.makeText(this, "Por favor selecciona al menos una imagen.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Selecciona imágenes"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                        addImageToPreview(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    imageUris.add(imageUri);
                    addImageToPreview(imageUri);
                }
            }
        }
    }

    private void addImageToPreview(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // Altura de la vista previa
        );
        layoutParams.setMargins(0, 16, 0, 16);
        imageView.setLayoutParams(layoutParams);

        layoutImagePreview.addView(imageView);
    }

    private void generatePdfWithImages() {
        try {
            InputStream inputStream = getAssets().open("PAGINA VACIA.pdf");
            PdfReader pdfReader = new PdfReader(inputStream);

            // Archivo PDF de salida
            String outputFilePath = getExternalFilesDir(null) + "/PhotosReport.pdf";
            PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(outputFilePath));
            PdfDocument pdfDocument = new PdfDocument(pdfReader, pdfWriter);
            Document document = new Document(pdfDocument);

            float[][] coordinates = {
                    {50, 500}, {300, 500}, // Primera fila
                    {50, 250}, {300, 250}  // Segunda fila
            };

            int imageCount = 0;
            for (Uri uri : imageUris) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ImageData imageData = ImageDataFactory.create(bitmapToBytes(bitmap));
                Image image = new Image(imageData);

                // Ajustar tamaño y posición de la imagen
                image.scaleToFit(200, 200);
                float[] coords = coordinates[imageCount % 4];
                image.setFixedPosition((imageCount / 4) + 1, coords[0], coords[1]); // Página actual
                document.add(image);

                imageCount++;

                // Añadir una nueva página después de cada 4 imágenes
                if (imageCount % 4 == 0 && imageCount < imageUris.size()) {
                    pdfDocument.addNewPage();
                }
            }

            document.close();

            Toast.makeText(this, "PDF generado en: " + outputFilePath, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar el PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }
}
