package vn.edu.usth.flickrbrowser.ui.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder> {

    private final List<PhotoItem> photos = new ArrayList<>();
    private OnPhotoClickListener clickListener;

    public interface OnPhotoClickListener {
        void onPhotoClick();
    }

    public GalleryAdapter(List<PhotoItem> photoList) {
        if (photoList != null) {
            photos.addAll(photoList);
        }
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoItem photo = photos.get(position);
        holder.bind(photo);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public PhotoItem getPhotoAt(int position) {
        if (position >= 0 && position < photos.size()) {
            return photos.get(position);
        }
        return null;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        ProgressBar progressBar;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
            progressBar = itemView.findViewById(R.id.progressBar);

            // Click to toggle UI
            photoView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onPhotoClick();
                }
            });
        }

        void bind(PhotoItem photo) {
            progressBar.setVisibility(View.VISIBLE);

            Glide.with(itemView.getContext())
                    .load(photo.getFullUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_grey)
                    .error(R.drawable.placeholder_grey)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                                     Object model,
                                                     com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                     boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                        Object model,
                                                        com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                        com.bumptech.glide.load.DataSource dataSource,
                                                        boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(photoView);
        }
    }
}
