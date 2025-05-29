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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Cadastro extends AppCompatActivity {
    // <editor-fold desc="Variáveis">
    private ViewFlipper viewFlipper;
    private EditText txtNome, txtIdade, txtPeso, txtAltura, txtEmail, txtSenha, txtConfirmarSenha;
    private RadioGroup radioGroupSexo;
    private RadioButton radioMasculino, radioFeminino;
    private Button btnPrev, btnNext, btnCadastrar, btnVoltarCadastro;
    private Spinner spinnerTipoSanguineo;
    // </editor-fold>
    // <editor-fold desc="Avatar">
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
// </editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        // <editor-fold desc="find.by.view">
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
        // </editor-fold>
        // <editor-fold desc="Ligação ao Firebase">
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/");
        DatabaseReference usuariosRef = database.getReference("usuarios");
        //</editor-fold>
        btnCadastrar.setOnClickListener(view -> {
            String nome = txtNome.getText().toString().trim();
            String idadeStr = txtIdade.getText().toString().trim();
            String pesoStr = txtPeso.getText().toString().trim();
            String alturaStr = txtAltura.getText().toString().trim();
            String email = txtEmail.getText().toString().trim();
            String senha = txtSenha.getText().toString();
            String confirmarSenha = txtConfirmarSenha.getText().toString();

            if (nome.isEmpty() || idadeStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                Toast.makeText(this, "Senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }

            int idade;
            double peso, altura;

            try {
                idade = Integer.parseInt(idadeStr);
                peso = Double.parseDouble(pesoStr);
                altura = Double.parseDouble(alturaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Idade, peso e altura devem ser números válidos", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedSexoId = radioGroupSexo.getCheckedRadioButtonId();
            String sexo = (selectedSexoId == R.id.radioMasculino) ? "Masculino" : "Feminino";

            int imagemIndex = viewFlipper.getDisplayedChild();
            String imagemSelecionada = "img" + imagemIndex;

            String[] tiposSanguineos = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            String tipoSanguineo = tiposSanguineos[imagemIndex % tiposSanguineos.length];

            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        Usuario usuario = new Usuario(nome, idade, sexo, imagemSelecionada, peso, altura, tipoSanguineo, email, senha);

                        usuariosRef.child(uid).setValue(usuario)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Cadastro.this, Login.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro no cadastro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // <editor-fold desc="View Flipper">
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
        //</editor-fold>
    }
}
