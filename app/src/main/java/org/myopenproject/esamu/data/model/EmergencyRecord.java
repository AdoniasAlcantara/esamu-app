package org.myopenproject.esamu.data.model;

import java.util.Date;

public class EmergencyRecord {
    private long id;
    private Date dateTime;
    private Status status;
    private int attachment = -1;
    private String location;

    // Getters

    public long getId() {
        return id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public Status getStatus() {
        return status;
    }

    public int getAttachment() {
        return attachment;
    }

    public String getLocation() {
        return location;
    }

    // Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setAttachment(int attachment) {
        this.attachment = attachment;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public enum Status {
        PENDENT, PROGRESS, FINISHED, CANCELED;

        public static Status valueOf(int value) {
            switch (value) {
                case 0: return PENDENT;
                case 1: return PROGRESS;
                case 2: return FINISHED;
                case 3: return CANCELED;
                default: return PENDENT;
            }
        }
    }
}
