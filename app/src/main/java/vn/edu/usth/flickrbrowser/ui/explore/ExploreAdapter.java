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

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PhotoItem p = data.get(pos);
        Glide.with(h.img.getContext()).load(p.getThumbUrl()).into(h.img);
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(p);
        });
        FavoritesStore store = FavoritesStore.get(h.itemView.getContext());
        boolean fav = store.isFavorite(p.id);
        h.btnFavorite.setImageResource(fav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
        h.btnFavorite.setOnClickListener(v -> {
            store.toggle(p);
            boolean nowFav = store.isFavorite(p.id);
            h.btnFavorite.setImageResource(nowFav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
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