package vn.edu.usth.flickrbrowser.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    private final int visibleThreshold;
    private final RecyclerView.LayoutManager layoutManager;

    public EndlessScrollListener(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        
        if (layoutManager instanceof GridLayoutManager) {
            visibleThreshold = ((GridLayoutManager) layoutManager).getSpanCount() * 2;
        } else {
            visibleThreshold = 5;
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
        super.onScrolled(view, dx, dy);
        
        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItemPosition = getLastVisibleItem();

        // If total item count changed, we've finished loading
        if (totalItemCount != previousTotalItemCount) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If not loading and user scrolled near bottom, load more
        if (!loading && (lastVisibleItemPosition + visibleThreshold) >= totalItemCount) {
            onLoadMore(totalItemCount);
            loading = true;
        }
    }

    private int getLastVisibleItem() {
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        return 0;
    }

    public void resetState() {
        previousTotalItemCount = 0;
        loading = true;
    }

    public abstract void onLoadMore(int totalItemsCount);
}
