package vn.edu.usth.flickrbrowser.core.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class ImageDownloader {

    public static void downloadImage(Context context, String imageUrl, String fileName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(context, "Invalid image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            
            // Set title and description
            request.setTitle("Flickr Image");
            request.setDescription("Downloading " + fileName);
            
            // Show notification during download
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            // Set destination in Pictures directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, 
                "FlickrBrowser/" + fileName + ".jpg");
            
            // Allow download over mobile and WiFi
            request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | 
                DownloadManager.Request.NETWORK_MOBILE);
            
            // Allow media scanner to find the file
            request.allowScanningByMediaScanner();
            
            // Get download service and enqueue
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(context, "Downloading image... 📥", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(context, "Download error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            android.util.Log.e("ImageDownloader", "Error downloading image", e);
        }
    }
    
    public static String generateFileName(String photoId, String title) {
        // Clean title for filename
        String cleanTitle = title != null ? title.replaceAll("[^a-zA-Z0-9]", "_") : "image";
        // Limit length
        if (cleanTitle.length() > 30) {
            cleanTitle = cleanTitle.substring(0, 30);
        }
        // Add photo ID for uniqueness
        String idPart = photoId != null ? photoId : String.valueOf(System.currentTimeMillis());
        return cleanTitle + "_" + idPart;
    }
}
