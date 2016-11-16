package com.example.administrato.rippleview.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import com.example.administrato.rippleview.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * 点击后具有水波纹效果的RippleView
 * Created by 李建涛 on 2016/11/7.
 */

public class RippleView extends Button {

    private Context mContext;
    private int mRippleColor;
    private float mAlphaFactor;
    private float mDensity;
    private boolean mHovor = true;
    private boolean mIsAnimating = false;
    private Paint mPaint;
    private float mMaxRadius;
    private float mDownX;
    private float mDownY;
    private float mRadius;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        mRippleColor = a.getColor(R.styleable.RippleView_rippleColor, mRippleColor);
        mAlphaFactor = a.getFloat(R.styleable.RippleView_alphaFactor, mAlphaFactor);
        mHovor = a.getBoolean(R.styleable.RippleView_hover, mHovor);
        /**一定要加上此句*/
        a.recycle();
    }

    /**
     * 初始化
     */
    private void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(100);
        setRippleColor(Color.WHITE, 0.2f);
    }

    private Path mPath = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        mPath.reset();
        mPath.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);
        canvas.clipPath(mPath);
        canvas.restore();
        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
    }

    /**
     * 设置扩散的颜色 以及 透明度
     *
     * @param rippleColor
     * @param alphaFactor
     */
    public void setRippleColor(int rippleColor, float alphaFactor) {
        this.mRippleColor = rippleColor;
        this.mAlphaFactor = alphaFactor;
    }

    /**
     * 设置是否有点击之后的水波纹效果
     *
     * @param enabled
     */
    public void setHovor(boolean enabled) {
        this.mHovor = enabled;
    }

    /**
     * 直接设置dp值
     *
     * @param dp
     * @return
     */
    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = (float) Math.sqrt(w * w + h * h);
    }

    private boolean mAnimationIsCancel;
    private Rect mRect;
    private ObjectAnimator mRadiusAnimator;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TouchEvent", String.valueOf(event.getActionMasked()));
        Log.d("mIsAnimating", String.valueOf(mIsAnimating));
        Log.d("mAnimationIsCancel", String.valueOf(mAnimationIsCancel));
        boolean superResult = super.onTouchEvent(event);
        /**首先调用parentView的onTouch方法，同时执行自己和ParentView的onTouchEvent方法*/
        ViewParent view=getParent();
        if (view instanceof ScrollerLayout){
            ((ScrollerLayout) view).onTouchEvent(event);
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && this.isEnabled() && mHovor) {
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancel = false;
            mDownX = event.getX();
            mDownY = event.getY();
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", 0, dp(50)).setDuration(500);
            mRadiusAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleView.this, 1);
                    mIsAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }

        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHovor) {
            mDownX = event.getX();
            mDownY = event.getY();

            // Cancel the ripple animation when moved outside
            if (mAnimationIsCancel = !mRect.contains(
                    getLeft() + (int) event.getX(),
                    getTop() + (int) event.getY())) {
                setRadius(0);
            } else {
                setRadius(dp(50));
            }
            if (!superResult) {


                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancel && this.isEnabled()) {
            mDownX = event.getX();
            mDownY = event.getY();

            final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                    * mDownY);
            float targetRadius = Math.max(tempRadius, mMaxRadius);

            if (mIsAnimating) {
                mRadiusAnimator.cancel();
            }
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", dp(50),
                    targetRadius);
            mRadiusAnimator.setDuration(500);
            mRadiusAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleView.this, 1);
                    mIsAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        }

        return superResult;
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setRadius(float radius) {
        mRadius = radius;
        if (mRadius > 0) {
            RadialGradient mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius, adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor, Shader.TileMode.MIRROR);
            mPaint.setShader(mRadialGradient);
        }
        invalidate();
    }


}
