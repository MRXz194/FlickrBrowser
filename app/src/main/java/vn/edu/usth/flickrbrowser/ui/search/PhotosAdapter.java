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
        if (items != null) data.addAll(items);
        notifyDataSetChanged(); // đủ dùng lúc này; sau có thể đổi sang DiffUtil
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
                .placeholder(R.drawable.bg_skeleton_rounded) // tạm
                .centerCrop()
                .into(h.img);
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(it);
        });

        // Favorite state
        FavoritesStore store = FavoritesStore.get(h.itemView.getContext());
        boolean fav = store.isFavorite(it.id);
        h.btnFavorite.setImageResource(fav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
        h.btnFavorite.setOnClickListener(v -> {
            store.toggle(it);
            boolean nowFav = store.isFavorite(it.id);
            h.btnFavorite.setImageResource(nowFav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
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
