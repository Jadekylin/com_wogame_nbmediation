// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.wogame.nbmediation.utils;

import android.util.Log;

public class NewApiUtils {

    public static final String TAG = "AdtDebug";
    public static boolean ENABLE_LOG = false;
    public static final String APPKEY = "kXDlKvOwFYf0inXBd65Pzo0vpF2utBim";

    public static final String P_BANNER = "260";
    public static final String P_NATIVE = "258";

    public static void printLog(String msg) {
        if (ENABLE_LOG) {
            Log.e(TAG, msg);
        }
    }
}
