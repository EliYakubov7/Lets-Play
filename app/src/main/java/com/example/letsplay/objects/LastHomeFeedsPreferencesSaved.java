package com.example.letsplay.objects;

import java.io.Serializable;

public class LastHomeFeedsPreferencesSaved implements Serializable {
    private boolean isByFriends,isByMostRecent,isByMyPosts,isByDistance;
    private int radius;

    public LastHomeFeedsPreferencesSaved(boolean isByFriends, boolean isByMostRecent, boolean isByMyPosts,
                                         boolean isByDistance, int radius) {
        this.isByFriends = isByFriends;
        this.isByMostRecent = isByMostRecent;
        this.isByMyPosts = isByMyPosts;
        this.isByDistance = isByDistance;
        this.radius = radius;
    }

    public boolean isByFriends() {
        return isByFriends;
    }

    public void setByFriends(boolean byFriends) {
        isByFriends = byFriends;
    }

    public boolean isByMostRecent() {
        return isByMostRecent;
    }

    public void setByMostRecent(boolean byMostRecent) {
        isByMostRecent = byMostRecent;
    }

    public boolean isByMyPosts() {
        return isByMyPosts;
    }

    public void setByMyPosts(boolean byMyPosts) {
        isByMyPosts = byMyPosts;
    }

    public boolean isByDistance() {
        return isByDistance;
    }

    public void setByDistance(boolean byDistance) {
        isByDistance = byDistance;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
