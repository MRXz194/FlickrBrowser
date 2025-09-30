package vn.edu.usth.flickrbrowser.ui.explore;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.*;
import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class ExploreFragment extends Fragment {
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ExploreAdapter adapter;
    private ViewGroup shimmerGrid;
    private View emptyRoot;
    private TextView emptyText;
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,@Nullable ViewGroup parent,@Nullable Bundle b){
        View v=inf.inflate(R.layout.fragment_explore,parent,false);
        swipe=v.findViewById(R.id.swipe); 
        rv=v.findViewById(R.id.recycler);

        shimmerGrid = v.findViewById(R.id.shimmerGrid);
        emptyRoot = v.findViewById(R.id.emptyView);
        if (emptyRoot != null) {
            emptyText = emptyRoot.findViewById(R.id.emptyText);
        }

        rv.setLayoutManager(new GridLayoutManager(requireContext(),2));
        adapter=new ExploreAdapter(item -> {
            android.content.Intent i = new android.content.Intent(requireContext(), vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);
            i.putExtra(vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.EXTRA_PHOTO, item);
            startActivity(i);
        });
        rv.setAdapter(adapter);
        swipe.setOnRefreshListener(this::load);
        return v;
    }
    @Override
    public void onViewCreated(@NonNull View v,@Nullable Bundle b){
        super.onViewCreated(v,b); load();
    }
    private void load(){
        swipe.setRefreshing(true);
        setState(new PhotoState.Loading());
        FlickrRepo.getRecent(1,12,new FlickrRepo.CB(){
            @Override
            public void ok(List<PhotoItem> items){
                swipe.setRefreshing(false);
                setState(new PhotoState.Success(items));
            }
            @Override
            public void err(Throwable t){
                swipe.setRefreshing(false);
                setState(new PhotoState.Error("Load error"));
            }
        });
    }

    private void setState(@NonNull PhotoState state) {
        if (state instanceof PhotoState.Loading){
            if (shimmerGrid != null) {
                shimmerGrid.setVisibility(View.VISIBLE);
                startShimmers(shimmerGrid);
            }
            if (rv != null) rv.setVisibility(View.GONE);
            if (emptyRoot != null) emptyRoot.setVisibility(View.GONE);
        }
        else if (state instanceof PhotoState.Success){
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            if (shimmerGrid != null) {
                stopShimmers(shimmerGrid);
                shimmerGrid.setVisibility(View.GONE);
            }
            if (emptyRoot != null) emptyRoot.setVisibility(View.GONE);

            if (rv != null) rv.setVisibility(View.VISIBLE);
            if (adapter != null) adapter.setData(items);
        }
        else if (state instanceof PhotoState.Empty) {
            if (shimmerGrid != null) {
                stopShimmers(shimmerGrid);
                shimmerGrid.setVisibility(View.GONE);
            }
            if (rv != null) rv.setVisibility(View.GONE);
            if (emptyRoot != null) emptyRoot.setVisibility(View.VISIBLE);
            if (emptyText != null) emptyText.setText(R.string.empty_default);
        }
        else if ( state instanceof PhotoState.Error){
            if (shimmerGrid != null) {
                stopShimmers(shimmerGrid);
                shimmerGrid.setVisibility(View.GONE);
            }
            if (rv != null) rv.setVisibility(View.GONE);
            if (emptyRoot != null) emptyRoot.setVisibility(View.GONE);

            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show();
        }
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