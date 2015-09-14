package com.example.simpleui;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by ggm on 6/22/15.
 */
public class SimpleUIApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "PihJMpOOpNYxpXN8wYcd3Jvn6R1x6IHOl6TA5gKc",
                "mnPmwNUDinSNH3b4RRiScFdkNRgLFxK61DVIpXYI");
    }

}
