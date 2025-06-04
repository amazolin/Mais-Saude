package com.example.maissade;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TelaEditarUsuario extends AppCompatActivity {
    private static final String TAG = "TelaEditarUsuario";

    // Mapeamento de strings de avatar para recursos drawable
    private final int[] avatarIds = {
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

    private EditText txtNomePerfil, txtIdadeSexoPerfil, txtPesoPerfil, txtAlturaPerfil;
    private Spinner spinnerTipoSanguineoEditar;
    private TextView txtIMCPerfil, txtNivelPerfil; // São TextViews, apenas para exibição
    private Button btnSalvarEdicao, btnPrevAvatar, btnNextAvatar;
    private ViewFlipper viewFlipperAvatars;

    private FirebaseAuth auth;
    private DatabaseReference userNodeReference;
    private String currentImagemPerfilString; // Para armazenar a string do avatar atual (e salvar a nova)
    private int currentAvatarIndex = 0; // Índice do avatar atualmente exibido no ViewFlipper

    // Variáveis para armazenar dados brutos recebidos da Intent
    private Long currentIdade;
    private String currentSexo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_editar_usuario);
        Log.d(TAG, "onCreate: Activity TelaEditarUsuario criada.");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        personalizarToolbarTitle(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado. Redirecionando para login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(currentUser.getUid());
        Log.d(TAG, "onCreate: Referência do Firebase configurada para: " + userNodeReference.toString());

        inicializarViews();
        configurarViewFlipper();
        configurarSpinnerTipoSanguineo(); // <--- CHAME AQUI PRIMEIRO
        preencherDadosDoIntent();         // <--- E DEPOIS DEIXE ESTE

        configurarListenerBotaoSalvar();
        configurarListenersNavegacaoAvatar();
        // A linha 'configurarSpinnerTipoSanguineo();' foi movida para cima
    }

    private void personalizarToolbarTitle(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (tv.getText() != null && "Editar Perfil".equals(tv.getText().toString())) {
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
        txtNomePerfil = findViewById(R.id.txtNomePerfil);
        txtIdadeSexoPerfil = findViewById(R.id.txtIdadeSexoPerfil);
        txtPesoPerfil = findViewById(R.id.txtPesoPerfil);
        txtAlturaPerfil = findViewById(R.id.txtAlturaPerfil);
        spinnerTipoSanguineoEditar = findViewById(R.id.spinnerTipoSanguineoEditar);
        txtIMCPerfil = findViewById(R.id.txtIMCPerfil); // Apenas exibição
        txtNivelPerfil = findViewById(R.id.txtNivelPerfil); // Apenas exibição
        btnSalvarEdicao = findViewById(R.id.btnSalvarEdicao);
        btnPrevAvatar = findViewById(R.id.btnPrevAvatar);
        btnNextAvatar = findViewById(R.id.btnNextAvatar);
        viewFlipperAvatars = findViewById(R.id.viewFlipperAvatars);
        Log.d(TAG, "inicializarViews: Views inicializadas.");
    }

    private void configurarViewFlipper() {
        // Adiciona as imagens de avatar ao ViewFlipper
        for (int avatarId : avatarIds) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(avatarId);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Ajusta a imagem dentro do ViewFlipper
            viewFlipperAvatars.addView(imageView);
        }
    }

    private void configurarListenersNavegacaoAvatar() {
        btnPrevAvatar.setOnClickListener(v -> {
            viewFlipperAvatars.showPrevious();
            currentAvatarIndex = viewFlipperAvatars.getDisplayedChild();
            currentImagemPerfilString = "avatar_" + currentAvatarIndex; // Atualiza a string do avatar
            Log.d(TAG, "Avatar Anterior. Índice: " + currentAvatarIndex + ", String: " + currentImagemPerfilString);
        });

        btnNextAvatar.setOnClickListener(v -> {
            viewFlipperAvatars.showNext();
            currentAvatarIndex = viewFlipperAvatars.getDisplayedChild();
            currentImagemPerfilString = "avatar_" + currentAvatarIndex; // Atualiza a string do avatar
            Log.d(TAG, "Próximo Avatar. Índice: " + currentAvatarIndex + ", String: " + currentImagemPerfilString);
        });
    }

    private void configurarSpinnerTipoSanguineo() {
        Log.d(TAG, "configurarSpinnerTipoSanguineo: Configurando o Spinner de Tipo Sanguíneo.");
        if (spinnerTipoSanguineoEditar == null) {
            Log.e(TAG, "configurarSpinnerTipoSanguineo: spinnerTipoSanguineoEditar é NULL. Verifique o layout.");
            return;
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_sanguineos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSanguineoEditar.setAdapter(adapter);

        spinnerTipoSanguineoEditar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Não precisamos fazer nada aqui, o valor será pego no salvarAlteracoesNoFirebase
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada a fazer
            }
        });
        Log.d(TAG, "configurarSpinnerTipoSanguineo: Spinner configurado.");
    }

    private void preencherDadosDoIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String nome = intent.getStringExtra("nome");
            currentIdade = intent.getLongExtra("idade", 0L);
            Long peso = intent.getLongExtra("peso", 0L);
            Long altura = intent.getLongExtra("altura", 0L);
            currentSexo = intent.getStringExtra("sexo");
            String tipoSanguineo = intent.getStringExtra("tipoSanguineo");
            currentImagemPerfilString = intent.getStringExtra("imagemPerfil"); // Armazena a string do avatar

            Log.d(TAG, "preencherDadosDoIntent: Dados recebidos - Nome: " + nome +
                    ", Idade: " + currentIdade +
                    ", Sexo: " + currentSexo +
                    ", Imagem: " + currentImagemPerfilString +
                    ", Tipo Sanguíneo RECEBIDO NA INTENT: '" + tipoSanguineo + "'"); // <-- NOVO LOG AQUI!

            if (txtNomePerfil != null && nome != null) txtNomePerfil.setText(nome);

            // Combina Idade e Sexo para o EditText 'txtIdadeSexoPerfil'
            if (txtIdadeSexoPerfil != null) {
                String idadeSexoText = (currentIdade != null && currentIdade > 0 ? currentIdade + " anos" : "Idade N/D") +
                        (currentSexo != null && !currentSexo.isEmpty() ? ", " + currentSexo : "");
                txtIdadeSexoPerfil.setText(idadeSexoText);
            }

            if (txtPesoPerfil != null && peso != null && peso > 0) txtPesoPerfil.setText(String.valueOf(peso));
            if (txtAlturaPerfil != null && altura != null && altura > 0) txtAlturaPerfil.setText(String.valueOf(altura));

            // Define a seleção inicial do Spinner
            if (spinnerTipoSanguineoEditar != null) { // Removi a checagem 'tipoSanguineo != null' daqui para logar sempre
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerTipoSanguineoEditar.getAdapter();
                if (adapter != null) {
                    if (tipoSanguineo == null) {
                        Log.w(TAG, "preencherDadosDoIntent: Tipo sanguíneo recebido é NULL. Definindo seleção para 0.");
                        spinnerTipoSanguineoEditar.setSelection(0); // Seleciona o primeiro item (geralmente "Não Informado")
                    } else {
                        int spinnerPosition = adapter.getPosition(tipoSanguineo);
                        Log.d(TAG, "preencherDadosDoIntent: Tentando encontrar '" + tipoSanguineo + "' no spinner. Posição encontrada: " + spinnerPosition); // <-- NOVO LOG AQUI!

                        if (spinnerPosition >= 0) {
                            spinnerTipoSanguineoEditar.setSelection(spinnerPosition);
                            Log.d(TAG, "preencherDadosDoIntent: Tipo sanguíneo '" + tipoSanguineo + "' selecionado no Spinner.");
                        } else {
                            Log.w(TAG, "preencherDadosDoIntent: Tipo sanguíneo '" + tipoSanguineo + "' NÃO ENCONTRADO NO SPINNER."); // <-- Log existente, mas agora com mais contexto
                            Log.w(TAG, "preencherDadosDoIntent: Opções do Spinner: " + java.util.Arrays.toString(getResources().getStringArray(R.array.tipos_sanguineos_array))); // <-- NOVO LOG AQUI!
                            // Tentar selecionar "Não Informado" se a opção existir
                            int naoInformadoPos = adapter.getPosition("Não Informado");
                            if (naoInformadoPos >= 0) {
                                spinnerTipoSanguineoEditar.setSelection(naoInformadoPos);
                                Log.d(TAG, "preencherDadosDoIntent: 'Não Informado' selecionado como fallback.");
                            } else {
                                spinnerTipoSanguineoEditar.setSelection(0); // Caso não exista "Não Informado", define a primeira opção
                                Log.d(TAG, "preencherDadosDoIntent: Primeira opção selecionada como fallback.");
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "preencherDadosDoIntent: Adapter do Spinner é NULL.");
                }
            } else {
                Log.e(TAG, "preencherDadosDoIntent: spinnerTipoSanguineoEditar é NULL. Verifique o layout.");
            }

            // Define o avatar inicial no ViewFlipper
            if (currentImagemPerfilString != null && currentImagemPerfilString.startsWith("avatar_")) {
                try {
                    int index = Integer.parseInt(currentImagemPerfilString.substring(7));
                    if (index >= 0 && index < avatarIds.length) {
                        currentAvatarIndex = index;
                        viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
                    } else {
                        Log.w(TAG, "preencherDadosDoIntent: Índice de avatar fora do intervalo. Usando avatar padrão (0).");
                        currentAvatarIndex = 0;
                        viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "preencherDadosDoIntent: Erro ao analisar índice de imagemSelecionada: " + currentImagemPerfilString, e);
                    currentAvatarIndex = 0;
                    viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
                }
            } else {
                Log.w(TAG, "preencherDadosDoIntent: imagemPerfil nula ou inválida. Usando avatar padrão (0).");
                currentAvatarIndex = 0;
                viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
            }

            // O IMC e o Nível Geral são apenas exibidos e não editados nesta tela
            // Se você quiser exibi-los aqui, precisará buscá-los do Firebase ou passá-los pela Intent.
            // Por enquanto, eles não são atualizados nesta tela de edição.
        } else {
            Log.w(TAG, "preencherDadosDoIntent: Intent ou Extras nulos, dados não foram passados.");
            Toast.makeText(this, "Não foi possível carregar os dados para edição.", Toast.LENGTH_SHORT).show();
        }
    }


    private void configurarListenerBotaoSalvar() {
        if (btnSalvarEdicao != null) {
            btnSalvarEdicao.setOnClickListener(v -> {
                salvarAlteracoesNoFirebase();
            });
        } else {
            Log.e(TAG, "configurarListenerBotaoSalvar: btnSalvarEdicao é NULL.");
        }
    }

    private void salvarAlteracoesNoFirebase() {
        // 1. Obter os dados atualizados dos EditTexts e Spinner
        String nomeAtualizado = txtNomePerfil.getText().toString().trim();
        String idadeSexoCompleto = txtIdadeSexoPerfil.getText().toString().trim();
        String pesoStr = txtPesoPerfil.getText().toString().trim();
        String alturaStr = txtAlturaPerfil.getText().toString().trim();
        String tipoSanguineoAtualizado = spinnerTipoSanguineoEditar.getSelectedItem().toString();

        // 2. Validação básica
        if (nomeAtualizado.isEmpty()) {
            txtNomePerfil.setError("Nome é obrigatório.");
            txtNomePerfil.requestFocus();
            return;
        }
        if (idadeSexoCompleto.isEmpty()) {
            txtIdadeSexoPerfil.setError("Idade e Sexo são obrigatórios.");
            txtIdadeSexoPerfil.requestFocus();
            return;
        }
        if (pesoStr.isEmpty()) {
            txtPesoPerfil.setError("Peso é obrigatório.");
            txtPesoPerfil.requestFocus();
            return;
        }
        if (alturaStr.isEmpty()) {
            txtAlturaPerfil.setError("Altura é obrigatória.");
            txtAlturaPerfil.requestFocus();
            return;
        }

        // Extrai idade e sexo do campo combinado
        Long idadeAtualizada = 0L;
        String sexoAtualizado = "";
        try {
            // Tenta parsear a idade do início da string
            String[] partes = idadeSexoCompleto.split(" anos, ");
            if (partes.length > 0) {
                idadeAtualizada = Long.parseLong(partes[0].replaceAll("[^0-9]", "")); // Remove não-números
                if (partes.length > 1) {
                    sexoAtualizado = partes[1].trim();
                } else {
                    // Se não houver ", " após " anos", tenta pegar o sexo se a string for "XX anos Sexo"
                    partes = idadeSexoCompleto.split(" anos ");
                    if (partes.length > 1) {
                        sexoAtualizado = partes[1].trim();
                    }
                }
            } else {
                // Tenta parsear apenas a idade se o formato for diferente
                idadeAtualizada = Long.parseLong(idadeSexoCompleto.replaceAll("[^0-9]", ""));
            }
            if (idadeAtualizada <= 0) {
                Toast.makeText(this, "Idade inválida. Use o formato 'XX anos, Sexo'.", Toast.LENGTH_LONG).show();
                txtIdadeSexoPerfil.setError("Idade inválida.");
                txtIdadeSexoPerfil.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de Idade/Sexo inválido. Use 'XX anos, Sexo'.", Toast.LENGTH_LONG).show();
            txtIdadeSexoPerfil.setError("Formato inválido.");
            txtIdadeSexoPerfil.requestFocus();
            return;
        }

        // Converter para os tipos corretos
        Long pesoAtualizado = Long.parseLong(pesoStr);
        Long alturaAtualizada = Long.parseLong(alturaStr);

        // 3. Criar um HashMap para as atualizações
        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", nomeAtualizado);
        updates.put("idade", idadeAtualizada);
        updates.put("sexo", sexoAtualizado); // Salva o sexo extraído
        updates.put("peso", pesoAtualizado);
        updates.put("altura", alturaAtualizada);
        updates.put("tipoSanguineo", tipoSanguineoAtualizado);
        updates.put("imagemPerfil", currentImagemPerfilString); // Salva a string do avatar

        // 4. Enviar as atualizações para o Firebase Realtime Database
        userNodeReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "salvarAlteracoesNoFirebase: Dados do usuário atualizados com sucesso!");
                    Toast.makeText(TelaEditarUsuario.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a TelaEditarUsuario e volta para a TelaUsuario
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "salvarAlteracoesNoFirebase: Erro ao atualizar dados do usuário.", e);
                    Toast.makeText(TelaEditarUsuario.this, "Erro ao atualizar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}