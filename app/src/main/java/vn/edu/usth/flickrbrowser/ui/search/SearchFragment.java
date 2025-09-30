package vn.edu.usth.flickrbrowser.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.data.FavoritesStore;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.core.util.NetUtils;
import vn.edu.usth.flickrbrowser.databinding.FragmentSearchBinding;
import vn.edu.usth.flickrbrowser.ui.common.EndlessScrollListener;
import vn.edu.usth.flickrbrowser.ui.common.GridSpacingDecoration;
import vn.edu.usth.flickrbrowser.ui.search.PhotosAdapter;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private PhotosAdapter adapter;
    private int currentPage = 1;
    private final int perPage = 24;
    private String currentQuery = "";
    private boolean isLoading = false;
    private EndlessScrollListener scrollListener;
    
    // Debouncing for auto-search
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new PhotosAdapter(item -> {
            android.content.Intent i = new android.content.Intent(requireContext(), vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);
            i.putExtra(vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.EXTRA_PHOTO, item);
            startActivity(i);
        });
        binding.rvPhotos.setAdapter(adapter);

        // Pull-to-Refresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (!currentQuery.isEmpty()) {
                doSearch(currentQuery);
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // 1) AppBar title
        binding.topAppBar.setTitle(R.string.search_hint);

        // 2) RecyclerView grid 2 cột
        int span = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), span);
        binding.rvPhotos.setLayoutManager(layoutManager);

        // 3) Spacing theo token
        int spacingPx = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        binding.rvPhotos.addItemDecoration(new GridSpacingDecoration(span, spacingPx, true));

        // 4) Setup infinite scroll
        scrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int totalItemsCount) {
                if (!isLoading && !currentQuery.isEmpty()) {
                    loadMorePhotos();
                }
            }
        };
        binding.rvPhotos.addOnScrollListener(scrollListener);

        // Search action on keyboard
        binding.edtQuery.setOnEditorActionListener((v, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(v.getText() != null ? v.getText().toString() : "");
                return true;
            }
            return false;
        });

        // Auto-search as you type (with 800ms delay)
        binding.edtQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Schedule new search after 800ms delay
                String query = s.toString().trim();
                searchRunnable = () -> {
                    if (query.length() >= 2) { // Only search if 2+ characters
                        doSearch(query);
                    } else if (query.isEmpty()) {
                        // Clear results when query is empty
                        adapter.submitList(null);
                        setState(new PhotoState.Empty());
                    }
                };
                searchHandler.postDelayed(searchRunnable, 800);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Observe favorites changes to update UI
        FavoritesStore.get(requireContext()).live().observe(getViewLifecycleOwner(), favoritesList -> {
            // Notify adapter to refresh favorite states
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });

        // Initial empty state
        setState(new PhotoState.Empty());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // tránh leak
    }

    private void setState(@NonNull PhotoState state) {
        if (state instanceof PhotoState.Loading) {
            binding.swipeRefresh.setRefreshing(true);
            binding.shimmerGrid.getRoot().setVisibility(View.VISIBLE);
            startShimmers(binding.shimmerGrid.getRoot());

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
        }
        else if (state instanceof PhotoState.Success) {
            binding.swipeRefresh.setRefreshing(false);
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.rvPhotos.setVisibility(View.VISIBLE);
            adapter.submitList(items);
        }
        else if (state instanceof PhotoState.Empty) {
            binding.swipeRefresh.setRefreshing(false);
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) {
                binding.emptyView.getRoot().setVisibility(View.VISIBLE);
                // Animate empty state
                View emptyIcon = binding.emptyView.getRoot().findViewById(R.id.emptyIcon);
                if (emptyIcon != null) {
                    android.view.animation.Animation pulse = android.view.animation.AnimationUtils.loadAnimation(
                        requireContext(), R.anim.pulse);
                    emptyIcon.startAnimation(pulse);
                }
                // Fade in animation
                binding.emptyView.getRoot().setAlpha(0f);
                binding.emptyView.getRoot().animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();
            }
        }
        else if (state instanceof PhotoState.Error) {
            binding.swipeRefresh.setRefreshing(false);
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);

            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void doSearch(String query) {
        // Validate empty query - don't call API
        if (query == null || query.trim().isEmpty()) {
            setState(new PhotoState.Empty());
            return;
        }
        
        // Check network before searching
        if (!NetUtils.hasNetwork(requireContext())) {
            Toast.makeText(requireContext(), R.string.error_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Reset for new search
        currentQuery = query;
        currentPage = 1;
        if (scrollListener != null) scrollListener.resetState();
        
        // Cancel any in-flight before starting
        FlickrRepo.cancelSearch();
        isLoading = true;
        setState(new PhotoState.Loading());
        
        FlickrRepo.search(query, currentPage, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                } else {
                    setState(new PhotoState.Success(items));
                }
            }

            @Override
            public void err(Throwable e) {
                isLoading = false;
                String errorMsg = e.getMessage() != null && e.getMessage().contains("timeout") 
                    ? getString(R.string.error_timeout) 
                    : getString(R.string.error_search_failed);
                setState(new PhotoState.Error(errorMsg));
            }
        });
    }

    private void loadMorePhotos() {
        if (isLoading) return;
        
        // Check network before loading more
        if (!NetUtils.hasNetwork(requireContext())) {
            Toast.makeText(requireContext(), R.string.error_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        
        isLoading = true;
        currentPage++;
        
        FlickrRepo.search(currentQuery, currentPage, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                if (items != null && !items.isEmpty()) {
                    adapter.addItems(items);
                    Toast.makeText(requireContext(), "Loaded " + items.size() + " more photos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void err(Throwable e) {
                isLoading = false;
                currentPage--; // Revert page on error
                String errorMsg = e.getMessage() != null && e.getMessage().contains("timeout") 
                    ? getString(R.string.error_timeout) 
                    : getString(R.string.error_search_failed);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout)root).startShimmer();
        }
        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i <vg.getChildCount(); i++){
                startShimmers(vg.getChildAt(i));
            }
        }
    }

    private void stopShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout) root).stopShimmer();
        }
        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i <vg.getChildCount(); i++){
                stopShimmers(vg.getChildAt(i));
            }
        }
    }
}
