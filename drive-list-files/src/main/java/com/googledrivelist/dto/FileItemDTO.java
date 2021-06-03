package com.googledrivelist.dto;

import java.io.Serializable;

public class FileItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String id;

    private String thumbnailLink;

    private String webViewLink;

    private String webContentLink;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public String getWebViewLink() { return webViewLink; }

    public void setWebViewLink(String webViewLink) { this.webViewLink = webViewLink; }

    public String getWebContentLink() { return webContentLink; }

    public void setWebContentLink(String webContentLink) { this.webContentLink = webContentLink; }

}
