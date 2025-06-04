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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Arrays; // Import necessário para Arrays.asList()

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

    private EditText txtNomePerfil, txtIdadePerfil, txtPesoPerfil, txtAlturaPerfil;
    private RadioGroup radioGroupSexo;
    private RadioButton radioMasculino, radioFeminino, radioOutro; // Mantido radioOutro, mas sua visibilidade dependerá do XML
    private Spinner spinnerTipoSanguineoEditar; // Adicionado de volta, pois é um spinner diferente

    private TextView txtIMCPerfil, txtNivelPerfil; // São TextViews, apenas para exibição
    private Button btnSalvarEdicao, btnPrevAvatar, btnNextAvatar;
    private ViewFlipper viewFlipperAvatars;

    private FirebaseAuth auth;
    private DatabaseReference userNodeReference;
    private String currentImagemPerfilString; // Para armazenar a string do avatar atual (e salvar a nova)
    private int currentAvatarIndex = 0; // Índice do avatar atualmente exibido no ViewFlipper

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
        configurarSpinnerTipoSanguineo(); // Mantido para o Tipo Sanguíneo
        // configurarSpinnerSexo(); // Removido, pois o sexo agora é RadioGroup
        preencherDadosDoIntent();

        configurarListenerBotaoSalvar();
        configurarListenersNavegacaoAvatar();
    }

    private void personalizarToolbarTitle(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (tv.getText() != null && "EDITAR PERFIL".equals(tv.getText().toString())) {
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
        txtIdadePerfil = findViewById(R.id.txtIdadePerfilUser);
        txtPesoPerfil = findViewById(R.id.txtPesoPerfil);
        txtAlturaPerfil = findViewById(R.id.txtAlturaPerfil);

        // Inicialização do RadioGroup e RadioButtons para Sexo
        radioGroupSexo = findViewById(R.id.radioGroupSexo);
        radioMasculino = findViewById(R.id.radioMasculino);
        radioFeminino = findViewById(R.id.radioFeminino);

        // Inicialização do Spinner para Tipo Sanguíneo
        spinnerTipoSanguineoEditar = findViewById(R.id.spinnerTipoSanguineoEditar); // Novo: Adicionei esta linha para o Spinner

        txtIMCPerfil = findViewById(R.id.txtIMCPerfil);
        txtNivelPerfil = findViewById(R.id.txtNivelPerfil);
        btnSalvarEdicao = findViewById(R.id.btnSalvarEdicao);
        btnPrevAvatar = findViewById(R.id.btnPrevAvatar);
        btnNextAvatar = findViewById(R.id.btnNextAvatar);
        viewFlipperAvatars = findViewById(R.id.viewFlipperAvatars);
        Log.d(TAG, "inicializarViews: Views inicializadas.");
    }

    private void configurarViewFlipper() {
        for (int avatarId : avatarIds) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(avatarId);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            viewFlipperAvatars.addView(imageView);
        }
    }

    private void configurarListenersNavegacaoAvatar() {
        btnPrevAvatar.setOnClickListener(v -> {
            viewFlipperAvatars.showPrevious();
            currentAvatarIndex = viewFlipperAvatars.getDisplayedChild();
            currentImagemPerfilString = "avatar_" + currentAvatarIndex;
            Log.d(TAG, "Avatar Anterior. Índice: " + currentAvatarIndex + ", String: " + currentImagemPerfilString);
        });

        btnNextAvatar.setOnClickListener(v -> {
            viewFlipperAvatars.showNext();
            currentAvatarIndex = viewFlipperAvatars.getDisplayedChild();
            currentImagemPerfilString = "avatar_" + currentAvatarIndex;
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
                // O valor será pego no salvarAlteracoesNoFirebase
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada a fazer
            }
        });
        Log.d(TAG, "configurarSpinnerTipoSanguineo: Spinner configurado.");
    }

    // Removido: configurarSpinnerSexo(), pois o sexo agora é RadioGroup.

    private void preencherDadosDoIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String nome = intent.getStringExtra("nome");
            Long idade = intent.getLongExtra("idade", 0L);
            String sexo = intent.getStringExtra("sexo");
            Long peso = intent.getLongExtra("peso", 0L);
            Long altura = intent.getLongExtra("altura", 0L);
            String tipoSanguineo = intent.getStringExtra("tipoSanguineo");
            currentImagemPerfilString = intent.getStringExtra("imagemPerfil");

            Log.d(TAG, "preencherDadosDoIntent: Dados recebidos - Nome: " + nome +
                    ", Idade: " + idade +
                    ", Sexo: " + sexo +
                    ", Imagem: " + currentImagemPerfilString +
                    ", Tipo Sanguíneo RECEBIDO NA INTENT: '" + tipoSanguineo + "'");

            if (txtNomePerfil != null && nome != null) {
                txtNomePerfil.setText(nome);
            }

            if (txtIdadePerfil != null) {
                if (idade != null && idade > 0) {
                    txtIdadePerfil.setText(String.valueOf(idade));
                } else {
                    txtIdadePerfil.setText("");
                }
            }

            // Preencher RadioGroup para Sexo
            if (sexo != null && !sexo.isEmpty()) {
                if (sexo.equalsIgnoreCase("Masculino")) {
                    radioMasculino.setChecked(true);
                } else if (sexo.equalsIgnoreCase("Feminino")) {
                    radioFeminino.setChecked(true);
                } else if (sexo.equalsIgnoreCase("Outro")) { // Se você tiver a opção "Outro"
                    radioOutro.setChecked(true);
                } else {
                    radioGroupSexo.clearCheck(); // Limpa a seleção se o valor não corresponder
                    Log.w(TAG, "preencherDadosDoIntent: Sexo '" + sexo + "' não corresponde a nenhuma opção de RadioButton.");
                }
            } else {
                radioGroupSexo.clearCheck(); // Nenhuma opção selecionada se sexo for nulo/vazio
            }


            if (txtPesoPerfil != null && peso != null && peso > 0) {
                txtPesoPerfil.setText(String.valueOf(peso));
            }
            if (txtAlturaPerfil != null && altura != null && altura > 0) {
                txtAlturaPerfil.setText(String.valueOf(altura));
            }

            // Define a seleção inicial do Spinner de Tipo Sanguíneo
            if (spinnerTipoSanguineoEditar != null) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerTipoSanguineoEditar.getAdapter();
                if (adapter != null) {
                    if (tipoSanguineo == null) {
                        Log.w(TAG, "preencherDadosDoIntent: Tipo sanguíneo recebido é NULL. Definindo seleção para 0.");
                        spinnerTipoSanguineoEditar.setSelection(0);
                    } else {
                        int spinnerPosition = adapter.getPosition(tipoSanguineo);
                        Log.d(TAG, "preencherDadosDoIntent: Tentando encontrar '" + tipoSanguineo + "' no spinner. Posição encontrada: " + spinnerPosition);

                        if (spinnerPosition >= 0) {
                            spinnerTipoSanguineoEditar.setSelection(spinnerPosition);
                            Log.d(TAG, "preencherDadosDoIntent: Tipo sanguíneo '" + tipoSanguineo + "' selecionado no Spinner.");
                        } else {
                            Log.w(TAG, "preencherDadosDoIntent: Tipo sanguíneo '" + tipoSanguineo + "' NÃO ENCONTRADO NO SPINNER.");
                            Log.w(TAG, "preencherDadosDoIntent: Opções do Spinner: " + Arrays.toString(getResources().getStringArray(R.array.tipos_sanguineos_array)));
                            int naoInformadoPos = adapter.getPosition("Não Informado");
                            if (naoInformadoPos >= 0) {
                                spinnerTipoSanguineoEditar.setSelection(naoInformadoPos);
                                Log.d(TAG, "preencherDadosDoIntent: 'Não Informado' selecionado como fallback.");
                            } else {
                                spinnerTipoSanguineoEditar.setSelection(0);
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
        // 1. Obter os dados atualizados dos EditTexts e RadioGroup/Spinner
        String nomeAtualizado = txtNomePerfil.getText().toString().trim();
        String idadeStr = txtIdadePerfil.getText().toString().trim();
        String pesoStr = txtPesoPerfil.getText().toString().trim();
        String alturaStr = txtAlturaPerfil.getText().toString().trim();
        String tipoSanguineoAtualizado = spinnerTipoSanguineoEditar.getSelectedItem().toString();

        // Obter o sexo selecionado do RadioGroup
        String sexoAtualizado = "";
        int selectedId = radioGroupSexo.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            sexoAtualizado = selectedRadioButton.getText().toString();
        }

        // 2. Validação básica
        if (nomeAtualizado.isEmpty()) {
            txtNomePerfil.setError("Nome é obrigatório.");
            txtNomePerfil.requestFocus();
            return;
        }

        // Validação da idade
        if (idadeStr.isEmpty()) {
            txtIdadePerfil.setError("Idade é obrigatória.");
            txtIdadePerfil.requestFocus();
            return;
        }
        Long idadeAtualizada;
        try {
            idadeAtualizada = Long.parseLong(idadeStr);
            if (idadeAtualizada < 0 || idadeAtualizada > 120) {
                txtIdadePerfil.setError("Idade deve ser entre 0 e 120 anos.");
                txtIdadePerfil.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            txtIdadePerfil.setError("Idade inválida. Digite um número.");
            txtIdadePerfil.requestFocus();
            return;
        }

        // Validação do sexo (verificar se algum RadioButton foi selecionado)
        if (sexoAtualizado.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione o sexo.", Toast.LENGTH_SHORT).show();
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

        // Converter para os tipos corretos (peso e altura)
        Long pesoAtualizado = Long.parseLong(pesoStr);
        Long alturaAtualizada = Long.parseLong(alturaStr);

        // 3. Criar um HashMap para as atualizações
        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", nomeAtualizado);
        updates.put("idade", idadeAtualizada);
        updates.put("sexo", sexoAtualizado); // Salva o sexo do RadioButton
        updates.put("peso", pesoAtualizado);
        updates.put("altura", alturaAtualizada);
        updates.put("tipoSanguineo", tipoSanguineoAtualizado);
        updates.put("imagemPerfil", currentImagemPerfilString);

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