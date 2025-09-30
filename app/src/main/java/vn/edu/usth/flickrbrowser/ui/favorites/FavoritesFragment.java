package vn.edu.usth.flickrbrowser.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.data.FavoritesStore;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.search.PhotosAdapter;

public class FavoritesFragment extends Fragment {

    private PhotosAdapter adapter;

    public FavoritesFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.recycler);
        final View shimmer = view.findViewById(R.id.shimmerGrid);
        final View emptyRoot = view.findViewById(R.id.emptyRoot);

        // Setup grid with spacing
        int span = 2;
        rv.setLayoutManager(new GridLayoutManager(requireContext(), span));
        
        // Add grid spacing decoration
        int spacingPx = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        rv.addItemDecoration(new vn.edu.usth.flickrbrowser.ui.common.GridSpacingDecoration(span, spacingPx, true));
        
        adapter = new PhotosAdapter(item -> {
            android.content.Intent i = new android.content.Intent(requireContext(), vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);
            i.putExtra(vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.EXTRA_PHOTO, item);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // Observe favorites changes
        FavoritesStore.get(requireContext()).live().observe(getViewLifecycleOwner(), list -> {
            List<PhotoItem> items = list;
            if (items == null || items.isEmpty()){
                rv.setVisibility(View.GONE);
                if (shimmer != null) shimmer.setVisibility(View.GONE);
                if (emptyRoot != null) emptyRoot.setVisibility(View.VISIBLE);
            } else {
                if (emptyRoot != null) emptyRoot.setVisibility(View.GONE);
                if (shimmer != null) shimmer.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                adapter.submitList(items);
            }
        });

    }
}
