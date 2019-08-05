package com.schechter.thirsty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    //private String mUID;
    private String some_data;



    //private List<String> mContributed;
    private HashMap<String, Boolean> starred;
    //private List<String> mVisited;


    public User(String uid) {
        //this.mUID = uid;
        this.some_data = "data here";
        this.starred = new HashMap<>();
        starred.put("0", true);

        //  mContributed = new ArrayList<>();
        //  mStarred = new ArrayList<>();
        //  mVisited = new ArrayList<>();
    }

    public HashMap<String, Boolean> getStarred() {
        return starred;
    }



    public String getSome_data() {
        return some_data;
    }

    public void setSome_data(String some_data) {
        this.some_data = some_data;
    }



    //public List<String> getContributed() {return mContributed;}

    //public List<String> getStarred() {return mStarred;}

    //public List<String> getVisited() {return mVisited;}


}
