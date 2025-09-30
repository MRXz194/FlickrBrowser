package vn.edu.usth.flickrbrowser.ui.about;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import vn.edu.usth.flickrbrowser.R;

public class AboutFragment extends Fragment {

    private View iconCard, appName, versionBadge;
    private View descCard, teamCard, featuresCard;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        iconCard = view.findViewById(R.id.iconCard);
        appName = view.findViewById(R.id.appName);
        versionBadge = view.findViewById(R.id.versionBadge);
        descCard = view.findViewById(R.id.descCard);
        teamCard = view.findViewById(R.id.teamCard);
        featuresCard = view.findViewById(R.id.featuresCard);
        
        // Start animations
        animateEntrance();
        
        // Add click animations for cards
        setupCardAnimations();
    }
    
    private void animateEntrance() {
        // Hide all views initially
        iconCard.setAlpha(0f);
        iconCard.setScaleX(0.3f);
        iconCard.setScaleY(0.3f);
        appName.setAlpha(0f);
        appName.setTranslationY(30f);
        versionBadge.setAlpha(0f);
        versionBadge.setScaleX(0.8f);
        versionBadge.setScaleY(0.8f);
        descCard.setAlpha(0f);
        descCard.setTranslationY(50f);
        teamCard.setAlpha(0f);
        teamCard.setTranslationY(50f);
        featuresCard.setAlpha(0f);
        featuresCard.setTranslationY(50f);
        
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Animate icon with bounce
        handler.postDelayed(() -> {
            Animation bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce_in);
            iconCard.startAnimation(bounceAnim);
            iconCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }, 100);
        
        // Animate app name
        handler.postDelayed(() -> {
            appName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }, 300);
        
        // Animate version badge
        handler.postDelayed(() -> {
            versionBadge.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }, 500);
        
        // Animate cards sequentially
        animateCard(descCard, 700);
        animateCard(teamCard, 850);
        animateCard(featuresCard, 1000);
    }
    
    private void animateCard(View card, long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }, delay);
    }
    
    private void setupCardAnimations() {
        // Add scale animation on click for interactive feel
        setupCardClick(descCard);
        setupCardClick(teamCard);
        setupCardClick(featuresCard);
    }
    
    private void setupCardClick(View card) {
        card.setOnClickListener(v -> {
            // Quick scale animation for feedback
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
        });
    }
}
