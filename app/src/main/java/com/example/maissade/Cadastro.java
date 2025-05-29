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
import com.example.model.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Cadastro extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private EditText txtNome, txtIdade, txtPeso, txtAltura, txtEmail, txtSenha, txtConfirmarSenha;
    private RadioGroup radioGroupSexo;
    private RadioButton radioMasculino, radioFeminino;
    private Button btnPrev, btnNext, btnCadastrar, btnVoltarCadastro;
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

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/");
        DatabaseReference usuariosRef = database.getReference("usuarios");

        btnCadastrar.setOnClickListener(view -> {
            String nome = txtNome.getText().toString();
            int idade = Integer.parseInt(txtIdade.getText().toString());

            int selectedSexoId = radioGroupSexo.getCheckedRadioButtonId();
            String sexo = selectedSexoId == R.id.radioMasculino ? "Masculino" : "Feminino";

            // Suponha que img0, img1, img2... sejam imagens do ViewFlipper
            int imagemIndex = viewFlipper.getDisplayedChild();
            String imagemSelecionada = "img" + imagemIndex;

            double peso = Double.parseDouble(txtPeso.getText().toString());
            double altura = Double.parseDouble(txtAltura.getText().toString());

            // Aqui está a correção!
            String[] tiposSanguineos = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            String tipoSanguineo = tiposSanguineos[imagemIndex];  // ou use outro índice se o flipper for separado

            String email = txtEmail.getText().toString();
            String senha = txtSenha.getText().toString();
            String confirmarSenha = txtConfirmarSenha.getText().toString();

            if (!senha.equals(confirmarSenha)) {
                Toast.makeText(this, "Senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario usuario = new Usuario(nome, idade, sexo, imagemSelecionada, peso,
                    altura, tipoSanguineo, email);

            String id = usuariosRef.push().getKey();
            usuariosRef.child(id).setValue(usuario)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            Intent in = new Intent(Cadastro.this, MainActivity.class);
            startActivity(in);
        });


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
