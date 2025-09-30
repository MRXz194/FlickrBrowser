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
    
    // Cache fragments to avoid recreation
    private SearchFragment searchFragment;
    private ExploreFragment exploreFragment;
    private FavoritesFragment favoritesFragment;
    private AboutFragment aboutFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // Initialize fragments once
        if (savedInstanceState == null) {
            searchFragment = new SearchFragment();
            exploreFragment = new ExploreFragment();
            favoritesFragment = new FavoritesFragment();
            aboutFragment = new AboutFragment();
        }
        
        // Set up bottom navigation listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                selectedFragment = searchFragment;
            } else if (itemId == R.id.navigation_explore) {
                selectedFragment = exploreFragment;
            } else if (itemId == R.id.navigation_favorites) {
                selectedFragment = favoritesFragment;
            } else if (itemId == R.id.navigation_about) {
                selectedFragment = aboutFragment;
            }
            
            if (selectedFragment != null && selectedFragment != currentFragment) {
                showFragment(selectedFragment);
                currentFragment = selectedFragment;
                return true;
            }
            return false;
        });

        // Load default fragment (Home/Search) on first launch
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }
    
    private void showFragment(Fragment fragment) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // Add smooth fade animations
        transaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out
        );
        
        // Hide all fragments
        if (searchFragment != null && searchFragment.isAdded()) {
            transaction.hide(searchFragment);
        }
        if (exploreFragment != null && exploreFragment.isAdded()) {
            transaction.hide(exploreFragment);
        }
        if (favoritesFragment != null && favoritesFragment.isAdded()) {
            transaction.hide(favoritesFragment);
        }
        if (aboutFragment != null && aboutFragment.isAdded()) {
            transaction.hide(aboutFragment);
        }
        
        // Show or add selected fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment);
        }
        
        transaction.commit();
    }
}