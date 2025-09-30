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
import vn.edu.usth.flickrbrowser.core.utils.ImageDownloader;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO = "extra_photo";

    private ImageView photoView;
    private TextView title, owner;
    private ChipGroup chipGroupTags;
    private ImageButton btnFavorite, btnShare, btnDownload;
    private PhotoItem currentPhoto;
    private FavoritesStore favoritesStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Setup toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Photo Details");
        }
        
        // Handle toolbar back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        photoView = findViewById(R.id.photoView);
        title = findViewById(R.id.photoTitle);
        owner = findViewById(R.id.photoOwner);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);

        favoritesStore = FavoritesStore.get(this);

        PhotoItem photo = (PhotoItem) getIntent().getSerializableExtra(EXTRA_PHOTO);
        if (photo != null) {
            currentPhoto = photo;
            bindPhoto(photo);
        }
    }

    private void bindPhoto(PhotoItem photo) {
        // Load ảnh full size
        Glide.with(this)
                .load(photo.getFullUrl())
                .placeholder(R.drawable.placeholder_grey)
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
            
            // Show feedback to user
            boolean isFav = favoritesStore.isFavorite(currentPhoto.id);
            String message = isFav ? "Added to favorites ❤️" : "Removed from favorites";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, photo.getFullUrl());
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnDownload.setOnClickListener(v -> {
            String fileName = ImageDownloader.generateFileName(photo.id, photo.title);
            ImageDownloader.downloadImage(this, photo.getFullUrl(), fileName);
        });
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