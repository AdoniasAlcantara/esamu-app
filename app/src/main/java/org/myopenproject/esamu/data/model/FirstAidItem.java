package org.myopenproject.esamu.data.model;

import android.graphics.Bitmap;

public class FirstAidItem {
    private Bitmap image;
    private String info;

    public FirstAidItem(Bitmap image, String info) {
        this.image = image;
        this.info = info;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getInfo() {
        return info;
    }
}