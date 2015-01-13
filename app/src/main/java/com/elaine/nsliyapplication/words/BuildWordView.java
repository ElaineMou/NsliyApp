package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.ViewActivity;

/**
 * A view that displays the word as it is constructed by the user.
 * Created by Elaine on 1/8/2015.
 */
public class BuildWordView extends FrameLayout {

    private float scale;

    public BuildWordView(Context context) {
        super(context);
        initView(context);
    }

    public BuildWordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BuildWordView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        initView(context);
    }

    private void initView(Context context) {
        scale = context.getResources().getDisplayMetrics().density;

        View view = inflate(getContext(), R.layout.view_build_word, null);
        addView(view);
    }

    public void addThumbnail(Drawable drawable){
        Context context = getContext();
        FrameLayout imageFrame = new FrameLayout(context);
        int frameSize = (int) (ViewActivity.VIEW_IMAGE_SIZE*scale);
        imageFrame.setLayoutParams(new GridView.LayoutParams(frameSize, frameSize));
        imageFrame.setForegroundGravity(Gravity.CENTER);
        imageFrame.setBackgroundColor(Color.LTGRAY);

        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(drawable);
        imageFrame.addView(imageView);

        ((LinearLayout)findViewById(R.id.thumbnail_series)).addView(imageFrame);
    }
}
