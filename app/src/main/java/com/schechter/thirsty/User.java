package com.schechter.thirsty;

import java.util.ArrayList;
import java.util.List;

public class User {

    //private String mUID;
    private String some_data;
    //private List<String> mContributed;
    //private List<String> mStarred;
    //private List<String> mVisited;


    public User(String uid) {
        //this.mUID = uid;
        this.some_data = "data here";

        //  mContributed = new ArrayList<>();
        //  mStarred = new ArrayList<>();
        //  mVisited = new ArrayList<>();
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
