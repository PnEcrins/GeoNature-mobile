package com.makina.ecrins.commons.ui.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This is a custom {@code ViewPager} implementation allowing disabling paging / swiping controls.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class EnablePagingViewPager extends ViewPager {
    private boolean mPagingEnabled;
    private boolean mPagingPreviousEnabled;
    private boolean mPagingNextEnabled;
    private float mLastX = 0;

    public EnablePagingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mPagingEnabled = true;
        this.mPagingPreviousEnabled = true;
        this.mPagingNextEnabled = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mPagingEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((mLastX > ev.getX()) && !this.mPagingPreviousEnabled) {
                    return false;
                }

                if ((mLastX < ev.getX()) && !this.mPagingNextEnabled) {
                    return false;
                }

                mLastX = ev.getX();
                break;
        }

        return this.mPagingEnabled && super.onTouchEvent(ev);
    }

    public void setPagingEnabled(boolean pPagingEnabled) {
        this.mPagingEnabled = pPagingEnabled;

        if (this.mPagingEnabled) {
            this.mPagingPreviousEnabled = true;
            this.mPagingNextEnabled = true;
        }
    }

    public void setPagingPreviousEnabled(boolean pPagingPreviousEnabled) {
        this.mPagingPreviousEnabled = pPagingPreviousEnabled;
    }

    public void setPagingNextEnabled(boolean pPagingNextEnabled) {
        this.mPagingPreviousEnabled = pPagingNextEnabled;
    }
}
