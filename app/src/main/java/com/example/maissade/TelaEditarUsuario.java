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
import java.util.Locale;
import java.util.Map;

public class TelaEditarUsuario extends AppCompatActivity {
    private static final String TAG = "TelaEditarUsuario";

    // Mapeamento de strings de avatar para recursos drawable
    private final int[] avatarIds = {R.drawable.avatarone, R.drawable.avatartwo, R.drawable.avatarthree, R.drawable.avatarfour, R.drawable.avatarfive, R.drawable.avatarsix, R.drawable.avatarseven, R.drawable.avatareight, R.drawable.avatarnine, R.drawable.avatarten, R.drawable.avatareleven, R.drawable.avatartwelve, R.drawable.avatartirteen, R.drawable.avatarfourteen, R.drawable.avatarfiftheen, R.drawable.avatarsixteen, R.drawable.avatarseventeen, R.drawable.avatareighteen, R.drawable.avatarnineteen, R.drawable.avatartwenty, R.drawable.avatartwentyone, R.drawable.avatartwentytwo, R.drawable.avatartwentythree, R.drawable.avatartwentyfour, R.drawable.avatartwentyfive, R.drawable.avatartwentysix, R.drawable.avatartwentyseven, R.drawable.avatartwentyeight, R.drawable.avatartwentynine};

    private EditText txtNomePerfil, txtIdadePerfil, txtPesoPerfil, txtAlturaPerfil;
    private RadioGroup radioGroupSexo;
    private RadioButton radioMasculino, radioFeminino, radioOutro; // Mantido radioOutro, mas sua visibilidade dependerá do XML
    private Spinner spinnerTipoSanguineoEditar; // Adicionado de volta, pois é um spinner diferente

    private TextView txtIMCEditar, txtNivelEditar; // São TextViews, apenas para exibição
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

        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/").getReference("usuarios").child(currentUser.getUid());
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

