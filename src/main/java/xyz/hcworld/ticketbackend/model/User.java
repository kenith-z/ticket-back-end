package xyz.hcworld.ticketbackend.model;

public class User {
    public String UserId;
    public String Business;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getBusiness() {
        return Business;
    }

    public void setBusiness(String business) {
        Business = business;
    }
}
