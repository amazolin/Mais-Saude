package com.example.maissade;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Cadastro extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private int[] avatarIds = {
            R.drawable.avatarone,
            R.drawable.avatartwo,
            R.drawable.avatarthree,
            R.drawable.avatarfour,
            R.drawable.avatarfive,
            R.drawable.avatarsixteen,
            R.drawable.avatarseven,
            R.drawable.avatareight,
            R.drawable.avatarnine,
            R.drawable.avatarten,
            R.drawable.avatareleven,
            R.drawable.avatartwelve,
            R.drawable.avatartirteen,
            R.drawable.avatarfourteen,
            R.drawable.avatarfiftheen,
            R.drawable.avatarseventeen,
            R.drawable.avatarnineteen,
            R.drawable.avatartwenty,
            R.drawable.avatartwentyone,
            R.drawable.avatartwentytwo,
            R.drawable.avatartwentythree,
            R.drawable.avatartwentyfour
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewFlipper = findViewById(R.id.viewFlipper);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);

        // Adiciona os avatares ao ViewFlipper com proporções preservadas
        for (int avatarId : avatarIds) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(avatarId);
            imageView.setAdjustViewBounds(true); // Mantém proporções
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Evita distorção
            viewFlipper.addView(imageView);
        }

        btnPrev.setOnClickListener(v -> viewFlipper.showPrevious());
        btnNext.setOnClickListener(v -> viewFlipper.showNext());
    }
}
