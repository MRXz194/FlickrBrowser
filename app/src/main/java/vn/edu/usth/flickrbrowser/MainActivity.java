package vn.edu.usth.flickrbrowser;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.usth.flickrbrowser.ui.about.AboutFragment;
import vn.edu.usth.flickrbrowser.ui.explore.ExploreFragment;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesFragment;
import vn.edu.usth.flickrbrowser.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // Set up bottom navigation listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.navigation_explore) {
                selectedFragment = new ExploreFragment();
            } else if (itemId == R.id.navigation_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (itemId == R.id.navigation_about) {
                selectedFragment = new AboutFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Load default fragment (Home/Search) on first launch
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }
}