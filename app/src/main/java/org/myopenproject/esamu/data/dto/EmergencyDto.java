package org.myopenproject.esamu.data.dto;

import com.google.gson.annotations.SerializedName;

public class EmergencyDto {
    @SerializedName("user_id")
    private String userId;

    private String imei;
    private Double latitude;
    private Double longitude;

    // Multimedia
    private String picture;
    private String video;
    private String voice;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    @Override
    public String toString() {
        return "EmergencyDto{" +
                "userId='" + userId + '\'' +
                ", imei='" + imei + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", picture='" + picture + '\'' +
                ", video='" + video + '\'' +
                ", voice='" + voice + '\'' +
                '}';
    }
}
