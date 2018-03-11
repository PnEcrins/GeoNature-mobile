package com.geonature.mobile.commons.ui.pager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.geonature.mobile.commons.R;

/**
 * Draws a line for each page.
 * The current page line is colored differently than the unselected page lines.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see <a href="https://github.com/JakeWharton/Android-ViewPagerIndicator">https://github.com/JakeWharton/Android-ViewPagerIndicator</a>
 */
public class UnderlinePageIndicator
        extends View
        implements IPagerIndicator {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ViewPager mViewPager;

    private int mCurrentPage;
    private float mPositionOffset;
    private int mScrollState;

    private int mSelectedColor;

    public UnderlinePageIndicator(Context context) {
        this(context,
             null);
    }

    public UnderlinePageIndicator(Context context,
                                  AttributeSet attrs) {
        this(context,
             attrs,
             R.attr.underlinePageIndicatorStyle);
    }

    public UnderlinePageIndicator(Context context,
                                  AttributeSet attrs,
                                  int defStyleAttr) {
        super(context,
              attrs,
              defStyleAttr);

        init(attrs,
             defStyleAttr);
    }

    @Override
    public void setViewPager(ViewPager viewPager) {
        if (mViewPager == viewPager) {
            return;
        }

        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
        }

        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);

        invalidate();
    }

    @Override
    public void setViewPager(ViewPager viewPager,
                             int initialPosition) {
        setViewPager(viewPager);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }

        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrolled(int position,
                               float positionOffset,
                               int positionOffsetPixels) {
        mCurrentPage = position;
        mPositionOffset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mPositionOffset = 0;
            invalidate();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;

        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }

        final int count = mViewPager.getAdapter() == null ? 0 : mViewPager.getAdapter()
                                                                          .getCount();
        if (count == 0) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        mPaint.setColor(mSelectedColor);

        final int paddingLeft = getPaddingLeft();
        final float pageWidth = (getWidth() - paddingLeft - getPaddingRight()) / (1f * count);
        final float left = paddingLeft + pageWidth * (mCurrentPage + mPositionOffset);
        final float right = left + pageWidth;
        final float top = getPaddingTop();
        final float bottom = getHeight() - getPaddingBottom();

        canvas.drawRect(left,
                        top,
                        right,
                        bottom,
                        mPaint);
    }

    private void init(AttributeSet attrs,
                      int defStyle) {
        if (isInEditMode()) {
            return;
        }

        // retrieve styles attributes
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                                                                          R.styleable.UnderlinePageIndicator,
                                                                          defStyle,
                                                                          0);

        mSelectedColor = typedArray.getColor(R.styleable.UnderlinePageIndicator_selectedColor,
                                             Color.BLUE);

        typedArray.recycle();
    }

    static class SavedState
            extends BaseSavedState {
        int currentPage;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest,
                                  int flags) {
            super.writeToParcel(dest,
                                flags);

            dest.writeInt(currentPage);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
