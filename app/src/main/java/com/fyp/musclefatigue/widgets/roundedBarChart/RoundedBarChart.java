package com.fyp.musclefatigue.widgets.roundedBarChart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.fyp.musclefatigue.R;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.Range;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;


/**
 * Created by M.Sufwan Ansari on 1/27/2022.
 * Copyright (c) 2022 MTPixels. All rights reserved.
 */

public class RoundedBarChart extends BarChart {
    public RoundedBarChart(Context context) {
        super(context);
    }

    public RoundedBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        readRadiusAttr(context, attrs);
    }

    public RoundedBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readRadiusAttr(context, attrs);
    }

    private void readRadiusAttr(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundedBarChart, 0, 0);
        try {
            setRadius(a.getDimensionPixelSize(R.styleable.RoundedBarChart_barRadius, 0));
        } finally {
            a.recycle();
        }
    }

    public void setRadius(int radius) {
        setRenderer(new RoundedBarChartRenderer(this, getAnimator(), getViewPortHandler(), radius));
    }

    private class RoundedBarChartRenderer extends BarChartRenderer {
        private int mRadius;
        private RectF mBarShadowRectBuffer = new RectF();

        RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int mRadius) {
            super(chart, animator, viewPortHandler);
            this.mRadius = mRadius;
        }

        @Override
        public void drawHighlighted(Canvas c, Highlight[] indices) {
            BarData barData = mChart.getBarData();

            for (Highlight high : indices) {

                IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());

                if (set == null || !set.isHighlightEnabled())
                    continue;

                BarEntry e = set.getEntryForXValue(high.getX(), high.getY());

                if (!isInBoundsX(e, set))
                    continue;

                Transformer trans = mChart.getTransformer(set.getAxisDependency());

                mHighlightPaint.setColor(set.getHighLightColor());
                mHighlightPaint.setAlpha(set.getHighLightAlpha());

                boolean isStack = high.getStackIndex() >= 0 && e.isStacked();

                final float y1;
                final float y2;

                if (isStack) {

                    if (mChart.isHighlightFullBarEnabled()) {

                        y1 = e.getPositiveSum();
                        y2 = -e.getNegativeSum();

                    } else {

                        Range range = e.getRanges()[high.getStackIndex()];

                        y1 = range.from;
                        y2 = range.to;
                    }

                } else {
                    y1 = e.getY();
                    y2 = 0.f;
                }

                prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2f, trans);

                setHighlightDrawPos(high, mBarRect);

                Path path2 = roundRect(mBarRect, mRadius, mRadius);
                c.drawPath(path2, mRenderPaint);

//                c.drawRoundRect(mBarRect, mRadius, mRadius, mHighlightPaint);
            }
        }

        @Override
        protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mBarBorderPaint.setColor(dataSet.getBarBorderColor());
            mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

            final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();

            // draw the bar shadow before the values
            if (mChart.isDrawBarShadowEnabled()) {
                mShadowPaint.setColor(dataSet.getBarShadowColor());

                BarData barData = mChart.getBarData();

                final float barWidth = barData.getBarWidth();
                final float barWidthHalf = barWidth / 2.0f;
                float x;

                for (int i = 0, count = Math.min((int) (Math.ceil((float) (dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
                     i < count;
                     i++) {

                    BarEntry e = dataSet.getEntryForIndex(i);

                    x = e.getX();

                    mBarShadowRectBuffer.left = x - barWidthHalf;
                    mBarShadowRectBuffer.right = x + barWidthHalf;

                    trans.rectValueToPixel(mBarShadowRectBuffer);

                    if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right))
                        continue;

                    if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                        break;

                    mBarShadowRectBuffer.top = mViewPortHandler.contentTop();
                    mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom();

                    c.drawRoundRect(mBarShadowRectBuffer, mRadius, mRadius, mShadowPaint);
                }
            }

            // initialize the buffer
            BarBuffer buffer = mBarBuffers[index];
            buffer.setPhases(phaseX, phaseY);
            buffer.setDataSet(index);
            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
            buffer.setBarWidth(mChart.getBarData().getBarWidth());

            buffer.feed(dataSet);

            trans.pointValuesToPixel(buffer.buffer);

            final boolean isSingleColor = dataSet.getColors().size() == 1;

            if (isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor());
            }

            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (!isSingleColor) {
                    // Set the color for the currently drawn value. If the index
                    // is out of bounds, reuse colors.
                    mRenderPaint.setColor(dataSet.getColor(j / 4));
                }

                if (dataSet.getGradientColor() != null) {
                    GradientColor gradientColor = dataSet.getGradientColor();
                    mRenderPaint.setShader(
                            new LinearGradient(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 3],
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    gradientColor.getStartColor(),
                                    gradientColor.getEndColor(),
                                    android.graphics.Shader.TileMode.MIRROR));
                }

                if (dataSet.getGradientColors() != null) {
                    mRenderPaint.setShader(
                            new LinearGradient(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 3],
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    dataSet.getGradientColor(j / 4).getStartColor(),
                                    dataSet.getGradientColor(j / 4).getEndColor(),
                                    android.graphics.Shader.TileMode.MIRROR));
                }

                Path path2 = roundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3]), mRadius, mRadius);
                c.drawPath(path2, mRenderPaint);

//                c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
//                        buffer.buffer[j + 3], mRadius, mRadius, mRenderPaint);

                if (drawBorder) {

                    Path path = roundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3]), mRadius, mRadius);
                    c.drawPath(path, mBarBorderPaint);

//                    c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
//                            buffer.buffer[j + 3], mRadius, mRadius, mBarBorderPaint);
                }
            }
        }
    }

    @NonNull
    private Path roundRect(@NonNull RectF rect, float rx, float ry) {
        float top = rect.top;
        float left = rect.left;
        float right = rect.right;
        float bottom = rect.bottom;
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;

        rx = width / 2;
        ry = width * 0.70f;

        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);

        path.rLineTo(0, ry);
        path.rLineTo(rx, 0);

        path.rLineTo(widthMinusCorners, 0);
        path.rLineTo(rx, 0);
        path.rLineTo(0, -ry);

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }
}
