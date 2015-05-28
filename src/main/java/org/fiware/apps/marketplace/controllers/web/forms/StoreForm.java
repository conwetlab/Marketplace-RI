package org.fiware.apps.marketplace.controllers.web.forms;

import javax.ws.rs.FormParam;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class StoreForm {

    private String displayName;
    private String url;
    private String comment;
    private String imageName;
    private String imageBase64;

    public StoreForm() {}

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getComment() {
        return comment;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    @FormParam("displayName")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @FormParam("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @FormParam("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @FormParam("imageName")
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @FormParam("imageBase64")
    @PartType("application/octet-stream")
    public void setImageData(byte[] imageBase64) {
        if (imageBase64 != null && imageBase64.length > 0) {
            this.imageBase64 = Base64.encodeBase64String(imageBase64);
        }
    }

}
