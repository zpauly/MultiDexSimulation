package com.zpauly.multidexsimulation;

import android.app.Application;
import android.content.Context;
/**
 * Created by zpauly on 2016/12/9.
 */

public class MultiDexApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }
}
