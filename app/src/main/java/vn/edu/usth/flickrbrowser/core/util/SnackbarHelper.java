package vn.edu.usth.flickrbrowser.core.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import vn.edu.usth.flickrbrowser.R;

public class SnackbarHelper {

    public enum Type {
        SUCCESS("✅", "#E8F5E9"),
        ERROR("❌", "#FFEBEE"),
        INFO("ℹ️", "#E3F2FD"),
        FAVORITE("❤️", "#FFE0F0"),
        REMOVE("💔", "#FFE0E0"),
        DOWNLOAD("⬇️", "#FFF3E0"),
        SHARE("📤", "#E8EAF6");

        public final String emoji;
        public final String bgColor;

        Type(String emoji, String bgColor) {
            this.emoji = emoji;
            this.bgColor = bgColor;
        }
    }

    public static void show(View parentView, String message, Type type) {
        show(parentView, message, type, Snackbar.LENGTH_SHORT, null, null);
    }

    public static void show(View parentView, String message, Type type, int duration) {
        show(parentView, message, type, duration, null, null);
    }

    public static void show(View parentView, String message, Type type, int duration, 
                           String actionText, View.OnClickListener actionListener) {
        Snackbar snackbar = Snackbar.make(parentView, "", duration);
        
        // Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        
        // Make snackbar background transparent to avoid black border
        layout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        layout.setElevation(0f);
        
        // Hide the default text
        TextView textView = layout.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);
        
        // Inflate custom view
        LayoutInflater inflater = LayoutInflater.from(parentView.getContext());
        View customView = inflater.inflate(R.layout.custom_snackbar, null);
        
        // Setup custom view
        TextView icon = customView.findViewById(R.id.snackbar_icon);
        TextView text = customView.findViewById(R.id.snackbar_text);
        com.google.android.material.button.MaterialButton action = customView.findViewById(R.id.snackbar_action);
        
        icon.setText(type.emoji);
        text.setText(message);
        
        // Set icon background color
        View iconCard = customView.findViewById(R.id.snackbar_icon);
        if (iconCard.getParent() instanceof com.google.android.material.card.MaterialCardView) {
            ((com.google.android.material.card.MaterialCardView) iconCard.getParent())
                .setCardBackgroundColor(android.graphics.Color.parseColor(type.bgColor));
        }
        
        // Setup action button if provided
        if (actionText != null && actionListener != null) {
            action.setVisibility(View.VISIBLE);
            action.setText(actionText);
            action.setOnClickListener(v -> {
                actionListener.onClick(v);
                snackbar.dismiss();
            });
        }
        
        // Add custom view to snackbar - remove all padding
        layout.setPadding(0, 0, 0, 0);
        layout.addView(customView, 0);
        
        // Animate entrance
        customView.setAlpha(0f);
        customView.setTranslationY(50f);
        customView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start();
        
        snackbar.show();
    }
}
