package com.auroral.album;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class PictureAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private ArrayList<String> mPictures;
    private RequestOptions mRequestOptions;
    private OnCameraListener mOnCameraListener;

    public PictureAdapter(Activity activity) {
        mActivity = activity;
        mRequestOptions = new RequestOptions()
                .placeholder(R.mipmap.album_icon)
                .error(R.mipmap.album_icon)
                .priority(Priority.HIGH)
                .transforms(new CenterCrop(), new RoundedCorners(DimensionUtils.getInstance(mActivity).dp2px(6)));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == 0) {
            return new CameraHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_picture, viewGroup, false));
        } else {
            return new PictureHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_picture, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof CameraHolder) {
            Glide.with(mActivity)
                    .load(R.mipmap.album_icon)
                    .apply(mRequestOptions)
                    .into(((CameraHolder) viewHolder).iv_picture);
            ((CameraHolder) viewHolder).iv_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnCameraListener != null) {
                        mOnCameraListener.camera();
                    }
                }
            });
        } else if (viewHolder instanceof PictureHolder) {
            Glide.with(mActivity)
                    .load(mPictures.get(i - 1))
                    .apply(mRequestOptions)
                    .into(((PictureHolder) viewHolder).iv_picture);
        }
    }

    @Override
    public int getItemCount() {
        return mPictures == null ? 1 : mPictures.size() + 1;
    }

    public void setPictures(ArrayList<String> pictures) {
        mPictures = pictures;
        notifyDataSetChanged();
    }

    public void setOnCameraListener(OnCameraListener cameraListener) {
        mOnCameraListener = cameraListener;
    }

    public interface OnCameraListener {
        void camera();
    }

    class CameraHolder extends RecyclerView.ViewHolder {

        private final SquareImageView iv_picture;

        CameraHolder(@NonNull View itemView) {
            super(itemView);
            iv_picture = (SquareImageView) itemView;
        }
    }

    class PictureHolder extends RecyclerView.ViewHolder {

        private final SquareImageView iv_picture;

        PictureHolder(@NonNull View itemView) {
            super(itemView);
            iv_picture = (SquareImageView) itemView;
        }
    }

}
