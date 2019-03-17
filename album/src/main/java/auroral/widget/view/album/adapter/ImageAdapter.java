package auroral.widget.view.album.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import auroral.widget.view.album.R;
import auroral.widget.view.album.config.AlbumConfig;
import auroral.widget.view.album.entity.Media;
import auroral.widget.view.album.utils.AnimationLoaderUtils;
import auroral.widget.view.album.utils.DateUtils;
import auroral.widget.view.album.utils.MediaTypeUtil;
import auroral.widget.view.album.utils.ToastManage;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int DURATION = 450;
    private Context context;
    private boolean showCamera = true;
    private OnSelectChangedListener mOnSelectChangedListener;
    private int maxSelectNum;
    private List<Media> images = new ArrayList<Media>();
    private List<Media> selectImages = new ArrayList<Media>();
    private boolean enablePreview;
    private int selectMode;
    private boolean enablePreviewVideo = false;
    private boolean enablePreviewAudio = false;
    private boolean is_checked_num;
    private Animation animation;
    private boolean zoomAnim;
    /**
     * 单选图片
     */
    private boolean isGo;

    public ImageAdapter(Context context) {
        this.context = context;
        this.selectMode = AlbumConfig.MULTIPLE;// 多选 or 单选 PictureConfig.MULTIPLE : PictureConfig.SINGLE
        this.showCamera = false;
        this.maxSelectNum = 8;// 最大图片选择数量
        this.enablePreview = false;// 是否可预览图片
        this.enablePreviewVideo = false;// 是否可预览视频
        this.enablePreviewAudio = false;// 是否可播放音频
        this.is_checked_num = false;// 选择图片后提示圆点中是否显示数字
        this.zoomAnim = true;// 图片列表点击 缩放效果 默认true
        animation = AnimationLoaderUtils.loadAnimation(context, R.anim.alb_select_image);
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public void bindImagesData(List<Media> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public void bindSelectImages(List<Media> images) {
        // 这里重新构构造一个新集合，不然会产生已选集合一变，结果集合也会添加的问题
        List<Media> selection = new ArrayList<>();
        for (Media media : images) {
            selection.add(media);
        }
        this.selectImages = selection;
        subSelectPosition();
        if (mOnSelectChangedListener != null) {
            mOnSelectChangedListener.onChange(selectImages);
        }
    }

    public List<Media> getSelectedImages() {
        if (selectImages == null) {
            selectImages = new ArrayList<>();
        }
        return selectImages;
    }

    public List<Media> getImages() {
        if (images == null) {
            images = new ArrayList<>();
        }
        return images;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return AlbumConfig.TYPE_CAMERA;
        } else {
            return AlbumConfig.TYPE_PICTURE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AlbumConfig.TYPE_CAMERA) {
            View view = LayoutInflater.from(context).inflate(R.layout.alb_item_camera, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.alb_item_image, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == AlbumConfig.TYPE_CAMERA) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "点击了相机", Toast.LENGTH_SHORT).show();
//                    if (imageSelectChangedListener != null) {
//                        imageSelectChangedListener.onTakePhoto();
//                    }
                }
            });
        } else {
            final ViewHolder contentHolder = (ViewHolder) holder;
            final Media image = images.get(showCamera ? position - 1 : position);
            image.position = contentHolder.getAdapterPosition();
            final String path = image.getPath();
            final String pictureType = image.getPictureType();
            if (is_checked_num) {
                notifyCheckChanged(contentHolder, image);
            }
            selectImage(contentHolder, isSelected(image), false);

            final int mediaMimeType = MediaTypeUtil.isPictureType(pictureType);
            boolean gif = MediaTypeUtil.isGif(pictureType);

            contentHolder.tv_isGif.setVisibility(gif ? View.VISIBLE : View.GONE);
            contentHolder.ll_duration.setVisibility(mediaMimeType == AlbumConfig.TYPE_VIDEO
                    ? View.VISIBLE : View.GONE);
            boolean eqLongImg = MediaTypeUtil.isLongImg(image);
            contentHolder.tv_long_chart.setVisibility(eqLongImg ? View.VISIBLE : View.GONE);
            long duration = image.getDuration();
            contentHolder.tv_duration.setText(DateUtils.timeParse(duration));
            RequestOptions options = new RequestOptions();
            options.override(160, 160);
            options.diskCacheStrategy(DiskCacheStrategy.ALL);
            options.centerCrop();
            options.placeholder(R.drawable.alb_image_bg);
            Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .apply(options)
                    .into(contentHolder.iv_picture);
            if (enablePreview || enablePreviewVideo || enablePreviewAudio) {
                contentHolder.ll_check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 如原图路径不存在或者路径存在但文件不存在
                        if (!new File(path).exists()) {
                            ToastManage.s(context, MediaTypeUtil.s(context, mediaMimeType));
                            return;
                        }
                        changeCheckboxState(contentHolder, image);
                    }
                });
            }
            contentHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 如原图路径不存在或者路径存在但文件不存在
                    if (!new File(path).exists()) {
                        ToastManage.s(context, MediaTypeUtil.s(context, mediaMimeType));
                        return;
                    }
                    int index = showCamera ? position - 1 : position;
                    boolean eqResult =
                            mediaMimeType == AlbumConfig.TYPE_IMAGE && enablePreview
                                    || mediaMimeType == AlbumConfig.TYPE_VIDEO && (enablePreviewVideo
                                    || selectMode == AlbumConfig.SINGLE)
                                    || mediaMimeType == AlbumConfig.TYPE_AUDIO && (enablePreviewAudio
                                    || selectMode == AlbumConfig.SINGLE);
                    if (eqResult) {
                        mOnSelectChangedListener.onPictureClick(image, index);
                    } else {
                        changeCheckboxState(contentHolder, image);
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return showCamera ? images.size() + 1 : images.size();
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        View headerView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerView = itemView;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_picture;
        TextView check;
        TextView tv_duration, tv_isGif, tv_long_chart;
        View contentView;
        LinearLayout ll_check, ll_duration;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            iv_picture = itemView.findViewById(R.id.iv_picture);
            check = itemView.findViewById(R.id.check);
            ll_check = itemView.findViewById(R.id.ll_check);
            ll_duration = itemView.findViewById(R.id.ll_duration);
            tv_duration = itemView.findViewById(R.id.tv_duration);
            tv_isGif = itemView.findViewById(R.id.tv_isGif);
            tv_long_chart = itemView.findViewById(R.id.tv_long_chart);
        }
    }

    public boolean isSelected(Media image) {
        for (Media media : selectImages) {
            if (media.getPath().equals(image.getPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 选择按钮更新
     */
    private void notifyCheckChanged(ViewHolder viewHolder, Media imageBean) {
        viewHolder.check.setText("");
        for (Media media : selectImages) {
            if (media.getPath().equals(imageBean.getPath())) {
                imageBean.setNum(media.getNum());
                media.setPosition(imageBean.getPosition());
                viewHolder.check.setText(String.valueOf(imageBean.getNum()));
            }
        }
    }

    /**
     * 改变图片选中状态
     *
     * @param contentHolder
     * @param image
     */

    private void changeCheckboxState(ViewHolder contentHolder, Media image) {
        boolean isChecked = contentHolder.check.isSelected();
        String pictureType = selectImages.size() > 0 ? selectImages.get(0).getPictureType() : "";
        if (!TextUtils.isEmpty(pictureType)) {
            boolean toEqual = MediaTypeUtil.mimeToEqual(pictureType, image.getPictureType());
            if (!toEqual) {
                ToastManage.s(context, "不能同时选择图片或视频");
                return;
            }
        }
        if (selectImages.size() >= maxSelectNum && !isChecked) {
            boolean eqImg = pictureType.startsWith(AlbumConfig.IMAGE);
            String str = eqImg ? "你最多可以选择" + maxSelectNum + "张图片" : "你最多可以选择" + maxSelectNum + "个视频";
            ToastManage.s(context, str);
            return;
        }

        if (isChecked) {
            for (Media media : selectImages) {
                if (media.getPath().equals(image.getPath())) {
                    selectImages.remove(media);
                    subSelectPosition();
                    disZoom(contentHolder.iv_picture);
                    break;
                }
            }
        } else {
            // 如果是单选，则清空已选中的并刷新列表(作单一选择)
            if (selectMode == AlbumConfig.SINGLE) {
                singleRadioMediaImage();
            }
            selectImages.add(image);
            image.setNum(selectImages.size());
//            VoiceUtils.playVoice(context, enableVoice);
            zoom(contentHolder.iv_picture);
        }
        //通知点击项发生了改变
        notifyItemChanged(contentHolder.getAdapterPosition());
        selectImage(contentHolder, !isChecked, true);
        if (mOnSelectChangedListener != null) {
            mOnSelectChangedListener.onChange(selectImages);
        }
    }

    /**
     * 单选模式
     */
    private void singleRadioMediaImage() {
        if (selectImages != null
                && selectImages.size() > 0) {
            isGo = true;
            Media media = selectImages.get(0);
            // 是否显示拍照按钮 显示 true 反之 false
            notifyItemChanged(true ? media.position :
                    isGo ? media.position : media.position > 0 ? media.position - 1 : 0);
            selectImages.clear();
        }
    }

    /**
     * 更新选择的顺序
     */
    private void subSelectPosition() {
        if (is_checked_num) {
            int size = selectImages.size();
            for (int index = 0, length = size; index < length; index++) {
                Media media = selectImages.get(index);
                media.setNum(index + 1);
                notifyItemChanged(media.position);
            }
        }
    }

    /**
     * 选中的图片并执行动画
     *
     * @param holder
     * @param isChecked
     * @param isAnim
     */
    public void selectImage(ViewHolder holder, boolean isChecked, boolean isAnim) {
        holder.check.setSelected(isChecked);
        if (isChecked) {
            if (isAnim) {
                if (animation != null) {
                    holder.check.startAnimation(animation);
                }
            }
            holder.iv_picture.setColorFilter(ContextCompat.getColor
                    (context, R.color.alb_image_overlay_selected), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.iv_picture.setColorFilter(ContextCompat.getColor
                    (context, R.color.alb_image_overlay), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public interface OnSelectChangedListener {

        /**
         * 已选Media回调
         *
         * @param selectImages
         */
        void onChange(List<Media> selectImages);

        /**
         * 图片预览回调
         *
         * @param media
         * @param position
         */
        void onPictureClick(Media media, int position);
    }

    public void setOnSelectChangedListener(OnSelectChangedListener
                                                   imageSelectChangedListener) {
        mOnSelectChangedListener = imageSelectChangedListener;
    }

    private void zoom(ImageView iv_img) {
        if (zoomAnim) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(iv_img, "scaleX", 1f, 1.12f),
                    ObjectAnimator.ofFloat(iv_img, "scaleY", 1f, 1.12f)
            );
            set.setDuration(DURATION);
            set.start();
        }
    }

    private void disZoom(ImageView iv_img) {
        if (zoomAnim) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(iv_img, "scaleX", 1.12f, 1f),
                    ObjectAnimator.ofFloat(iv_img, "scaleY", 1.12f, 1f)
            );
            set.setDuration(DURATION);
            set.start();
        }
    }
}
