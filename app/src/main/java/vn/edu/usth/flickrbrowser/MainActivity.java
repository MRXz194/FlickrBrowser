package vn.edu.usth.flickrbrowser;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.usth.flickrbrowser.core.util.ThemeManager;
import vn.edu.usth.flickrbrowser.ui.about.AboutFragment;
import vn.edu.usth.flickrbrowser.ui.explore.ExploreFragment;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesFragment;
import vn.edu.usth.flickrbrowser.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TAB = "selected_tab";
    private BottomNavigationView bottomNavigation;
    
    // Cache fragments to avoid recreation
    private SearchFragment searchFragment;
    private ExploreFragment exploreFragment;
    private FavoritesFragment favoritesFragment;
    private AboutFragment aboutFragment;
    private Fragment currentFragment;
    private int selectedTabId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setContentView
        ThemeManager.getInstance(this).applySavedTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // Restore fragments from FragmentManager if they exist
        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.navigation_home);
            searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("search");
            exploreFragment = (ExploreFragment) getSupportFragmentManager().findFragmentByTag("explore");
            favoritesFragment = (FavoritesFragment) getSupportFragmentManager().findFragmentByTag("favorites");
            aboutFragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag("about");
        }
        
        // Initialize fragments if they don't exist
        if (searchFragment == null) searchFragment = new SearchFragment();
        if (exploreFragment == null) exploreFragment = new ExploreFragment();
        if (favoritesFragment == null) favoritesFragment = new FavoritesFragment();
        if (aboutFragment == null) aboutFragment = new AboutFragment();
        
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
                selectedTabId = itemId;
                return true;
            }
            return false;
        });

        // Restore selected tab or load default
        if (savedInstanceState != null) {
            bottomNavigation.setSelectedItemId(selectedTabId);
        } else {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
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
        
        // Show or add selected fragment with tag
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            String tag = getFragmentTag(fragment);
            transaction.add(R.id.fragment_container, fragment, tag);
        }
        
        transaction.commit();
    }
    
    private String getFragmentTag(Fragment fragment) {
        if (fragment instanceof SearchFragment) return "search";
        if (fragment instanceof ExploreFragment) return "explore";
        if (fragment instanceof FavoritesFragment) return "favorites";
        if (fragment instanceof AboutFragment) return "about";
        return "unknown";
    }
}