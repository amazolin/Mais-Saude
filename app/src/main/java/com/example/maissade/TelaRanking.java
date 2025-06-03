package com.example.maissade;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.model.Ranking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class TelaRanking extends AppCompatActivity {

    private ListView listaRanking;
    private ArrayList<Ranking> listaUsuarios = new ArrayList<>();
    private ArrayAdapter<Ranking> adapter;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_ranking);

        listaRanking = findViewById(R.id.listaRanking);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaUsuarios);
        listaRanking.setAdapter(adapter);

        usuariosRef = FirebaseDatabase.getInstance("https://mais-saude-21343-default-rtdb.firebaseio.com/")
                .getReference("usuarios");

        carregarRanking();
    }

    private void carregarRanking() {
        usuariosRef.orderByChild("xp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaUsuarios.clear();

                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {
                    String nome = usuarioSnap.child("nome").getValue(String.class);

                    Object xpObj = usuarioSnap.child("xp").getValue();
                    Long xp = 0L;

                    if (xpObj instanceof Long) {
                        xp = (Long) xpObj;
                    } else if (xpObj instanceof Integer) {
                        xp = ((Integer) xpObj).longValue();
                    } else if (xpObj instanceof String) {
                        try {
                            xp = Long.parseLong((String) xpObj);
                        } catch (NumberFormatException e) {
                            continue; // ignora usuário com xp inválido
                        }
                    } else {
                        continue; // ignora usuário com xp ausente ou de tipo inesperado
                    }


                    if (nome != null) {
                        listaUsuarios.add(new Ranking(nome, xp));
                    }
                }

                // Inverte a lista para mostrar maior XP no topo
                Collections.reverse(listaUsuarios);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TelaRanking.this, "Erro ao carregar ranking", Toast.LENGTH_SHORT).show();
                Log.e("FIREBASE_ERROR", "Erro: " + error.getMessage());
            }
        });
    }
}
