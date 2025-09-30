package vn.edu.usth.flickrbrowser.core.utils;

import android.content.Context;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public class FlickrGlideModule extends AppGlideModule {
    
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Set disk cache size to 250 MB
        int diskCacheSizeBytes = 1024 * 1024 * 250;
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        
        // Set default request options
        builder.setDefaultRequestOptions(
            new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // Save memory
                .disallowHardwareConfig() // Prevent some crashes
        );
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        return false; // We handle configuration manually
    }
}
