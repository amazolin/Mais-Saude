package com.example.maissade;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Para exibir mensagens rápidas

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TelaAgua extends AppCompatActivity {

    // 1. Declaração das Views
    private TextView tvMeta;
    private TextView tvQuantidadeIngerida;
    private ProgressBar progressBar;
    private EditText edtQuantiaAgua;
    private Button btn100ml;
    private Button btn250ml;
    private Button btnAdicionar;

    // Variáveis para controle da lógica
    private int metaAguaMl = 2000; // Meta de água em mililitros
    private int quantidadeIngeridaMl = 0; // Quantidade de água ingerida atualmente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilita o modo EdgeToEdge para uma experiência de tela cheia
        setContentView(R.layout.activity_tela_agua); // Define o layout da atividade

        // Configuração de insets para barras do sistema (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Inicialização das Views (conectando as variáveis Java aos IDs do XML)
        tvMeta = findViewById(R.id.tvMeta);
        tvQuantidadeIngerida = findViewById(R.id.tvQuantidadeIngerida);

        edtQuantiaAgua = findViewById(R.id.edtQuantiaAgua);
        btn100ml = findViewById(R.id.btn100ml);
        btn250ml = findViewById(R.id.btn250ml);
        btnAdicionar = findViewById(R.id.btnAdicionar);

        // Define a meta no TextView
        tvMeta.setText("Meta de ingestão: " + metaAguaMl + "ml");

        // Configura o ProgressBar
        progressBar.setMax(metaAguaMl); // O máximo da ProgressBar é a meta de água

        // Atualiza a UI inicial
        updateUI();

        // 3. Configuração dos Listeners de Botões

        // Botão para adicionar 100ml
        btn100ml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarAgua(100);
            }
        });

        // Botão para adicionar 250ml
        btn250ml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarAgua(250);
            }
        });

        // Botão para adicionar quantidade digitada
        btnAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantiaStr = edtQuantiaAgua.getText().toString();
                if (!quantiaStr.isEmpty()) {
                    try {
                        int quantia = Integer.parseInt(quantiaStr);
                        if (quantia > 0) {
                            adicionarAgua(quantia);
                            edtQuantiaAgua.setText(""); // Limpa o EditText após adicionar
                        } else {
                            Toast.makeText(TelaAgua.this, "Por favor, digite uma quantidade maior que zero.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(TelaAgua.this, "Por favor, digite um número válido.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TelaAgua.this, "Por favor, digite a quantidade de água.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Método para adicionar a quantidade de água e atualizar a UI.
     * @param quantidade A quantidade de água a ser adicionada em ml.
     */
    private void adicionarAgua(int quantidade) {
        quantidadeIngeridaMl += quantidade;
        // Garante que a quantidade não ultrapasse a meta para fins de exibição se desejar
        // if (quantidadeIngeridaMl > metaAguaMl) {
        //     quantidadeIngeridaMl = metaAguaMl;
        // }
        updateUI();
        Toast.makeText(this, quantidade + "ml adicionados!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Atualiza o TextView de quantidade ingerida e o ProgressBar.
     */
    private void updateUI() {
        tvQuantidadeIngerida.setText(quantidadeIngeridaMl + " ml");
        progressBar.setProgress(quantidadeIngeridaMl);

        // Opcional: Mostrar uma mensagem ao atingir ou ultrapassar a meta
        if (quantidadeIngeridaMl >= metaAguaMl) {
            Toast.makeText(this, "Parabéns! Meta de água atingida!", Toast.LENGTH_LONG).show();
        }
    }
}