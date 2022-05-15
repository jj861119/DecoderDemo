package org.tensorflow.lite.examples.classification.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.tensorflow.lite.examples.classification.R;

/**
 * TODO: document your custom view class.
 */
public class MyView extends View {
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private int ratioWidth;
    private int ratioHeight;

    public MyView(Context context) {
        super(context);
        init(null, 0);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MyView, defStyle, 0);

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.MyView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.MyView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.MyView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(0xFFFF6600);
        paint.setColor(0xF11F6600);
        paint.setStyle(Paint.Style.STROKE);
        Log.v("myView", "myView");
        Log.v("ratioWidth", String.valueOf(ratioWidth));
        Log.v("ratioHeight", String.valueOf(ratioHeight));

        Log.v("this.getWidth()", String.valueOf(this.getWidth()));
        Log.v("this.getHeight()", String.valueOf(this.getHeight()));
        if(ratioWidth!=0 && ratioHeight!=0)
        {
            int textureW, textureH;
            if (this.getWidth() < this.getHeight() * ratioWidth / ratioHeight) {
                Log.v("tag1", "tag1");
                textureW = this.getWidth();
                textureH=this.getWidth() * ratioHeight / ratioWidth;
            } else {
                textureW = this.getHeight() * ratioWidth / ratioHeight;
                textureH=this.getHeight();
            }
            Log.v("tag1textureW", String.valueOf(textureW));
            Log.v("tag1textureH", String.valueOf(textureH));


            Float centreX=this.getX() + textureW  / 2;
            Float centreY=this.getY() + textureH / 2;

            int boxWidth = textureW / 4;
            int boxHeight = boxWidth*3/2;
//        Log.v("myView X", String.valueOf(this.getX()));
//        Log.v("myView Y", String.valueOf(this.getY()));
//            Log.v("textureratioWidth", String.valueOf(ratioWidth));
//            Log.v("textureratioHeight", String.valueOf(ratioHeight));
//            Log.v("textureW", String.valueOf(textureW));
//            Log.v("textureH", String.valueOf(textureH));
            Log.v("cenX", String.valueOf(centreX));
            Log.v("cenY", String.valueOf(centreY));
            Log.v("myView L", String.valueOf(centreX-boxWidth));
            Log.v("myView T", String.valueOf(centreY-boxHeight));
            Log.v("myView R", String.valueOf(centreX+boxWidth));
            Log.v("myView B", String.valueOf(centreY+boxHeight));

            canvas.drawRect(centreX-(int)(boxWidth*1.05),centreY-(int)(boxHeight*1.05),centreX+(int)(boxWidth*1.05),centreY+(int)(boxHeight*1.05),paint);
            canvas.drawCircle(centreX,centreY,2,paint);

            //canvas.drawRect(0,0,textureW,textureH,paint);
        }
    }

    public void setAspectRatio(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        ratioWidth = height;
        ratioHeight = width;


        requestLayout();
    }

//    @Override
//    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        final int width = MeasureSpec.getSize(widthMeasureSpec);
//        final int height = MeasureSpec.getSize(heightMeasureSpec);
//        Log.v("onMeasure width", String.valueOf(width));
//        Log.v("onMeasure height", String.valueOf(height));
//        if (0 == ratioWidth || 0 == ratioHeight) {
//            setMeasuredDimension(width, height);
//        } else {
//            if (width < height * ratioWidth / ratioHeight) {
//                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
//            } else {
//                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
//            }
//        }
//    }





    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view"s example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view"s example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}