package com.zx.test;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by Abner on 15/1/23.
 * QQ 230877476
 * Email nimengbo@gmail.com
 */
public class MyScroller extends Scroller {
    public MyScroller(Context context) {
        super(context);
    }

    public MyScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public MyScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }


    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        super.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        if (mScrollLinstener != null) {
            mScrollLinstener.OnFinished(getFinalX());
        }
    }

    public interface ScrollLinstener {
        void OnFinished(int finalX);
    }

    private ScrollLinstener mScrollLinstener;

    public void setmScrollLinstener(ScrollLinstener mScrollLinstener) {
        this.mScrollLinstener = mScrollLinstener;
    }
}
