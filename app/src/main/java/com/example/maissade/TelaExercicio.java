package com.example.maissade;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.model.Exercicio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class TelaExercicio extends AppCompatActivity {
    private DatabaseReference userNodeReference;
    private FirebaseUser currentUser;

    private int minutosExercicio = 0;
    private int minutosBicicleta = 0;
    private int minutosCorrida = 0;
    private int minutosNatacao = 0;

    private TextView txtExercicio, txtBicicleta, txtCorrida, txtNatacao;

    private DatabaseReference exercicioRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_exercicio);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        exercicioRef = database.getReference("exercicios");

        txtExercicio = findViewById(R.id.exercicio_hours);
        txtBicicleta = findViewById(R.id.bike_hours);
        txtCorrida = findViewById(R.id.corrida_hours);
        txtNatacao = findViewById(R.id.natacao_hours);

        // Botões +
        findViewById(R.id.button_increase_exercicio).setOnClickListener(v -> {
            minutosExercicio += 10;
            txtExercicio.setText(minutosExercicio + " m");
        });

        findViewById(R.id.button_increase_sono).setOnClickListener(v -> {
            minutosBicicleta += 10;
            txtBicicleta.setText(minutosBicicleta + " m");
        });

        findViewById(R.id.button_increase_corrida).setOnClickListener(v -> {
            minutosCorrida += 10;
            txtCorrida.setText(minutosCorrida + " m");
        });

        findViewById(R.id.button_increase_natacao).setOnClickListener(v -> {
            minutosNatacao += 10;
            txtNatacao.setText(minutosNatacao + " m");
        });

        // Botões de ícone que salvam o progresso
        findViewById(R.id.btn_exercicio).setOnClickListener(v -> salvarExercicioTipo("exercicio", minutosExercicio));
        findViewById(R.id.btn_sono).setOnClickListener(v -> salvarExercicioTipo("bicicleta", minutosBicicleta));
        findViewById(R.id.btn_corrida).setOnClickListener(v -> salvarExercicioTipo("corrida", minutosCorrida));
        findViewById(R.id.btn_natacao).setOnClickListener(v -> salvarExercicioTipo("natacao", minutosNatacao));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(currentUser.getUid());

    }
    private void salvarExercicioTipo(String tipo, int minutos) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        int xpGanho = calcularXpPorMinuto(minutos); // cada 10 min = 5 XP, por exemplo

        DatabaseReference usuarioRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(userId);

        usuarioRef.child("xp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long xpAtual = snapshot.getValue(Long.class);
                if (xpAtual == null) xpAtual = 0L;
                long novoXp = xpAtual + xpGanho;

                Map<String, Object> atualizacoes = new HashMap<>();
                atualizacoes.put("xp", novoXp);

                usuarioRef.updateChildren(atualizacoes).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        salvarRegistroExercicio(tipo, minutos, userId);
                        Toast.makeText(TelaExercicio.this, tipo + " salvo! +" + xpGanho + " XP", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TelaExercicio.this, "Erro ao atualizar XP.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TelaExercicio.this, "Erro ao acessar dados do usuário.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void salvarExercicioNoFirebase() {
        Exercicio exercicio = new Exercicio(minutosExercicio, minutosBicicleta, minutosCorrida, minutosNatacao, null);
        exercicioRef.push().setValue(exercicio).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Exercício salvo com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void salvarRegistroExercicio(String tipo, int minutos, String userId) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("tipo", tipo);
        dados.put("minutos", minutos);
        dados.put("usuario", userId);
        dados.put("timestamp", ServerValue.TIMESTAMP);

        exercicioRef.push().setValue(dados);
    }
    private int calcularXpPorMinuto(int minutos) {
        return (minutos / 10) * 5; // Ex: 10 min = 5 XP, 20 min = 10 XP
    }


}

