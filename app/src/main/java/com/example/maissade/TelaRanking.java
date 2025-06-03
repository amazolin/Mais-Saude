package com.example.maissade;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maissade.R;
import com.example.model.Ranking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.maissade.adapters.RankingAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

    public class TelaRanking extends AppCompatActivity {

        private ListView listaRanking;
        private ArrayList<Ranking> rankingList;
        private RankingAdapter adapter;  // alterado para nosso adapter customizado
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
            carregarRanking();
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

                // Ordena do maior para o menor XP
                Collections.sort(rankingList, new Comparator<Ranking>() {
                    @Override
                    public int compare(Ranking o1, Ranking o2) {
                        return Long.compare(o2.getXp(), o1.getXp());
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TelaRanking.this, "Erro ao carregar usuários", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
