package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetailsActivity extends AppCompatActivity {
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        String history = intent.getStringExtra(Contract.Quote.COLUMN_HISTORY);
        String symbol = intent.getStringExtra(Contract.Quote.COLUMN_SYMBOL);
        final List<String> historyPoints = Arrays.asList(history.split("\n"));
        Collections.reverse(historyPoints);

        TextView stockName = (TextView) findViewById(R.id.stockName);

        stockName.setText(symbol);

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setViewPortOffsets(120, 180, 0, 0);
        mChart.setBackgroundColor(Color.WHITE);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        mChart.setMaxHighlightDistance(300);

        XAxis x = mChart.getXAxis();
        x.setDrawGridLines(true);
        x.setEnabled(true);
        x.setLabelRotationAngle(-90);
        x.setGranularity(1f);
        x.setTextSize(12);
        x.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("MMM yyyy");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int)value;
                if (index < 0 || index >= historyPoints.size()) return "";

                String[] parts = historyPoints.get(index).split(", ");
                return mFormat.format(new Date(Long.parseLong(parts[0])));
            }
        });


        YAxis y = mChart.getAxisLeft();
        y.setTextColor(Color.BLACK);
        y.setDrawGridLines(true);
        y.setAxisLineColor(Color.BLACK);
        y.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return "$" + String.format("%.1f", value);
            }
        });
        y.setTextSize(12);
        mChart.getAxisRight().setEnabled(false);


        // add data
        setData(historyPoints);

        mChart.getLegend().setEnabled(false);
        // dont forget to refresh the drawing
        mChart.invalidate();
    }


    private void setData(List<String> historyPoints) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        int i=0;
        for (String point : historyPoints) {
            String[] parts = point.split(", ");
            float y = Float.parseFloat(parts[1]);
            yVals.add(new Entry(i++, y));
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals, "Historic Data");

            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.BLUE);
            set1.setFillColor(Color.BLUE);
            set1.setDrawFilled(true);
            set1.setFillAlpha(100);

            // create a data object with the datasets
            LineData data = new LineData(set1);
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            mChart.setData(data);
        }
    }
}
