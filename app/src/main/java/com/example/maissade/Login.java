package com.example.maissade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    // <editor-fold desc="Variavies">
    private Button btnCadastrar, btnLogin;
    private EditText txtUsername, txtPassword;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
// </editor-fold>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // <editor-fold desc="find.by.view">
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnLogin = findViewById(R.id.btnLogin);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        // </editor-fold>
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios");

        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Cadastro.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = txtUsername.getText().toString().trim();
            String senha = txtPassword.getText().toString();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(authResult -> {
                        String uid = auth.getCurrentUser().getUid();

                        usuariosRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Usuario usuario = snapshot.getValue(Usuario.class);
                                    Toast.makeText(Login.this, "Bem-vindo, " + usuario.nome, Toast.LENGTH_SHORT).show();

                                    // Redireciona para a próxima tela
                                    Intent intent = new Intent(Login.this, MainActivity.class); // Troque para sua activity principal
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Usuário não encontrado no banco", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Login.this, "Erro ao acessar dados: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Login.this, "Erro de login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
