package com.example.gaferreports;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.annotations.Nullable;

public class TestPhotosActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView[] imageViews = new ImageView[8];
    private Uri[] imageUris = new Uri[8];
    private int currentImageIndex = 0;

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

        // Configurar un click listener para cada ImageView
        for (int i = 0; i < imageViews.length; i++) {
            int index = i;
            imageViews[i].setOnClickListener(v -> {
                currentImageIndex = index;
                openFileChooser();
            });
        }

        // Botón para generar el PDF
        Button generatePdfButton = findViewById(R.id.btn_generate_pdf);
        generatePdfButton.setOnClickListener(v -> generatePdfWithImages());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUris[currentImageIndex] = data.getData();
            imageViews[currentImageIndex].setImageURI(imageUris[currentImageIndex]);
        }
    }

    private void generatePdfWithImages() {
        // Aquí implementas la lógica para generar el PDF con las imágenes adjuntadas
    }
}
