package vn.edu.usth.flickrbrowser.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.data.FavoritesStore;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.core.util.SnackbarHelper;
import vn.edu.usth.flickrbrowser.core.utils.ImageDownloader;

public class GalleryActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTOS = "extra_photos";
    public static final String EXTRA_POSITION = "extra_position";

    private ViewPager2 viewPager;
    private GalleryAdapter adapter;
    private List<PhotoItem> photos;
    private int currentPosition = 0;

    private AppBarLayout appBar;
    private MaterialToolbar toolbar;
    private TextView photoCounter, photoTitle, photoOwner;
    private View bottomBar;
    private MaterialButton btnFavorite, btnShare, btnDownload, btnInfo;

    private FavoritesStore favoritesStore;
    private boolean isUiVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen immersive mode
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        setContentView(R.layout.activity_gallery);

        // Get data from intent
        Intent intent = getIntent();
        photos = (List<PhotoItem>) intent.getSerializableExtra(EXTRA_PHOTOS);
        currentPosition = intent.getIntExtra(EXTRA_POSITION, 0);

        if (photos == null || photos.isEmpty()) {
            finish();
            return;
        }

        initViews();
        setupViewPager();
        setupButtons();
        updateUI(currentPosition);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        appBar = findViewById(R.id.appBar);
        toolbar = findViewById(R.id.toolbar);
        photoCounter = findViewById(R.id.photoCounter);
        photoTitle = findViewById(R.id.photoTitle);
        photoOwner = findViewById(R.id.photoOwner);
        bottomBar = findViewById(R.id.bottomBar);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);
        btnInfo = findViewById(R.id.btnInfo);

        favoritesStore = FavoritesStore.get(this);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        adapter = new GalleryAdapter(photos);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        // Toggle UI on photo click
        adapter.setOnPhotoClickListener(this::toggleUI);

        // Update UI when page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updateUI(position);
            }
        });
    }

    private void setupButtons() {
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> sharePhoto());
        btnDownload.setOnClickListener(v -> downloadPhoto());
        btnInfo.setOnClickListener(v -> openPhotoDetail());
    }

    private void updateUI(int position) {
        PhotoItem photo = adapter.getPhotoAt(position);
        if (photo == null) return;

        // Update counter
        photoCounter.setText((position + 1) + " / " + photos.size());

        // Update photo info
        photoTitle.setText(photo.getTitle());
        photoOwner.setText("by " + photo.getOwner());

        // Update favorite button
        updateFavoriteButton(photo);
    }

    private void updateFavoriteButton(PhotoItem photo) {
        boolean isFavorite = favoritesStore.isFavorite(photo.id);
        btnFavorite.setIcon(getDrawable(isFavorite ? 
            R.drawable.baseline_favorite_24 : 
            R.drawable.outline_favorite_24));
        btnFavorite.setIconTint(getColorStateList(isFavorite ? 
            android.R.color.holo_red_light : 
            android.R.color.white));
    }

    private void toggleUI() {
        isUiVisible = !isUiVisible;
        
        appBar.animate()
            .alpha(isUiVisible ? 1f : 0f)
            .translationY(isUiVisible ? 0f : -appBar.getHeight())
            .setDuration(300)
            .start();

        bottomBar.animate()
            .alpha(isUiVisible ? 1f : 0f)
            .translationY(isUiVisible ? 0f : bottomBar.getHeight())
            .setDuration(300)
            .start();
    }

    private void toggleFavorite() {
        PhotoItem photo = adapter.getPhotoAt(currentPosition);
        if (photo == null) return;

        favoritesStore.toggle(photo);
        updateFavoriteButton(photo);

        boolean isFavorite = favoritesStore.isFavorite(photo.id);
        SnackbarHelper.show(findViewById(android.R.id.content),
            isFavorite ? "Added to favorites" : "Removed from favorites",
            isFavorite ? SnackbarHelper.Type.FAVORITE : SnackbarHelper.Type.REMOVE);
    }

    private void sharePhoto() {
        PhotoItem photo = adapter.getPhotoAt(currentPosition);
        if (photo == null) return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, photo.getFullUrl());
        startActivity(Intent.createChooser(shareIntent, "Share photo"));
    }

    private void downloadPhoto() {
        PhotoItem photo = adapter.getPhotoAt(currentPosition);
        if (photo == null) return;

        String fileName = ImageDownloader.generateFileName(photo.id, photo.title);
        ImageDownloader.downloadImage(this, photo.getFullUrl(), fileName, 
            findViewById(android.R.id.content));
    }

    private void openPhotoDetail() {
        PhotoItem photo = adapter.getPhotoAt(currentPosition);
        if (photo == null) return;

        Intent intent = new Intent(this, vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);
        intent.putExtra(vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.EXTRA_PHOTO, photo);
        startActivity(intent);
    }
}
