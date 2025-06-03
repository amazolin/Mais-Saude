package com.example.maissade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.model.Usuario; // Seu modelo de usuário
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001; // Código de requisição para o Google Sign-In

    private Button btnCadastrar, btnLogin;
    private EditText txtUsername, txtPassword;
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
    private ImageButton btnGoogleSignIn;
    private GoogleSignInClient googleSignInClient;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa componentes
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnLogin = findViewById(R.id.btnLogin);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/").getReference("usuarios");

        // Configurar opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Importante para Firebase Auth
                .requestEmail() // Solicita o e-mail do usuário
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Listener para o botão de cadastro
        btnCadastrar.setOnClickListener(v -> {
            pararMusica(); // Para a música antes de ir para a tela de Cadastro
            startActivity(new Intent(Login.this, Cadastro.class));
        });

        // Listener para o botão de login normal (email/senha)
        btnLogin.setOnClickListener(v -> {
            String email = txtUsername.getText().toString().trim();
            String senha = txtPassword.getText().toString();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(authResult -> {
                        // Após o login bem-sucedido, verifica o status do cadastro
                        verificarESeguir(auth.getCurrentUser());
                    })
                    .addOnFailureListener(e -> Toast.makeText(Login.this, "Erro de login: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Listener para o botão de login com Google
        btnGoogleSignIn.setOnClickListener(v -> signIn());

        // Verifica se o usuário já está logado no Firebase Auth ao iniciar a Activity
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            verificarESeguir(currentUser);
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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Obtém a conta Google do resultado do login
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Preenche os campos com dados da conta Google
                if (account != null) {
                    txtUsername.setText(account.getEmail()); // campo de email
                    txtPassword.setText(account.getDisplayName()); // nome do usuário (só como exemplo)
                }

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
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Verifica se o usuário já existe no seu Realtime Database
                            usuariosRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        // O usuário NÃO existe no Realtime Database, então cria uma nova entrada
                                        String nome = user.getDisplayName();
                                        String email = user.getEmail();
                                        String imagemPerfil = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

                                        // Cria um novo objeto Usuario com os dados disponíveis do Google e valores padrão
                                        // para indicar que o cadastro precisa ser completado.
                                        Usuario newUsuario = new Usuario(nome, email, imagemPerfil);

                                        usuariosRef.child(user.getUid()).setValue(newUsuario)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Login", "Novo usuário cadastrado no Realtime Database via Google: " + user.getUid());
                                                    verificarESeguir(user); // Prossegue para a UI
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Login", "Erro ao cadastrar novo usuário no Realtime Database: " + e.getMessage());
                                                    Toast.makeText(Login.this, "Erro ao cadastrar informações do usuário: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    verificarESeguir(user); // Ainda prossegue para a UI mesmo com erro no banco
                                                });
                                    } else {
                                        // O usuário JÁ existe no Realtime Database
                                        Log.d("Login", "Usuário já existe no Realtime Database: " + user.getUid());
                                        verificarESeguir(user); // Prossegue para a UI
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Login", "Erro ao verificar usuário no Realtime Database: " + error.getMessage());
                                    Toast.makeText(Login.this, "Erro ao verificar dados do usuário: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    verificarESeguir(user); // Ainda prossegue para a UI
                                }
                            });
                        }
                    } else {
                        Toast.makeText(Login.this, "Erro ao autenticar com Google: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Metodo para verificar o status do cadastro do usuário (sem a flag 'cadastroCompleto')
    private void verificarESeguir(FirebaseUser user) {
        if (user == null) {
            return;
        }

        usuariosRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    boolean isCadastroCompleto = false;

                    if (usuario != null) {
                        // Define o que significa "cadastro completo" verificando os campos essenciais.
                        // Assume que valores padrão (0, "Não informado", etc.) indicam incompletude.
                        isCadastroCompleto = (usuario.idade != 0 &&
                                !usuario.sexo.equals("Não informado") &&
                                usuario.peso != 0.0 &&
                                usuario.altura != 0.0 &&
                                !usuario.tipoSanguineo.equals("Não informado") &&
                                usuario.nome != null && !usuario.nome.trim().isEmpty());
                    }

                    if (isCadastroCompleto) {
                        Toast.makeText(Login.this, "Bem-vindo, " + usuario.nome, Toast.LENGTH_SHORT).show();
                        pararMusica();
                        startActivity(new Intent(Login.this, TelaUsuario.class)); // Redireciona para a tela principal
                        finish();
                    } else {
                        // Cadastro incompleto, redireciona para a tela de cadastro para completar
                        Toast.makeText(Login.this, "Seu cadastro está incompleto. Por favor, complete-o.", Toast.LENGTH_LONG).show();
                        pararMusica();
                        Intent intent = new Intent(Login.this, Cadastro.class);
                        // Passa o UID do usuário para a tela de Cadastro para que ela saiba qual usuário está editando
                        intent.putExtra("user_id", user.getUid());
                        startActivity(intent);
                    }
                } else {
                    // Usuário logado no Firebase Auth, mas não há registro correspondente no Realtime Database.
                    // Isso pode acontecer se o usuário se logou via Google pela primeira vez.
                    // Criamos uma entrada inicial e o redirecionamos para o cadastro.
                    String nome = user.getDisplayName();
                    String email = user.getEmail();
                    String imagemPerfil = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

                    Usuario newUsuario = new Usuario(nome, email, imagemPerfil);
                    // Os valores padrão para idade, sexo, peso, altura, tipoSanguineo no construtor
                    // já indicarão que o cadastro está incompleto.

                    usuariosRef.child(user.getUid()).setValue(newUsuario)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Login", "Novo usuário inicializado no DB para complemento: " + user.getUid());
                                Toast.makeText(Login.this, "Bem-vindo! Por favor, complete seu cadastro.", Toast.LENGTH_LONG).show();
                                pararMusica();
                                Intent intent = new Intent(Login.this, Cadastro.class);
                                intent.putExtra("user_id", user.getUid());
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Login", "Erro ao inicializar usuário no DB: " + e.getMessage());
                                Toast.makeText(Login.this, "Erro ao preparar seu cadastro. Tente novamente.", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Login", "Erro ao buscar dados do usuário para verificação: " + error.getMessage());
                Toast.makeText(Login.this, "Erro ao verificar status do cadastro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                // Em caso de erro, por segurança, você pode redirecionar para a tela de cadastro
                pararMusica();
                Intent intent = new Intent(Login.this, Cadastro.class);
                if (user != null) {
                    intent.putExtra("user_id", user.getUid());
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que a música toque ao voltar para a tela de Login
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.fantasy);
            mediaPlayer.setLooping(true); // Toca em loop
            mediaPlayer.start();
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Reinicia se estiver pausada
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
            mediaPlayer.release(); // Libera os recursos do MediaPlayer
            mediaPlayer = null;
        }
    }
}