package com.example.letsplay.application;

import android.app.Application;

/**This class knows always if the ProfileActivity (The main activity of our app)
  is alive or not for notifications control*/
public class MyApplication extends Application {

    private static boolean activityAlive=false;

    public static boolean isActivityAlive() {
        return activityAlive;
    }

    public static void activityDestroyed() {
        activityAlive = false;
    }

    public static void activityCreated() {
        activityAlive = true;
    }


}
