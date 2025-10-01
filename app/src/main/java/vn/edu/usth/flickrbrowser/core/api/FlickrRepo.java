package vn.edu.usth.flickrbrowser.core.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FlickrRepo {

    // Callback domain cho UI
    public interface CB {
        void ok(List<PhotoItem> items);
        void err(Throwable e);
    }

    private static final String TAG = "API";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static FlickrApi API;
    private static FlickrApi api() {
        if (API == null) {
            // dùng ApiClient.getClient() như bạn đã triển khai
            API = ApiClient.getClient().create(FlickrApi.class);
        }
        return API;
    }

    // Giữ request search đang chạy để có thể huỷ khi user gõ tiếp
    private static Call<ResponseBody> inFlightSearch;

    public static void cancelSearch() {
        if (inFlightSearch != null && !inFlightSearch.isCanceled()) {
            inFlightSearch.cancel();
        }
        inFlightSearch = null;
    }

    // ---- getRecent ----
    public static void getRecent(int page, int perPage, CB cb) {
        Map<String, String> p = new HashMap<>();
        p.put("page", String.valueOf(Math.max(1, page)));
        p.put("per_page", String.valueOf(Math.max(1, perPage)));

        api().getRecent(p).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> r) {
                try {
                    String body = r.body() != null ? r.body().string()
                            : (r.errorBody() != null ? r.errorBody().string() : "");
                    Log.d(TAG, "getRecent code=" + r.code() + " body=" + body);
                    if (r.isSuccessful()) {
                        List<PhotoItem> out = parseToPhotos(body);
                        if (out.isEmpty()) {
                            Log.d(TAG, "getRecent: No photos parsed, trying fallback");
                            getRecentFallback(cb);
                        } else {
                            MAIN.post(() -> cb.ok(out));
                        }
                    } else {
                        Log.d(TAG, "getRecent: API failed with code " + r.code() + ", trying fallback");
                        getRecentFallback(cb);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "getRecent: Exception, trying fallback: " + e);
                    getRecentFallback(cb);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "getRecent fail: " + t + ", trying fallback");
                getRecentFallback(cb);
            }
        });
    }

    // Fallback: Use Flickr public feed (no API key needed)
    private static void getRecentFallback(CB cb) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://www.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                int code = conn.getResponseCode();
                Log.d(TAG, "Fallback API code=" + code);
                
                if (code == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String body = response.toString();
                    Log.d(TAG, "Fallback body length=" + body.length());
                    List<PhotoItem> out = parseToPhotos(body);
                    MAIN.post(() -> cb.ok(out));
                } else {
                    MAIN.post(() -> cb.err(new RuntimeException("Fallback HTTP " + code)));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.d(TAG, "Fallback error: " + e);
                MAIN.post(() -> cb.err(e));
            }
        }).start();
    }

    // ---- search ----
    public static void search(String query, int page, int perPage, CB cb) {
        cancelSearch();

        Map<String, String> p = new HashMap<>();
        p.put("text", query == null ? "" : query.trim());
        p.put("page", String.valueOf(Math.max(1, page)));
        p.put("per_page", String.valueOf(Math.max(1, perPage)));

        inFlightSearch = api().search(p);
        inFlightSearch.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> r) {
                try {
                    String body = r.body() != null ? r.body().string()
                            : (r.errorBody() != null ? r.errorBody().string() : "");
                    Log.d(TAG, "search code=" + r.code() + " body=" + body);
                    if (r.isSuccessful()) {
                        List<PhotoItem> out = parseToPhotos(body);
                        if (out.isEmpty()) {
                            Log.d(TAG, "search: No photos parsed, trying fallback");
                            searchFallback(query, cb);
                        } else {
                            MAIN.post(() -> cb.ok(out));
                        }
                    } else {
                        Log.d(TAG, "search: API failed with code " + r.code() + ", trying fallback");
                        searchFallback(query, cb);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "search: Exception, trying fallback: " + e);
                    searchFallback(query, cb);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled()) {
                    Log.d(TAG, "search canceled");
                } else {
                    Log.d(TAG, "search fail: " + t + ", trying fallback");
                    searchFallback(query, cb);
                }
            }
        });
    }

    // Cache for fallback search results to avoid duplicates
    private static Map<String, List<PhotoItem>> fallbackCache = new HashMap<>();
    private static Map<String, Integer> fallbackPageIndex = new HashMap<>();
    
    // Fallback: Search using multiple Flickr public feeds with variety
    private static void searchFallback(String query, CB cb) {
        new Thread(() -> {
            try {
                String cacheKey = query.toLowerCase().trim();
                
                // Get or initialize page index for this query
                int pageIndex = fallbackPageIndex.getOrDefault(cacheKey, 0);
                
                // Multiple strategies to get variety:
                // 1. tagmode=any (match any tag)
                // 2. tagmode=all (match all tags)
                // 3. Different tag combinations
                String[] tagModes = {"any", "all"};
                String tagMode = tagModes[pageIndex % tagModes.length];
                
                // Split query into tags for better results
                String[] queryParts = query.trim().split("\\s+");
                String tags = String.join(",", queryParts);
                String encodedTags = java.net.URLEncoder.encode(tags, "UTF-8");
                
                // Build URL with variety parameters
                StringBuilder urlBuilder = new StringBuilder("https://www.flickr.com/services/feeds/photos_public.gne?");
                urlBuilder.append("tags=").append(encodedTags);
                urlBuilder.append("&tagmode=").append(tagMode);
                urlBuilder.append("&format=json&nojsoncallback=1");
                
                // Add language parameter for variety (rotate through languages)
                String[] langs = {"en-us", "es-us", "fr-fr", "de-de", "it-it", "pt-br", "ja-jp", "ko-kr"};
                String lang = langs[pageIndex % langs.length];
                urlBuilder.append("&lang=").append(lang);
                
                String urlStr = urlBuilder.toString();
                Log.d(TAG, "Search Fallback URL: " + urlStr + " (page index: " + pageIndex + ")");
                
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                int code = conn.getResponseCode();
                Log.d(TAG, "Search Fallback API code=" + code + " query=" + query);
                
                if (code == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String body = response.toString();
                    Log.d(TAG, "Search Fallback body length=" + body.length());
                    List<PhotoItem> newPhotos = parseToPhotos(body);
                    
                    // Get cached photos for this query
                    List<PhotoItem> cachedPhotos = fallbackCache.getOrDefault(cacheKey, new ArrayList<>());
                    
                    // Filter out duplicates
                    List<PhotoItem> uniquePhotos = new ArrayList<>();
                    for (PhotoItem photo : newPhotos) {
                        boolean isDuplicate = false;
                        for (PhotoItem cached : cachedPhotos) {
                            if (photo.id.equals(cached.id) || photo.fullUrl.equals(cached.fullUrl)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        if (!isDuplicate) {
                            uniquePhotos.add(photo);
                        }
                    }
                    
                    // Add to cache
                    cachedPhotos.addAll(uniquePhotos);
                    fallbackCache.put(cacheKey, cachedPhotos);
                    
                    // Increment page index for next load
                    fallbackPageIndex.put(cacheKey, pageIndex + 1);
                    
                    Log.d(TAG, "Search Fallback: " + uniquePhotos.size() + " unique photos (total cached: " + cachedPhotos.size() + ")");
                    
                    List<PhotoItem> finalPhotos = uniquePhotos;
                    MAIN.post(() -> cb.ok(finalPhotos));
                } else {
                    MAIN.post(() -> cb.err(new RuntimeException("Fallback HTTP " + code)));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.d(TAG, "Search Fallback error: " + e);
                MAIN.post(() -> cb.err(e));
            }
        }).start();
    }
    
    // Clear cache for a specific query (call when starting new search)
    public static void clearSearchCache(String query) {
        String cacheKey = query.toLowerCase().trim();
        fallbackCache.remove(cacheKey);
        fallbackPageIndex.remove(cacheKey);
    }

    // ---- Parser JSON -> List<PhotoItem> ----
    private static List<PhotoItem> parseToPhotos(String json) {
        List<PhotoItem> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);

            // Case 1: Flickr chuẩn (photos -> photo[])
            JSONObject photos = root.optJSONObject("photos");
            JSONArray arr = photos != null ? photos.optJSONArray("photo") : null;
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    PhotoItem p = new PhotoItem();
                    p.id     = o.optString("id", "");
                    p.title  = o.optString("title", "");
                    p.server = o.optString("server", "");
                    p.secret = o.optString("secret", "");
                    p.owner  = o.optString("owner", "");
                    out.add(p);
                }
                return out;
            }

            // Case 2: Public feed fallback (items[])
            JSONArray items = root.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject o = items.optJSONObject(i);
                    if (o == null) continue;
                    PhotoItem p = new PhotoItem();
                    
                    // Save the direct Flickr page link
                    String link = o.optString("link", "");
                    p.flickrPageLink = link; // Save original link for opening in browser
                    
                    // Generate unique ID from link or use hash
                    if (!link.isEmpty()) {
                        // Extract photo ID from Flickr link
                        String[] parts = link.split("/");
                        for (String part : parts) {
                            if (part.matches("\\d+")) {
                                p.id = "fallback_" + part;
                                break;
                            }
                        }
                    }
                    // If still no ID, generate from URL hash
                    if (p.id == null || p.id.isEmpty()) {
                        JSONObject media = o.optJSONObject("media");
                        String url = media != null ? media.optString("m", "") : "";
                        p.id = "fallback_" + Math.abs(url.hashCode());
                    }
                    
                    p.title = o.optString("title", "");
                    p.owner = o.optString("author", "");
                    JSONObject media = o.optJSONObject("media");
                    String url = media != null ? media.optString("m", "") : "";
                    p.thumbUrl = url;
                    p.fullUrl  = url.replace("_m.jpg", "_b.jpg"); // Get larger version
                    out.add(p);
                }
            }
        } catch (Exception ignore) {
            Log.d(TAG, "parse error: " + ignore);
        }
        return out;
    }
}
