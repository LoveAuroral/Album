package auroral.widget.view.album.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.auroral.album.R;
import auroral.widget.view.album.entity.Media;
import auroral.widget.view.album.entity.MediaFolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private Context mContext;
    private List<MediaFolder> mFolders = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public FolderAdapter(Context context) {
        super();
        this.mContext = context;
    }

    public void bindFolderData(List<MediaFolder> folders) {
        this.mFolders = folders;
        notifyDataSetChanged();
    }

    public List<MediaFolder> getFolderData() {
        if (mFolders == null) {
            mFolders = new ArrayList<>();
        }
        return mFolders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.alb_item_dialog_folder, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final MediaFolder folder = mFolders.get(position);
        String name = folder.getName();
        int imageNum = folder.getImageNum();
        String imagePath = folder.getFirstImagePath();
        boolean isChecked = folder.isChecked();
        int checkedNum = folder.getCheckedNum();
        holder.tv_sign.setVisibility(checkedNum > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setSelected(isChecked);
        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.alb_default_icon)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(160, 160);
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(imagePath)
                .apply(options)
                .into(new BitmapImageViewTarget(holder.iv_first_image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.
                                        create(mContext.getResources(), resource);
                        circularBitmapDrawable.setCornerRadius(8);
                        holder.iv_first_image.setImageDrawable(circularBitmapDrawable);
                    }
                });

        holder.tv_image_num.setText("(" + imageNum + ")");
        holder.tv_folder_name.setText(name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    for (MediaFolder mediaFolder : mFolders) {
                        mediaFolder.setChecked(false);
                    }
                    folder.setChecked(true);
                    notifyDataSetChanged();
                    onItemClickListener.onItemClick(folder.getName(), folder.getImages());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFolders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_first_image;
        TextView tv_folder_name, tv_image_num, tv_sign;

        ViewHolder(View itemView) {
            super(itemView);
            iv_first_image = itemView.findViewById(R.id.iv_first_image);
            tv_folder_name = itemView.findViewById(R.id.tv_folder_name);
            tv_image_num = itemView.findViewById(R.id.tv_image_num);
            tv_sign = itemView.findViewById(R.id.tv_sign);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(String folderName, List<Media> images);
    }
}
