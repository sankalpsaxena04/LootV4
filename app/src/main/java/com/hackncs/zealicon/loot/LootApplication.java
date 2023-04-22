package com.hackncs.zealicon.loot;
import android.app.Application;
import android.content.Context;
import java.util.ArrayList;

import androidx.multidex.MultiDex;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;


public class LootApplication extends Application {
    User user;
    ArrayList<Mission> missions;


    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(new AndroidLogAdapter());
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);



    }



}
