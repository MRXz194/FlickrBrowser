package vn.edu.usth.flickrbrowser.ui.explore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.core.data.FavoritesStore;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.VH> {
    private final List<PhotoItem> data = new ArrayList<>();
    public interface OnItemClick {
        void onClick(PhotoItem item);
    }
    private final OnItemClick onItemClick;

    public ExploreAdapter() {
        this(null);
    }

    public ExploreAdapter(OnItemClick cb) {
        this.onItemClick = cb;
    }

    public void setData(List<PhotoItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(List<PhotoItem> newItems) {
        if (newItems != null && !newItems.isEmpty()) {
            int startPosition = data.size();
            data.addAll(newItems);
            notifyItemRangeInserted(startPosition, newItems.size());
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PhotoItem p = data.get(pos);
        Glide.with(h.img.getContext())
                .load(p.getThumbUrl())
                .placeholder(R.drawable.bg_skeleton_rounded)
                .centerCrop()
                .into(h.img);
        
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onClick(p);
            } else {
                // Default: Open gallery
                openGallery(h.itemView.getContext(), h.getAdapterPosition());
            }
        });
        
        FavoritesStore store = FavoritesStore.get(h.itemView.getContext());
        boolean fav = store.isFavorite(p.id);
        h.btnFavorite.setImageResource(fav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
        // Set tint color based on favorite state
        androidx.core.widget.ImageViewCompat.setImageTintList(h.btnFavorite, 
            android.content.res.ColorStateList.valueOf(
                fav ? 0xFFFF4081 : 0xFFFFFFFF)); // Pink when favorited, white when not
        
        // Remove old listeners
        h.btnFavorite.setOnClickListener(null);
        
        h.btnFavorite.setOnClickListener(v -> {
            // Get current position to avoid stale data
            int currentPos = h.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            
            PhotoItem currentItem = data.get(currentPos);
            android.util.Log.d("ExploreAdapter", "Toggle favorite for: " + currentItem.id + " - " + currentItem.title);
            
            store.toggle(currentItem);
            
            // Update button immediately
            boolean nowFav = store.isFavorite(currentItem.id);
            h.btnFavorite.setImageResource(nowFav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
            // Update tint color
            androidx.core.widget.ImageViewCompat.setImageTintList(h.btnFavorite, 
                android.content.res.ColorStateList.valueOf(
                    nowFav ? 0xFFFF4081 : 0xFFFFFFFF)); // Pink when favorited, white when not
            
            // Animate the button
            android.view.animation.Animation anim = android.view.animation.AnimationUtils.loadAnimation(
                h.itemView.getContext(), R.anim.favorite_scale);
            h.btnFavorite.startAnimation(anim);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void openGallery(android.content.Context context, int position) {
        android.content.Intent intent = new android.content.Intent(context, 
            vn.edu.usth.flickrbrowser.ui.gallery.GalleryActivity.class);
        intent.putExtra(vn.edu.usth.flickrbrowser.ui.gallery.GalleryActivity.EXTRA_PHOTOS, 
            (java.io.Serializable) new ArrayList<>(data));
        intent.putExtra(vn.edu.usth.flickrbrowser.ui.gallery.GalleryActivity.EXTRA_POSITION, position);
        context.startActivity(intent);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageButton btnFavorite;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgPhoto);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }
}