package auroral.widget.view.album.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Auroral 2018/12/3 16:13
 */
public class PermissionsUtils {

    private Activity mActivity;
    private int mRequestCode;

    /**
     * init PermissionsUtils.
     *
     * @param activity    Activity
     * @param requestCode requestCode
     */
    private PermissionsUtils(Activity activity, int requestCode) {
        mActivity = activity;
        mRequestCode = requestCode;
    }

    /**
     * Get PermissionsUtils instance.
     * All method use,will be starts here.
     *
     * @param activity    Activity
     * @param requestCode requestCode
     * @return PermissionsUtils
     */
    public static PermissionsUtils getInstance(Activity activity, int requestCode) {
        return new PermissionsUtils(activity, requestCode);
    }

    /**
     * check permission.
     *
     * @param permissions permission
     * @return boolean
     */
    public boolean checkPermissions(String... permissions) {
        boolean hasAllPermissions = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissions != null) {
                for (String permission : permissions) {
                    if (ContextCompat.checkSelfPermission(mActivity, permission)
                            != PackageManager.PERMISSION_GRANTED) {
                        hasAllPermissions = false;
                    }
                }
                if (!hasAllPermissions) {
                    ActivityCompat.requestPermissions(mActivity, permissions, mRequestCode);
                }
            }
        }
        return hasAllPermissions;
    }

    /**
     * request permissions result.
     *
     * @param requestCode  requestCode
     * @param grantResults grantResults
     * @return boolean
     */
    public boolean requestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        boolean hasAllPermissions = false;
        if (requestCode == mRequestCode) {
            hasAllPermissions = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermissions = false;
                }
            }
        }
        return hasAllPermissions;
    }

}
