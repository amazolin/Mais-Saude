package com.example.maissade;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TelaUsuario extends AppCompatActivity {

    private static final String TAG = "TelaUsuario";

    private ImageView imgAvatarPerfil;
    private TextView txtNomePerfil, txtIdadeSexoPerfil, txtPesoPerfil, txtAlturaPerfil, txtTipoSanguineoPerfil, txtIMCPerfil, txtNivelGeralStatus;
    private ProgressBar progressBarMissao;
    private TextView textNivelEstrela;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userNodeReference;
    private ValueEventListener xpListener;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Menu menu;

    private static final int XP_POR_NIVEL = 100;

    private String currentNome;
    private Long currentIdade;
    private Long currentPeso;
    private Long currentAltura;
    private String currentSexo;
    private String currentTipoSanguineo;
    private String currentImagemSelecionada;
    private double currentIMC = 0.0; // Adicionado para armazenar o IMC
    private String currentNivel = ""; // Adicionado para armazenar o nível de XP

    private final int[] avatarResourceIds = {
            R.drawable.avatarone, R.drawable.avatartwo, R.drawable.avatarthree,
            R.drawable.avatarfour, R.drawable.avatarfive, R.drawable.avatarsix,
            R.drawable.avatarseven, R.drawable.avatareight, R.drawable.avatarnine,
            R.drawable.avatarten, R.drawable.avatareleven, R.drawable.avatartwelve,
            R.drawable.avatartirteen,
            R.drawable.avatarfourteen, R.drawable.avatarfiftheen,
            R.drawable.avatarsixteen, R.drawable.avatarseventeen, R.drawable.avatareighteen,
            R.drawable.avatarnineteen, R.drawable.avatartwenty, R.drawable.avatartwentyone,
            R.drawable.avatartwentytwo, R.drawable.avatartwentythree, R.drawable.avatartwentyfour,
            R.drawable.avatartwentyfive, R.drawable.avatartwentysix, R.drawable.avatartwentyseven,
            R.drawable.avatartwentyeight, R.drawable.avatartwentynine
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_usuario);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        personalizarToolbarTitle(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        inicializarViews();
        configurarBottomNavigation();
        configurarMediaPlayer();

        if (progressBarMissao != null) {
            progressBarMissao.setMax(XP_POR_NIVEL);
        }
    }

    private void personalizarToolbarTitle(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (tv.getText() != null && "Perfil".equals(tv.getText().toString())) {
                    Typeface minhaFonte = ResourcesCompat.getFont(this, R.font.aboreto);
                    if (minhaFonte != null) {
                        tv.setTypeface(minhaFonte);
                    }
                    break;
                }
            }
        }
    }

    private void inicializarViews() {
        imgAvatarPerfil = findViewById(R.id.imgAvatarPerfil);
        txtNomePerfil = findViewById(R.id.txtNomePerfil);
        txtIdadeSexoPerfil = findViewById(R.id.txtIdadeSexoPerfil);
        txtPesoPerfil = findViewById(R.id.txtPesoPerfil);
        txtAlturaPerfil = findViewById(R.id.txtAlturaPerfil);
        txtTipoSanguineoPerfil = findViewById(R.id.txtTipoSanguineoPerfil);
        txtIMCPerfil = findViewById(R.id.txtIMCEditar); // Usando o ID do layout da tela de edição, se for para exibir o IMC do perfil aqui. Verifique se é o correto para esta tela.
        txtNivelGeralStatus = findViewById(R.id.txtNivelPerfil); // Usando o ID do layout da tela de edição. Verifique se é o correto para esta tela.
        progressBarMissao = findViewById(R.id.progressBarMissao);
        textNivelEstrela = findViewById(R.id.textNivel);
    }

    private void configurarMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.fantasy);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
        } else {
            Log.e(TAG, "configurarMediaPlayer: Falha ao criar MediaPlayer. Verifique o recurso raw/fantasy.mp3.");
        }
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sono) {
                startActivity(new Intent(this, TelaSono.class));
                return true;
            } else if (id == R.id.agua) {
                startActivity(new Intent(this, TelaAgua.class));
                return true;
            } else if (id == R.id.exercicios) {
                startActivity(new Intent(this, TelaExercicio.class));
                return true;
            } else if (id == R.id.ranking) {
                startActivity(new Intent(this, TelaRanking.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
            removeXpListener();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(currentUser.getUid());

        attachXpListener();

        if (isPlaying && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        atualizarIconeSom();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeXpListener();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void attachXpListener() {
        if (userNodeReference == null) {
            currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                        .getReference("usuarios")
                        .child(currentUser.getUid());
            } else {
                Toast.makeText(this, "Erro de autenticação. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, Login.class));
                finish();
                return;
            }
        }

        if (xpListener == null) {
            xpListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (auth.getCurrentUser() == null) {
                        return;
                    }

                    if (snapshot.exists()) {
                        currentNome = snapshot.child("nome").getValue(String.class);
                        currentSexo = snapshot.child("sexo").getValue(String.class);
                        currentTipoSanguineo = snapshot.child("tipoSanguineo").getValue(String.class);
                        currentIdade = snapshot.child("idade").getValue(Long.class);
                        currentPeso = snapshot.child("peso").getValue(Long.class);
                        currentAltura = snapshot.child("altura").getValue(Long.class);
                        currentImagemSelecionada = snapshot.child("imagemPerfil").getValue(String.class);
                        Long xpAtual = snapshot.child("xp").getValue(Long.class);

                        atualizarAvatar(currentImagemSelecionada);
                        atualizarTextosPerfil(currentNome, currentSexo, currentTipoSanguineo, currentIdade, currentPeso, currentAltura);
                        calcularEAtualizarIMC(currentPeso, currentAltura); // Esta chamada irá atualizar currentIMC
                        atualizarUiXpENivel(xpAtual != null ? xpAtual : 0L); // Esta chamada irá atualizar currentNivel

                    } else {
                        Toast.makeText(TelaUsuario.this, "Perfil do usuário não encontrado. Dados podem estar incompletos.", Toast.LENGTH_SHORT).show();
                        atualizarUiXpENivel(0L);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (auth.getCurrentUser() == null && error.getCode() == DatabaseError.PERMISSION_DENIED) {
                        Log.w(TAG, "onCancelled: Erro de permissão após logout. Mensagem: " + error.getMessage());
                    } else {
                        Toast.makeText(TelaUsuario.this, "Erro ao carregar dados: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            };
            userNodeReference.addValueEventListener(xpListener);
        }
    }

    private void removeXpListener() {
        if (xpListener != null && userNodeReference != null) {
            userNodeReference.removeEventListener(xpListener);
            xpListener = null;
        }
    }

    private void atualizarTextosPerfil(String nome, String sexo, String tipoSanguineo, Long idade, Long peso, Long altura) {
        if (txtNomePerfil != null) txtNomePerfil.setText(nome != null ? nome : "Nome N/D");
        if (txtPesoPerfil != null) txtPesoPerfil.setText(peso != null ? peso + " kg" : "0 kg");
        if (txtAlturaPerfil != null) txtAlturaPerfil.setText(altura != null ? altura + " cm" : "0 cm");

        if (txtIdadeSexoPerfil != null) {
            String idadeText = (idade != null && idade > 0) ? idade + " anos" : "Idade N/D";
            String sexoText = (sexo != null && !sexo.isEmpty()) ? sexo : "";
            if (!sexoText.isEmpty()) {
                txtIdadeSexoPerfil.setText(idadeText + ", " + sexoText);
            } else {
                txtIdadeSexoPerfil.setText(idadeText);
            }
        }

        if (txtTipoSanguineoPerfil != null) txtTipoSanguineoPerfil.setText(tipoSanguineo != null ? tipoSanguineo : "N/D");
    }

    private void atualizarAvatar(String imagemSelecionada) {
        if (imgAvatarPerfil == null) {
            return;
        }

        int defaultAvatar = R.drawable.avatarone;

        if (imagemSelecionada != null && imagemSelecionada.startsWith("avatar_")) {
            try {
                int index = Integer.parseInt(imagemSelecionada.substring(7));
                if (index >= 0 && index < avatarResourceIds.length) {
                    imgAvatarPerfil.setImageResource(avatarResourceIds[index]);
                } else {
                    imgAvatarPerfil.setImageResource(defaultAvatar);
                }
            } catch (NumberFormatException e) {
                imgAvatarPerfil.setImageResource(defaultAvatar);
            }
        } else {
            imgAvatarPerfil.setImageResource(defaultAvatar);
        }
    }

    private void calcularEAtualizarIMC(Long peso, Long altura) {
        if (txtIMCPerfil == null || txtNivelGeralStatus == null) {
            return;
        }

        if (peso != null && altura != null && peso > 0 && altura > 0) {
            double alturaMetros = altura / 100.0;
            double imc = peso / (alturaMetros * alturaMetros);
            txtIMCPerfil.setText(String.format("%.2f", imc));
            this.currentIMC = imc; // <<-- ATUALIZA A VARIÁVEL DE CLASSE currentIMC

            String nivelStatus;
            if (imc < 18.5) {
                nivelStatus = "Abaixo do peso";
            } else if (imc < 24.9) {
                nivelStatus = "Peso normal";
            } else if (imc < 29.9) {
                nivelStatus = "Sobrepeso";
            } else if (imc < 34.9) {
                nivelStatus = "Obesidade Grau I";
            } else if (imc < 39.9) {
                nivelStatus = "Obesidade Grau II (severa)";
            } else {
                nivelStatus = "Obesidade Grau III (mórbida)";
            }
            txtNivelGeralStatus.setText(nivelStatus);
        } else {
            txtIMCPerfil.setText("--");
            txtNivelGeralStatus.setText("Dados incompletos");
            this.currentIMC = 0.0; // Define valor padrão se os dados forem inválidos
        }
    }

    private void atualizarUiXpENivel(long totalXp) {
        if (textNivelEstrela == null || progressBarMissao == null) {
            return;
        }

        progressBarMissao.setMax(XP_POR_NIVEL);

        int nivelAtual = (int) (totalXp / XP_POR_NIVEL) + 1;
        int xpNoNivelAtual = (int) (totalXp % XP_POR_NIVEL);

        textNivelEstrela.setText(String.valueOf(nivelAtual));
        progressBarMissao.setProgress(xpNoNivelAtual);
        this.currentNivel = String.valueOf(nivelAtual); // <<-- ATUALIZA A VARIÁVEL DE CLASSE currentNivel
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_usuario, menu);
        this.menu = menu;
        atualizarIconeSom();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_sobre) {
            mostrarSobre();
            return true;
        } else if (id == R.id.item_apagar_conta) {
            confirmarExclusao();
            return true;
        } else if (id == R.id.item_mutar) {
            alternarMusica();
            return true;
        } else if (id == R.id.item_sair) {
            fazerLogout();
            return true;
        } else if (id == R.id.item_editar) {
            Intent intent = new Intent(TelaUsuario.this, TelaEditarUsuario.class);

            intent.putExtra("nome", currentNome);
            intent.putExtra("idade", currentIdade != null ? currentIdade : 0L);
            intent.putExtra("peso", currentPeso != null ? currentPeso : 0L);
            intent.putExtra("altura", currentAltura != null ? currentAltura : 0L);
            intent.putExtra("sexo", currentSexo);
            intent.putExtra("tipoSanguineo", currentTipoSanguineo);
            intent.putExtra("imagemPerfil", currentImagemSelecionada);

            // Adicionando IMC e Nível
            intent.putExtra("imc", currentIMC);
            intent.putExtra("nivelStatus", txtNivelGeralStatus.getText().toString()); // Nível de XP

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void alternarMusica() {
        if (mediaPlayer == null) {
            configurarMediaPlayer();
            if (mediaPlayer == null) {
                Toast.makeText(this, "Erro ao iniciar áudio.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isPlaying) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
        atualizarIconeSom();
    }

    private void atualizarIconeSom() {
        if (menu != null) {
            MenuItem itemSom = menu.findItem(R.id.item_mutar);
            if (itemSom != null) {
                itemSom.setIcon(isPlaying ? R.drawable.volume : R.drawable.mudo);
            }
        }
    }

    private void mostrarSobre() {
        FrameLayout sobreOverlay = findViewById(R.id.sobre_overlay);
        if (sobreOverlay != null) {
            sobreOverlay.setVisibility(View.VISIBLE);
            sobreOverlay.setOnClickListener(v -> sobreOverlay.setVisibility(View.GONE));
        }
    }

    private void fazerLogout() {
        removeXpListener();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        auth.signOut();
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar conta")
                .setMessage("Tem certeza que deseja apagar sua conta? Essa ação é irreversível. Você irá perder todos os seus itens e XP.")
                .setPositiveButton("Sim", (dialog, which) -> apagarConta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void apagarConta() {
        FirebaseUser userParaApagar = auth.getCurrentUser();

        if (userParaApagar == null) {
            Toast.makeText(this, "Nenhum usuário logado para apagar.", Toast.LENGTH_SHORT).show();
            fazerLogout();
            return;
        }

        final String uid = userParaApagar.getUid();
        removeXpListener();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        DatabaseReference dbUserNodeParaExcluir = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(uid);

        dbUserNodeParaExcluir.removeValue()
                .addOnSuccessListener(aVoid -> {
                    userParaApagar.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(TelaUsuario.this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                Intent intent = new Intent(TelaUsuario.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(TelaUsuario.this, "Erro ao excluir da autenticação: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                fazerLogout();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaUsuario.this, "Erro ao excluir dados do banco: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fazerLogout();
                });
    }
}