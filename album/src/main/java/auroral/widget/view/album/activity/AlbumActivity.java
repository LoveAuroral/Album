package auroral.widget.view.album.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import auroral.widget.view.album.R;
import auroral.widget.view.album.adapter.ImageAdapter;
import auroral.widget.view.album.config.AlbumConfig;
import auroral.widget.view.album.decoration.ImageDecoration;
import auroral.widget.view.album.dialog.FolderDialog;
import auroral.widget.view.album.entity.Media;
import auroral.widget.view.album.entity.MediaFolder;
import auroral.widget.view.album.model.MediaLoader;
import auroral.widget.view.album.utils.AnimationLoaderUtils;
import auroral.widget.view.album.utils.MediaTypeUtil;
import auroral.widget.view.album.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Auroral 2018/11/28 17:54
 */
public class AlbumActivity extends AppCompatActivity {
    protected Context mContext;
    private MediaLoader mMediaLoader;
    private ImageAdapter mImageAdapter;
    private FolderDialog mFolderDialog;
    private Animation mAnimation;
    private List<MediaFolder> mMediaFolders;
    private List<Media> mSelectImages;
    private TextView tv_content, tv_finish, tv_select_folder, tv_select_image_num, tv_select_image_sign;
    private ImageView iv_left_arrow;
    private RecyclerView rv_media;
    private String[] mImages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alb_activity_album);
        mContext = getApplicationContext();
        initData();
        initView();
        initViewData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_content.setText("相册");
    }

    private void initData() {
        mImages = getIntent().getStringArrayExtra("images");
        mFolderDialog = new FolderDialog(this);
        mFolderDialog.setOnClickListener(new FolderDialog.OnClickListener() {
            @Override
            public void onClick(String folderName, List<Media> images) {
                tv_content.setText(folderName);
                mImageAdapter.bindImagesData(images);
            }
        });
        mImageAdapter = new ImageAdapter(mContext);
        mImageAdapter.setOnSelectChangedListener(new ImageAdapter.OnSelectChangedListener() {
            @Override
            public void onChange(List<Media> selectImages) {
                mSelectImages = selectImages;
                for (MediaFolder folder : mMediaFolders) {
                    folder.setCheckedNum(0);
                }
                if (mSelectImages != null && mSelectImages.size() > 0) {
                    for (MediaFolder folder : mMediaFolders) {
                        int num = 0;// 记录当前相册下有多少张是选中的
                        List<Media> images = folder.getImages();
                        for (Media media : images) {
                            String path = media.getPath();
                            for (Media m : mSelectImages) {
                                if (path.equals(m.getPath())) {
                                    num++;
                                    folder.setCheckedNum(num);
                                }
                            }
                        }
                    }
                    updateViewData(mSelectImages.size());
                } else {
                    updateViewData(0);
                }
            }

            @Override
            public void onPictureClick(Media media, int position) {

            }
        });
        mMediaLoader = new MediaLoader(this, AlbumConfig.TYPE_IMAGE, false, 5 * 60 * 1000, 0);
        mAnimation = AnimationLoaderUtils.loadAnimation(mContext, R.anim.alb_select_image);
    }

    private void initView() {
        tv_content = findViewById(R.id.tv_content);
        iv_left_arrow = findViewById(R.id.iv_left_arrow);
        tv_select_folder = findViewById(R.id.tv_select_folder);
        tv_finish = findViewById(R.id.tv_finish);
        tv_select_image_num = findViewById(R.id.tv_select_image_num);
        tv_select_image_sign = findViewById(R.id.tv_select_image_sign);
        rv_media = findViewById(R.id.rv_media);
    }

    private void initViewData() {
        tv_content.setText("相册");
        tv_select_image_sign.setText("请选择");
        tv_select_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFolderDialog.show();
            }
        });
        iv_left_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        rv_media.addItemDecoration(new ImageDecoration(4,
                ScreenUtils.dip2px(this, 2), false));
        rv_media.setLayoutManager(new GridLayoutManager(this, 4));
        rv_media.setAdapter(mImageAdapter);
        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectImages != null && mSelectImages.size() > 0) {
                    String[] paths = new String[mSelectImages.size()];
                    for (int i = 0; i < mSelectImages.size(); i++) {
                        paths[i] = mSelectImages.get(i).getPath();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("paths", paths);
                    int result = MediaTypeUtil.isVideo(mSelectImages.get(0).getPictureType()) ? 99 : 98;
                    setResult(result, intent);
                    finish();
                } else {
                    Log.i("Auroral", "没有选中的图片");
                }
            }
        });
        getData();
    }

    @SuppressLint("SetTextI18n")
    private void updateViewData(int selectNum) {
        if (selectNum > 0) {
            tv_select_image_num.startAnimation(mAnimation);
            tv_select_image_num.setText(selectNum + "");
            tv_select_image_num.setVisibility(View.VISIBLE);
            tv_select_image_sign.setSelected(true);
            tv_select_image_sign.setText("已选择");
            tv_finish.setSelected(true);
        } else {
            tv_select_image_num.setVisibility(View.INVISIBLE);
            tv_select_image_sign.setSelected(false);
            tv_select_image_sign.setText("请选择");
            tv_finish.setSelected(false);
        }

    }

    private void getData() {
        mMediaLoader.loadAllMedia(new MediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<MediaFolder> folders) {
                mMediaFolders = folders;
                if (mMediaFolders.size() > 0) {
                    MediaFolder folder = mMediaFolders.get(0);
                    folder.setChecked(true);
                    List<Media> localImg = folder.getImages();
                    mFolderDialog.setFolderInfo(mMediaFolders);
                    mImageAdapter.bindImagesData(localImg);
                }
                // Binds the selected image after all images have been loaded.
                if (mImages != null && mImages.length > 0) {
                    List<Media> selection = new ArrayList<>();
                    for (String path :
                            mImages) {
                        selection.add(new Media(path, 0, AlbumConfig.TYPE_IMAGE, AlbumConfig.IMAGE));
                    }
                    mImageAdapter.bindSelectImages(selection);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
