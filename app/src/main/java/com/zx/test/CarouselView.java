package com.zx.test;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhaoxin on 14/12/18.
 * QQ:343986392
 * https://github.com/zhaoxin1943
 */
public class CarouselView extends HorizontalScrollView {
    private static final String TAG = "CarouselView";
    /**
     * 默认初始位置
     */
    private static final int DEFAULT_SELECTED = 0;
    /**
     * 偏移量，表示第一个元素离开初始位置的距离
     */
    private int offset = DEFAULT_SELECTED;
    private static final int ITEM_MARGIN = 40;
    private static final int DURATION = 3000;
    private static final float TO_SCALE = 1.2f;
    public ItemScrollCallBack itemScrollCallBack;
    private List<String> pics = new ArrayList<String>();
    private LinearLayout carouse_ll;
    private int SIZE = 5;
    private int[] ids = {R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e};
    /**
     * 第一个元素的MarginLeft
     */
    private int firstMarginLeft = 0;
    /**
     * 屏幕横向中点的x值
     */
    private int halfPositionX;
    private boolean isAutoScroll = true;
    private int width;
    private int itemWidth, halfItemWidth;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == 1) {
                if (isAutoScroll) {
                    offset++;
                    scrollImage(offset);

                    if (offset < SIZE) {
                        Message message = Message.obtain();
                        message.what = 1;
                        mHandler.sendMessageDelayed(message, DURATION);
                    } else {
                        isAutoScroll = false;
                    }
                }
            } else if (what == 2) {
                offset = (getScrollX()) / itemWidth;
                scrollImage(offset);
            }

        }
    };

    public CarouselView(Context context) {
        super(context);
        initView();

    }

    public CarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CarouselView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.carouse_layout, this);
        setHorizontalScrollBarEnabled(false);
        Message message = Message.obtain();
        message.what = 1;
        mHandler.sendMessageDelayed(message, DURATION);
        if (itemScrollCallBack != null) {
            itemScrollCallBack.onItemSelected(carouse_ll.getChildAt(DEFAULT_SELECTED), DEFAULT_SELECTED);
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 70, dm);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        carouse_ll = (LinearLayout) findViewById(R.id.carouse_ll);
        addPics();
    }

    private void addPics() {


        for (int i = 0; i < SIZE; i++) {
            addPic(i, ids[i], false);
        }
    }

    public void addPic(int i, int resId, boolean isNeedScroll) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
        lp.leftMargin = ITEM_MARGIN;
        CircleImageView imageView = new CircleImageView(getContext());
        imageView.setBorderColor(Color.parseColor("#FF000000"));

        imageView.setBorderWidth(2);
        imageView.setImageResource(resId);
        final int fi = i;
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isAutoScroll = false;
                if (fi == offset) {
                    //todo 点击中心点的回调
                    Log.i("zhaoxin", "center click");
                } else {
                    moveToCenter(fi);
                }
            }
        });

        if (isNeedScroll) {
            LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) carouse_ll.getChildAt(carouse_ll.getChildCount() - 1).getLayoutParams();
            lp2.rightMargin = 0;
            lp.rightMargin = firstMarginLeft;
        }
        carouse_ll.addView(imageView, lp);

        if (isNeedScroll) {
            SIZE++;
            //smoothScrollTo((itemWidth + ITEM_MARGIN) * (SIZE + 1), 0);
            moveToCenter(SIZE);
        }
    }

    /**
     * 根据点击图片的位置来判断要滑动的距离，实现点击的图片滑到中心的效果
     *
     * @param fi
     */
    private void moveToCenter(int fi) {
        offset = fi;
        scrollImage(offset);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) carouse_ll.getChildAt(0).getLayoutParams();
        lp.leftMargin = firstMarginLeft;
        ViewHelper.setScaleX(carouse_ll.getChildAt(0), TO_SCALE);
        ViewHelper.setScaleY(carouse_ll.getChildAt(0), TO_SCALE);

        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) carouse_ll.getChildAt(SIZE - 1).getLayoutParams();
        lp2.rightMargin = firstMarginLeft;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        itemWidth = carouse_ll.getChildAt(0).getMeasuredWidth();
        halfItemWidth = itemWidth / 2;
        firstMarginLeft = (getMeasuredWidth() - itemWidth) / 2;
        halfPositionX = getResources().getDisplayMetrics().widthPixels / 2;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        for (int i = 0; i < SIZE; i++) {
            View view = carouse_ll.getChildAt(i);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            if ((halfPositionX - (location[0] + halfItemWidth) < itemWidth + ITEM_MARGIN)
                    || (location[0] + halfItemWidth - halfPositionX < itemWidth + ITEM_MARGIN)) {
                float scale = TO_SCALE - ((float) Math.abs(location[0] + halfItemWidth - halfPositionX) / (itemWidth + ITEM_MARGIN) / 10) * 2;
                ViewHelper.setScaleX(view, scale);
                ViewHelper.setScaleY(view, scale);

            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        isAutoScroll = false;
        if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {

            Message msg = Message.obtain();
            msg.what = 2;
            mHandler.sendMessageDelayed(msg, 100);
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 滚动到某个位置
     *
     * @param offset
     */
    public void scrollImage(int offset) {
        smoothScrollTo((itemWidth + ITEM_MARGIN) * (offset), 0);
        View itemView;
        if (offset > 0) {
            itemView = carouse_ll.getChildAt(offset - 1);
        } else {
            itemView = carouse_ll.getChildAt(0);
        }
        Log.d(TAG, " offset : " + offset);
        Log.d(TAG, " view : " + itemView);
        //TODO 回调
        if (itemScrollCallBack != null) {
            itemScrollCallBack.onItemSelected(itemView, offset);
        }
    }

    public void setCallBack(ItemScrollCallBack callBack) {
        this.itemScrollCallBack = callBack;
    }

    public void setSelection(int index) {
        offset = index;
        scrollImage(offset);
    }

    public void AutoScroll() {
        isAutoScroll = true;
        Message message = Message.obtain();
        message.what = 1;
        mHandler.sendMessageDelayed(message, DURATION);
    }

    public void StopScroll() {
        isAutoScroll = false;
        Message message = Message.obtain();
        message.what = 2;
        mHandler.sendMessageDelayed(message, DURATION);
    }

    public int getCount() {
        return pics.size();
    }

    public interface ItemScrollCallBack {
        void onItemSelected(View view, int position);
    }
}