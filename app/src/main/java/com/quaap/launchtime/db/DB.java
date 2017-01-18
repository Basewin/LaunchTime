package com.quaap.launchtime.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.ViewGroup;

import com.quaap.launchtime.components.AppShortcut;
import com.quaap.launchtime.components.Categories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tom on 1/8/17.
 * <p>
 * Copyright (C) 2017  tom
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
public class DB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 1;

    private static final String ACTVNAME = "actvname";
    private static final String PKGNAME = "pkgname";
    private static final String LABEL = "label";
    private static final String LABELFULL = "labelfull";
    private static final String CATID = "catID";
    private static final String ISWIDGET = "iswidget";
    private static final String INDEX = "pos";
    private static final String TIME = "time";
    private static final String ISTINY = "tiny";


    private static final String APP_TABLE = "apps";
    private static final String[] appcolumns = {ACTVNAME, PKGNAME, LABEL, CATID, ISWIDGET};
    private static final String[] appcolumntypes = {"TEXT primary key", "TEXT", "TEXT", "TEXT", "SHORT"};
    private static final String APP_TABLE_CREATE = buildCreateTableStmt(APP_TABLE, appcolumns, appcolumntypes);

    private static final String[] appcolumnsindex = {PKGNAME, CATID};

    private static final String APP_ORDER_TABLE = "apps_order";
    private static final String[] appordercolumns = {CATID, ACTVNAME, INDEX};
    private static final String[] appordercolumntypes = {"TEXT", "TEXT", "INT"};
    private static final String APP_ORDER_TABLE_CREATE = buildCreateTableStmt(APP_ORDER_TABLE, appordercolumns, appordercolumntypes);

    private static final String[] appordercolumnsindex = {CATID + ", " + ACTVNAME, INDEX};


    private static final String TAB_ORDER_TABLE = "tab_order";
    private static final String[] tabordercolumns = {CATID, LABEL, LABELFULL, ISTINY, INDEX};
    private static final String[] tabordercolumntypes = {"TEXT primary key", "TEXT", "TEXT", "SHORT", "INT"};
    private static final String TAB_ORDER_TABLE_CREATE = buildCreateTableStmt(TAB_ORDER_TABLE, tabordercolumns, tabordercolumntypes);

    private static final String[] tabordercolumnsindex = {INDEX};


    private static final String APP_HISTORY_TABLE = "apps_hist";
    private static final String[] apphistorycolumns = {ACTVNAME, TIME};
    private static final String[] apphistorycolumntypes = {"TEXT", "DATETIME"};
    private static final String APP_HISTORY_TABLE_CREATE = buildCreateTableStmt(APP_HISTORY_TABLE, apphistorycolumns, apphistorycolumntypes);

    private static final String[] apphistorycolumnsindex = {TIME, ACTVNAME};


    private boolean firstRun;

    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.getWritableDatabase();
    }

    private static String buildCreateTableStmt(String tablename, String[] cols, String[] coltypes) {

        String create = "CREATE TABLE " + tablename + " (";
        for (int i = 0; i < cols.length; i++) {
            if (i != 0) create += ", ";
            create += cols[i] + " " + coltypes[i];
        }
        create += ");";
        return create;

    }

    private static String buildIndexStmt(String tablename, String col) {


        return "CREATE INDEX " + ((col + tablename).replaceAll("\\W+", "_")) + " on " + tablename + "(" + col + ");";


    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        firstRun = true;
        sqLiteDatabase.execSQL(APP_TABLE_CREATE);
        for (String createind : appcolumnsindex) {
            sqLiteDatabase.execSQL(buildIndexStmt(APP_TABLE, createind));
        }

        sqLiteDatabase.execSQL(APP_ORDER_TABLE_CREATE);
        for (String createind : appordercolumnsindex) {
            sqLiteDatabase.execSQL(buildIndexStmt(APP_ORDER_TABLE, createind));
        }

        sqLiteDatabase.execSQL(TAB_ORDER_TABLE_CREATE);
        for (String createind : tabordercolumnsindex) {
            sqLiteDatabase.execSQL(buildIndexStmt(TAB_ORDER_TABLE, createind));
        }

        sqLiteDatabase.execSQL(APP_HISTORY_TABLE_CREATE);
        for (String createind : apphistorycolumnsindex) {
            sqLiteDatabase.execSQL(buildIndexStmt(APP_HISTORY_TABLE, createind));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public List<String> getAppActvNames() {
        List<String> actvnames = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(APP_TABLE, new String[]{ACTVNAME}, null, null, null, null, LABEL);

        while (cursor.moveToNext()) {
            actvnames.add(cursor.getString(0));
        }
        cursor.close();
        return actvnames;
    }

    public AppShortcut getApp(String actvname) {

        AppShortcut appShortcut = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(APP_TABLE, appcolumns, ACTVNAME + "=?", new String[]{actvname}, null, null, null);

        if (cursor.moveToNext()) { //ACTVNAME, PKGNAME, LABEL, CATID
            String pkgname = cursor.getString(1);
            String label = cursor.getString(2);
            String catID = cursor.getString(3);
            boolean widget = cursor.getShort(4) == 1;

            // Log.d("LaunchDB", "getApp " + pkgname + " " + catID);
            appShortcut = AppShortcut.createAppShortcut(actvname, pkgname, label, catID, widget);
        }
        cursor.close();
        return appShortcut;
    }

    public List<AppShortcut> getApps(String catID) {

        List<AppShortcut> apps = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(APP_TABLE, appcolumns, CATID + "=?", new String[]{catID}, null, null, LABEL);

        while (cursor.moveToNext()) {
            String actvname = cursor.getString(0);
            String pkgname = cursor.getString(1);
            String label = cursor.getString(2);
            boolean widget = cursor.getShort(4) == 1;

            if (widget) {
                Log.d("db", "Found widget: " + actvname + " " + pkgname);
            }
            apps.add(AppShortcut.createAppShortcut(actvname, pkgname, label, catID, widget));
        }
        cursor.close();

        return apps;
    }

    public void addApp(AppShortcut shortcut) {
        addApp(shortcut.getActivityName(), shortcut.getPackageName(), shortcut.getLabel(), shortcut.getCategory(), shortcut.isWidget());
    }

    public void addApps(List<AppShortcut> shortcuts) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (AppShortcut shortcut : shortcuts) {
            addApp(db, shortcut.getActivityName(), shortcut.getPackageName(), shortcut.getLabel(), shortcut.getCategory(), shortcut.isWidget());
        }
    }

    public void addApp(String actvname, String pkgname, String label, String catID, boolean widget) {
        SQLiteDatabase db = this.getWritableDatabase();
        addApp(db, actvname, pkgname, label, catID, widget);
    }

    private void addApp(SQLiteDatabase db, String actvname, String pkgname, String label, String catID, boolean widget) {
        try {

            String dbcat = getCategoryDisplay(catID);
            if (dbcat==null) {
                Log.i("LaunchDB", "Use of category not in the database: " + catID, new Throwable());
                catID = Categories.CAT_OTHER;  //the user deleted the category
            }

            ContentValues values = new ContentValues();
            values.put(ACTVNAME, actvname);
            values.put(PKGNAME, pkgname);
            values.put(LABEL, label);
            values.put(CATID, catID);
            values.put(ISWIDGET, widget ? 1 : 0);

            db.insert(APP_TABLE, null, values);
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't insert package " + pkgname, e);
        }
    }

    public Cursor getAppCursor(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Cursor cursor = db.query(APP_TABLE, new String[]{CATID}, null, null, null, null, INDEX, null);
        Cursor cursor = db.rawQuery(
                "select distinct " + ACTVNAME + " _id, " + LABEL + " label " +
                        " from " + APP_TABLE +
                        " where " + LABEL + " like ? and " +  ISWIDGET + "=0 " +
                        " order by 2 ",
                new String[]{filter});

        return cursor;
    }

    public void updateAppCategory(String actvname, String catID) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(CATID, catID);

        db.update(APP_TABLE, values, ACTVNAME + "=?", new String[]{actvname});

        // Log.d("LaunchDB", "update " + pkgname + " " + catID);
    }

    public boolean deleteApp(String actvorpkgname) {
        return deleteApp(actvorpkgname, false);
    }

    public boolean deleteApp(String actvorpkgname, boolean isPackagename) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(APP_TABLE, (isPackagename?PKGNAME:ACTVNAME) + "=?", new String[]{actvorpkgname});
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't delete app " + actvorpkgname, e);
            return false;
        }
        return true;
    }

    public boolean addCategory(String catID, String displayName, String displayNameFull) {
        return addCategory(catID, displayName, displayNameFull, -1);
    }

    public boolean addCategory(String catID, String displayName, String displayNameFull, int index) {
        return addCategory(catID, displayName, displayNameFull, false, index);
    }

    public boolean addCategory(String catID, String displayName, String displayNameFull, boolean isTiny) {
        return addCategory(catID, displayName, displayNameFull, isTiny, -1);
    }

    public boolean addCategory(String catID, String displayName, String displayNameFull, boolean isTiny, int index) {
        try {

            for (String existcat: getCategories()) {
                if (existcat.equalsIgnoreCase(catID)) return false;
            }

            SQLiteDatabase db = this.getWritableDatabase();

            // Log.d("DB", "adding catID " + catID);
            ContentValues values = new ContentValues();
            values.put(CATID, catID);
            values.put(LABEL, displayName);
            values.put(LABELFULL, displayNameFull);
            values.put(ISTINY, isTiny?1:0);
            values.put(INDEX, index);

            db.insert(TAB_ORDER_TABLE, null, values);
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't add catID " + catID, e);
            return false;
        }
        return true;
    }


    public boolean updateCategory(String catID, String displayName, String displayNameFull) {
        return updateCategory(catID, displayName, displayNameFull, false);
    }

    public boolean updateCategory(String catID, String displayName, String displayNameFull, boolean isTiny) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            // Log.d("DB", "adding catID " + catID);
            ContentValues values = new ContentValues();

            values.put(LABEL, displayName);
            values.put(LABELFULL, displayNameFull);
            values.put(ISTINY, isTiny?1:0);

            db.update(TAB_ORDER_TABLE, values, CATID + "=?", new String[]{catID});
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't select catID " + catID, e);
            return false;
        }
        return true;
    }

    public boolean deleteCategory(String catID) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {

            db.beginTransaction();

            {
                //Update existing apps with the deleted category to category other
                ContentValues values = new ContentValues();
                values.put(CATID, Categories.CAT_OTHER);
                db.update(APP_TABLE, values, CATID + "=?", new String[]{catID});
            }

            {
                //Move old cat app order to category other
                List<String> oldAppOrder = getAppCategoryOrder(catID);
                List<String> otherAppOrder = getAppCategoryOrder(Categories.CAT_OTHER);

                otherAppOrder.addAll(oldAppOrder);
                setAppCategoryOrder(Categories.CAT_OTHER, otherAppOrder, true);
            }

            db.delete(TAB_ORDER_TABLE, CATID + "=?", new String[]{catID});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't delete catID " + catID, e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(true, TAB_ORDER_TABLE, new String[]{CATID}, null, null, null, null, INDEX, null);

        //Log.d("DB", "getting catagories");
        while (cursor.moveToNext()) {

            categories.add(cursor.getString(0));
            //          Log.d("DB", "got catID " + cursor.getString(0));
        }
        cursor.close();
        return categories;
    }

    public String getCategoryDisplay(String catID) {
        String display = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TAB_ORDER_TABLE, new String[]{LABEL}, CATID + "=?", new String[]{catID}, null, null, null, null);

        if (cursor.moveToNext()) {
            display = cursor.getString(0);
        }
        cursor.close();
        return display;
    }

    public String getCategoryDisplayFull(String catID) {
        String display = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TAB_ORDER_TABLE, new String[]{LABELFULL}, CATID + "=?", new String[]{catID}, null, null, null, null);

        if (cursor.moveToNext()) {
            display = cursor.getString(0);
        }
        cursor.close();
        return display;
    }

    public boolean isTinyCategory(String catID) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TAB_ORDER_TABLE, new String[]{ISTINY}, CATID + "=?", new String[]{catID}, null, null, null, null);

        if (cursor.moveToNext()) {
            return cursor.getShort(0)==1;
        }
        cursor.close();
        return false;
    }

    public void setCategoryOrder(ViewGroup container) {

        List<String> cats = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            Object tag = container.getChildAt(i).getTag();
            if (tag instanceof String) {
                cats.add((String) tag);
            }
        }

        setCategoryOrder(cats);
    }

    public void setCategoryOrder(List<String> apps) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            for (int i = 0; i < apps.size(); i++) {

                ContentValues values = new ContentValues();

                values.put(INDEX, i);

                db.update(TAB_ORDER_TABLE, values, CATID + "=?", new String[]{apps.get(i)});
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't setCategoryOrder ", e);

        } finally {
            db.endTransaction();
        }
    }

    public void setAppCategoryOrder(String catID, ViewGroup container) {

        List<AppShortcut> apps = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            Object tag = container.getChildAt(i).getTag();
            if (tag instanceof AppShortcut) {
                apps.add((AppShortcut) tag);
            }
        }

        setAppCategoryOrder(catID, apps);
    }

    public void setAppCategoryOrder(String catID, List<AppShortcut> apps) {
        List<String> actvnames = new ArrayList<>();
        for (AppShortcut app : apps) {
            actvnames.add(app.getActivityName());
        }
        setAppCategoryOrder(catID, actvnames, true);
    }

    public void setAppCategoryOrder(String catID, List<String> actvnames, boolean dummy) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();
            db.delete(APP_ORDER_TABLE, CATID + "=?", new String[]{catID}); //CATID, PKGNAME, INDEX};

            for (int i = 0; i < actvnames.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(CATID, catID);
                values.put(ACTVNAME, actvnames.get(i));
                values.put(INDEX, i);
                db.insert(APP_ORDER_TABLE, null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("LaunchDB", "Can't setAppCategoryOrder for catID " + catID, e);

        } finally {
            db.endTransaction();
        }
    }

    public void addAppCategoryOrder(String catID, String actvname) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {

            ContentValues values = new ContentValues();
            values.put(CATID, catID);
            values.put(ACTVNAME, actvname);
            values.put(INDEX, 100);
            db.insert(APP_ORDER_TABLE, null, values);

        } catch (Exception e) {
            Log.e("LaunchDB", "Can't setAppCategoryOrder for catID " + catID, e);

        }
    }

    public List<String> getAppCategoryOrder(String catID) {
        List<String> actvnames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(APP_ORDER_TABLE, new String[]{ACTVNAME}, CATID + "=?", new String[]{catID}, null, null, INDEX);

        while (cursor.moveToNext()) {
            actvnames.add(cursor.getString(0));
        }
        cursor.close();
        return actvnames;

    }

    public void appLaunched(String activityname) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ACTVNAME, activityname);
        values.put(TIME, System.currentTimeMillis());
        db.insert(APP_HISTORY_TABLE, null, values);

    }

    public void deleteAppLaunchedRecord(String activityname) {

        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(ACTVNAME, activityname);
//        values.put(TIME, System.currentTimeMillis());
//        db.insert(APP_HISTORY_TABLE, null, values);

        db.delete(APP_HISTORY_TABLE, ACTVNAME+"=?", new String[] {activityname});

    }


    public List<String> getAppLaunchedList() {

        List<String> activitynames = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                true,
                APP_HISTORY_TABLE,
                new String[]{ACTVNAME},
                null, null, null, null, TIME + " desc", null);


        while (cursor.moveToNext()) {
            activitynames.add(cursor.getString(0));
        }
        cursor.close();
        return activitynames;

    }

    public int getAppLaunchedCount(String activityname) {
        return getAppLaunchedCount(activityname, new Date(0));
    }

    public int getAppLaunchedCount(String activityname, Date after) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                APP_HISTORY_TABLE,
                new String[]{"count(*)"},
                ACTVNAME + "=? and " + TIME + ">?",
                new String[]{activityname, after.getTime() + ""},
                null, null, null, null);

        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;

    }

}
