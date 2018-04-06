package com.incree.mpandroidchart.custom.barchartrenderer.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    protected BarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mChart = (BarChart) findViewById(R.id.chart1);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.getDescription().setEnabled(false);

        setData();

        mChart.setRenderer(new MyBarChartRenderer(mChart, mChart.getAnimator(), mChart.getViewPortHandler()));
    }

    private void setData() {

        float start = 1f;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < 12; i++) {
            float mult = (500 + 1);
            float val = (float) (Math.random() * mult);

            yVals1.add(new BarEntry(i, val));
        }

        BarDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "The year 2017");

            set1.setDrawIcons(false);

            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            mChart.setData(data);
        }
    }

    class MyBarChartRenderer extends BarChartRenderer {
        public MyBarChartRenderer(BarDataProvider chart, ChartAnimator animator,
                                  ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);

            initBuffers();
        }

        @Override
        public void drawData(Canvas c) {
            Drawable d = getResources().getDrawable(R.drawable.zzang);
            d.setBounds(0, 0, c.getWidth(), c.getHeight());
            d.draw(c);

            super.drawData(c);
        }

        private RectF mBarShadowRectBuffer = new RectF();

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

                for (int i = 0, count = Math.min((int)(Math.ceil((float)(dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
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

                    c.drawRect(mBarShadowRectBuffer, mShadowPaint);
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

                c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mRenderPaint);

                if (drawBorder) {
                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3], mBarBorderPaint);
                }

                Drawable d = getResources().getDrawable(R.drawable.fade_red);
                d.setBounds((int)buffer.buffer[j], (int)buffer.buffer[j + 1], (int)buffer.buffer[j + 2],
                        (int)buffer.buffer[j + 3]);
                d.draw(c);
            }
        }

        @Override
        public void drawExtras(Canvas c) {
            super.drawExtras(c);
/*
            Paint p = new Paint();
            p.setAlpha(90);

            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.zzang);
            c.drawBitmap(image, 0, 0, p);
*/
        }
    }
}
