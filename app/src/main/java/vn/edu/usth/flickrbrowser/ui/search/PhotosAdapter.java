package vn.edu.usth.flickrbrowser.ui.search;

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
import vn.edu.usth.flickrbrowser.core.data.FavoritesStore;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH> {
    private final List<PhotoItem> data = new ArrayList<>();
    public interface OnItemClick { void onClick(PhotoItem item); }
    private final OnItemClick onItemClick;

    public PhotosAdapter(){ this(null); }
    public PhotosAdapter(OnItemClick cb){ this.onItemClick = cb; }

    public void submitList(List<PhotoItem> items) {
        data.clear();
        if (items != null) {
            // Create new list to ensure proper update
            data.addAll(new ArrayList<>(items));
        }
        notifyDataSetChanged();
    }

    public void addItems(List<PhotoItem> newItems) {
        if (newItems != null && !newItems.isEmpty()) {
            int startPosition = data.size();
            data.addAll(newItems);
            notifyItemRangeInserted(startPosition, newItems.size());
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PhotoItem it = data.get(pos);
        Glide.with(h.img.getContext())
                .load(it.getThumbUrl())
                .placeholder(R.drawable.bg_skeleton_rounded)
                .centerCrop()
                .into(h.img);
        
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(it);
        });

        // Favorite state - use the actual position item
        FavoritesStore store = FavoritesStore.get(h.itemView.getContext());
        boolean fav = store.isFavorite(it.id);
        h.btnFavorite.setImageResource(fav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
        // Set tint color based on favorite state
        androidx.core.widget.ImageViewCompat.setImageTintList(h.btnFavorite, 
            android.content.res.ColorStateList.valueOf(
                fav ? 0xFFFF4081 : 0xFFFFFFFF)); // Pink when favorited, white when not
        
        // Remove old listeners to prevent issues
        h.btnFavorite.setOnClickListener(null);
        
        h.btnFavorite.setOnClickListener(v -> {
            // Get current position to avoid stale data
            int currentPos = h.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            
            PhotoItem currentItem = data.get(currentPos);
            android.util.Log.d("PhotosAdapter", "Toggle favorite for: " + currentItem.id + " - " + currentItem.title);
            
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

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageButton btnFavorite;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
