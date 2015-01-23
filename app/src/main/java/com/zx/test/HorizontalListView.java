package com.zx.test;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.nineoldandroids.view.ViewHelper;

import java.util.LinkedList;
import java.util.Queue;


public class HorizontalListView extends AdapterView<ListAdapter> {
    public boolean mAlwaysOverrideTouch = true;
    protected ListAdapter mAdapter;
    private int mLeftViewIndex = -1;
    private int mRightViewIndex = 0;
    protected int mCurrentX;
    protected int mNextX;
    private int mMaxX = Integer.MAX_VALUE;
    private int mDisplayOffset = 0;
    protected MyScroller mScroller;
    private GestureDetector mGesture;
    private Queue<View> mRemovedViewQueue = new LinkedList<>();
    private OnItemSelectedListener mOnItemSelected;
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private boolean mDataChanged = false;
    private int marginEdge = -1;
    private int halfItemWidth = -1;
    private int itemWidth = -1;
    private static final float TO_SCALE = 1.2f;
    /**
     * 每个item之间的距离，相当于dividetWidth
     */
    private static final int CHILD_MARGIN = 40;
    /**
     * 屏幕宽度的一半
     */
    private int halfScreenWidth;
    private static final int PADDING_TOP = 30;

    private boolean isAutoScroll = false;
    private static final int DURATION = 1000;
    private int offset = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == 1) {
                if (isAutoScroll) {
                    offset++;
                    setSelection(offset);
                    if (offset < mAdapter.getCount() - 1) {
                        Message message = Message.obtain();
                        message.what = 1;
                        mHandler.sendMessageDelayed(message, DURATION);
                    } else {
                        offset = -1; //方便下次从0开始
                        isAutoScroll = false;
                    }
                }
            }
        }
    };

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private synchronized void initView() {
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;
        mScroller = new MyScroller(getContext());
        mScroller.setmScrollLinstener(new MyScroller.ScrollLinstener() {
            @Override
            public void OnFinished(int finalX) {
                int offset = finalX / itemWidth;

                if (offset < 0) {
                    offset = 0;
                }
                if (offset > mAdapter.getCount() - 1) {
                    offset = mAdapter.getCount() - 1;
                }
                Log.d("OnFinished", "offset : " + offset);
                setSelection(offset);
            }
        });
        mGesture = new GestureDetector(getContext(), mOnGesture);
        halfScreenWidth = getResources().getDisplayMetrics().widthPixels >> 1;
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelected = listener;
    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClicked = listener;
    }

    @Override
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClicked = listener;
    }

    private DataSetObserver mDataObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            synchronized (HorizontalListView.this) {
                mDataChanged = true;
            }
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        }

    };

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View getSelectedView() {
        //TODO: implement
        return null;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataObserver);
        reset();
    }

    private synchronized void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    /**
     * 滑动到某个位置
     *
     * @param position
     */
    @Override
    public void setSelection(int position) {
        //TODO: implement
        if (position > mAdapter.getCount() - 1) {
            position = mAdapter.getCount() - 1;
        }
        int childWidth = getChildAt(0).getWidth();
        int positionX = position * childWidth;
        //所有item的总长度
        int maxWidth = mAdapter.getCount() * childWidth;
        if (positionX <= 0) {
            positionX = 0;
        }
        if (positionX > maxWidth) {
            positionX = maxWidth;
        }
        scrollTo(positionX);
        if (scrollCallBack != null) {
            scrollCallBack.onScrollStop(position);
        }
    }

    private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        //在layout的过程中添加View，如果需要在onLayout过程中添加很多View，这个方法是有用的
        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
    }


    /**
     * 滑动过程中会一直调用
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
            return;
        }

        if (mDataChanged) {
            int oldCurrentX = mCurrentX;
            initView();
            removeAllViewsInLayout();
            mNextX = oldCurrentX;
            mDataChanged = false;
        }

        if (mScroller.computeScrollOffset()) {
            int scrollx = mScroller.getCurrX();
            mNextX = scrollx;
        }

        if (mNextX <= 0) {
            mNextX = 0;
            mScroller.forceFinished(true);
        }
        if (mNextX >= mMaxX) {
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }

        //为负数时表示向右边滑动
        int dx = mCurrentX - mNextX;

        removeNonVisibleItems(dx);
        fillList(dx);
        positionItems(dx);

        mCurrentX = mNextX;

        if (!mScroller.isFinished()) {
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });

        }
        scaleChild();
    }

    //dx为负数时表示向右边滑动
    private void fillList(final int dx) {
        int edge = 0;
        View child = getChildAt(getChildCount() - 1);
        if (child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);

        edge = 0;
        child = getChildAt(0);
        if (child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);


    }

    //这里dx为正，表示向左滑动的距离，距离越大，要取出可复用的View越多
    private void fillListRight(int rightEdge, final int dx) {
        //表示向右滑动，右边没有View了
        while (rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {
            //这个时候需要从mRemovedViewQueue队列中取出View，在这里实现View的复用
            View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
            //If index is negative, it means put it at the end of the list.
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();

            if (mRightViewIndex == mAdapter.getCount() - 1) {
                mMaxX = mCurrentX + rightEdge - getWidth() + marginEdge;
            }

            if (mMaxX < 0) {
                mMaxX = 0;
            }
            mRightViewIndex++;
        }

    }

    //这里dx为负，表示向右滑动，需要从mRemovedViewQueue取出View填充在左边
    private void fillListLeft(int leftEdge, final int dx) {
        while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
            View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, 0);
            leftEdge -= child.getMeasuredWidth();
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
    }

    //滑出屏幕的View在这里remove
    //dx为负数时表示向右边滑动
    private void removeNonVisibleItems(final int dx) {
        View child = getChildAt(0);
        //如果是向右滑动，删除左边滑出屏幕的View
        while (child != null && child.getRight() + dx <= 0) {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);

        }

        child = getChildAt(getChildCount() - 1);
        //如果是向左滑动，删除右边滑出屏幕的View
        while (child != null && child.getLeft() + dx >= getWidth()) {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount() - 1);
        }
    }

    /**
     * 调用child的layout，布局各个子View
     *
     * @param dx
     */
    private void positionItems(final int dx) {
        if (getChildCount() > 0) {
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                if (i == 0) {
                    if (marginEdge < 0) {
                        marginEdge = halfScreenWidth - (childWidth >> 1);
                    }

                    left += marginEdge;
                    child.layout(left, PADDING_TOP, left + childWidth, child.getMeasuredHeight() + PADDING_TOP);
                } else {
                    child.layout(left, PADDING_TOP, left + childWidth, child.getMeasuredHeight() + PADDING_TOP);
                }

                left += (childWidth + child.getPaddingRight() + CHILD_MARGIN);
            }
        }
    }

    public synchronized void scrollTo(int x) {
        mScroller.startScroll(mNextX, 0, x - mNextX, 0, 500); //增加 duration
        requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        boolean gestureHandled = false;
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                gestureHandled = mGesture.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                gestureHandled = mGesture.onTouchEvent(ev);
                //todo 这里判断是否还在滑动
                Log.i("zhaoxin", "test");
                break;
        }
        handled |= gestureHandled;
        return handled;
    }

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                              float velocityY) {
        synchronized (HorizontalListView.this) {
            mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
            Log.d("Gesture", "onFling");
        }
        requestLayout();
        return true;
    }

    protected boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        if (isAutoScroll) {
            StopScroll(); //有触摸后就停止自动轮播
        }
        return true;
    }

    private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return HorizontalListView.this.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {

            synchronized (HorizontalListView.this) {
                if (mNextX + distanceX <= mMaxX) { //超出后回弹
                    mNextX += (int) distanceX;
                }
                //方便监听停止滚动
                HorizontalListView.this.onFling(e1, e2, 0, 0);
            }
            return true;
        }

        /**
         * 点击动作执行完毕时会调用
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemClicked != null) {
                        mOnItemClicked.onItemClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
//                    setSelection(mLeftViewIndex + 1 + i);
                    if (mOnItemSelected != null) {
                        mOnItemSelected.onItemSelected(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    Log.d("onSingleTapConfirmed ", i + " mLeftViewIndex + 1 + i :" + (mLeftViewIndex + 1 + i + ""));
                    break;
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemLongClicked != null) {
                        mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
        }

        /**
         * MotionEvent是否在View的范围内发生
         * @param e
         * @param child
         * @return
         */
        private boolean isEventWithinView(MotionEvent e, View child) {
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0];
            int right = left + child.getWidth();
            int top = childPosition[1];
            int bottom = top + child.getHeight();
            viewRect.set(left, top, right, bottom);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
    };

    /**
     * item的缩放
     */
    private void scaleChild() {
        int childCount = getChildCount();
        if (halfItemWidth < 0) {
            itemWidth = getChildAt(0).getWidth();
            halfItemWidth = itemWidth >> 1;
        }
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            if ((halfScreenWidth - (location[0] + halfItemWidth) < itemWidth + CHILD_MARGIN)
                    || (location[0] + halfItemWidth - halfScreenWidth < itemWidth + CHILD_MARGIN)) {
                float scale = TO_SCALE - ((float) Math.abs(location[0] + halfItemWidth - halfScreenWidth) / (itemWidth + CHILD_MARGIN) / 10) * 2;
                ViewHelper.setScaleX(view, scale);
                ViewHelper.setScaleY(view, scale);

            }
        }
    }

    public void AutoScroll() {
        isAutoScroll = true;
        Message message = Message.obtain();
        message.what = 1;
        mHandler.sendMessageDelayed(message, 0);
    }

    public void StopScroll() {
        isAutoScroll = false;
    }

    /**
     * 滚动结束后 滚到中间那个
     *
     * @param position
     */
    private synchronized void moveToCenter(int position) {
        setSelection(position);
    }

    public interface ItemScrollCallBack {
        /**
         * 滚动后 当前显示的item的postion
         *
         * @param position
         */
        void onScrollStop(int position);
    }

    private ItemScrollCallBack scrollCallBack;

    public void setScrollCallBack(ItemScrollCallBack scrollCallBack) {
        this.scrollCallBack = scrollCallBack;
    }
}
