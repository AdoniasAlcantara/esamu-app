package org.myopenproject.esamu.data.service;

import java.util.Map;

public class Message {
    private int code;
    private String description;
    private Map<String, String> details;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Message{" +
                "code=" + code +
                ", description='" + description + '\'' +
                ", details=" + details +
                '}';
    }
}
