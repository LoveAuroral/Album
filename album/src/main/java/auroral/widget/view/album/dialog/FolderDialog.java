package auroral.widget.view.album.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.auroral.album.R;
import auroral.widget.view.album.adapter.FolderAdapter;
import auroral.widget.view.album.decoration.FolderDecoration;
import auroral.widget.view.album.entity.Media;
import auroral.widget.view.album.entity.MediaFolder;
import auroral.widget.view.album.utils.ScreenUtils;

import java.util.List;

/**
 * Created by Auroral 2018/11/21 20:26
 */
public class FolderDialog extends Dialog {

    private Activity mActivity;
    private FolderAdapter mPhotoFolderAdapter;
    private Animation mAnimationIn, mAnimationOut;
    private View v_blank;
    private RecyclerView rv_folder_info;
    private OnClickListener mOnClickListener;

    public FolderDialog(Activity activity) {
        super(activity, R.style.alb_dialog_folder);
        setContentView(R.layout.alb_dialog_folder);
        mActivity = activity;
        initData();
        initView();
        initViewData();
    }

    private void initData() {
        mAnimationIn = AnimationUtils.loadAnimation(mActivity, R.anim.alb_dialog_show);
        mAnimationOut = AnimationUtils.loadAnimation(mActivity, R.anim.alb_dialog_dismiss);
        mPhotoFolderAdapter = new FolderAdapter(mActivity);
        mPhotoFolderAdapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String folderName, List<Media> images) {
                mOnClickListener.onClick(folderName, images);
                hide();
            }
        });
    }

    private void initView() {
        v_blank = findViewById(R.id.v_blank);
        rv_folder_info = findViewById(R.id.rv_folder_info);
    }

    private void initViewData() {
        v_blank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        rv_folder_info.addItemDecoration(new FolderDecoration(
                mActivity,
                LinearLayoutManager.HORIZONTAL,
                ScreenUtils.dip2px(mActivity,
                        0),
                ContextCompat.getColor(mActivity,
                        R.color.alb_transparent)));
        rv_folder_info.setLayoutManager(new LinearLayoutManager(mActivity));
        rv_folder_info.setAdapter(mPhotoFolderAdapter);
    }

    public void setFolderInfo(List<MediaFolder> folders) {
        mPhotoFolderAdapter.bindFolderData(folders);
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(layoutParams);
            getWindow().setDimAmount(0.3f);
        }
        rv_folder_info.startAnimation(mAnimationIn);
        mPhotoFolderAdapter.notifyDataSetChanged();
    }

    @Override
    public void hide() {
        rv_folder_info.startAnimation(mAnimationOut);
        mAnimationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                FolderDialog.super.hide();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }

    @Override
    public void onBackPressed() {
        hide();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(String folderName, List<Media> images);
    }

}
