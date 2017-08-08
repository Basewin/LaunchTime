package com.quaap.launchtime.components;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.quaap.launchtime.GlobState;
import com.quaap.launchtime.db.DB;

import java.util.List;

/**
 * Created by tom on 8/8/17.
 */

public class AppLauncher {


    private static String TAG = "LT AppLauncher";

    private Activity activity;

    public AppLauncher(Activity activity) {
        this.activity = activity;
    }

    private DB db() {
        return GlobState.getGlobState(activity).getDB();
    }
    //Run/open the thing that was clicked
    public void launchApp(String activityname, String pkgname) {
        try {
            launchApp(db().getApp(new ComponentName(pkgname, activityname)));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void launchApp(final AppShortcut app) {
        String activityname = app.getLinkBaseActivityName();

        try {

            //needed to place in the open apps list
            Intent intent = getAppIntent(app);

            if (isValidActivity(intent)) {
                // actually start it
                activity.startActivity(intent);
            } else {
                Toast.makeText(activity, "Could not launch item", Toast.LENGTH_LONG).show();
            }

            //log the launch
            if (app.isAppLink()) {
                db().appLaunched(new ComponentName(app.getPackageName(), app.getLinkBaseActivityName()));
            } else {
                db().appLaunched(app.getComponentName());
            }
        } catch (Exception e) {
            Log.d(TAG, "Could not launch " + activityname, e);
            Toast.makeText(activity, "Could not launch item: " + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
        //showButtonBar(false, true);
    }

    public Intent getAppIntent(final AppShortcut app) {
        String activityname = app.getLinkBaseActivityName();
        String packagename = app.getPackageName();
        String uristr = null;
        Uri uri = null;

        if (app.isLink()) {
            uristr = app.getLinkUri();
            if (uristr!=null) {
                uri = Uri.parse(uristr);
                if (app.isAppLink()) {
                    uri = null;
                }
            }

        }
        // Log.d(TAG, app.getActivityName() + " " + app.getPackageName() + " " + uristr);
        Intent intent;
        // is Link is a shortcut?
        if (app.isActionLink()) {
            //Change "CALL" to "DIAL" so we can avoid needing the
            // android.permission.CALL_PHONE permission
            if (activityname.startsWith("android.intent.action.CALL")) {
                activityname = "android.intent.action.DIAL";
            }
            //build an activity-specific intent with the uri
            intent = new Intent(activityname, uri);
        } else {
            //regualt activity, start with MAIN
            if (uristr == null) {
                intent = new Intent(Intent.ACTION_MAIN);
            } else {
                intent = new Intent(Intent.ACTION_MAIN, uri);
            }
            intent.setClassName(packagename, activityname);
        }

        //needed to place in the open apps list
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;

    }

    public boolean isValidActivity(AppShortcut app) {
        return isValidActivity(getAppIntent(app));
    }

    public boolean isValidActivity(Intent intent) {
        List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

}
