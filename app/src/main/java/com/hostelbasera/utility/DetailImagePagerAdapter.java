package com.hostelbasera.utility;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hostelbasera.R;
import com.hostelbasera.main.FullScreenImageActivity;

import java.util.ArrayList;

public class DetailImagePagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private long mLastClickTime = 0;
    private ArrayList<String> arrSliderImages;

    public DetailImagePagerAdapter(Context context, ArrayList<String> arrSliderImages) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.arrSliderImages = arrSliderImages;
    }

    @Override
    public int getCount() {
        return arrSliderImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.image_slider_item, container, false);
        ImageView imageView = itemView.findViewById(R.id.imageView);
        ImageView imgPlaceHolder = itemView.findViewById(R.id.img_place_holder);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                mContext.startActivity(new Intent(mContext, FullScreenImageActivity.class)
                        .putExtra(Constant.ArrProductImages, arrSliderImages)
                        .putExtra(Constant.Position, position));
            }
        });

//        imageView.setImageResource(mResources[position]);

       /* Glide.with(mContext)
                .load(mContext.getString(R.string.slider_server_url) + arrSliderImages.get(position).slide_image)
                .apply(new RequestOptions()
//                        .fitCenter()
//                        .centerCrop()
//                        .placeholder(R.mipmap.ic_launcher)
                        .dontAnimate()
                        .priority(Priority.HIGH))
                .into(imageView);*/

        imgPlaceHolder.setVisibility(View.VISIBLE);

        Glide.with(mContext)
                .load(mContext.getString(R.string.image_url) + arrSliderImages.get(position)).apply(new RequestOptions().dontAnimate())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        imgPlaceHolder.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}

