package dev.dect.kapture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class KStaticViewPager extends ViewPager {

    public KStaticViewPager(final Context context) {
        super(context);
    }

    public KStaticViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int mode = MeasureSpec.getMode(heightMeasureSpec);

        if(mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            int highestChildHeight = 0;

            for(int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);

                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                if(child.getMeasuredHeight() > highestChildHeight) {
                    highestChildHeight = child.getMeasuredHeight();
                };
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(highestChildHeight, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final int childrenCount = getChildCount();

        setOffscreenPageLimit(childrenCount - 1);

        setAdapter(new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
                return container.getChildAt(position);
            }

            @Override
            public boolean isViewFromObject(@NonNull final View arg0, @NonNull final Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return childrenCount;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {}
        });
    }
}