package com.example.administrato.rippleview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * 测试Scroller的使用
 * Created by 李建涛 on 2016/11/9.
 */

public class XRelativeLayout extends FrameLayout {

    private Context mContext;
    private Scroller mScroller;
    private int mLastX;
    private int mLastY;

    public XRelativeLayout(Context context) {
        this(context,null);
    }

    public XRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public XRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init();
    }
    /**初始化**/
    private void init() {
        mScroller = new Scroller(mContext);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action=event.getAction();
        Log.i("TAG action 的二进制 ",Integer.toBinaryString(action));
        Log.i("TAG actionMasked 的二进制 ",Integer.toBinaryString(event.getActionMasked()));
        int x= (int) event.getRawX();
        int y= (int) event.getRawY();
        switch (action&event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                mLastX=x;
                mLastY=y;
                return true;
            case MotionEvent.ACTION_MOVE:
                int disX=x-mLastX;
                int disY=y-mLastY;
                scrollBy(-disX,-disY);
                mLastX=x;
                mLastY=y;
                return true;
            case MotionEvent.ACTION_UP:
                mScroller.startScroll(getScrollX(),getScrollY(),-getScrollX(),-getScrollY(),1000);
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
            if (mScroller.computeScrollOffset()){
                scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
                invalidate();
            }
    }
}
