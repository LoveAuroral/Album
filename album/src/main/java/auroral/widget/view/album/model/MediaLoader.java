package auroral.widget.view.album.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import auroral.widget.view.album.config.AlbumConfig;
import auroral.widget.view.album.entity.Media;
import auroral.widget.view.album.entity.MediaFolder;
import auroral.widget.view.album.utils.MediaTypeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class MediaLoader {
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final String DURATION = "duration";
    private static final String NOT_GIF = "!='image/gif'";
    private static final int AUDIO_DURATION = 500;// 过滤掉小于500毫秒的录音
    private int type;
    private FragmentActivity activity;
    private boolean isGif;
    private long videoMaxS;
    private long videoMinS;

    // 媒体文件数据库字段
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            DURATION};

    // 图片
    private static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_NOT_GIF = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF;

    // 查询条件(音视频)
    private static String getSelectionArgsForSingleMediaCondition(String time_condition) {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + " AND " + time_condition;
    }

    // 全部模式下条件
    private static String getSelectionArgsForAllMediaCondition(String time_condition, boolean isGif) {
        String condition = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + (isGif ? "" : " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)
                + " OR "
                + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + time_condition) + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0";
        return condition;
    }

    // 获取图片or视频
    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    /**
     * 获取指定类型的文件
     *
     * @param mediaType
     * @return
     */
    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    public MediaLoader(FragmentActivity activity, int type, boolean isGif, long videoMaxS, long videoMinS) {
        this.activity = activity;
        this.type = type;
        this.isGif = isGif;
        this.videoMaxS = videoMaxS;
        this.videoMinS = videoMinS;
    }

    public void loadAllMedia(final LocalMediaLoadListener imageLoadListener) {
        activity.getSupportLoaderManager().initLoader(type, null,
                new LoaderManager.LoaderCallbacks<Cursor>() {
                    @NonNull
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        CursorLoader cursorLoader = null;
                        switch (id) {
                            case AlbumConfig.TYPE_ALL:
                                String all_condition = getSelectionArgsForAllMediaCondition(getDurationCondition(0, 0), isGif);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI,
                                        PROJECTION, all_condition,
                                        SELECTION_ALL_ARGS, ORDER_BY);
                                break;
                            case AlbumConfig.TYPE_IMAGE:
                                // 只获取图片
                                String[] MEDIA_TYPE_IMAGE = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI,
                                        PROJECTION, isGif ? SELECTION : SELECTION_NOT_GIF, MEDIA_TYPE_IMAGE
                                        , ORDER_BY);
                                break;
                            case AlbumConfig.TYPE_VIDEO:
                                // 只获取视频
                                String video_condition = getSelectionArgsForSingleMediaCondition(getDurationCondition(0, 0));
                                String[] MEDIA_TYPE_VIDEO = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI, PROJECTION, video_condition, MEDIA_TYPE_VIDEO
                                        , ORDER_BY);
                                break;
                            case AlbumConfig.TYPE_AUDIO:
                                String audio_condition = getSelectionArgsForSingleMediaCondition(getDurationCondition(0, AUDIO_DURATION));
                                String[] MEDIA_TYPE_AUDIO = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO);
                                cursorLoader = new CursorLoader(
                                        activity, QUERY_URI, PROJECTION, audio_condition, MEDIA_TYPE_AUDIO
                                        , ORDER_BY);
                                break;
                        }
                        return cursorLoader;
                    }

                    @Override
                    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                        try {
                            List<MediaFolder> imageFolders = new ArrayList<>();
                            MediaFolder allImageFolder = new MediaFolder();
                            List<Media> latelyImages = new ArrayList<>();
                            if (data != null) {
                                int count = data.getCount();
                                if (count > 0) {
                                    data.moveToFirst();
                                    do {
                                        String path = data.getString
                                                (data.getColumnIndexOrThrow(PROJECTION[1]));

                                        String pictureType = data.getString
                                                (data.getColumnIndexOrThrow(PROJECTION[2]));

                                        int w = data.getInt
                                                (data.getColumnIndexOrThrow(PROJECTION[3]));

                                        int h = data.getInt
                                                (data.getColumnIndexOrThrow(PROJECTION[4]));

                                        int duration = data.getInt
                                                (data.getColumnIndexOrThrow(PROJECTION[5]));

                                        Media image = new Media
                                                (path, duration, type, pictureType, w, h);

                                        MediaFolder folder = getImageFolder(path, imageFolders);
                                        List<Media> images = folder.getImages();
                                        images.add(image);
                                        folder.setImageNum(folder.getImageNum() + 1);
                                        latelyImages.add(image);
                                        int imageNum = allImageFolder.getImageNum();
                                        allImageFolder.setImageNum(imageNum + 1);
                                    } while (data.moveToNext());

                                    if (latelyImages.size() > 0) {
                                        sortFolder(imageFolders);
                                        imageFolders.add(0, allImageFolder);
                                        allImageFolder.setFirstImagePath
                                                (latelyImages.get(0).getPath());
                                        String title = type == MediaTypeUtil.ofAudio() ? "音频" : "相册";
                                        allImageFolder.setName(title);
                                        allImageFolder.setImages(latelyImages);
                                    }
                                    imageLoadListener.loadComplete(imageFolders);
                                } else {
                                    // 如果没有相册
                                    imageLoadListener.loadComplete(imageFolders);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                    }
                });
    }

    /**
     * 文件夹数量进行排序
     *
     * @param imageFolders
     */
    private void sortFolder(List<MediaFolder> imageFolders) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, new Comparator<MediaFolder>() {
            @Override
            public int compare(MediaFolder lhs, MediaFolder rhs) {
                if (lhs.getImages() == null || rhs.getImages() == null) {
                    return 0;
                }
                int lsize = lhs.getImageNum();
                int rsize = rhs.getImageNum();
                return lsize == rsize ? 0 : (lsize < rsize ? 1 : -1);
            }
        });
    }

    /**
     * 创建相应文件夹
     *
     * @param path
     * @param imageFolders
     * @return
     */
    private MediaFolder getImageFolder(String path, List<MediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();
        for (MediaFolder folder : imageFolders) {
            // 同一个文件夹下，返回自己，否则创建新文件夹
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        MediaFolder newFolder = new MediaFolder();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        newFolder.setFirstImagePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    /**
     * 获取视频(最长或最小时间)
     *
     * @param exMaxLimit
     * @param exMinLimit
     * @return
     */
    private String getDurationCondition(long exMaxLimit, long exMinLimit) {
        long maxS = videoMaxS == 0 ? Long.MAX_VALUE : videoMaxS;
        if (exMaxLimit != 0) maxS = Math.min(maxS, exMaxLimit);

        return String.format(Locale.CHINA, "%d <%s duration and duration <= %d",
                Math.max(exMinLimit, videoMinS),
                Math.max(exMinLimit, videoMinS) == 0 ? "" : "=",
                maxS);
    }

    public interface LocalMediaLoadListener {
        void loadComplete(List<MediaFolder> folders);
    }
}
