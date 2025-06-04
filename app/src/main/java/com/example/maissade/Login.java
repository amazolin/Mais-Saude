package com.example.maissade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Importe esta classe!
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private Button btnCadastrar, btnLogin;
    private EditText txtUsername, txtPassword;
    private ImageButton imgButtonSenha; // <<<<< DECLARE AQUI!
    private boolean isPasswordVisible = false; // <<<<< DECLARE E INICIALIZE AQUI!

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializa componentes
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnLogin = findViewById(R.id.btnLogin);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        imgButtonSenha = findViewById(R.id.imgButtonSenha); // <<<<< INICIALIZE AQUI DENTRO DO onCreate()!

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/").getReference("usuarios");

        btnCadastrar.setOnClickListener(v -> {
            pararMusica();
            startActivity(new Intent(Login.this, Cadastro.class));
        });

        btnLogin.setOnClickListener(v -> {
            String email = txtUsername.getText().toString().trim();
            String senha = txtPassword.getText().toString();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(authResult -> {
                        String uid = auth.getCurrentUser().getUid();

                        usuariosRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Usuario usuario = snapshot.getValue(Usuario.class);
                                    if (usuario != null && usuario.nome != null) {
                                        Toast.makeText(Login.this, "Bem-vindo, " + usuario.nome, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Login.this, "Bem-vindo!", Toast.LENGTH_SHORT).show();
                                    }

                                    pararMusica();
                                    startActivity(new Intent(Login.this, TelaUsuario.class));
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Login.this, "Erro ao acessar dados do usuário: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(Login.this, "Erro de login: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // <<<<< LÓGICA DO BOTÃO DENTRO DO onCreate() >>>>>
        imgButtonSenha.setOnClickListener(v -> {
            if (isPasswordVisible) { //
                // Se a senha estiver visível, esconda-a
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //
                imgButtonSenha.setImageResource(R.drawable.esconder); // Mude para o ícone de olho cortado
            } else { //
                // Se a senha estiver escondida, mostre-a
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //
                imgButtonSenha.setImageResource(R.drawable.mostrar); // Mude para o ícone de olho aberto
            }
            // Move o cursor para o final do texto
            txtPassword.setSelection(txtPassword.getText().length());
            isPasswordVisible = !isPasswordVisible; // Inverte o estado da visibilidade
        });

        // Configuração de insets do sistema (tela cheia)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.fantasy);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararMusica();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararMusica();
    }

    private void pararMusica() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}