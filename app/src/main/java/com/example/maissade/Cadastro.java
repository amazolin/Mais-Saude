package com.example.maissade;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Spinner;


        import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Cadastro extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private EditText txtNome;
    private EditText txtIdade;
    private EditText txtPeso;
    private EditText txtAltura;
    private EditText txtEmail;
    private EditText txtSenha;
    private EditText txtConfirmarSenha;
    private RadioGroup radioGroupSexo;

    private RadioButton radioMasculino;
    private RadioButton radioFeminino;

    private Button btnPrev;
    private Button btnNext;
    private Button btnCadastrar;
    private Button btnVoltarCadastro;
    private Spinner spinnerTipoSanguineo;

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
            R.drawable.avatartwentyfour,
            R.drawable.avatartwentyfive,
            R.drawable.avatartwentysix,
            R.drawable.avatartwentyseven,
            R.drawable.avatartwentyeight,
            R.drawable.avatartwentynine

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);


        viewFlipper = findViewById(R.id.viewFlipper);
        txtNome = findViewById(R.id.txtNome);
        txtIdade = findViewById(R.id.txtIdade);
        txtPeso = findViewById(R.id.txtPeso);
        txtAltura = findViewById(R.id.txtAltura);
        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);
        txtConfirmarSenha = findViewById(R.id.txtConfirmarSenha);

        radioGroupSexo = findViewById(R.id.radioGroupSexo);
        radioFeminino =findViewById(R.id.radioFeminino);
        radioMasculino = findViewById(R.id.radioMasculino);

        btnCadastrar =findViewById(R.id.btnCadastrar);
        btnNext = findViewById(R.id.btnNext);
        btnPrev =findViewById(R.id.btnPrev);
        btnVoltarCadastro= findViewById(R.id.btnVoltarCadastro);

        viewFlipper = findViewById(R.id.viewFlipper);
        spinnerTipoSanguineo = findViewById(R.id.spinnerTipoSanguineo);


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
