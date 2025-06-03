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
    private FirebaseUser currentUser; // Pode ser atualizado, então não confie nele cegamente após onStart
    private DatabaseReference userNodeReference;
    private ValueEventListener xpListener;
    private MediaPlayer mediaPlayer;

    private boolean isPlaying = false;
    private Menu menu; // Para o ícone de som

    private static final int XP_POR_NIVEL = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_usuario);
        Log.d(TAG, "onCreate: Activity Criada");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        personalizarToolbarTitle(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        // currentUser será verificado e configurado em onStart ou quando necessário

        inicializarViews();
        configurarBottomNavigation();
        configurarMediaPlayer();

        if (progressBarMissao != null) {
            progressBarMissao.setMax(XP_POR_NIVEL);
        } else {
            Log.e(TAG, "onCreate: progressBarMissao é NULL após findViewById!");
        }
        // Teste de UI de XP removido do onCreate para simplificar,
        // será atualizado pelo listener real em onStart/onDataChange
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
        Log.d(TAG, "inicializarViews: Iniciando views...");
        imgAvatarPerfil = findViewById(R.id.imgAvatarPerfil);
        txtNomePerfil = findViewById(R.id.txtNomePerfil);
        txtIdadeSexoPerfil = findViewById(R.id.txtIdadeSexoPerfil);
        txtPesoPerfil = findViewById(R.id.txtPesoPerfil);
        txtAlturaPerfil = findViewById(R.id.txtAlturaPerfil);
        txtTipoSanguineoPerfil = findViewById(R.id.txtTipoSanguineoPerfil);
        txtIMCPerfil = findViewById(R.id.txtIMCPerfil);
        txtNivelGeralStatus = findViewById(R.id.txtNivelPerfil);
        progressBarMissao = findViewById(R.id.progressBarMissao);
        textNivelEstrela = findViewById(R.id.textNivel);
        Log.d(TAG, "inicializarViews: Views inicializadas.");
    }

    private void configurarMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.fantasy);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            Log.d(TAG, "configurarMediaPlayer: MediaPlayer configurado.");
        } else {
            Log.e(TAG, "configurarMediaPlayer: Falha ao criar MediaPlayer.");
        }
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // Usando if-else if para melhor legibilidade e eficiência
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
        Log.d(TAG, "onStart: Activity iniciando.");

        currentUser = auth.getCurrentUser(); // Atualiza o currentUser
        if (currentUser == null) {
            Log.e(TAG, "onStart: Usuário não autenticado. Redirecionando para login.");
            Toast.makeText(this, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
            // Remove qualquer listener que possa ter sido anexado erroneamente
            removeXpListener();
            startActivity(new Intent(this, Login.class));
            finish();
            return; // Importante para não continuar a execução de onStart
        }

        // Configura a referência do nó do usuário APENAS se o usuário estiver autenticado
        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(currentUser.getUid());
        Log.d(TAG, "onStart: Referência do Firebase configurada para: " + userNodeReference.toString());

        attachXpListener(); // Anexa o listener para dados do usuário

        if (isPlaying && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            Log.d(TAG, "onStart: Retomando música.");
            mediaPlayer.start();
        }
        atualizarIconeSom(); // Garante que o ícone esteja correto ao iniciar/retornar
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity parando.");
        removeXpListener(); // Remove o listener

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.d(TAG, "onStop: Pausando música.");
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity destruída.");
        // O listener já deve ter sido removido em onStop
        // Libera o MediaPlayer
        if (mediaPlayer != null) {
            Log.d(TAG, "onDestroy: Liberando MediaPlayer.");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void attachXpListener() {
        // Garante que userNodeReference não seja null
        if (userNodeReference == null) {
            Log.e(TAG, "attachXpListener: userNodeReference é null. Não é possível anexar listener.");
            // Tenta obter o usuário atual novamente, caso algo tenha mudado
            currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                        .getReference("usuarios")
                        .child(currentUser.getUid());
            } else {
                // Se ainda for nulo, redireciona para login, pois não há como prosseguir
                Log.e(TAG, "attachXpListener: currentUser ainda é null após re-verificação. Redirecionando para login.");
                Toast.makeText(this, "Erro de autenticação. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, Login.class));
                finish();
                return;
            }
        }

        if (xpListener == null) {
            Log.d(TAG, "attachXpListener: Criando e anexando novo ValueEventListener para " + userNodeReference.toString());
            xpListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Verifica se o usuário ainda está logado ANTES de processar os dados
                    // Isso é uma dupla checagem, útil se o listener demorar para ser removido em um logout rápido
                    if (auth.getCurrentUser() == null) {
                        Log.w(TAG, "onDataChange: Recebeu dados, mas o usuário não está mais logado. Ignorando.");
                        return;
                    }

                    Log.d(TAG, "onDataChange: Dados recebidos do Firebase. Snapshot: " + snapshot.toString());
                    if (snapshot.exists()) {
                        Log.d(TAG, "onDataChange: Snapshot existe.");
                        String nome = snapshot.child("nome").getValue(String.class);
                        String sexo = snapshot.child("sexo").getValue(String.class);
                        String tipoSanguineo = snapshot.child("tipoSanguineo").getValue(String.class);
                        Long idadeLong = snapshot.child("idade").getValue(Long.class);
                        Long pesoLong = snapshot.child("peso").getValue(Long.class);
                        Long alturaLong = snapshot.child("altura").getValue(Long.class);
                        String imagemSelecionada = snapshot.child("imagemPerfil").getValue(String.class);
                        Long xpAtual = snapshot.child("xp").getValue(Long.class);

                        Log.d(TAG, "onDataChange: Nome=" + nome + ", XP Atual=" + xpAtual);

                        atualizarAvatar(imagemSelecionada);
                        atualizarTextosPerfil(nome, sexo, tipoSanguineo, idadeLong, pesoLong, alturaLong);
                        calcularEAtualizarIMC(pesoLong, alturaLong);
                        atualizarUiXpENivel(xpAtual != null ? xpAtual : 0L);

                    } else {
                        // Verifica se currentUser não é nulo antes de usar o UID no log
                        String uid = (currentUser != null) ? currentUser.getUid() : "desconhecido (usuário pode ter deslogado)";
                        Log.w(TAG, "onDataChange: Snapshot não existe para o usuário " + uid);
                        Toast.makeText(TelaUsuario.this, "Perfil do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                        atualizarUiXpENivel(0L);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Verifica se o usuário ainda está logado ANTES de mostrar o erro
                    // Se o erro for por permissão devido a logout, esta mensagem pode ser mais informativa
                    if (auth.getCurrentUser() == null && error.getCode() == DatabaseError.PERMISSION_DENIED) {
                        Log.w(TAG, "onCancelled: Erro de permissão após logout. Mensagem: " + error.getMessage());
                        // Não mostra Toast para o usuário neste caso, pois é esperado durante o logout.
                    } else {
                        Log.e(TAG, "onCancelled: Erro ao carregar dados do Firebase: " + error.getMessage(), error.toException());
                        Toast.makeText(TelaUsuario.this, "Erro ao carregar dados: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            };
            userNodeReference.addValueEventListener(xpListener);
        } else {
            Log.d(TAG, "attachXpListener: xpListener já existe. Não recriando.");
        }
    }

    private void removeXpListener() {
        if (xpListener != null && userNodeReference != null) {
            Log.d(TAG, "removeXpListener: Removendo xpListener de " + userNodeReference.toString());
            userNodeReference.removeEventListener(xpListener);
            xpListener = null;
        } else {
            Log.d(TAG, "removeXpListener: xpListener ou userNodeReference é null, nada a remover.");
        }
        // Definir userNodeReference como null também pode ser útil para evitar usá-lo acidentalmente
        // userNodeReference = null; // Opcional, mas pode ajudar a pegar erros mais cedo
    }


    private void atualizarTextosPerfil(String nome, String sexo, String tipoSanguineo, Long idade, Long peso, Long altura) {
        if (txtNomePerfil != null) txtNomePerfil.setText(nome != null ? nome : "N/D");
        if (txtPesoPerfil != null) txtPesoPerfil.setText(peso != null ? peso + " kg" : "0 kg");
        if (txtAlturaPerfil != null) txtAlturaPerfil.setText(altura != null ? altura + " cm" : "0 cm");
        if (txtIdadeSexoPerfil != null) txtIdadeSexoPerfil.setText(
                (idade != null ? idade + " anos" : "Idade N/D") + (sexo != null ? ", " + sexo : "")
        );
        if (txtTipoSanguineoPerfil != null) txtTipoSanguineoPerfil.setText(tipoSanguineo != null ? tipoSanguineo : "N/D");
    }


    private void atualizarAvatar(String imagemSelecionada) {
        if (imgAvatarPerfil == null) {
            Log.w(TAG, "atualizarAvatar: imgAvatarPerfil é null.");
            return;
        }
        int[] avatarIds = {
                R.drawable.avatarone, R.drawable.avatartwo, R.drawable.avatarthree,
                R.drawable.avatarfour, R.drawable.avatarfive, R.drawable.avatarsix,
                R.drawable.avatarseven, R.drawable.avatareight, R.drawable.avatarnine,
                R.drawable.avatarten, R.drawable.avatareleven, R.drawable.avatartwelve,
                R.drawable.avatartirteen, R.drawable.avatarfourteen, R.drawable.avatarfiftheen,
                R.drawable.avatarsixteen, R.drawable.avatarseventeen, R.drawable.avatareighteen,
                R.drawable.avatarnineteen, R.drawable.avatartwenty, R.drawable.avatartwentyone,
                R.drawable.avatartwentytwo, R.drawable.avatartwentythree, R.drawable.avatartwentyfour,
                R.drawable.avatartwentyfive, R.drawable.avatartwentysix, R.drawable.avatartwentyseven,
                R.drawable.avatartwentyeight, R.drawable.avatartwentynine
        };

        int defaultAvatar = R.drawable.avatarone;

        if (imagemSelecionada != null && imagemSelecionada.startsWith("avatar_")) {
            try {
                int index = Integer.parseInt(imagemSelecionada.substring(7)); // pega o número depois de "avatar_"
                if (index >= 0 && index < avatarIds.length) {
                    imgAvatarPerfil.setImageResource(avatarIds[index]);
                } else {
                    Log.w(TAG, "atualizarAvatar: índice fora do intervalo. Usando padrão.");
                    imgAvatarPerfil.setImageResource(defaultAvatar);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "atualizarAvatar: erro ao analisar índice de imagemSelecionada: " + imagemSelecionada, e);
                imgAvatarPerfil.setImageResource(defaultAvatar);
            }
        } else {
            Log.w(TAG, "atualizarAvatar: imagemSelecionada inválida: " + imagemSelecionada + ". Usando avatar padrão.");
            imgAvatarPerfil.setImageResource(defaultAvatar);
        }
    }

    private void calcularEAtualizarIMC(Long peso, Long altura) {
        if (txtIMCPerfil == null || txtNivelGeralStatus == null) {
            Log.w(TAG, "calcularEAtualizarIMC: txtIMCPerfil ou txtNivelGeralStatus é null.");
            return;
        }

        if (peso != null && altura != null && peso > 0 && altura > 0) {
            double alturaMetros = altura / 100.0;
            double imc = peso / (alturaMetros * alturaMetros);
            txtIMCPerfil.setText(String.format("%.2f", imc));

            String nivelStatus;
            if (imc < 18.5) nivelStatus = "Abaixo do peso";
            else if (imc < 24.9) nivelStatus = "Peso normal";
            else if (imc < 29.9) nivelStatus = "Acima do peso";
            else nivelStatus = "Obesidade";
            txtNivelGeralStatus.setText(nivelStatus);
        } else {
            txtIMCPerfil.setText("--");
            txtNivelGeralStatus.setText("Dados incompletos");
        }
    }

    private void atualizarUiXpENivel(long totalXp) {
        Log.d(TAG, "atualizarUiXpENivel: totalXp = " + totalXp);

        if (textNivelEstrela == null || progressBarMissao == null) {
            Log.e(TAG, "atualizarUiXpENivel: textNivelEstrela ou progressBarMissao é NULL.");
            return;
        }

        // Garante que o máximo da barra de progresso é 100
        progressBarMissao.setMax(XP_POR_NIVEL);

        // Cálculo do nível e do XP dentro do nível atual
        int nivelAtual = (int) (totalXp / XP_POR_NIVEL) + 1;
        int xpNoNivelAtual = (int) (totalXp % XP_POR_NIVEL);

        // Atualiza UI
        textNivelEstrela.setText(String.valueOf(nivelAtual));
        progressBarMissao.setProgress(xpNoNivelAtual);

        Log.d(TAG, "atualizarUiXpENivel: UI de XP atualizada. Nível: " + nivelAtual + ", Progresso: " + xpNoNivelAtual);
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
            fazerLogout(); // Renomeado para evitar conflito com o nome da classe Login
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void alternarMusica() {
        if (mediaPlayer == null) {
            Log.w(TAG, "alternarMusica: MediaPlayer é null, tentando recriar.");
            configurarMediaPlayer();
            if (mediaPlayer == null) {
                Toast.makeText(this, "Erro ao iniciar áudio.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isPlaying) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Log.d(TAG, "alternarMusica: Música pausada.");
            }
            isPlaying = false;
        } else {
            mediaPlayer.start();
            Log.d(TAG, "alternarMusica: Música iniciada.");
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

    // Renomeado para fazerLogout para clareza
    private void fazerLogout() {
        Log.d(TAG, "fazerLogout: Iniciando processo de logout...");

        // 1. Remover o listener ANTES de deslogar
        removeXpListener();

        // 2. Parar a música
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.d(TAG, "fazerLogout: Parando música.");
            mediaPlayer.stop();
            // Considerar mediaPlayer.release() aqui também se não for mais usar nesta sessão da activity
        }

        // 3. Deslogar o usuário
        Log.d(TAG, "fazerLogout: Chamando auth.signOut().");
        auth.signOut();

        // 4. Navegar para a tela de Login e finalizar esta activity
        Log.d(TAG, "fazerLogout: Navegando para LoginActivity e finalizando TelaUsuario.");
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
        Log.d(TAG, "apagarConta: Iniciando processo de exclusão da conta.");
        FirebaseUser userParaApagar = auth.getCurrentUser(); // Pega o usuário atual ANTES de remover listener e deslogar

        if (userParaApagar == null) {
            Log.w(TAG, "apagarConta: Nenhum usuário logado para apagar. Redirecionando para Login.");
            Toast.makeText(this, "Nenhum usuário logado para apagar.", Toast.LENGTH_SHORT).show();
            fazerLogout(); // Usa o método de logout para uma saída limpa
            return;
        }

        String uid = userParaApagar.getUid();

        // 1. Remover o listener do Firebase para evitar qualquer callback durante a exclusão
        removeXpListener();

        // 2. Parar a música
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        // 3. Remover dados do Realtime Database
        DatabaseReference dbUserNodeParaExcluir = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(uid);

        dbUserNodeParaExcluir.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "apagarConta: Dados do usuário removidos do Realtime Database com sucesso.");
                    // 4. Excluir o usuário da Autenticação
                    userParaApagar.delete() // Agora userParaApagar é o usuário que estava logado
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "apagarConta: Usuário removido da Autenticação com sucesso.");
                                Toast.makeText(TelaUsuario.this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show();
                                // 5. Deslogar explicitamente (embora user.delete() já faça isso) e navegar
                                auth.signOut(); // Garante o estado de logout
                                Intent intent = new Intent(TelaUsuario.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "apagarConta: Erro ao excluir usuário da Autenticação.", e);
                                Toast.makeText(TelaUsuario.this, "Erro ao excluir da autenticação: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                // Mesmo em falha, tentar deslogar e ir para login pode ser uma boa ideia
                                fazerLogout();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "apagarConta: Erro ao excluir dados do Realtime Database.", e);
                    Toast.makeText(TelaUsuario.this, "Erro ao excluir dados do banco: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Mesmo em falha, tentar deslogar e ir para login pode ser uma boa ideia
                    fazerLogout();
                });
    }
}