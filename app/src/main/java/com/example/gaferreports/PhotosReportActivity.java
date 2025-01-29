package com.example.gaferreports;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotosReportActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private LinearLayout layoutImagePreview;
    private String enterpriseCode;
    private String enterpriseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_report);

        layoutImagePreview = findViewById(R.id.layout_image_preview);
        Button btnSelectImages = findViewById(R.id.btn_select_images);
        Button btnGeneratePdf = findViewById(R.id.btn_generate_pdf);

        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        if (enterpriseCode == null) {
            Toast.makeText(this, "Enterprise code not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        fetchCompanyName();

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

    private void fetchCompanyName() {
        if (enterpriseCode == null) {
            Toast.makeText(PhotosReportActivity.this, "Enterprise code is missing", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PhotosReportActivity.this, "Error retrieving company name", Toast.LENGTH_SHORT).show();
            }
        });
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
            PdfDocument templatePdf = new PdfDocument(pdfReader); // Abrimos la plantilla

            // Archivo PDF de salida
            String outputFilePath = getExternalFilesDir(null) + "/PhotosReport.pdf";
            PdfWriter pdfWriter = new PdfWriter(outputFilePath);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            float[][] coordinates = {
                    {70, 400}, {310, 400}, // Primera fila de la página 1
                    {70, 80}, {310, 80}, // Segunda fila
            };

            int imageCount = 0;
            for (Uri uri : imageUris) {
                // Si es la primera imagen o cada 4 imágenes, creamos una nueva página
                if (imageCount % 4 == 0) {
                    PdfPage newPage = pdfDocument.addNewPage();
                    PdfCanvas canvas = new PdfCanvas(newPage);

                    // 📌 Copiar el encabezado
                    PdfPage templatePage = templatePdf.getPage(1);
                    canvas.addXObject(templatePage.copyAsFormXObject(pdfDocument), 0, 0);
                }

                // Obtener la página actual donde agregaremos la imagen
                PdfPage currentPage = pdfDocument.getPage(pdfDocument.getNumberOfPages());

                // Convertir la imagen y optimizarla
                Bitmap bitmap = getOptimizedBitmap(uri);
                if (bitmap == null) continue;

                ImageData imageData = ImageDataFactory.create(bitmapToBytes(bitmap));
                Image image = new Image(imageData);

                // Configurar tamaño y posición
                float[] coords = coordinates[imageCount % 4];
                image.scaleToFit(300, 300);
                image.setFixedPosition(pdfDocument.getNumberOfPages(), coords[0], coords[1]);

                // Añadir imagen al documento
                document.add(image);

                imageCount++;
            }

            // Cerrar documentos después de añadir todas las imágenes
            document.close();
            templatePdf.close();

            // Copiar a Descargas y abrir el PDF
            String downloadsFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/PhotosReport.pdf";
            copyFileToDownloads(outputFilePath, downloadsFilePath);
            openGeneratedPDF(downloadsFilePath);

            Toast.makeText(this, "PDF generado en: " + downloadsFilePath, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar el PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getOptimizedBitmap(Uri imageUri) {
        try {
            // Obtener datos EXIF para detectar orientación
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ExifInterface exif = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exif = new ExifInterface(inputStream);
            }

            // Leer la orientación
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Decodificar la imagen con un tamaño manejable
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reducimos el tamaño de la imagen
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);

            // Aplicar la rotación necesaria
            return rotateBitmap(bitmap, orientation);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (bitmap == null) return null;

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }



    private void copyFileToDownloads(String sourcePath, String destinationPath) {
        try {
            File sourceFile = new File(sourcePath);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Usamos MediaStore para Android 10+ (API 29+)
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, "PhotosReport.pdf");
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(uri);
                         FileInputStream in = new FileInputStream(sourceFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                    }
                }
            } else {
                // Método tradicional para Android 9 y anteriores
                File destinationFile = new File(destinationPath);
                if (!destinationFile.exists()) {
                    destinationFile.createNewFile();
                }

                try (FileInputStream in = new FileInputStream(sourceFile);
                     FileOutputStream out = new FileOutputStream(destinationFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            }

            Toast.makeText(this, "PDF copiado exitosamente a Descargas", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al copiar el archivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void openGeneratedPDF(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(intent, "Abrir PDF con..."));
            } catch (Exception e) {
                Toast.makeText(this, "No hay una aplicación para abrir PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }
}
