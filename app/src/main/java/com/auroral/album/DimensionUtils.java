package com.auroral.album;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Created by Auroral 2018/12/5 11:03
 */
public class DimensionUtils {

    @SuppressLint("StaticFieldLeak")
    private static DimensionUtils mDimensionUtils;
    private final Context mContext;

    private DimensionUtils(Context context) {
        mContext = context;
    }

    /**
     * Get DimensionUtils instance.
     * All method use,will be starts here.
     *
     * @param context Context
     * @return DimensionUtils
     */
    public static DimensionUtils getInstance(Context context) {
        if (mDimensionUtils == null) {
            mDimensionUtils = new DimensionUtils(context);
        }
        return mDimensionUtils;
    }

    /**
     * dp to px.
     *
     * @param dp dp
     * @return px
     */
    public int dp2px(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    /**
     * px to dp.
     *
     * @param px px
     * @return dp
     */
    public int px2dp(int px) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * sp to px.
     *
     * @param sp sp
     * @return px
     */
    public int sp2px(int sp) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    /**
     * px to sp.
     *
     * @param px px
     * @return sp
     */
    public int px2sp(int px) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }
}
