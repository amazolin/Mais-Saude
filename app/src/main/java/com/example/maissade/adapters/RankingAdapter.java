package com.example.maissade.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.maissade.R;
import com.example.model.Ranking;

import java.util.List;

public class RankingAdapter extends ArrayAdapter<Ranking> {

    private Context context;
    private List<Ranking> rankingList;

    public RankingAdapter(Context context, List<Ranking> rankingList) {
        super(context, 0, rankingList);
        this.context = context;
        this.rankingList = rankingList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.item_ranking, parent, false);
        }

        Ranking currentRanking = rankingList.get(position);

        TextView posicao = listItem.findViewById(R.id.posicaoRanking);
        TextView nome = listItem.findViewById(R.id.nomeUsuario);
        TextView xp = listItem.findViewById(R.id.xpUsuario);

        // 🥇 Emojis para top 3 ou números simples
        switch (position) {
            case 0:
                posicao.setText("🥇");
                break;
            case 1:
                posicao.setText("🥈");
                break;
            case 2:
                posicao.setText("🥉");
                break;
            default:
                posicao.setText(String.valueOf(position + 1));
        }

        nome.setText(currentRanking.getNome());
        xp.setText(currentRanking.getXp() + " XP");

        return listItem;
    }
}