        txtIMCEditar = findViewById(R.id.txtIMCEditar);
        txtNivelEditar = findViewById(R.id.txtNivelEditar);
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
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.tipos_sanguineos_array, android.R.layout.simple_spinner_item);
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
            double imc = intent.getDoubleExtra("imc", 0.0);
            String nivelStatus = intent.getStringExtra("nivelStatus");

            Log.d(TAG, "preencherDadosDoIntent: Dados recebidos - Nome: " + nome + ", Idade: " + idade + ", Sexo: " + sexo + ", IMC: " + imc + ", Nível (Status IMC): " + nivelStatus);

            if (txtNomePerfil != null && nome != null) {
                txtNomePerfil.setText(nome);
            }

            if (txtIdadePerfil != null) {
                txtIdadePerfil.setText(idade > 0 ? String.valueOf(idade) : "");
            }

            if (sexo != null && !sexo.isEmpty()) {
                if (sexo.equalsIgnoreCase("Masculino")) {
                    radioMasculino.setChecked(true);
                } else if (sexo.equalsIgnoreCase("Feminino")) {
                    radioFeminino.setChecked(true);
                } else if (sexo.equalsIgnoreCase("Outro")) {
                    radioOutro.setChecked(true);
                } else {
                    radioGroupSexo.clearCheck();
                    Log.w(TAG, "preencherDadosDoIntent: Sexo '" + sexo + "' não corresponde a nenhuma opção.");
                }
            } else {
                radioGroupSexo.clearCheck();
            }

            if (txtPesoPerfil != null && peso > 0) {
                txtPesoPerfil.setText(String.valueOf(peso));
            }

            if (txtAlturaPerfil != null && altura > 0) {
                txtAlturaPerfil.setText(String.valueOf(altura));
            }

            if (spinnerTipoSanguineoEditar != null) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerTipoSanguineoEditar.getAdapter();
                if (adapter != null) {
                    if (tipoSanguineo == null) {
                        spinnerTipoSanguineoEditar.setSelection(0);
                    } else {
                        int spinnerPosition = adapter.getPosition(tipoSanguineo);
                        if (spinnerPosition >= 0) {
                            spinnerTipoSanguineoEditar.setSelection(spinnerPosition);
                        } else {
                            int fallback = adapter.getPosition("Não Informado");
                            spinnerTipoSanguineoEditar.setSelection(fallback >= 0 ? fallback : 0);
                        }
                    }
                }
            }

            if (currentImagemPerfilString != null && currentImagemPerfilString.startsWith("avatar_")) {
                try {
                    int index = Integer.parseInt(currentImagemPerfilString.substring(7));
                    if (index >= 0 && index < avatarIds.length) {
                        currentAvatarIndex = index;
                        viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
                    } else {
                        currentAvatarIndex = 0;
                        viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
                    }
                } catch (NumberFormatException e) {
                    currentAvatarIndex = 0;
                    viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
                }
            } else {
                currentAvatarIndex = 0;
                viewFlipperAvatars.setDisplayedChild(currentAvatarIndex);
            }

            //  Aqui exibimos o IMC e o Nível do usuário
            if (txtIMCEditar != null) {
                txtIMCEditar.setText(String.format(Locale.getDefault(), "%.2f", imc));
            }
            if (txtNivelEditar != null) { // txtNivelEditar pode estar exibindo o status de peso, mas aqui é o Nível de XP
                txtNivelEditar.setText(nivelStatus);
            }

        } else {
            Log.w(TAG, "preencherDadosDoIntent: Intent ou Extras nulos.");
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
        // 1. Obter os dados atualizados dos campos de entrada
        String nomeAtualizado = txtNomePerfil.getText().toString().trim();
        String idadeStr = txtIdadePerfil.getText().toString().trim();
        String pesoStr = txtPesoPerfil.getText().toString().trim();
        String alturaStr = txtAlturaPerfil.getText().toString().trim();
        String tipoSanguineoAtualizado = spinnerTipoSanguineoEditar.getSelectedItem().toString();

        // 2. Obter o sexo selecionado do RadioGroup
        String sexoAtualizado = "";
        int selectedId = radioGroupSexo.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            sexoAtualizado = selectedRadioButton.getText().toString();
        }

        // 3. Validação dos campos obrigatórios
        if (nomeAtualizado.isEmpty()) {
            txtNomePerfil.setError("Nome é obrigatório.");
            txtNomePerfil.requestFocus();
            return;
        }

        if (idadeStr.isEmpty()) {
            txtIdadePerfil.setError("Idade é obrigatória.");
            txtIdadePerfil.requestFocus();
            return;
        }

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

        // 4. Conversões de strings para números com validações
        long idadeAtualizada;
        double pesoAtualizado;
        double alturaAtualizada;

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

        try {
            pesoAtualizado = Double.parseDouble(pesoStr);
        } catch (NumberFormatException e) {
            txtPesoPerfil.setError("Peso inválido. Digite um número.");
            txtPesoPerfil.requestFocus();
            return;
        }

        try {
            alturaAtualizada = Double.parseDouble(alturaStr);
            if (alturaAtualizada <= 0) {
                txtAlturaPerfil.setError("Altura deve ser maior que zero.");
                txtAlturaPerfil.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            txtAlturaPerfil.setError("Altura inválida. Digite um número.");
            txtAlturaPerfil.requestFocus();
            return;
        }

        // 5. Calcular IMC
        double alturaMetros = alturaAtualizada / 100.0;
        double imc = pesoAtualizado / (alturaMetros * alturaMetros);

        // 6. Determinar o nível do usuário com base no IMC
        String nivelUsuario;
        if (imc < 18.5) {
            nivelUsuario = "Abaixo do peso";
        } else if (imc < 25) {
            nivelUsuario = "Peso normal";
        } else if (imc < 30) {
            nivelUsuario = "Sobrepeso";
        } else if (imc < 35) {
            nivelUsuario = "Obesidade Grau I";
        } else if (imc < 40) {
            nivelUsuario = "Obesidade Grau II";
        } else {
            nivelUsuario = "Obesidade Grau III";
        }

        // 7. Criar um HashMap com os dados atualizados
        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", nomeAtualizado);
        updates.put("idade", idadeAtualizada);
        updates.put("sexo", sexoAtualizado);
        updates.put("peso", pesoAtualizado);
        updates.put("altura", alturaAtualizada);
        updates.put("tipoSanguineo", tipoSanguineoAtualizado);
        updates.put("imagemPerfil", currentImagemPerfilString); // certifique-se que está sendo atualizado corretamente ao trocar avatar
        updates.put("imc", imc);
        updates.put("nivel", nivelUsuario);

        // 8. Enviar os dados atualizados para o Firebase
        userNodeReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "salvarAlteracoesNoFirebase: Dados do usuário atualizados com sucesso!");
                    Toast.makeText(TelaEditarUsuario.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a tela de edição e volta para a anterior
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "salvarAlteracoesNoFirebase: Erro ao atualizar dados do usuário.", e);
                    Toast.makeText(TelaEditarUsuario.this, "Erro ao atualizar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}