package auroral.widget.view.album.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import auroral.widget.view.album.activity.AlbumActivity;

import java.util.ArrayList;

/**
 * Created by Auroral 2018/12/3 14:58
 */
public class AlbumUtils {

    private Activity mActivity;
    private final int GET_REQUEST = 2;
    private AlbumListener mAlbumListener;
    private PermissionsUtils mPermissionsUtils;
    private String[] mImages;

    /**
     * init AlbumUtil.
     *
     * @param activity Activity
     */
    private AlbumUtils(Activity activity) {
        mActivity = activity;
        mPermissionsUtils = PermissionsUtils.getInstance(mActivity, GET_REQUEST);
    }

    /**
     * Get AlbumUtil instance.
     * All method use,will be starts here.
     *
     * @param activity Activity
     * @return AlbumUtil
     */
    public static AlbumUtils getInstance(Activity activity) {
        return new AlbumUtils(activity);
    }

    public void startAlbum() {
        if (mPermissionsUtils.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            openAlbum();
        }
    }

    public void bindSelectImages(ArrayList<String> pictures) {
        if (pictures != null && pictures.size() > 0) {
            String[] paths = new String[pictures.size()];
            for (int i = 0; i < pictures.size(); i++) {
                paths[i] = pictures.get(i);
            }
            mImages = paths;
        }
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_REQUEST) {
            String[] paths;
            if (resultCode == 98) {
                paths = data.getStringArrayExtra("paths");
                mAlbumListener.albumResult(paths, MediaType.PICTURE);
            }
            if (resultCode == 99) {
                paths = data.getStringArrayExtra("paths");
                mAlbumListener.albumResult(paths, MediaType.VIDEO);
            }
        }
    }

    public void requestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (mPermissionsUtils.requestPermissionsResult(requestCode, grantResults)) {
            openAlbum();
        } else {
            mAlbumListener.errorResult("请检查读写权限,没有权限", ErrorType.PERMISSIONS);
        }
    }

    public void setAlbumListener(AlbumListener albumListener) {
        mAlbumListener = albumListener;
    }

    public interface AlbumListener {
        void errorResult(String msg, ErrorType type);

        void albumResult(String[] paths, MediaType type);
    }

    /**
     * media type.
     */
    public enum MediaType {
        /**
         * picture
         */
        PICTURE,
        /**
         * video
         */
        VIDEO
    }

    /**
     * error type.
     */
    public enum ErrorType {
        PERMISSIONS
    }

    private void openAlbum() {
        Intent intent = new Intent(mActivity, AlbumActivity.class);
        intent.putExtra("images", mImages);
        mActivity.startActivityForResult(intent, GET_REQUEST);
    }
}
