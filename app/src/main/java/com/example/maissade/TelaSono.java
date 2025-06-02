package com.example.maissade;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Sono;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TelaSono extends AppCompatActivity {

    private static final String TAG = "TelaSono";

    private double sleepHours = 0;
    private TextView textview_hours; // Único TextView que esta tela gerencia diretamente
    private Button buttonIncrease, buttonDecrease, btnAdicionarSono;

    private DatabaseReference userNodeReference;
    private DatabaseReference sonoNodeReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private long currentXp = 0; // Ainda necessário para calcular o novo XP
    private long lastSleepTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_sono); // Certifique-se que o layout não espera mais um textview_xp_atual_sono

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textview_hours = findViewById(R.id.textview_hours);
        buttonIncrease = findViewById(R.id.button_increase);
        buttonDecrease = findViewById(R.id.button_decrease);
        btnAdicionarSono = findViewById(R.id.btnAdicionar);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userNodeReference = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios")
                .child(currentUser.getUid());

        sonoNodeReference = userNodeReference.child("sono");

        loadUserXpAndTimestamp(); // Carrega o XP atual e o último registro para lógica interna
        updateSleepHoursText();

        buttonIncrease.setOnClickListener(v -> {
            sleepHours += 0.5;
            if (sleepHours > 24) sleepHours = 24;
            updateSleepHoursText();
        });

        buttonDecrease.setOnClickListener(v -> {
            if (sleepHours > 0) {
                sleepHours -= 0.5;
                if (sleepHours < 0) sleepHours = 0;
                updateSleepHoursText();
            }
        });

        btnAdicionarSono.setOnClickListener(v -> {
            verificarEAdicionarSono();
        });

        FrameLayout dicaOverlay = findViewById(R.id.dica_overlay);
        Button buttonDica = findViewById(R.id.button_dicasono);
        buttonDica.setOnClickListener(v -> dicaOverlay.setVisibility(View.VISIBLE));
        dicaOverlay.setOnClickListener(v -> dicaOverlay.setVisibility(View.GONE));
    }

    private void updateSleepHoursText() {
        if (textview_hours != null) {
            textview_hours.setText(String.format("%.1f h", sleepHours));
        }
    }

    private void loadUserXpAndTimestamp() {
        if (userNodeReference == null) return;

        // Listener para buscar o XP atual UMA VEZ. Não precisa ficar ouvindo mudanças de XP aqui.
        userNodeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long xpFromDB = dataSnapshot.child("xp").getValue(Long.class);
                    Long timestampFromDB = dataSnapshot.child("ultimoRegistroSonoTimestamp").getValue(Long.class);

                    currentXp = (xpFromDB != null) ? xpFromDB : 0L;
                    lastSleepTimestamp = (timestampFromDB != null) ? timestampFromDB : 0L;
                    Log.d(TAG, "loadUserXpAndTimestamp: XP atual carregado = " + currentXp + ", Ultimo Registro TS = " + lastSleepTimestamp);
                } else {
                    Log.w(TAG, "loadUserXpAndTimestamp: Perfil do usuário não encontrado no DB para buscar XP.");
                    // Não precisa de Toast aqui, pois o usuário não vê o XP nesta tela.
                    // O valor padrão de currentXp (0L) será usado.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "loadUserXpAndTimestamp: Erro ao carregar dados do usuário: " + databaseError.getMessage());
                Toast.makeText(TelaSono.this, "Erro ao carregar dados do perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean podeRegistrarSonoHoje() {
        if (lastSleepTimestamp == 0) {
            Log.d(TAG, "podeRegistrarSonoHoje: Nunca registrou antes. Permitido.");
            return true;
        }
        Calendar ultimoRegistroCal = Calendar.getInstance();
        ultimoRegistroCal.setTimeInMillis(lastSleepTimestamp);

        Calendar hojeCal = Calendar.getInstance();

        boolean podeRegistrar = !(ultimoRegistroCal.get(Calendar.YEAR) == hojeCal.get(Calendar.YEAR) &&
                ultimoRegistroCal.get(Calendar.DAY_OF_YEAR) == hojeCal.get(Calendar.DAY_OF_YEAR));
        Log.d(TAG, "podeRegistrarSonoHoje: Pode registrar? " + podeRegistrar);
        return podeRegistrar;
    }

    private void verificarEAdicionarSono() {
        if (currentUser == null) {
            Toast.makeText(this, "Não foi possível salvar. Usuário não logado.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sleepHours <= 0) {
            Toast.makeText(this, "Por favor, adicione horas de sono antes de salvar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recarregar o XP e o timestamp mais recentes antes de salvar,
        // para o caso de ter mudado em outra tela ou instância desde que a TelaSono foi aberta.
        // Isso é uma boa prática para evitar sobrescrever o XP com um valor antigo.
        userNodeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long xpFromDB = dataSnapshot.child("xp").getValue(Long.class);
                    Long timestampFromDB = dataSnapshot.child("ultimoRegistroSonoTimestamp").getValue(Long.class);
                    currentXp = (xpFromDB != null) ? xpFromDB : 0L; // Atualiza currentXp com o valor mais recente
                    lastSleepTimestamp = (timestampFromDB != null) ? timestampFromDB : 0L; // Atualiza lastSleepTimestamp

                    Log.d(TAG, "verificarEAdicionarSono (pre-check): XP atual = " + currentXp + ", UltimoRegistroTS = " + lastSleepTimestamp);

                    if (podeRegistrarSonoHoje()) {
                        salvarHorasSonoEAtualizarXPInternamente();
                    } else {
                        Toast.makeText(TelaSono.this, "Você já registrou seu sono hoje. Volte amanhã para ganhar mais XP!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "verificarEAdicionarSono: Perfil não encontrado ao tentar revalidar XP e timestamp.");
                    Toast.makeText(TelaSono.this, "Erro ao verificar dados do perfil. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "verificarEAdicionarSono: Erro ao recarregar dados do usuário: " + databaseError.getMessage());
                Toast.makeText(TelaSono.this, "Erro ao verificar dados do perfil. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Renomeado para indicar que a atualização de UI de XP não ocorre aqui
    private void salvarHorasSonoEAtualizarXPInternamente() {
        String registroId = sonoNodeReference.push().getKey();
        if (registroId == null) {
            Toast.makeText(this, "Erro ao gerar ID para o registro de sono.", Toast.LENGTH_SHORT).show();
            return;
        }

        int xpGanho;
        if (sleepHours >= 8) {
            xpGanho = 20;
        } else if (sleepHours >= 6) {
            xpGanho = 10;
        } else if (sleepHours >= 4) {
            xpGanho = 5;
        } else {
            xpGanho = 0;
        }
        Log.d(TAG, "salvarHorasSonoEAtualizarXPInternamente: Horas de sono = " + sleepHours + ", XP Ganho = " + xpGanho);

        if (xpGanho == 0 && sleepHours > 0) {
            Toast.makeText(this, "Sono registrado, mas a quantidade não foi suficiente para ganhar XP desta vez.", Toast.LENGTH_LONG).show();
        }

        Sono registroSono = new Sono(sleepHours);

        Map<String, Object> atualizacoesUsuario = new HashMap<>();
        long novoXp = currentXp + xpGanho; // currentXp foi atualizado no listener de verificarEAdicionarSono
        atualizacoesUsuario.put("xp", novoXp);
        atualizacoesUsuario.put("ultimoRegistroSonoTimestamp", System.currentTimeMillis());

        userNodeReference.updateChildren(atualizacoesUsuario)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "salvarHorasSonoEAtualizarXPInternamente: XP ("+novoXp+") e timestamp atualizados no DB.");
                        // Não precisa atualizar currentXp localmente aqui se a activity for fechada ou
                        // se a próxima leitura sempre buscará do DB.
                        // No entanto, se o usuário puder registrar várias vezes (com a lógica de podeRegistrarSonoHoje desabilitada para teste),
                        // seria bom atualizar:
                        // currentXp = novoXp;
                        // lastSleepTimestamp = System.currentTimeMillis();
                        // updateSleepHoursText(); // Apenas se houver algo para atualizar visualmente nesta tela

                        sonoNodeReference.child(registroId).setValue(registroSono)
                                .addOnSuccessListener(aVoid -> {
                                    String mensagemSucesso = "Sono registrado!";
                                    if (xpGanho > 0) {
                                        mensagemSucesso += " Você ganhou " + xpGanho + " XP!";
                                    }
                                    Toast.makeText(TelaSono.this, mensagemSucesso, Toast.LENGTH_SHORT).show();
                                    // finish(); // Boa prática: fechar a tela após uma ação bem-sucedida como esta.
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "salvarHorasSonoEAtualizarXPInternamente: Erro ao salvar detalhes do sono: " + e.getMessage());
                                    Toast.makeText(TelaSono.this, "XP atualizado, mas erro ao salvar detalhes do sono: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Log.e(TAG, "salvarHorasSonoEAtualizarXPInternamente: Erro ao atualizar XP: " + (task.getException() != null ? task.getException().getMessage() : "Erro desconhecido"));
                        Toast.makeText(TelaSono.this, "Erro ao atualizar XP: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
    }
}