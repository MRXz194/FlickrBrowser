package vn.edu.usth.flickrbrowser.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.data.FavoritesStore;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.core.util.NetUtils;
import vn.edu.usth.flickrbrowser.core.util.SnackbarHelper;
import vn.edu.usth.flickrbrowser.core.utils.ImageDownloader;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO = "extra_photo";

    private ImageView photoView;
    private TextView title, owner;
    private ChipGroup chipGroupTags;
    private ImageButton btnFavorite, btnShare, btnOpen, btnDownload;
    private PhotoItem currentPhoto;
    private FavoritesStore favoritesStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        
        // Enable transitions
        supportPostponeEnterTransition();

        // Setup toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Photo Details");
        }
        
        // Handle toolbar back button with animation
        toolbar.setNavigationOnClickListener(v -> {
            supportFinishAfterTransition();
        });

        photoView = findViewById(R.id.photoView);
        title = findViewById(R.id.photoTitle);
        owner = findViewById(R.id.photoOwner);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnOpen = findViewById(R.id.btnOpen);
        btnDownload = findViewById(R.id.btnDownload);

        favoritesStore = FavoritesStore.get(this);
        
        // Observe favorites changes to update button state
        favoritesStore.live().observe(this, favoritesList -> {
            updateFavoriteButton();
        });

        PhotoItem photo = (PhotoItem) getIntent().getSerializableExtra(EXTRA_PHOTO);
        if (photo != null) {
            currentPhoto = photo;
            bindPhoto(photo);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update favorite button when returning to this screen
        updateFavoriteButton();
    }

    private void bindPhoto(PhotoItem photo) {
        // Load ảnh full size with error handling and transition
        Glide.with(this)
                .load(photo.getFullUrl())
                .placeholder(R.drawable.placeholder_grey)
                .error(R.drawable.placeholder_grey)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        Toast.makeText(DetailActivity.this, R.string.error_load_image, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        // Animate content fade in
                        animateContentIn();
                        return false;
                    }
                })
                .into(photoView);

        title.setText(photo.getTitle());
        owner.setText("by " + photo.getOwner());

        chipGroupTags.removeAllViews();
        if (photo.getTags() != null) {
            for (String tag : photo.getTags().split(" ")) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setOnClickListener(v -> {
                    // TODO: trigger search by tag
                });
                chipGroupTags.addView(chip);
            }
        }

        // Update favorite button state
        updateFavoriteButton();

        btnFavorite.setOnClickListener(v -> {
            favoritesStore.toggle(currentPhoto);
            updateFavoriteButton();
            
            // Show beautiful snackbar feedback
            boolean isFav = favoritesStore.isFavorite(currentPhoto.id);
            if (isFav) {
                SnackbarHelper.show(findViewById(android.R.id.content), 
                    "Added to favorites", 
                    SnackbarHelper.Type.FAVORITE);
            } else {
                SnackbarHelper.show(findViewById(android.R.id.content), 
                    "Removed from favorites", 
                    SnackbarHelper.Type.REMOVE);
            }
        });

        btnShare.setOnClickListener(v -> {
            // Check network before sharing
            if (!NetUtils.hasNetwork(this)) {
                SnackbarHelper.show(findViewById(android.R.id.content), 
                    getString(R.string.error_no_network), 
                    SnackbarHelper.Type.ERROR);
                return;
            }
            
            // Validate URL
            String url = photo.getFullUrl();
            if (url == null || url.isEmpty()) {
                SnackbarHelper.show(findViewById(android.R.id.content), 
                    getString(R.string.error_empty_url), 
                    SnackbarHelper.Type.ERROR);
                return;
            }
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
            
            SnackbarHelper.show(findViewById(android.R.id.content), 
                "Sharing photo...", 
                SnackbarHelper.Type.SHARE);
        });

        btnOpen.setOnClickListener(v -> {
            // Check network before opening
            if (!NetUtils.hasNetwork(this)) {
                SnackbarHelper.show(findViewById(android.R.id.content), 
                    getString(R.string.error_no_network), 
                    SnackbarHelper.Type.ERROR);
                return;
            }
            
            // Get Flickr page URL (not image URL)
            String url = photo.getFlickrPageUrl();
            if (url == null || url.isEmpty()) {
                SnackbarHelper.show(findViewById(android.R.id.content), 
                    getString(R.string.error_empty_url), 
                    SnackbarHelper.Type.ERROR);
                return;
            }
            
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setData(Uri.parse(url));
            startActivity(openIntent);
            
            SnackbarHelper.show(findViewById(android.R.id.content), 
                "Opening in browser...", 
                SnackbarHelper.Type.INFO);
        });

        btnDownload.setOnClickListener(v -> {
            String fileName = ImageDownloader.generateFileName(photo.id, photo.title);
            ImageDownloader.downloadImage(this, photo.getFullUrl(), fileName);
        });
    }

    private void animateContentIn() {
        // Animate title slide in
        title.setAlpha(0f);
        title.setTranslationY(30f);
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start();
        
        // Animate owner slide in
        owner.setAlpha(0f);
        owner.setTranslationY(30f);
        owner.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(200)
            .start();
        
        // Animate chips
        chipGroupTags.setAlpha(0f);
        chipGroupTags.setTranslationY(30f);
        chipGroupTags.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(300)
            .start();
    }
    
    private void updateFavoriteButton() {
        if (currentPhoto != null && favoritesStore != null) {
            boolean isFavorite = favoritesStore.isFavorite(currentPhoto.id);
            btnFavorite.setImageResource(isFavorite ? 
                R.drawable.baseline_favorite_24 : 
                R.drawable.outline_favorite_24);
            
            // Update color: pink when favorited, default when not
            androidx.core.widget.ImageViewCompat.setImageTintList(btnFavorite, 
                android.content.res.ColorStateList.valueOf(
                    isFavorite ? 0xFFFF4081 : 0xFF757575)); // Pink or gray
            
            btnFavorite.setContentDescription(isFavorite ? 
                getString(R.string.cd_unfavorite) : 
                getString(R.string.cd_favorite));
        }
    }
}