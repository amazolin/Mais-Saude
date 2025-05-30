package com.example.maissade;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Usuario; // Seu modelo de usuário
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private Button btnCadastrar, btnLogin;
    private EditText txtUsername, txtPassword;
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
    private ImageButton btnGoogleSignIn;

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
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/").getReference("usuarios");

        btnCadastrar.setOnClickListener(v -> {
            pararMusica(); // Parar música antes de ir para Cadastro
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
                                    Toast.makeText(Login.this, "Bem-vindo, " + usuario.nome, Toast.LENGTH_SHORT).show();

                                    pararMusica(); // Parar música antes de ir para próxima tela
                                    startActivity(new Intent(Login.this, TelaUsuario.class));
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Login.this, "Erro ao acessar dados: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(Login.this, "Erro de login: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Sempre reinicia a música ao voltar para a tela Login
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.fantasy);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararMusica(); // Para a música ao sair da tela Login
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararMusica(); // Garante liberação dos recursos
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


        /*
        // Configurar opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Importante para Firebase Auth
                .requestEmail() // Solicita o e-mail do usuário
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> signIn());

        // Verificar se o usuário já está logado no Firebase Auth
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    // Inicia o fluxo de login do Google
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                // Obtém a conta Google do resultado do login
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                // Autentica o Firebase com a conta Google
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e("Login", "Falha na autenticação Google: " + e.getMessage());
                Toast.makeText(Login.this, "Falha na autenticação Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Autentica o Firebase usando as credenciais da conta Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // Cria uma credencial Firebase com o ID Token do Google
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Autenticação Firebase bem-sucedida
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Verifica se o usuário já existe no seu Realtime Database
                            usuariosRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        // O usuário NÃO existe no Realtime Database, então cria uma nova entrada
                                        String nome = user.getDisplayName(); // Nome do Google
                                        String email = user.getEmail();     // E-mail do Google
                                        String imagemPerfil = null; // A URL da foto de perfil pode vir do Google (user.getPhotoUrl().toString()), mas trataremos como String aqui
                                        if (user.getPhotoUrl() != null) {
                                            imagemPerfil = user.getPhotoUrl().toString();
                                        }

                                        // Cria um novo objeto Usuario com os dados disponíveis do Google e valores padrão/nulos para os outros
                                        Usuario newUsuario = new Usuario(
                                                nome,
                                                0,         // idade padrão (ou solicite depois)
                                                "Não informado", // sexo padrão (ou solicite depois)
                                                imagemPerfil,
                                                0.0,       // peso padrão (ou solicite depois)
                                                0.0,       // altura padrão (ou solicite depois)
                                                "Não informado", // tipoSanguineo padrão (ou solicite depois)
                                                email,
                                                null       // senha não é aplicável para login Google
                                        );

                                        usuariosRef.child(user.getUid()).setValue(newUsuario) // Usa o UID do Firebase Auth como chave
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Login", "Novo usuário cadastrado no Realtime Database: " + user.getUid());
                                                    updateUI(user); // Prossegue para a UI
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Login", "Erro ao cadastrar novo usuário no Realtime Database: " + e.getMessage());
                                                    Toast.makeText(Login.this, "Erro ao cadastrar informações do usuário: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    updateUI(user); // Ainda prossegue para a UI mesmo com erro no banco
                                                });
                                    } else {
                                        // O usuário JÁ existe no Realtime Database
                                        Log.d("Login", "Usuário já existe no Realtime Database: " + user.getUid());
                                        updateUI(user); // Prossegue para a UI
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Login", "Erro ao verificar usuário no Realtime Database: " + error.getMessage());
                                    Toast.makeText(Login.this, "Erro ao verificar dados do usuário: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    updateUI(user); // Ainda prossegue para a UI
                                }
                            });
                        }
                    } else {
                        Toast.makeText(Login.this, "Erro ao autenticar com Google: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Atualiza a interface do usuário com base no status de login do Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(Login.this, "Bem-vindo, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            // Aqui você pode adicionar uma lógica para verificar se o perfil está completo.
            // Se não estiver, redirecione para uma tela de completude do perfil.
            startActivity(new Intent(Login.this, TelaUsuario.class)); // Altere para sua tela principal
            finish();
        } else {
            // Lidar com o caso em que o usuário é nulo (ex: mostrar tela de login)
        }
    }
}
*/