package com.example.gaferreports;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestPhotosActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private GridLayout gridImages;
    private String enterpriseCode;
    private String enterpriseName;

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 o inferior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_photos);
        requestStoragePermissions();
        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        if (enterpriseCode == null) {
            Toast.makeText(this, "Enterprise code not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enterpriseCode = getIntent().getStringExtra("enterpriseCode");
        fetchCompanyName();  // Fetch the company name dynamically

        gridImages = findViewById(R.id.grid_images);
        Button btnSelectImages = findViewById(R.id.btn_select_images);
        Button btnGeneratePdf = findViewById(R.id.btn_generate_pdf);

        // Seleccionar imágenes
        btnSelectImages.setOnClickListener(v -> openFileChooser());

        // Generar PDF
        btnGeneratePdf.setOnClickListener(v -> {
            if (imageUris.size() == 8) {
                generatePdfWithImages();
            } else {
                Toast.makeText(this, "Por favor selecciona 8 imágenes.", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Selecciona hasta 8 imágenes"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUris.clear();
            gridImages.removeAllViews(); // Limpiar vista previa anterior

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && i < 8; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    addImageToGrid(imageUri);
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
                addImageToGrid(data.getData());
            }

            if (imageUris.size() < 8) {
                Toast.makeText(this, "Por favor selecciona al menos 8 imágenes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addImageToGrid(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 250;
        layoutParams.height = 250;
        layoutParams.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(layoutParams);
        gridImages.addView(imageView);
    }

    private void generatePdfWithImages() {
        try {
            InputStream inputStream = getAssets().open("ESPACIO FOTOS.pdf");
            PdfReader pdfReader = new PdfReader(inputStream);
            PdfDocument templatePdf = new PdfDocument(pdfReader);

            // 🔹 Definir ruta del archivo en la carpeta privada
            String privateFilePath = getExternalFilesDir(null) + "/ESPACIO_FOTOS_" + enterpriseName + ".pdf";

            // 🔹 Generar el PDF en la carpeta privada
            PdfWriter privateWriter = new PdfWriter(new FileOutputStream(privateFilePath));
            PdfDocument privatePdfDocument = new PdfDocument(privateWriter);
            Document privateDocument = new Document(privatePdfDocument);

            float[][] coordinates = {
                    {70, 400}, {310, 400},
                    {70, 80}, {310, 80},
                    {70, 400}, {310, 400},
                    {70, 80}, {310, 80}
            };

            for (int i = 0; i < imageUris.size(); i++) {
                // 🔹 Si es la primera imagen o cada 4 imágenes, agregamos una nueva página con la plantilla
                if (i % 4 == 0) {
                    PdfPage privatePage = privatePdfDocument.addNewPage();
                    PdfCanvas privateCanvas = new PdfCanvas(privatePage);
                    PdfPage templatePage = templatePdf.getPage(1);
                    privateCanvas.addXObject(templatePage.copyAsFormXObject(privatePdfDocument), 0, 0);
                }

                // 🔹 Obtener la página actual donde agregaremos la imagen
                PdfPage privateCurrentPage = privatePdfDocument.getPage(privatePdfDocument.getNumberOfPages());

                // 🔹 Convertir la imagen y optimizarla
                Bitmap bitmap = getOptimizedBitmap(imageUris.get(i));
                if (bitmap == null) continue;

                ImageData imageData = ImageDataFactory.create(bitmapToBytes(bitmap));
                Image privateImage = new Image(imageData);

                float[] coords = coordinates[i % 4];
                privateImage.scaleToFit(300, 300);
                privateImage.setFixedPosition(privatePdfDocument.getNumberOfPages(), coords[0], coords[1]);

                privateDocument.add(privateImage);
            }

            // 🔹 Cerrar documentos después de añadir todas las imágenes
            privateDocument.close();
            privatePdfDocument.close();
            templatePdf.close();

            // 🔹 Redirigir al menú SOLO DESPUÉS de que todo haya terminado
            Toast.makeText(this, "PDF generado en Descargas y en la carpeta privada.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(TestPhotosActivity.this, MenuActivity.class);
            intent.putExtra("enterpriseCode", enterpriseCode);
            startActivity(intent);
            finish();


            // 🔹 Guardar PDF en Descargas usando MediaStore
            String downloadsFilePath = savePdfToDownloads(privateFilePath);

            openGeneratedPDF(downloadsFilePath);


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

            // Decodificar la imagen con un tamaño manejable (reducimos resolución)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // 🔹 Reduce la imagen a 1/2 de su tamaño original
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);

            // Aplicar la rotación necesaria
            return rotateBitmap(bitmap, orientation);

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna null si hay un problema al cargar la imagen
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
                return bitmap; // No necesita rotación
        }

        // Crear un nuevo bitmap rotado
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }



    private void copyFileToDownloads(String sourcePath, String destinationPath) {
        try {
            File sourceFile = new File(sourcePath);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Usamos MediaStore para Android 10+ (API 29+)
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, "ESPACIO_FOTOS_" + enterpriseName + ".pdf");
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

    private String savePdfToDownloads(String privateFilePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "ESPACIO_FOTOS_" + enterpriseName + ".pdf");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri);
                     FileInputStream in = new FileInputStream(new File(privateFilePath))) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    return uri.getPath(); // 🔹 Devolvemos la ruta del PDF
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { // Android 9 o inferior
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File downloadsFile = new File(downloadsDir, "ESPACIO_FOTOS_" + enterpriseName + ".pdf");

            try (FileInputStream in = new FileInputStream(new File(privateFilePath));
                 FileOutputStream out = new FileOutputStream(downloadsFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                return downloadsFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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

}
