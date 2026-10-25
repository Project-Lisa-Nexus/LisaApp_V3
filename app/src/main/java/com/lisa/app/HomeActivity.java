package com.lisa.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HomeActivity extends Activity {

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static final String COLORE_SFONDO_ROOT = "#000000";
    private static final String COLORE_SFONDO_SCHEDA = "#0A0A0A";
    private static final String COLORE_BORDO_SCHEDA = "#00E5FF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(COLORE_SFONDO_ROOT));

        boolean isWide = getResources().getConfiguration().screenWidthDp >= 600;

        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(isWide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        FrameLayout.LayoutParams lpMain = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpMain.bottomMargin = dp(60);
        root.addView(mainContainer, lpMain);

        // --- AVATAR ---
        LinearLayout avatarPanel = new LinearLayout(this);
        avatarPanel.setOrientation(LinearLayout.VERTICAL);
        avatarPanel.setGravity(Gravity.CENTER_HORIZONTAL); // Imposta la gravità al centro orizzontale
        LinearLayout.LayoutParams lpAvatar = isWide ?
            new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f) :
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        avatarPanel.setLayoutParams(lpAvatar);

        // Svuota il pannello
        avatarPanel.removeAllViews();

        // 1. Immagine di Lisa (in alto, occupa lo spazio)
        ImageView avatarImage = new ImageView(this);
        avatarImage.setImageResource(R.drawable.lisa_avatar);
        avatarImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lpAvatarImg = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        lpAvatarImg.setMargins(dp(16), dp(16), dp(16), dp(8));
        avatarPanel.addView(avatarImage, lpAvatarImg);

        // 2. Microfono con le onde (subito sotto)
        FrameLayout micButton = new FrameLayout(this);
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
        
        android.graphics.drawable.LayerDrawable layerMic = new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{outerMic, innerMic});
        layerMic.setLayerInset(1, dp(3), dp(3), dp(3), dp(3));
        micButton.setBackground(layerMic);

        ImageView micIcon = new ImageView(this);
        micIcon.setImageResource(R.drawable.mic_onde);
        micIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        micButton.addView(micIcon, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout.LayoutParams lpMicBtn = new LinearLayout.LayoutParams(dp(80), dp(80));
        lpMicBtn.gravity = Gravity.CENTER_HORIZONTAL;
        lpMicBtn.bottomMargin = dp(16);
        avatarPanel.addView(micButton, lpMicBtn);
        // --- CLICK E ANIMAZIONE MICROFONO ---
        android.animation.ObjectAnimator pulseX = android.animation.ObjectAnimator.ofFloat(micButton, "scaleX", 1f, 1.05f, 1f);
        android.animation.ObjectAnimator pulseY = android.animation.ObjectAnimator.ofFloat(micButton, "scaleY", 1f, 1.05f, 1f);
        pulseX.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        pulseY.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        pulseX.setDuration(1000);
        pulseY.setDuration(1000);

        final boolean[] animAttiva = {false};
        final android.os.Handler checkHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        final Runnable checkAscolto = new Runnable() {
            @Override
            public void run() {
                if (!LisaVoiceService.isInAscolto()) {
                    // Lisa si è fermata (es. per "basta"), fermiamo l'animazione
                    animAttiva[0] = false;
                    pulseX.cancel(); pulseY.cancel();
                    micButton.setScaleX(1f); micButton.setScaleY(1f);
                } else if (animAttiva[0]) {
                    checkHandler.postDelayed(this, 500);
                }
            }
        };

        micButton.setOnClickListener(v -> {
            if (LisaVoiceService.isInAscolto()) {
                // Ferma manualmente
                LisaVoiceService.fermaAscolto(this);
                animAttiva[0] = false;
                pulseX.cancel(); pulseY.cancel();
                micButton.setScaleX(1f); micButton.setScaleY(1f);
            } else {
                // Avvia
                LisaVoiceService.avviaAscolto(this);
                animAttiva[0] = true;
                pulseX.start(); pulseY.start();
                checkHandler.postDelayed(checkAscolto, 500);
                android.widget.Toast.makeText(this, "Lisa in ascolto...", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        // ------------------------------------



        // 3. Spazio vuoto in fondo per la futura vignetta (altezza fissa)
        View spazioVignetta = new View(this);
        LinearLayout.LayoutParams lpSpazio = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60));
        avatarPanel.addView(spazioVignetta, lpSpazio);

        mainContainer.addView(avatarPanel);

        // --- GRIGLIA ---
        LinearLayout gridPanel = new LinearLayout(this);
        gridPanel.setOrientation(LinearLayout.VERTICAL);
        gridPanel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpGrid = isWide ?
            new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f) :
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        gridPanel.setLayoutParams(lpGrid);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setPadding(dp(12), dp(12), dp(12), dp(12));

        grid.addView(createCard("Lisa AI", R.drawable.icon_ai, 0));
        grid.addView(createCard("Lisa Pad", R.drawable.icon_pad, 1));
        grid.addView(createCard("Lisa Voice", R.drawable.icon_voice, 0));
        grid.addView(createCard("Lisa Eye", R.drawable.icon_eye, 1));

        gridPanel.addView(grid);
        mainContainer.addView(gridPanel);

        // BottomBar nel FrameLayout radice, posizionata in basso (gravity BOTTOM)
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER);
        bottomBar.setBackgroundColor(Color.parseColor(COLORE_SFONDO_ROOT));
        bottomBar.setPadding(0, dp(8), 0, dp(8));

        bottomBar.addView(createBottomIcon(R.drawable.ic_home_home));
        bottomBar.addView(createBottomIcon(R.drawable.ic_home_star));
        bottomBar.addView(createBottomIcon(R.drawable.ic_home_profile));
        bottomBar.addView(createBottomIcon(R.drawable.ic_home_menu));

        FrameLayout.LayoutParams lpBottomBar = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60));
        lpBottomBar.gravity = Gravity.BOTTOM;
        root.addView(bottomBar, lpBottomBar);

        // Freccia in alto a destra nel FrameLayout radice
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_home_arrow);
        arrow.setPadding(dp(16), dp(16), dp(16), dp(16));
        arrow.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MainActivity.class)));

        FrameLayout.LayoutParams lpArrow = new FrameLayout.LayoutParams(dp(64), dp(64));
        lpArrow.gravity = Gravity.TOP | Gravity.END;
        lpArrow.topMargin = dp(8);
        lpArrow.rightMargin = dp(8);
        root.addView(arrow, lpArrow);

        setContentView(root);
    }

    private View createCard(String title, int imageResId, int col) {
        FrameLayout card = new FrameLayout(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(180);
        lp.columnSpec = GridLayout.spec(col, 1f);
        lp.setMargins(dp(8), dp(8), dp(8), dp(8));
        card.setLayoutParams(lp);

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

        android.graphics.drawable.LayerDrawable layerCard = new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{outerCard, innerCard});
        layerCard.setLayerInset(1, dp(3), dp(3), dp(3), dp(3));
        card.setBackground(layerCard);

        ImageView imgIcon = new ImageView(this);
        imgIcon.setImageResource(imageResId);
        imgIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams lpImg = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpImg.bottomMargin = dp(40);
        lpImg.topMargin = dp(8);
        lpImg.leftMargin = dp(8);
        lpImg.rightMargin = dp(8);
        card.addView(imgIcon, lpImg);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams lpRow = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        lpRow.gravity = Gravity.BOTTOM;
        lpRow.setMargins(dp(16), 0, dp(16), 0);
        card.addView(bottomRow, lpRow);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lpTv = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bottomRow.addView(tv, lpTv);

        TextView gear = new TextView(this);
        gear.setText("⚙️");
        gear.setTextSize(20);
        gear.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lpGear = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomRow.addView(gear, lpGear);

        return card;
    }

    private View createBottomIcon(int iconResId) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(iconResId);
        iv.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        iv.setLayoutParams(lp);
        return iv;
    }
}
