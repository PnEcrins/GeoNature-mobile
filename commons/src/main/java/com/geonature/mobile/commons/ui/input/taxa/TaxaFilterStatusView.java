package com.geonature.mobile.commons.ui.input.taxa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.geonature.mobile.commons.R;

/**
 * Custom {@code View} displaying the current status filter for taxa.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaFilterStatusView extends View {

    private final Paint mBgPaints = new Paint();
    private final Paint mLinePaints = new Paint();
    private int mWidth;
    private int mHeight;

    private boolean mIsToSearchSelected = false;
    private boolean mIsNewSelected = false;
    private boolean mIsOptionalSelected = false;

    public TaxaFilterStatusView(Context context) {
        this(context, null);
    }

    public TaxaFilterStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.actionButtonStyle);
    }

    public TaxaFilterStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        mBgPaints.setAntiAlias(true);
        mBgPaints.setStyle(Paint.Style.FILL);
        mBgPaints.setColor(0xff000000);

        mLinePaints.setAntiAlias(true);
        mLinePaints.setStyle(Paint.Style.STROKE);
        mLinePaints.setColor(0xff000000);
        mLinePaints.setStrokeWidth(0.5f);

        RectF mOvals = new RectF(2, 2, mWidth - 2, mHeight - 2);

        mBgPaints.setColor(getContext().getResources().getColor(R.color.taxon_status_color_search));

        if (mIsToSearchSelected) {
            mBgPaints.setAlpha(255);
        }
        else {
            mBgPaints.setAlpha(100);
        }

        canvas.drawArc(mOvals, 150, 120, true, mBgPaints);
        canvas.drawArc(mOvals, 150, 120, true, mLinePaints);

        mBgPaints.setColor(getContext().getResources().getColor(R.color.taxon_status_color_new));

        if (mIsNewSelected) {
            mBgPaints.setAlpha(255);
        }
        else {
            mBgPaints.setAlpha(100);
        }

        canvas.drawArc(mOvals, 270, 120, true, mBgPaints);
        canvas.drawArc(mOvals, 270, 120, true, mLinePaints);

        mBgPaints.setColor(getContext().getResources().getColor(R.color.taxon_status_color_optional));

        if (mIsOptionalSelected) {
            mBgPaints.setAlpha(255);
        }
        else {
            mBgPaints.setAlpha(100);
        }

        canvas.drawArc(mOvals, 30, 120, true, mBgPaints);
        canvas.drawArc(mOvals, 30, 120, true, mLinePaints);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.setMeasuredDimension(mWidth, mHeight);
    }

    public void setToSearchSelected(boolean isToSearchSelected) {
        this.mIsToSearchSelected = isToSearchSelected;
        invalidate();
    }

    public void setNewSelected(boolean isNewSelected) {
        this.mIsNewSelected = isNewSelected;
        invalidate();
    }

    public void setOptionalSelected(boolean isOptionalSelected) {
        this.mIsOptionalSelected = isOptionalSelected;
        invalidate();
    }
}
