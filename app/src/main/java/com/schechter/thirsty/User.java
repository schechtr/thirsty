package com.schechter.thirsty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {


    // profile info
    private String name, bio, city, state, country;
    private String profile_pic_url;

    private HashMap<String, Boolean> starred;

    private HashMap<String, Boolean> contributed;

    public User(String uid) {
        //this.mUID = uid;
        this.starred = new HashMap<>();
        this.contributed = new HashMap<>();
        starred.put("0", true);
        contributed.put("0", true);

        this.name = "";
        this.bio = "";
        this.city = "";
        this.state = "";
        this.country = "";
        this.profile_pic_url = "";

    }

    public HashMap<String, Boolean> getStarred() {
        return starred;
    }
    public HashMap<String, Boolean> getContributed() { return contributed; };

    public String getName() { return name; }
    public String getBio() { return bio; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getProfile_pic_url() { return profile_pic_url; }

}
