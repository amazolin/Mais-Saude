package com.example.maissade;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TelaSono extends AppCompatActivity {

    private int sleepHours = 0;
    private TextView textview_hours;
    private Button buttonIncrease, buttonDecrease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_sono);

        // Aplicar insets visuais
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Estes devem estar fora do listener!
        textview_hours = findViewById(R.id.textview_hours);
        buttonIncrease = findViewById(R.id.button_increase);
        buttonDecrease = findViewById(R.id.button_decrease);

        // Inicializa o valor
        updateTextView();

        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sleepHours++;
                updateTextView();
            }
        });

        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sleepHours > 0) {
                    sleepHours--;
                    updateTextView();
                }
            }
        });
        TextView dicaText = findViewById(R.id.dica_text);
        Button buttonDica = findViewById(R.id.button_dica);

        buttonDica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dicaText.setVisibility(View.VISIBLE);
            }
        });

        dicaText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dicaText.setVisibility(View.GONE);
            }
        });

    }

    private void updateTextView() {
        textview_hours.setText(sleepHours + " h");
    }


}
