package vn.edu.usth.flickrbrowser.core.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FavoritesStore {
    private static final String PREF = "favorites_store";
    private static final String KEY = "favorites_json";

    private static FavoritesStore INSTANCE;
    public static FavoritesStore get(Context ctx){
        if (INSTANCE == null){
            INSTANCE = new FavoritesStore(ctx.getApplicationContext());
        }
        return INSTANCE;
    }

    private final SharedPreferences sp;
    private final MutableLiveData<List<PhotoItem>> live = new MutableLiveData<>(new ArrayList<>());
    private List<PhotoItem> cache = new ArrayList<>(); // Internal cache for immediate access

    private FavoritesStore(Context appCtx){
        sp = appCtx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        cache = load();
        live.postValue(new ArrayList<>(cache));
    }

    public LiveData<List<PhotoItem>> live(){ return live; }

    public synchronized List<PhotoItem> getAll(){
        List<PhotoItem> cur = live.getValue();
        return cur != null ? new ArrayList<>(cur) : new ArrayList<>();
    }

    public synchronized boolean isFavorite(String id){
        if (id == null) return false;
        // Check cache directly for immediate result
        for (PhotoItem p : cache){
            if (id.equals(p.id)) return true;
        }
        return false;
    }

    public synchronized void toggle(PhotoItem p){
        if (p == null) return;
        if (isFavorite(p.id)) removeById(p.id); else add(p);
    }

    public synchronized void add(PhotoItem p){
        // Check cache first
        for (PhotoItem it: cache){ 
            if (p.id != null && p.id.equals(it.id)) return; // Already exists
        }
        cache.add(p);
        persist(new ArrayList<>(cache));
    }

    public synchronized void removeById(String id){
        Iterator<PhotoItem> it = cache.iterator();
        while (it.hasNext()){
            if (id != null && id.equals(it.next().id)) {
                it.remove();
            }
        }
        persist(new ArrayList<>(cache));
    }

    private void persist(List<PhotoItem> list){
        try{
            JSONArray arr = new JSONArray();
            for (PhotoItem p: list){
                JSONObject o = new JSONObject();
                o.put("id", p.id);
                o.put("server", p.server);
                o.put("secret", p.secret);
                o.put("title", p.title);
                o.put("owner", p.owner);
                o.put("thumbUrl", p.thumbUrl);
                o.put("fullUrl", p.fullUrl);
                o.put("tags", p.tags);
                o.put("flickrPageLink", p.flickrPageLink);
                arr.put(o);
            }
            sp.edit().putString(KEY, arr.toString()).commit(); // Use commit for immediate save
            
            // Update LiveData IMMEDIATELY (sync) on main thread or current thread
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                // Already on main thread, update directly
                live.setValue(new ArrayList<>(list));
            } else {
                // Post to main thread and update immediately in current value too
                live.postValue(new ArrayList<>(list));
            }
        }catch(Exception e){ 
            android.util.Log.e("FavoritesStore", "Error persisting: " + e.getMessage());
        }
    }

    private List<PhotoItem> load(){
        List<PhotoItem> out = new ArrayList<>();
        try{
            String s = sp.getString(KEY, "");
            if (s == null || s.isEmpty()) return out;
            JSONArray arr = new JSONArray(s);
            for (int i=0;i<arr.length();i++){
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                PhotoItem p = new PhotoItem();
                p.id = o.optString("id", "");
                p.server = o.optString("server", "");
                p.secret = o.optString("secret", "");
                p.title = o.optString("title", "");
                p.owner = o.optString("owner", "");
                p.thumbUrl = o.optString("thumbUrl", "");
                p.fullUrl = o.optString("fullUrl", "");
                p.tags = o.optString("tags", "");
                p.flickrPageLink = o.optString("flickrPageLink", "");
                out.add(p);
            }
        }catch(Exception ignore){ }
        return out;
    }
}
