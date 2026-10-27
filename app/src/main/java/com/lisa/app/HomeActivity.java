package com.lisa.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

public class HomeActivity extends Activity {

    private static final String COLORE_SFONDO_SCHEDA = "#0A0A0A";
    private static final String COLORE_BORDO_SCHEDA = "#00E5FF";

    private FrameLayout micButton;
    private ObjectAnimator pulseX;
    private ObjectAnimator pulseY;
    private boolean[] animAttiva = {false};
    private final Handler checkHandler = new Handler(Looper.getMainLooper());

    private final Runnable checkAscolto = new Runnable() {
        @Override
        public void run() {
            if (!LisaVoiceService.isInAscolto()) {
                // Lisa si è fermata (es. per "basta"), fermiamo l'animazione
                animAttiva[0] = false;
                if (pulseX != null) pulseX.cancel();
                if (pulseY != null) pulseY.cancel();
                if (micButton != null) {
                    micButton.setScaleX(1f);
                    micButton.setScaleY(1f);
                }
            } else if (animAttiva[0]) {
                checkHandler.postDelayed(this, 500);
            }
        }
    };

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Imposta i colori delle barre di sistema
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        // Carica il layout XML (Fase 2)
        setContentView(R.layout.activity_home);

        // --- COLLEGAMENTO ID E LISTENERS ---

        // Freccia in alto a destra
        ImageView arrow = findViewById(R.id.arrow_image);
        if (arrow != null) {
            arrow.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MainActivity.class)));
        }

        // Microfono
        micButton = findViewById(R.id.mic_button);
        if (micButton != null) {
            // Applica lo stile dinamico al microfono (bordo neon) per mantenere continuità visiva
            GradientDrawable outerMic = new GradientDrawable();
            outerMic.setShape(GradientDrawable.RECTANGLE);
            outerMic.setCornerRadius(dp(16));
            outerMic.setColor(Color.parseColor(COLORE_SFONDO_SCHEDA));
            outerMic.setStroke(dp(4), Color.parseColor(COLORE_BORDO_SCHEDA));

            GradientDrawable innerMic = new GradientDrawable();
            innerMic.setShape(GradientDrawable.RECTANGLE);
            innerMic.setColor(Color.TRANSPARENT);
            innerMic.setCornerRadius(dp(12));
            innerMic.setStroke(dp(2), Color.WHITE);

            android.graphics.drawable.LayerDrawable layerMic = new android.graphics.drawable.LayerDrawable(
                new android.graphics.drawable.Drawable[]{outerMic, innerMic});
            layerMic.setLayerInset(1, dp(3), dp(3), dp(3), dp(3));
            micButton.setBackground(layerMic);

            // Preparazione animazioni pulse
            pulseX = ObjectAnimator.ofFloat(micButton, "scaleX", 1f, 1.05f, 1f);
            pulseY = ObjectAnimator.ofFloat(micButton, "scaleY", 1f, 1.05f, 1f);
            pulseX.setRepeatCount(ValueAnimator.INFINITE);
            pulseY.setRepeatCount(ValueAnimator.INFINITE);
            pulseX.setDuration(1000);
            pulseY.setDuration(1000);

            micButton.setOnClickListener(v -> {
                if (LisaVoiceService.isInAscolto()) {
                    LisaVoiceService.fermaAscolto(this);
                    animAttiva[0] = false;
                    pulseX.cancel();
                    pulseY.cancel();
                    micButton.setScaleX(1f);
                    micButton.setScaleY(1f);
                } else {
                    LisaVoiceService.avviaAscolto(this);
                    animAttiva[0] = true;
                    pulseX.start();
                    pulseY.start();
                    checkHandler.postDelayed(checkAscolto, 500);
                    Toast.makeText(this, "Lisa in ascolto...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Card AI, Pad, Voice, Eye
        setupCard(R.id.card_ai);
        setupCard(R.id.card_pad);
        setupCard(R.id.card_voice);
        setupCard(R.id.card_eye);

        // Bottom Bar Icons
        setupBottomIcon(R.id.bottom_home);
        setupBottomIcon(R.id.bottom_star);
        setupBottomIcon(R.id.bottom_profile);
        setupBottomIcon(R.id.bottom_menu);
    }

    private void setupCard(int cardId) {
        View card = findViewById(cardId);
        if (card != null) {
            // Applica lo stile neon alle card (bordo e sfondo)
            GradientDrawable outerCard = new GradientDrawable();
            outerCard.setShape(GradientDrawable.RECTANGLE);
            outerCard.setColor(Color.parseColor(COLORE_SFONDO_SCHEDA));
            outerCard.setCornerRadius(dp(16));
            outerCard.setStroke(dp(4), Color.parseColor(COLORE_BORDO_SCHEDA));

            GradientDrawable innerCard = new GradientDrawable();
            innerCard.setShape(GradientDrawable.RECTANGLE);
            innerCard.setColor(Color.TRANSPARENT);
            innerCard.setCornerRadius(dp(12));
            innerCard.setStroke(dp(2), Color.WHITE);

            android.graphics.drawable.LayerDrawable layerCard = new android.graphics.drawable.LayerDrawable(
                new android.graphics.drawable.Drawable[]{outerCard, innerCard});
            layerCard.setLayerInset(1, dp(3), dp(3), dp(3), dp(3));
            card.setBackground(layerCard);

            // Listener (al momento vuoti o pronti per future attività)
            card.setOnClickListener(v -> {
                // Azione card
            });
        }
    }

    private void setupBottomIcon(int iconId) {
        View icon = findViewById(iconId);
        if (icon != null) {
            icon.setOnClickListener(v -> {
                // Azione bottom bar
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkHandler.removeCallbacks(checkAscolto);
    }
}
