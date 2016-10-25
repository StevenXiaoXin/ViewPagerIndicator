package com.example.liuzhuang.viewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 *
 * 自定义的导航栏
 * Created by liuzhuang on 2016/10/20.
 */

public class ViewPagerIndicator extends LinearLayout {

    //画笔
    private Paint mPaint;
    //闭合图形
    private Path mPath;
    //三角形宽度
    private int mTriangleWidth;
    //三角形高度
    private int mTriangleHight;
    //三角形和tab的宽度比例
    private static final float RADIO_TRIANGLE_WIDTH = 1 / 9f;
    //三角形初始位置
    private int mInitTranslationX;
    //三角形移动位置
    private int mTranslationX;

    private int mTabVisiableCount;
    private static final int COUNT_DEFAULT_TAB = 4;
    private List<String> mTitles;
    private static final int COLOE_TEXT_NORMAL = 0x77FFFFFF;
    private static final int COLOE_TEXT_HIGHTLINGHT = 0xFFFFFFFF;

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        //获取可见tab的数量
        TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);

        mTabVisiableCount = a.getInt(R.styleable.ViewPagerIndicator_visiable_tab_count, COUNT_DEFAULT_TAB);
        if (mTabVisiableCount < 0) {
            mTabVisiableCount = COUNT_DEFAULT_TAB;
        }
        a.recycle();

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#ffffffff"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(3));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mInitTranslationX+mTranslationX,getHeight()+2);
        canvas.drawPath(mPath, mPaint);

        canvas.restore();

        super.dispatchDraw(canvas);
    }

    /**
     * 一般可以初始化view的大小
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mTriangleWidth = (int) (w / mTabVisiableCount * RADIO_TRIANGLE_WIDTH);
        mInitTranslationX = w / mTabVisiableCount / 2 - mTriangleWidth / 2;
        mTriangleHight = 2*mTriangleWidth /3;

        initTriangle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int cCount = getChildCount();
        if (cCount == 0) {
            return;
        } else {
            for (int i=0;i<cCount;i++) {
                View view = getChildAt(i);
                LinearLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
                lp.weight = 0;
                lp.width = getScreenWidth() / mTabVisiableCount;
                view.setLayoutParams(lp);
            }
            setItemClickEvent();

        }

    }

    /**
     * 获取屏幕宽度
     * @return
     */
    private int getScreenWidth() {

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 初始化三角形
     */
    private void initTriangle() {

        mPath = new Path();
        mPath.moveTo(0, 0);
        mPath.lineTo(mTriangleWidth,0);
        mPath.lineTo(mTriangleWidth/2,-mTriangleHight);
        mPath.close();
    }

    /**
     * 指示器跟随手指进行滚动
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {
        int tabWidth = getWidth() / mTabVisiableCount;
        mTranslationX = (int) (tabWidth * (offset + position));
        //容器移动，在tab处于移动至最后一个时
        if (position >= (mTabVisiableCount - 2) && offset > 0 && getChildCount() > mTabVisiableCount) {

            if (mTabVisiableCount != 1) {
                this.scrollTo(
                        (position - (mTabVisiableCount - 2)) * tabWidth + (int) (tabWidth * offset), 0);
            } else {
                this.scrollTo(position * tabWidth + (int) (tabWidth * offset), 0);
            }
        }
        invalidate();

    }

    public void setTabItemTitles(List<String> titles) {

        if (titles != null && titles.size() > 0) {
            this.removeAllViews();
            mTitles = titles;
            for (String title : mTitles) {
                addView(geterateTextView(title));
            }
            setItemClickEvent();
        }
    }

    /**
     * 设置可见tab数量
     * @param count
     */
    public void setVisiableCount(int count) {
        mTabVisiableCount = count;
    }

    /**
     * 根据title创建tab
     * @param title
     * @return
     */
    private View geterateTextView(String title) {
        TextView tv = new TextView(getContext());
        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.width = getScreenWidth() / mTabVisiableCount;
        tv.setText(title);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        tv.setTextColor(COLOE_TEXT_NORMAL);
        tv.setLayoutParams(lp);
        return tv;
    }

    private ViewPager mViewPager;

    public interface PageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    //接口回调
    public PageChangeListener mListener;
    public void setOnPageChangeListener(PageChangeListener listener) {

        this.mListener = listener;
    }

    /**
     * 设置关联的ViewPager
     * @param viewPager
     * @param pos
     */
    public void setViewPager(ViewPager viewPager,int pos) {
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //偏移量是tabWidth*positionOffset+position*tabWidth
                scroll(position, positionOffset);
                //每个方法的回调
                if (mListener != null) {
                    mListener.onPageScrolled(position,positionOffset,positionOffsetPixels);

                }
            }
            @Override
            public void onPageSelected(int position) {
                if (mListener != null) {
                    mListener.onPageSelected(position);

                }
                hightLightText(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mListener != null) {
                    mListener.onPageScrollStateChanged(state);

                }
            }
        });
        mViewPager.setCurrentItem(pos);
        hightLightText(pos);
    }

    /**
     * 重置文本颜色
     */
    private void resetTextColor() {
        for (int i=0;i<getChildCount();i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(COLOE_TEXT_NORMAL);

            }
        }
    }

    /**
     * 高亮某个tab的文本
     * @param pos
     */
    private void hightLightText(int pos) {
        resetTextColor();
        View view = getChildAt(pos);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(COLOE_TEXT_HIGHTLINGHT);

        }
    }

    /**
     * 设置tab的点击事件
     */
    private void setItemClickEvent() {
        int cCount = getChildCount();
        for (int i=0;i<cCount;i++) {
            final int j=i;
            final View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }

}
