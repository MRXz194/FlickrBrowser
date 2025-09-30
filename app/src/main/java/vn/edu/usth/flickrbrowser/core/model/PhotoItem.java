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
    public String flickrPageLink = ""; // Direct link to Flickr page (from fallback API)

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
        // Priority 1: Use direct link if available (from fallback API)
        if (notEmpty(flickrPageLink)) {
            return flickrPageLink;
        }
        // Priority 2: Build URL from id/owner (from official API)
        if (notEmpty(id) && notEmpty(owner) && !id.startsWith("fallback_")) {
            return "https://www.flickr.com/photos/" + owner + "/" + id;
        }
        // Priority 3: Fallback to image URL
        return getFullUrl();
    }

    private boolean okFlickr() {
        return notEmpty(server) && notEmpty(id) && notEmpty(secret);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
