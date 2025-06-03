package com.example.maissade;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.model.Ranking;
import com.example.maissade.adapters.RankingAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TelaRanking extends AppCompatActivity {
    private String nomeUsuarioLogado = "NomeDoUsuarioLogado"; // será atualizado dinamicamente
    private ListView listaRanking;
    private ArrayList<Ranking> rankingList;
    private RankingAdapter adapter;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_ranking);

        listaRanking = findViewById(R.id.listaRanking);
        rankingList = new ArrayList<>();
        adapter = new RankingAdapter(this, rankingList);
        listaRanking.setAdapter(adapter);

        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String emailLogado = user.getEmail();

            usuariosRef.orderByChild("email").equalTo(emailLogado).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String nome = ds.child("nome").getValue(String.class);
                        if (nome != null) {
                            nomeUsuarioLogado = nome;
                            carregarRanking();
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TelaRanking.this, "Erro ao obter dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            nomeUsuarioLogado = "";
            carregarRanking();
        }
    }

    private void carregarRanking() {
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rankingList.clear();

                for (DataSnapshot usuarioSnapshot : snapshot.getChildren()) {
                    String nome = usuarioSnapshot.child("nome").getValue(String.class);
                    Long xp = usuarioSnapshot.child("xp").getValue(Long.class);

                    if (nome != null && xp != null) {
                        rankingList.add(new Ranking(nome, xp));
                    }
                }

                // Ordenar por XP
                Collections.sort(rankingList, new Comparator<Ranking>() {
                    @Override
                    public int compare(Ranking o1, Ranking o2) {
                        return Long.compare(o2.getXp(), o1.getXp());
                    }
                });

                // Definir posição real
                for (int i = 0; i < rankingList.size(); i++) {
                    rankingList.get(i).setPosicaoReal(i + 1);
                }

                // Montar top 3 + o usuário logado (se não estiver no top)
                ArrayList<Ranking> listaParaExibir = new ArrayList<>();

                int maxTop = Math.min(3, rankingList.size());
                for (int i = 0; i < maxTop; i++) {
                    listaParaExibir.add(rankingList.get(i));
                }

                boolean usuarioNosTop3 = false;
                for (int i = 0; i < maxTop; i++) {
                    if (rankingList.get(i).getNome().equals(nomeUsuarioLogado)) {
                        usuarioNosTop3 = true;
                        break;
                    }
                }

                if (!usuarioNosTop3) {
                    for (Ranking r : rankingList) {
                        if (r.getNome().equals(nomeUsuarioLogado)) {
                            listaParaExibir.add(r);
                            break;
                        }
                    }
                }

                rankingList.clear();
                rankingList.addAll(listaParaExibir);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TelaRanking.this, "Erro ao carregar usuários", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
