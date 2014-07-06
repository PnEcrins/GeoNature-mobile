package com.makina.ecrins.commons.ui.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This is a custom <code>ViewPager</code> implementation allowing disabling paging / swiping controls.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class EnablePagingViewPager extends ViewPager {
    private boolean mPagingEnabled;
    private boolean mPagingLeftEnabled;
    private boolean mPagingRightEnabled;
    private float mLastX = 0;

    public EnablePagingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mPagingEnabled = true;
        this.mPagingLeftEnabled = true;
        this.mPagingRightEnabled = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mPagingEnabled) {
            return super.onInterceptTouchEvent(ev);
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((mLastX > ev.getX()) && !this.mPagingLeftEnabled) {
                    return false;
                }

                if ((mLastX < ev.getX()) && !this.mPagingRightEnabled) {
                    return false;
                }

                mLastX = ev.getX();
                break;
        }

        if (this.mPagingEnabled) {
            return super.onTouchEvent(ev);
        }
        else {
            return false;
        }
    }

    public void setPagingEnabled(boolean pPagingEnabled) {
        this.mPagingEnabled = pPagingEnabled;

        if (this.mPagingEnabled) {
            this.mPagingLeftEnabled = true;
            this.mPagingRightEnabled = true;
        }
    }

    public void setPagingLeftEnabled(boolean pPagingLeftEnabled) {
        this.mPagingLeftEnabled = pPagingLeftEnabled;
    }

    public void setPagingRightEnabled(boolean pPagingRightEnabled) {
        this.mPagingLeftEnabled = pPagingRightEnabled;
    }
}
