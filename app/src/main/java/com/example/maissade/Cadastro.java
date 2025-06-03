package com.example.maissade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils; // Import para TextUtils
import android.util.Patterns; // Import para Patterns (validação de email)
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
// import android.widget.RadioButton; // Removido se não estiver usando IDs individuais
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // Import para @NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Usuario; // Certifique-se que esta é a classe Usuario correta
import com.google.android.gms.tasks.OnCompleteListener; // Import para OnCompleteListener
import com.google.android.gms.tasks.Task; // Import para Task
import com.google.firebase.auth.AuthResult; // Import para AuthResult
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Cadastro extends AppCompatActivity {
    // <editor-fold desc="Variáveis">
    private ViewFlipper viewFlipper;
    private EditText txtNome, txtIdade, txtPeso, txtAltura, txtEmail, txtSenha, txtConfirmarSenha;
    private RadioGroup radioGroupSexo;
    private Button btnPrev, btnNext, btnCadastrar, btnVoltarCadastro;
    private Spinner spinnerTipoSanguineo;
    private MediaPlayer mediaPlayer;

    private final int[] avatarIds = { // Adicionado 'final' pois não muda
            R.drawable.avatarone, R.drawable.avatartwo, R.drawable.avatarthree,
            R.drawable.avatarfour, R.drawable.avatarfive, R.drawable.avatarsix,
            R.drawable.avatarsixteen, R.drawable.avatarseven, R.drawable.avatareight,
            R.drawable.avatareighteen, R.drawable.avatarnine, R.drawable.avatarten,
            R.drawable.avatareleven, R.drawable.avatartwelve, R.drawable.avatartirteen,
            R.drawable.avatarfourteen, R.drawable.avatarfiftheen, R.drawable.avatarseventeen,
            R.drawable.avatarnineteen, R.drawable.avatartwenty, R.drawable.avatartwentyone,
            R.drawable.avatartwentytwo, R.drawable.avatartwentythree, R.drawable.avatartwentyfour,
            R.drawable.avatartwentyfive, R.drawable.avatartwentysix, R.drawable.avatartwentyseven,
            R.drawable.avatartwentyeight, R.drawable.avatartwentynine
    };

    private DatabaseReference usuariosRef;
    private FirebaseAuth firebaseAuth;

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
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnVoltarCadastro = findViewById(R.id.btnVoltarCadastro);
        spinnerTipoSanguineo = findViewById(R.id.spinnerTipoSanguineo);

        // <editor-fold desc="Ligação ao Firebase">
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/"); // CONFIRME ESTA URL
        usuariosRef = database.getReference("usuarios");
        firebaseAuth = FirebaseAuth.getInstance();

        btnCadastrar.setOnClickListener(view -> registrarUsuario());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // <editor-fold desc="View Flipper">
        for (int avatarId : avatarIds) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(avatarId);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // FIT_CENTER pode ser melhor para avatares
            viewFlipper.addView(imageView);
        }
        btnPrev.setOnClickListener(v -> viewFlipper.showPrevious());
        btnNext.setOnClickListener(v -> viewFlipper.showNext());
        btnVoltarCadastro.setOnClickListener(v -> finish());
    }

    private boolean validarCampos(String nome, String idadeStr, String pesoStr, String alturaStr, String email, String senha, String confirmarSenha) {
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(idadeStr) || TextUtils.isEmpty(pesoStr) ||
                TextUtils.isEmpty(alturaStr) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha) ||
                TextUtils.isEmpty(confirmarSenha)) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Email inválido");
            txtEmail.requestFocus();
            Toast.makeText(this, "Formato de email inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (senha.length() < 6) {
            txtSenha.setError("A senha deve ter no mínimo 6 caracteres");
            txtSenha.requestFocus();
            Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!senha.equals(confirmarSenha)) {
            txtConfirmarSenha.setError("Senhas não coincidem");
            txtConfirmarSenha.requestFocus();
            Toast.makeText(this, "Senhas não coincidem", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void registrarUsuario() {
        String nome = txtNome.getText().toString().trim();
        String idadeStr = txtIdade.getText().toString().trim();
        String pesoStr = txtPeso.getText().toString().trim();
        String alturaStr = txtAltura.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String senha = txtSenha.getText().toString(); // Usada apenas para o Auth
        String confirmarSenha = txtConfirmarSenha.getText().toString();

        if (!validarCampos(nome, idadeStr, pesoStr, alturaStr, email, senha, confirmarSenha)) {
            return;
        }

        int idade;
        double peso, altura;

        try {
            idade = Integer.parseInt(idadeStr);
            if (idade <= 0 || idade > 120) { // Validação básica de idade
                txtIdade.setError("Idade inválida");
                Toast.makeText(this, "Idade inválida", Toast.LENGTH_SHORT).show();
                return;
            }
            peso = Double.parseDouble(pesoStr);
            if (peso <= 0 || peso > 500) { // Validação básica de peso
                txtPeso.setError("Peso inválido");
                Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            altura = Double.parseDouble(alturaStr);
            if (altura <= 50 || altura > 300) { // Validação básica de altura em centímetros
                txtAltura.setError("Altura inválida (em cm, ex: 175)");
                Toast.makeText(this, "Altura inválida (em cm, ex: 175)", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Idade, peso e altura devem ser números válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedSexoId = radioGroupSexo.getCheckedRadioButtonId();
        if (selectedSexoId == -1) {
            Toast.makeText(this, "Selecione o sexo", Toast.LENGTH_SHORT).show();
            return;
        }
        String sexo = (selectedSexoId == R.id.radioMasculino) ? "Masculino" : "Feminino";

        int imagemIndex = viewFlipper.getDisplayedChild();
        String imagemSelecionada = "avatar_" + imagemIndex; // Nome do recurso (ou identificador)

        String tipoSanguineo;
        if (spinnerTipoSanguineo.getSelectedItemPosition() == 0 && spinnerTipoSanguineo.getItemAtPosition(0).toString().toLowerCase().contains("selecione")) {
            // Assume que o primeiro item é um placeholder como "Selecione..."
            Toast.makeText(this, "Selecione o tipo sanguíneo", Toast.LENGTH_SHORT).show();
            return;
        }
        tipoSanguineo = spinnerTipoSanguineo.getSelectedItem().toString();


        // Exibe um loading (opcional, mas recomendado para feedback ao usuário)
        // ProgressBar progressBar = findViewById(R.id.cadastro_progress_bar); // Você precisaria adicionar isso ao seu XML
        // if(progressBar != null) progressBar.setVisibility(View.VISIBLE);
        // btnCadastrar.setEnabled(false); // Desabilita o botão durante o processo

        firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // if(progressBar != null) progressBar.setVisibility(View.GONE);
                        // btnCadastrar.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                // ***** A MUDANÇA MAIS IMPORTANTE ESTÁ AQUI *****
                                // Usa o construtor de Usuario que NÃO pede senha.
                                // A classe Usuario já inicializa xp e ultimoRegistroSonoTimestamp com 0.

                                Usuario usuario = new Usuario(nome, idade, sexo, imagemSelecionada, peso, altura, tipoSanguineo, email);

                                usuariosRef.child(uid).setValue(usuario)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() { // Usar OnCompleteListener
                                            @Override
                                            public void onComplete(@NonNull Task<Void> saveTask) {
                                                if (saveTask.isSuccessful()) {
                                                    Toast.makeText(Cadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
                                                    // firebaseUser.sendEmailVerification(); // Descomente para enviar email de verificação
                                                    Intent intent = new Intent(Cadastro.this, Login.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha de atividades
                                                    startActivity(intent);
                                                    finish(); // Garante que esta activity seja finalizada
                                                } else {
                                                    Toast.makeText(Cadastro.this, "Erro ao salvar dados do perfil: " + saveTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    // Lógica de fallback: O usuário foi criado no Auth, mas não no DB.
                                                    // Você pode tentar deletar o usuário do Auth: firebaseUser.delete();
                                                    // Ou fornecer uma opção para o usuário tentar novamente.
                                                }
                                            }
                                        });
                            } else {
                                // Caso raro onde firebaseUser é null mesmo com task.isSuccessful()
                                Toast.makeText(Cadastro.this, "Falha no cadastro. Tente novamente.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Tratar falhas do createUserWithEmailAndPassword
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                txtSenha.setError("Senha fraca.");
                                txtSenha.requestFocus();
                                Toast.makeText(Cadastro.this, "Senha fraca. Use pelo menos 6 caracteres.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                txtEmail.setError("Email inválido.");
                                txtEmail.requestFocus();
                                Toast.makeText(Cadastro.this, "Formato de email inválido.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthUserCollisionException e) {
                                txtEmail.setError("Email já cadastrado.");
                                txtEmail.requestFocus();
                                Toast.makeText(Cadastro.this, "Este email já está cadastrado.", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(Cadastro.this, "Falha no cadastro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    // <editor-fold desc="Ciclo de Vida da Música">
    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.celtic);
            if (mediaPlayer != null) { // Verifica se a criação foi bem sucedida
                mediaPlayer.setLooping(true);
            }
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}