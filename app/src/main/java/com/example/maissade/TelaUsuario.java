package com.example.maissade;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
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

import com.example.model.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TelaUsuario extends AppCompatActivity {
    // <editor-fold desc="Variaveis">
    private ImageView imgAvatarPerfil;
    private TextView txtNomePerfil, txtIdadeSexoPerfil, txtPesoPerfil, txtAlturaPerfil, txtTipoSanguineoPerfil, txtIMCPerfil, txtNivelPerfil;
    private Button btnSair;
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
    //</editor-fold>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_usuario);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = null;

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            if (toolbar.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) toolbar.getChildAt(i);
                if (tv.getText() != null && tv.getText().toString().equals("Perfil")) {
                    toolbarTitle = tv;
                    break;
                }
            }
        }

        if (toolbarTitle != null) {
            Typeface minhaFonte = ResourcesCompat.getFont(this, R.font.aboreto); // substitua por sua fonte
            toolbarTitle.setTypeface(minhaFonte);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        // <editor-fold desc="Ligação ao Firebase">
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/").getReference("usuarios");
        //</editor-fold>
        // <editor-fold desc="find.by.view">
        imgAvatarPerfil = findViewById(R.id.imgAvatarPerfil);
        txtNomePerfil = findViewById(R.id.txtNomePerfil);
        txtIdadeSexoPerfil = findViewById(R.id.txtIdadeSexoPerfil);
        txtPesoPerfil = findViewById(R.id.txtPesoPerfil);
        txtAlturaPerfil = findViewById(R.id.txtAlturaPerfil);
        txtTipoSanguineoPerfil = findViewById(R.id.txtTipoSanguineoPerfil);
        txtIMCPerfil = findViewById(R.id.txtIMCPerfil);
        txtNivelPerfil = findViewById(R.id.txtNivelPerfil);
        //</editor-fold>
        carregarDadosUsuario();

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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, TelaRanking.class));
                finish();
                return true;
            }

            return false;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_usuario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_apagar_conta) {
            confirmarExclusao();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void carregarDadosUsuario() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            usuariosRef.child(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String sexo = snapshot.child("sexo").getValue(String.class);
                    String tipoSanguineo = snapshot.child("tipoSanguineo").getValue(String.class);
                    Long idade = snapshot.child("idade").getValue(Long.class);
                    Long peso = snapshot.child("peso").getValue(Long.class);
                    Long altura = snapshot.child("altura").getValue(Long.class);

                    // Pega os dados do usuário e coloca na tela de usuário.
                    txtNomePerfil.setText(nome != null ? nome : "Nome não encontrado");
                    txtPesoPerfil.setText(peso != null ? peso + " kg" : "0 kg");
                    txtAlturaPerfil.setText(altura != null ? altura + " cm" : "0 cm");
                    txtIdadeSexoPerfil.setText((idade != null ? idade + " anos" : "Idade não informada") +
                            (sexo != null ? ", " + sexo : ""));
                    txtTipoSanguineoPerfil.setText(tipoSanguineo != null ? tipoSanguineo : "Não informado");




                    // Realiza o cálculo do imc e por enquanto deixa o nível da pessoa como os níveis do imc
                    if (peso != null && altura != null && altura > 0) {
                        double alturaMetros = altura / 100.0;
                        double imc = peso / (alturaMetros * alturaMetros);

                        // IMC com 2 casas decimais
                        txtIMCPerfil.setText(String.format("%.2f", imc));

                        // Determina o nível de acordo com imc
                        String nivel;
                        if (imc < 18.5) {
                            nivel = "Abaixo do peso";
                        } else if (imc >= 25) {
                            nivel = "Acima do peso";
                        } else {
                            nivel = "Normal";
                        }
                        txtNivelPerfil.setText(nivel);
                    } else {
                        txtIMCPerfil.setText("--");
                        txtNivelPerfil.setText("Dados incompletos");
                    }
                } else {
                    Toast.makeText(TelaUsuario.this, "Usuário não encontrado no banco de dados", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(TelaUsuario.this, "Erro ao buscar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(TelaUsuario.this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar conta")
                .setMessage("Tem certeza que deseja apagar sua conta? Essa ação é irreversível. Você ira perder " +
                        "todos os seus itens e XP.")
                .setPositiveButton("Sim", (dialog, which) -> apagarConta())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void apagarConta() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        if (user != null) {
            String uid = user.getUid();

            // Primeiro apaga do banco de dados
            usuariosRef.child(uid).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Agora apaga da autenticação
                        user.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show();
                                    // Voltar à tela de login
                                    startActivity(new Intent(this, Login.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erro ao excluir do auth: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao excluir dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}

