package vn.edu.usth.flickrbrowser.core.model;

import java.io.Serializable;

public class PhotoItem implements Serializable {
    public String id = "";
    public String server = "";
    public String secret = "";
    public String title = "";
    public String owner = "";
    public String thumbUrl = "";
    public String fullUrl = "";
    public String tags = ""; // optional, may be empty

    public String getThumbUrl() {
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            return thumbUrl;
        }
        if (okFlickr()) {
            return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_w.jpg";
        }
        return "";
    }

    public String getFullUrl() {
        if (fullUrl != null && !fullUrl.isEmpty()) {
            return fullUrl;
        }
        if (okFlickr()) {
            return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_b.jpg";
        }
        return getThumbUrl();
    }

    public String getTitle() { return title; }
    public String getOwner() { return owner; }
    public String getTags()  { return tags; }
    
    // Get Flickr page URL to open in browser
    public String getFlickrPageUrl() {
        if (notEmpty(id) && notEmpty(owner)) {
            return "https://www.flickr.com/photos/" + owner + "/" + id;
        }
        // Fallback to image URL if we don't have owner info
        return getFullUrl();
    }

    private boolean okFlickr() {
        return notEmpty(server) && notEmpty(id) && notEmpty(secret);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
