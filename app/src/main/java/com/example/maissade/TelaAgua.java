package com.example.maissade; // Substitua pelo seu pacote

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TelaAgua extends AppCompatActivity {

    private static final String TAG = "TelaAgua";

    // Constantes para a lógica de XP da água
    private static final int XP_BASE_AGUA_2100ML = 20;
    private static final int XP_EXTRA_POR_LITRO_AGUA = 5;
    private static final int MARCO_INICIAL_ML = 2100;
    private static final int INCREMENTO_LITRO_ML = 1000;

    private TextView tvMeta;
    private TextView tvQuantidadeIngerida;
    private ProgressBar progressBar;
    private EditText edtQuantiaAgua;
    private Button btn100ml;
    private Button btn250ml;
    private Button btnAdicionar;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userNodeReference;
    private String userNodePathString; // Para logar o caminho sem chamar getPath() repetidamente

    private int metaAguaMl = 2000;
    private int quantidadeIngeridaHojeFirebase = 0;
    private int xpGanhoAguaHojeFirebase = 0;
    private long xpTotalUsuario = 0;

    // !!!!! IMPORTANTE: SUBSTITUA PELA URL REAL DO SEU BANCO DE DADOS !!!!!
    private static final String FIREBASE_DATABASE_URL = "https://mais-saude-21343-default-rtdb.firebaseio.com/";
    // Exemplo: "https://SEU-PROJETO-ID-default-rtdb.firebaseio.com/"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_agua); // Certifique-se que o nome do layout está correto

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvMeta = findViewById(R.id.tvMeta);
        tvQuantidadeIngerida = findViewById(R.id.tvQuantidadeIngerida);
        progressBar = findViewById(R.id.progressBar);
        edtQuantiaAgua = findViewById(R.id.edtQuantiaAgua);
        btn100ml = findViewById(R.id.btn100ml);
        btn250ml = findViewById(R.id.btn250ml);
        btnAdicionar = findViewById(R.id.btnAdicionar);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "onCreate: Usuário NÃO AUTENTICADO. A tela não pode funcionar.");
            Toast.makeText(this, "Usuário não autenticado. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "onCreate: Usuário autenticado: " + currentUser.getUid());

        if (FIREBASE_DATABASE_URL.equals("COLOQUE_A_URL_DO_SEU_FIREBASE_DATABASE_AQUI") || FIREBASE_DATABASE_URL.isEmpty() || !FIREBASE_DATABASE_URL.startsWith("https://")) {
            Log.e(TAG, "onCreate: URL do Firebase Database NÃO CONFIGURADA CORRETAMENTE. Verifique a constante FIREBASE_DATABASE_URL.");
            Toast.makeText(this, "Erro de configuração do app. Contate o suporte.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            String uid = currentUser.getUid();
            userNodePathString = "usuarios/" + uid; // Constrói o caminho como string
            userNodeReference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                    .getReference(userNodePathString);
            Log.d(TAG, "onCreate: Referência do Firebase configurada para o caminho: " + userNodePathString);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Erro ao obter instância do Firebase Database. Verifique a URL e as dependências.", e);
            Toast.makeText(this, "Erro ao conectar com o banco de dados.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        tvMeta.setText("Meta de ingestão: " + metaAguaMl + "ml");
        progressBar.setMax(metaAguaMl);

        carregarDadosDoFirebase();

        btn100ml.setOnClickListener(v -> processarAdicaoAgua(100));
        btn250ml.setOnClickListener(v -> processarAdicaoAgua(250));
        btnAdicionar.setOnClickListener(v -> {
            String quantiaStr = edtQuantiaAgua.getText().toString().trim();
            if (!TextUtils.isEmpty(quantiaStr)) {
                try {
                    int quantia = Integer.parseInt(quantiaStr);
                    if (quantia > 0) {
                        processarAdicaoAgua(quantia);
                        edtQuantiaAgua.setText("");
                    } else {
                        Toast.makeText(TelaAgua.this, "Quantidade deve ser maior que zero.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(TelaAgua.this, "Número inválido.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TelaAgua.this, "Digite a quantidade.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDica = findViewById(R.id.button_dicaagua);
        View dicaOverlay = findViewById(R.id.dica_overlay);
        if (btnDica != null && dicaOverlay != null) {
            btnDica.setOnClickListener(v -> dicaOverlay.setVisibility(View.VISIBLE));
            dicaOverlay.setOnClickListener(v -> dicaOverlay.setVisibility(View.GONE));
        } else {
            Log.w(TAG, "onCreate: Botão de dica ou overlay não encontrado no layout.");
        }
    }

    private String getHojeFormatado() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void carregarDadosDoFirebase() {
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "carregarDadosDoFirebase: Usuário se tornou NULO antes de carregar dados.");
            return;
        }
        if (userNodeReference == null) {
            Log.e(TAG, "carregarDadosDoFirebase: userNodeReference é NULO.");
            return;
        }

        Log.d(TAG, "carregarDadosDoFirebase: Iniciando leitura para UID: " + currentUser.getUid() + " em " + userNodePathString);

        userNodeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "carregarDadosDoFirebase: Snapshot recebido. Dados existem.");
                    // Correção: xpFromDB declarado aqui
                    Long xpFromDB = snapshot.child("xp").getValue(Long.class);
                    xpTotalUsuario = (xpFromDB != null) ? xpFromDB : 0L;

                    DataSnapshot aguaConsumoSnapshot = snapshot.child("aguaConsumoDiario");
                    String dataSalva = aguaConsumoSnapshot.child("data").getValue(String.class);
                    String hojeFormatado = getHojeFormatado();

                    if (hojeFormatado.equals(dataSalva) && aguaConsumoSnapshot.exists()) { // Verifica se aguaConsumoSnapshot existe antes de usar
                        Long consumidoHojeDB = aguaConsumoSnapshot.child("totalMlConsumidoHoje").getValue(Long.class);
                        Long xpAguaHojeDB = aguaConsumoSnapshot.child("xpGanhoAguaHoje").getValue(Long.class);
                        quantidadeIngeridaHojeFirebase = (consumidoHojeDB != null) ? consumidoHojeDB.intValue() : 0;
                        xpGanhoAguaHojeFirebase = (xpAguaHojeDB != null) ? xpAguaHojeDB.intValue() : 0;
                        Log.d(TAG, "carregarDadosDoFirebase: Dados de água para HOJE ("+hojeFormatado+") carregados: " + quantidadeIngeridaHojeFirebase + "ml, " + xpGanhoAguaHojeFirebase + "xp.");
                    } else {
                        Log.d(TAG, "carregarDadosDoFirebase: Data no Firebase (" + dataSalva + ") é diferente de hoje (" + hojeFormatado + ") ou nó aguaConsumoDiario não existe. Resetando contadores diários.");
                        quantidadeIngeridaHojeFirebase = 0;
                        xpGanhoAguaHojeFirebase = 0;
                    }
                    Log.d(TAG, "carregarDadosDoFirebase: XP Total Usuário final=" + xpTotalUsuario);
                } else {
                    Log.w(TAG, "carregarDadosDoFirebase: Nó do usuário NÃO ENCONTRADO no Firebase. Path: " + userNodePathString + ". Será tratado como novo usuário para esta tela.");
                    xpTotalUsuario = 0L;
                    quantidadeIngeridaHojeFirebase = 0;
                    xpGanhoAguaHojeFirebase = 0;
                }
                atualizarUIComDadosDoFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "carregarDadosDoFirebase: Erro ao carregar dados do Firebase (onCancelled): " + error.getMessage(), error.toException());
                Toast.makeText(TelaAgua.this, "Erro ao carregar dados. Verifique a conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarUIComDadosDoFirebase() {
        tvQuantidadeIngerida.setText(quantidadeIngeridaHojeFirebase + " ml");
        progressBar.setProgress(Math.min(quantidadeIngeridaHojeFirebase, metaAguaMl));
        Log.d(TAG, "atualizarUIComDadosDoFirebase: UI atualizada com " + quantidadeIngeridaHojeFirebase + "ml.");
    }

    private void processarAdicaoAgua(int quantidadeAdicionadaMl) {
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "processarAdicaoAgua - Tentativa de ADIÇÃO com usuário NULO ANTES da leitura.");
            Toast.makeText(TelaAgua.this, "Erro crítico: Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }
        if (userNodeReference == null) {
            Log.e(TAG, "processarAdicaoAgua: userNodeReference é NULO.");
            return;
        }

        Log.d(TAG, "processarAdicaoAgua: Iniciando para " + quantidadeAdicionadaMl + "ml. Usuário: " + currentUser.getUid());

        userNodeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser localCurrentUser = firebaseAuth.getCurrentUser();
                if (localCurrentUser == null) {
                    Log.e(TAG, "processarAdicaoAgua - onDataChange: Usuário se tornou NULO DURANTE a leitura. Abortando escrita.");
                    Toast.makeText(TelaAgua.this, "Erro: Sessão expirou durante a operação.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TelaAgua.this.currentUser == null || !TelaAgua.this.currentUser.getUid().equals(localCurrentUser.getUid())) {
                    Log.w(TAG, "processarAdicaoAgua - onDataChange: currentUser global mudou ou era nulo. Usando localCurrentUser recém-obtido: " + localCurrentUser.getUid());
                    TelaAgua.this.currentUser = localCurrentUser;
                    // Se o UID mudou, precisamos ATUALIZAR userNodeReference e userNodePathString também!
                    String newUid = localCurrentUser.getUid();
                    userNodePathString = "usuarios/" + newUid;
                    userNodeReference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference(userNodePathString);
                    Log.i(TAG, "processarAdicaoAgua - onDataChange: userNodeReference atualizado para: " + userNodePathString);
                }

                long xpTotalUsuarioAntesDaAdicao = 0L;
                // Correção: xpFromDB declarado aqui
                Long xpFromDB = null;
                if (snapshot.child("xp").exists()) {
                    xpFromDB = snapshot.child("xp").getValue(Long.class);
                    xpTotalUsuarioAntesDaAdicao = (xpFromDB != null) ? xpFromDB : 0L;
                } else {
                    Log.w(TAG, "processarAdicaoAgua - onDataChange: Nó 'xp' não encontrado para usuário " + localCurrentUser.getUid() + ". Considerando XP inicial como 0.");
                }


                DataSnapshot aguaConsumoSnapshot = snapshot.child("aguaConsumoDiario");
                String dataSalva = aguaConsumoSnapshot.child("data").getValue(String.class);
                String hojeFormatado = getHojeFormatado();

                int consumidoAntesDaAdicaoNesteCiclo = 0;
                int xpAguaGanhoAntesDaAdicaoNesteCiclo = 0;

                if (hojeFormatado.equals(dataSalva) && aguaConsumoSnapshot.exists()) { // Verifica se aguaConsumoSnapshot existe
                    Log.d(TAG, "processarAdicaoAgua - onDataChange: Dados de água para HOJE ("+hojeFormatado+") encontrados no DB.");
                    Long consumidoHojeDB = aguaConsumoSnapshot.child("totalMlConsumidoHoje").getValue(Long.class);
                    Long xpAguaHojeDB = aguaConsumoSnapshot.child("xpGanhoAguaHoje").getValue(Long.class);
                    consumidoAntesDaAdicaoNesteCiclo = (consumidoHojeDB != null) ? consumidoHojeDB.intValue() : 0;
                    xpAguaGanhoAntesDaAdicaoNesteCiclo = (xpAguaHojeDB != null) ? xpAguaHojeDB.intValue() : 0;
                } else {
                    Log.d(TAG, "processarAdicaoAgua - onDataChange: Sem dados de água para hoje ou data ("+dataSalva+") difere de hoje ("+hojeFormatado+") ou nó aguaConsumoDiario não existe. Iniciando ciclo diário.");
                    // Não precisa resetar as variáveis locais aqui, pois elas serão calculadas e atualizadas de qualquer forma.
                }

                int totalConsumidoAposAdicao = consumidoAntesDaAdicaoNesteCiclo + quantidadeAdicionadaMl;
                int xpGanhoNestaAdicao = 0;
                int novoXpAguaTotalHoje = xpAguaGanhoAntesDaAdicaoNesteCiclo;

                if (consumidoAntesDaAdicaoNesteCiclo < MARCO_INICIAL_ML && totalConsumidoAposAdicao >= MARCO_INICIAL_ML) {
                    if (xpAguaGanhoAntesDaAdicaoNesteCiclo < XP_BASE_AGUA_2100ML) {
                        int xpParaConceder = XP_BASE_AGUA_2100ML - xpAguaGanhoAntesDaAdicaoNesteCiclo;
                        xpGanhoNestaAdicao += xpParaConceder;
                        novoXpAguaTotalHoje += xpParaConceder;
                    }
                }
                if (totalConsumidoAposAdicao > MARCO_INICIAL_ML) {
                    int litrosAcimaMarcoAntes = (consumidoAntesDaAdicaoNesteCiclo > MARCO_INICIAL_ML) ?
                            (consumidoAntesDaAdicaoNesteCiclo - MARCO_INICIAL_ML) / INCREMENTO_LITRO_ML : 0;
                    int litrosAcimaMarcoDepois = (totalConsumidoAposAdicao - MARCO_INICIAL_ML) / INCREMENTO_LITRO_ML;
                    int novosLitrosCompletosParaXp = litrosAcimaMarcoDepois - litrosAcimaMarcoAntes;
                    if (novosLitrosCompletosParaXp > 0) {
                        int xpExtraGanho = novosLitrosCompletosParaXp * XP_EXTRA_POR_LITRO_AGUA;
                        xpGanhoNestaAdicao += xpExtraGanho;
                        novoXpAguaTotalHoje += xpExtraGanho;
                    }
                }
                xpGanhoNestaAdicao = Math.max(0, xpGanhoNestaAdicao); // Garante que XP ganho não seja negativo
                novoXpAguaTotalHoje = Math.max(xpAguaGanhoAntesDaAdicaoNesteCiclo, novoXpAguaTotalHoje); // Garante que XP total do dia não diminua
                if (xpGanhoNestaAdicao > 0) { // Garante que o XP total do dia seja incrementado corretamente
                    novoXpAguaTotalHoje = Math.max(novoXpAguaTotalHoje, xpAguaGanhoAntesDaAdicaoNesteCiclo + xpGanhoNestaAdicao);
                }


                Log.d(TAG, "Cálculo XP: Adicionando " + quantidadeAdicionadaMl + "ml. " +
                        "Consumido Antes (ciclo): " + consumidoAntesDaAdicaoNesteCiclo + "ml, XP Água Antes (ciclo): " + xpAguaGanhoAntesDaAdicaoNesteCiclo + ". " +
                        "Total Consumido Depois: " + totalConsumidoAposAdicao + "ml, Novo XP Água Total Hoje: " + novoXpAguaTotalHoje + ". " +
                        "XP Ganho Nesta Adição: " + xpGanhoNestaAdicao + ". XP Total Usuário ANTES da Adição (lido do DB): " + xpTotalUsuarioAntesDaAdicao);

                Map<String, Object> updates = new HashMap<>();
                updates.put("aguaConsumoDiario/data", hojeFormatado);
                updates.put("aguaConsumoDiario/totalMlConsumidoHoje", totalConsumidoAposAdicao);
                updates.put("aguaConsumoDiario/xpGanhoAguaHoje", novoXpAguaTotalHoje);

                final int finalXpGanhoNestaAdicao = xpGanhoNestaAdicao;
                final int finalNovoXpAguaTotalHoje = novoXpAguaTotalHoje;
                final int finalTotalConsumidoAposAdicao = totalConsumidoAposAdicao;
                final long finalNovoXpTotalUsuario = xpTotalUsuarioAntesDaAdicao + finalXpGanhoNestaAdicao;


                if (finalXpGanhoNestaAdicao > 0) {
                    updates.put("xp", finalNovoXpTotalUsuario);
                    Log.d(TAG, "Atualizando XP total do usuário para: " + finalNovoXpTotalUsuario);
                }

                FirebaseUser userParaEscrita = firebaseAuth.getCurrentUser();
                if (userParaEscrita == null) {
                    Log.e(TAG, "processarAdicaoAgua - Tentativa de ESCRITA com usuário NULO APÓS CÁLCULOS. UID local era: " + (localCurrentUser != null ? localCurrentUser.getUid() : "[local era nulo também]"));
                    Toast.makeText(TelaAgua.this, "Erro crítico: Sessão inválida ao tentar salvar. Faça login novamente.", Toast.LENGTH_LONG).show();
                    return;
                }
                // Garante que a referência de escrita seja para o usuário correto, especialmente se mudou durante o callback
                DatabaseReference refParaEscrita = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("usuarios").child(userParaEscrita.getUid());

                Log.d(TAG, "Preparando para ATUALIZAR Firebase. Usuário para escrita: " + userParaEscrita.getUid());
                Log.d(TAG, "Caminho da atualização: " + refParaEscrita.toString()); // Usar toString() que é permitido
                Log.d(TAG, "Dados COMPLETOS a serem enviados (updates Map): " + updates.toString());


                refParaEscrita.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Dados da água e XP salvos com sucesso no Firebase!");
                        String mensagem = quantidadeAdicionadaMl + "ml adicionados!";
                        if (finalXpGanhoNestaAdicao > 0) {
                            mensagem += " Você ganhou " + finalXpGanhoNestaAdicao + " XP!";
                        }
                        Toast.makeText(TelaAgua.this, mensagem, Toast.LENGTH_SHORT).show();

                        quantidadeIngeridaHojeFirebase = finalTotalConsumidoAposAdicao;
                        xpGanhoAguaHojeFirebase = finalNovoXpAguaTotalHoje;
                        if (finalXpGanhoNestaAdicao > 0) {
                            xpTotalUsuario = finalNovoXpTotalUsuario;
                        }
                        atualizarUIComDadosDoFirebase();
                    } else {
                        Log.e(TAG, "FALHA AO SALVAR dados no Firebase: ", task.getException());
                        if (task.getException() != null) {
                            Log.e(TAG, "Detalhes da EXCEÇÃO ao salvar: " + task.getException().getMessage());
                            task.getException().printStackTrace();
                        }
                        Toast.makeText(TelaAgua.this, "Falha ao salvar. Verifique os logs (Permission Denied ou Validação).", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "processarAdicaoAgua - Erro ao revalidar dados antes de salvar (onCancelled): " + error.getMessage(), error.toException());
                Toast.makeText(TelaAgua.this, "Erro de rede ou permissão ao ler dados. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}