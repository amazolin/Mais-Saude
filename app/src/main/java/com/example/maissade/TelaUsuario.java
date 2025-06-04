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

    // Componentes da UI
    private ImageView imgAvatarPerfil;
    private TextView txtNomePerfil, txtIdadeSexoPerfil, txtPesoPerfil, txtAlturaPerfil, txtTipoSanguineoPerfil, txtIMCPerfil, txtNivelGeralStatus;
    private ProgressBar progressBarMissao;
    private TextView textNivelEstrela;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userNodeReference;
    private ValueEventListener xpListener; // Listener para observação de dados do usuário

    // Mídia
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // Estado de reprodução da música
    private Menu menu; // Para gerenciar o ícone de som na Toolbar

    // Constantes
    private static final int XP_POR_NIVEL = 100;

    // Variáveis de classe para armazenar os dados do usuário do Firebase
    // Essas variáveis são essenciais para passar os dados para a TelaEditarUsuario
    private String currentNome;
    private Long currentIdade;
    private Long currentPeso;
    private Long currentAltura;
    private String currentSexo;
    private String currentTipoSanguineo; // Variável para armazenar o tipo sanguíneo
    private String currentImagemSelecionada;

    // Mapeamento de strings de avatar para recursos drawable (precisa ser o mesmo da TelaEditarUsuario)
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
        Log.d(TAG, "onCreate: Activity TelaUsuario criada.");

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
        } else {
            Log.e(TAG, "onCreate: progressBarMissao é NULL após findViewById! Verifique o layout.");
        }
    }

    /**
     * Personaliza o título da Toolbar aplicando uma fonte customizada.
     * @param toolbar A Toolbar a ser personalizada.
     */
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

    /**
     * Inicializa todas as views da interface de usuário.
     */
    private void inicializarViews() {
        Log.d(TAG, "inicializarViews: Iniciando views...");
        imgAvatarPerfil = findViewById(R.id.imgAvatarPerfil);
        txtNomePerfil = findViewById(R.id.txtNomePerfil);
        txtIdadeSexoPerfil = findViewById(R.id.txtIdadeSexoPerfil);
        txtPesoPerfil = findViewById(R.id.txtPesoPerfil);
        txtAlturaPerfil = findViewById(R.id.txtAlturaPerfil);
        txtTipoSanguineoPerfil = findViewById(R.id.txtTipoSanguineoPerfil);
        txtIMCPerfil = findViewById(R.id.txtIMCPerfil);
        txtNivelGeralStatus = findViewById(R.id.txtNivelPerfil); // No XML está como txtNivelPerfil
        progressBarMissao = findViewById(R.id.progressBarMissao);
        textNivelEstrela = findViewById(R.id.textNivel); // No XML está como textNivel
        Log.d(TAG, "inicializarViews: Views inicializadas.");
    }

    /**
     * Configura o MediaPlayer para reprodução de música de fundo.
     */
    private void configurarMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.fantasy); // Assumindo R.raw.fantasy existe
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true); // Faz a música tocar em loop
            Log.d(TAG, "configurarMediaPlayer: MediaPlayer configurado.");
        } else {
            Log.e(TAG, "configurarMediaPlayer: Falha ao criar MediaPlayer. Verifique o recurso raw/fantasy.mp3.");
        }
    }

    /**
     * Configura o listener para a BottomNavigationView.
     */
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
        Log.d(TAG, "onStart: Activity iniciando.");

        currentUser = auth.getCurrentUser(); // Obtém o usuário Firebase atual
        if (currentUser == null) {
            Log.e(TAG, "onStart: Usuário não autenticado. Redirecionando para login.");
            Toast.makeText(this, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
            removeXpListener(); // Garante que o listener seja removido para evitar vazamentos
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Configura a referência ao nó do usuário no Firebase Realtime Database
        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(currentUser.getUid());
        Log.d(TAG, "onStart: Referência do Firebase configurada para: " + userNodeReference.toString());

        attachXpListener(); // Anexa o listener para observar as mudanças no XP e perfil do usuário

        // Retoma a música se estava tocando
        if (isPlaying && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            Log.d(TAG, "onStart: Retomando música.");
            mediaPlayer.start();
        }
        atualizarIconeSom(); // Garante que o ícone de som esteja correto ao retornar
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity parando.");
        removeXpListener(); // Remove o listener quando a Activity não está visível para economizar recursos

        // Pausa a música quando a Activity não está mais visível
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.d(TAG, "onStop: Pausando música.");
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity destruída.");
        // Libera o MediaPlayer para evitar vazamentos de memória
        if (mediaPlayer != null) {
            Log.d(TAG, "onDestroy: Liberando MediaPlayer.");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Anexa um ValueEventListener para observar mudanças nos dados do usuário no Firebase.
     */
    private void attachXpListener() {
        // Verifica se a referência do Firebase está inicializada
        if (userNodeReference == null) {
            Log.e(TAG, "attachXpListener: userNodeReference é null. Tentando re-inicializar.");
            currentUser = auth.getCurrentUser(); // Re-verifica o usuário
            if (currentUser != null) {
                userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                        .getReference("usuarios")
                        .child(currentUser.getUid());
            } else {
                Log.e(TAG, "attachXpListener: currentUser ainda é null após re-verificação. Redirecionando para login.");
                Toast.makeText(this, "Erro de autenticação. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, Login.class));
                finish();
                return;
            }
        }

        // Anexa o listener apenas se ele ainda não estiver anexado
        if (xpListener == null) {
            Log.d(TAG, "attachXpListener: Criando e anexando novo ValueEventListener para " + userNodeReference.toString());
            xpListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Evita processar dados se o usuário deslogou enquanto o listener estava ativo
                    if (auth.getCurrentUser() == null) {
                        Log.w(TAG, "onDataChange: Recebeu dados, mas o usuário não está mais logado. Ignorando.");
                        return;
                    }

                    Log.d(TAG, "onDataChange: Dados recebidos do Firebase. Snapshot: " + snapshot.toString());
                    if (snapshot.exists()) {
                        // Armazena os dados brutos nas variáveis de classe
                        currentNome = snapshot.child("nome").getValue(String.class);
                        currentSexo = snapshot.child("sexo").getValue(String.class);
                        currentTipoSanguineo = snapshot.child("tipoSanguineo").getValue(String.class); // <-- Obtendo o tipo sanguíneo
                        currentIdade = snapshot.child("idade").getValue(Long.class);
                        currentPeso = snapshot.child("peso").getValue(Long.class);
                        currentAltura = snapshot.child("altura").getValue(Long.class);
                        currentImagemSelecionada = snapshot.child("imagemPerfil").getValue(String.class);
                        Long xpAtual = snapshot.child("xp").getValue(Long.class);

                        Log.d(TAG, "onDataChange: Nome=" + currentNome + ", XP Atual=" + xpAtual + ", Tipo Sanguíneo=" + currentTipoSanguineo);

                        // Atualiza a UI com os dados obtidos
                        atualizarAvatar(currentImagemSelecionada);
                        atualizarTextosPerfil(currentNome, currentSexo, currentTipoSanguineo, currentIdade, currentPeso, currentAltura);
                        calcularEAtualizarIMC(currentPeso, currentAltura);
                        atualizarUiXpENivel(xpAtual != null ? xpAtual : 0L);

                    } else {
                        String uid = (currentUser != null) ? currentUser.getUid() : "desconhecido (usuário pode ter deslogado)";
                        Log.w(TAG, "onDataChange: Snapshot não existe para o usuário " + uid + ". Dados do perfil podem estar ausentes.");
                        Toast.makeText(TelaUsuario.this, "Perfil do usuário não encontrado. Dados podem estar incompletos.", Toast.LENGTH_SHORT).show();
                        atualizarUiXpENivel(0L); // Reseta a UI de XP
                        // Considere limpar os campos de texto do perfil aqui ou preencher com "N/D"
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Trata o erro de permissão que pode ocorrer se o usuário deslogar
                    if (auth.getCurrentUser() == null && error.getCode() == DatabaseError.PERMISSION_DENIED) {
                        Log.w(TAG, "onCancelled: Erro de permissão após logout. Mensagem: " + error.getMessage());
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

    /**
     * Remove o ValueEventListener para evitar vazamentos de memória e leituras desnecessárias.
     */
    private void removeXpListener() {
        if (xpListener != null && userNodeReference != null) {
            Log.d(TAG, "removeXpListener: Removendo xpListener de " + userNodeReference.toString());
            userNodeReference.removeEventListener(xpListener);
            xpListener = null; // Limpa a referência para que possa ser recriada
        } else {
            Log.d(TAG, "removeXpListener: xpListener ou userNodeReference é null, nada a remover.");
        }
    }

    /**
     * Atualiza os TextViews do perfil com os dados do usuário.
     */
    private void atualizarTextosPerfil(String nome, String sexo, String tipoSanguineo, Long idade, Long peso, Long altura) {
        if (txtNomePerfil != null) txtNomePerfil.setText(nome != null ? nome : "Nome N/D");
        if (txtPesoPerfil != null) txtPesoPerfil.setText(peso != null ? peso + " kg" : "0 kg");
        if (txtAlturaPerfil != null) txtAlturaPerfil.setText(altura != null ? altura + " cm" : "0 cm");

        // Combina idade e sexo para exibição
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

    /**
     * Atualiza a ImageView do avatar com base na string do avatar selecionado.
     * @param imagemSelecionada A string que representa o avatar (e.g., "avatar_0").
     */
    private void atualizarAvatar(String imagemSelecionada) {
        if (imgAvatarPerfil == null) {
            Log.w(TAG, "atualizarAvatar: imgAvatarPerfil é null. Não é possível atualizar.");
            return;
        }

        int defaultAvatar = R.drawable.avatarone; // Avatar padrão caso ocorra um erro

        if (imagemSelecionada != null && imagemSelecionada.startsWith("avatar_")) {
            try {
                int index = Integer.parseInt(imagemSelecionada.substring(7)); // Extrai o número do "avatar_X"
                if (index >= 0 && index < avatarResourceIds.length) {
                    imgAvatarPerfil.setImageResource(avatarResourceIds[index]);
                } else {
                    Log.w(TAG, "atualizarAvatar: Índice de avatar fora do intervalo (" + index + "). Usando padrão.");
                    imgAvatarPerfil.setImageResource(defaultAvatar);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "atualizarAvatar: Erro ao analisar índice de imagemSelecionada: " + imagemSelecionada, e);
                imgAvatarPerfil.setImageResource(defaultAvatar);
            }
        } else {
            Log.w(TAG, "atualizarAvatar: imagemSelecionada inválida ou nula: " + imagemSelecionada + ". Usando avatar padrão.");
            imgAvatarPerfil.setImageResource(defaultAvatar);
        }
    }

    /**
     * Calcula e atualiza o IMC e o status geral com base no peso e altura.
     */
    private void calcularEAtualizarIMC(Long peso, Long altura) {
        if (txtIMCPerfil == null || txtNivelGeralStatus == null) {
            Log.w(TAG, "calcularEAtualizarIMC: txtIMCPerfil ou txtNivelGeralStatus é null. Não é possível calcular.");
            return;
        }

        if (peso != null && altura != null && peso > 0 && altura > 0) {
            double alturaMetros = altura / 100.0; // Converte cm para metros
            double imc = peso / (alturaMetros * alturaMetros);
            txtIMCPerfil.setText(String.format("%.2f", imc)); // Formata para 2 casas decimais

            String nivelStatus;
            if (imc < 18.5) {
                nivelStatus = "Abaixo do peso";
            } else if (imc < 24.9) {
                nivelStatus = "Peso normal";
            } else if (imc < 29.9) {
                nivelStatus = "Sobrepeso"; // (Acima do peso - Grau I)
            } else if (imc < 34.9) {
                nivelStatus = "Obesidade Grau I";
            } else if (imc < 39.9) {
                nivelStatus = "Obesidade Grau II (severa)";
            } else {
                nivelStatus = "Obesidade Grau III (mórbida)";
            }
            txtNivelGeralStatus.setText(nivelStatus);
        } else {
            txtIMCPerfil.setText("--"); // Valores padrão se os dados forem inválidos/ausentes
            txtNivelGeralStatus.setText("Dados incompletos");
        }
    }

    /**
     * Atualiza a barra de progresso de XP e o nível do usuário.
     * @param totalXp O total de pontos de experiência do usuário.
     */
    private void atualizarUiXpENivel(long totalXp) {
        Log.d(TAG, "atualizarUiXpENivel: totalXp = " + totalXp);

        if (textNivelEstrela == null || progressBarMissao == null) {
            Log.e(TAG, "atualizarUiXpENivel: textNivelEstrela ou progressBarMissao é NULL. Verifique o layout.");
            return;
        }

        progressBarMissao.setMax(XP_POR_NIVEL); // Define o máximo da barra para o XP necessário para o próximo nível

        int nivelAtual = (int) (totalXp / XP_POR_NIVEL) + 1; // Calcula o nível atual
        int xpNoNivelAtual = (int) (totalXp % XP_POR_NIVEL); // Calcula o XP dentro do nível atual

        textNivelEstrela.setText(String.valueOf(nivelAtual));
        progressBarMissao.setProgress(xpNoNivelAtual);

        Log.d(TAG, "atualizarUiXpENivel: UI de XP atualizada. Nível: " + nivelAtual + ", Progresso: " + xpNoNivelAtual);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_usuario, menu);
        this.menu = menu; // Armazena a referência do menu
        atualizarIconeSom(); // Atualiza o ícone de som ao criar o menu
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
            // AQUI É ONDE TODOS OS DADOS SÃO PASSADOS PARA A TELA DE EDIÇÃO
            Intent intent = new Intent(TelaUsuario.this, TelaEditarUsuario.class);

            // Passa TODOS os dados do usuário usando as variáveis de classe
            // É crucial que essas variáveis (currentNome, currentIdade, etc.)
            // estejam preenchidas pelo ValueEventListener antes de chamar esta Intent.
            // Para garantir que não enviamos `null`, usamos operadores ternários ou
            // valores padrão (`0L` para Long) para tipos primitivos.
            intent.putExtra("nome", currentNome);
            intent.putExtra("idade", currentIdade != null ? currentIdade : 0L);
            intent.putExtra("peso", currentPeso != null ? currentPeso : 0L);
            intent.putExtra("altura", currentAltura != null ? currentAltura : 0L);
            intent.putExtra("sexo", currentSexo);
            intent.putExtra("tipoSanguineo", currentTipoSanguineo); // <-- ESTE É O CAMPO QUE FOI VERIFICADO!
            intent.putExtra("imagemPerfil", currentImagemSelecionada);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Alterna o estado de reprodução da música de fundo (tocar/pausar).
     */
    private void alternarMusica() {
        if (mediaPlayer == null) {
            Log.w(TAG, "alternarMusica: MediaPlayer é null. Tentando recriar.");
            configurarMediaPlayer(); // Tenta recriar o MediaPlayer
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
        atualizarIconeSom(); // Atualiza o ícone de som na Toolbar
    }

    /**
     * Atualiza o ícone de som na Toolbar com base no estado de reprodução da música.
     */
    private void atualizarIconeSom() {
        if (menu != null) {
            MenuItem itemSom = menu.findItem(R.id.item_mutar);
            if (itemSom != null) {
                itemSom.setIcon(isPlaying ? R.drawable.volume : R.drawable.mudo); // Assumindo drawables 'volume' e 'mudo'
            }
        }
    }

    /**
     * Mostra a tela de sobreposição "Sobre".
     */
    private void mostrarSobre() {
        FrameLayout sobreOverlay = findViewById(R.id.sobre_overlay);
        if (sobreOverlay != null) {
            sobreOverlay.setVisibility(View.VISIBLE);
            // Define um clique para fechar a sobreposição ao tocar fora
            sobreOverlay.setOnClickListener(v -> sobreOverlay.setVisibility(View.GONE));
        } else {
            Log.e(TAG, "mostrarSobre: sobre_overlay é NULL. Verifique o layout.");
        }
    }

    /**
     * Realiza o logout do usuário e redireciona para a tela de Login.
     */
    private void fazerLogout() {
        Log.d(TAG, "fazerLogout: Iniciando processo de logout...");
        removeXpListener(); // Remove o listener antes de deslogar
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.d(TAG, "fazerLogout: Parando música.");
            mediaPlayer.stop();
        }
        Log.d(TAG, "fazerLogout: Chamando auth.signOut().");
        auth.signOut(); // Desloga do Firebase Auth
        Log.d(TAG, "fazerLogout: Navegando para LoginActivity e finalizando TelaUsuario.");
        Intent intent = new Intent(this, Login.class);
        // Limpa a pilha de atividades para que o usuário não possa voltar para TelaUsuario
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finaliza a atividade atual
    }

    /**
     * Exibe um diálogo de confirmação antes de apagar a conta.
     */
    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar conta")
                .setMessage("Tem certeza que deseja apagar sua conta? Essa ação é irreversível. Você irá perder todos os seus itens e XP.")
                .setPositiveButton("Sim", (dialog, which) -> apagarConta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Apaga a conta do usuário do Firebase Authentication e do Realtime Database.
     */
    private void apagarConta() {
        Log.d(TAG, "apagarConta: Iniciando processo de exclusão da conta.");
        FirebaseUser userParaApagar = auth.getCurrentUser();

        if (userParaApagar == null) {
            Log.w(TAG, "apagarConta: Nenhum usuário logado para apagar. Redirecionando para Login.");
            Toast.makeText(this, "Nenhum usuário logado para apagar.", Toast.LENGTH_SHORT).show();
            fazerLogout(); // Se não há usuário, apenas desloga
            return;
        }

        final String uid = userParaApagar.getUid();
        removeXpListener(); // Remove o listener antes de apagar para evitar callbacks

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        // Primeiro, apaga os dados do usuário do Realtime Database
        DatabaseReference dbUserNodeParaExcluir = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(uid);

        dbUserNodeParaExcluir.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "apagarConta: Dados do usuário removidos do Realtime Database com sucesso.");
                    // Depois de apagar os dados, apaga o usuário da Autenticação
                    userParaApagar.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "apagarConta: Usuário removido da Autenticação com sucesso.");
                                Toast.makeText(TelaUsuario.this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show();
                                auth.signOut(); // Garante que o usuário está deslogado
                                // Redireciona para a tela de Login e limpa a pilha de atividades
                                Intent intent = new Intent(TelaUsuario.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "apagarConta: Erro ao excluir usuário da Autenticação.", e);
                                Toast.makeText(TelaUsuario.this, "Erro ao excluir da autenticação: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                fazerLogout(); // Se a exclusão falhar, desloga para evitar estado inconsistente
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "apagarConta: Erro ao excluir dados do Realtime Database.", e);
                    Toast.makeText(TelaUsuario.this, "Erro ao excluir dados do banco: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fazerLogout(); // Se a exclusão falhar, desloga para evitar estado inconsistente
                });
    }
}