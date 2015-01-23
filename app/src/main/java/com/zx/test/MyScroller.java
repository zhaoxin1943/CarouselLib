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
    public boolean computeScrollOffset() {
        boolean scrolling = super.computeScrollOffset();
        if (!scrolling) {
            if (mScrollLinstener != null) {
                mScrollLinstener.OnFinished();
            }
        }
        return scrolling;
    }

    public interface ScrollLinstener {
        void OnFinished();
    }

    private ScrollLinstener mScrollLinstener;

    public void setmScrollLinstener(ScrollLinstener mScrollLinstener) {
        this.mScrollLinstener = mScrollLinstener;
    }
}
