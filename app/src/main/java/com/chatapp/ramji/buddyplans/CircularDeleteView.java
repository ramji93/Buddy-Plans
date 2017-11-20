package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by user on 04-05-2017.
 */

public class CircularDeleteView extends View {

    Paint Fillpaint;



    public CircularDeleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void init()

    {
        Fillpaint = new Paint();
        Fillpaint.setAntiAlias(true);
        Fillpaint.setColor(Color.parseColor("#6e42f4"));

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);



       setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int radius = getMeasuredWidth()/2;
        final int center = getMeasuredWidth()/2;
        canvas.drawCircle(center,center,radius,Fillpaint);



    }
}
