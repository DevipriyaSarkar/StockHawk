package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.ChartEntry;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.style.DashAnimation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Devipriya on 5/27/2016.
 */
public class StocksGraphActivity extends AppCompatActivity{

    LineChartView lineChartView;
    ArrayList<Float> bidPriceList;
    ArrayList<Integer> isCurrentList;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        //retrieve which stock symbol is clicked
        String selSymbol = getIntent().getStringExtra("selSymbol");
        //set title accordingly
        setTitle(selSymbol.toUpperCase() + " " + getString(R.string.text_graph));

        lineChartView = (LineChartView) findViewById(R.id.lineChartView);

        //initialise the data set
        LineSet dataSet = new LineSet();

        //retrieve old stocks
        Cursor cur = this.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[] { QuoteColumns.BIDPRICE, QuoteColumns.SYMBOL, QuoteColumns.ISCURRENT },
                QuoteColumns.SYMBOL + "=?",
                new String[] {selSymbol}, null);

        //get 10 latest bid prices
        if(cur != null) {
            cur.moveToLast();
            //initialise the variables
            count = 0;
            bidPriceList = new ArrayList<>();
            isCurrentList = new ArrayList<>();
            while (!cur.isBeforeFirst()) {
                if (count == 10)
                    break;
                count++;
                String symbol = cur.getString(cur.getColumnIndex("symbol"));
                String bidPrice = cur.getString(cur.getColumnIndex("bid_price"));
                int isCurrent = cur.getInt(cur.getColumnIndex("is_current"));
                Log.d("QUERY", "\nSymbol: " + symbol + ", Bid price: " + bidPrice + ", isCurrent: " + String.valueOf(isCurrent));
                //save the queried values in array list
                bidPriceList.add(Float.parseFloat(bidPrice));
                isCurrentList.add(isCurrent);
                cur.moveToPrevious();
            }
        }
        if (cur != null) {
            cur.close();
        }

        //add points to the data set such that the latest value is the last point
        count = 0;
        for(int i = (bidPriceList.size() - 1); i >= 0; i--) {
            Point point = new Point("null", 0);
            if(isCurrentList.get(i) == 0) {
                //old bid prices in yellow
                count++;
                point = new Point(String.valueOf(count), bidPriceList.get(i));
                point.setColor(ContextCompat.getColor(getApplicationContext(), R.color.yellow));
                point.setRadius(Tools.fromDpToPx(6));
            } else if(isCurrentList.get(i) == 1) {
                //current bid price shown in red
                point = new Point(getString(R.string.graph_label_now), bidPriceList.get(i));
                point.setColor(ContextCompat.getColor(getApplicationContext(), R.color.material_red_700));
                point.setRadius(Tools.fromDpToPx(8));
            }
            if(!point.getLabel().equals("null"))    //point not null
                dataSet.addPoint(point);
        }

        ArrayList<Float> bidPriceListSorted = new ArrayList<>();    //to sort and obtain the min and max values
        final ArrayList<Float> bidPriceList = new ArrayList<>();    //original array list to obtain the bid price of the point clicked
        for(ChartEntry chartEntry : dataSet.getEntries()) {         //in the order inserted in the data set
            bidPriceListSorted.add(chartEntry.getValue());
            bidPriceList.add(chartEntry.getValue());
        }
        //get the maximum and minimum values of the points to set appropriate scale
        Collections.sort(bidPriceListSorted);
        int min = (int) Math.floor(bidPriceListSorted.get(0));
        int max = (int) Math.ceil(bidPriceListSorted.get(bidPriceListSorted.size() - 1));

        //customisation

/*        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.white));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));*/
        //lineChartView.setGrid(ChartView.GridType.FULL, gridPaint);

        lineChartView.setAxisBorderValues(min, max, 1);
        lineChartView.setXLabels(AxisController.LabelPosition.OUTSIDE);
        lineChartView.setYLabels(AxisController.LabelPosition.OUTSIDE);
        dataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.material_blue_500))
                .setDotsStrokeColor(ContextCompat.getColor(getApplicationContext(), R.color.material_blue_500))
                .setDashed(new float[] {10f, 10f})
                .setThickness(5);

        //add data set to the chart
        lineChartView.addData(dataSet);

        //method to show tooltip showing the bid price when a point is clicked
        lineChartView.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                Tooltip tip = new Tooltip(getApplicationContext(), R.layout.tooltip, R.id.tooltip_text);
                tip.setVerticalAlignment(Tooltip.Alignment.TOP_BOTTOM);
                tip.prepare(rect, bidPriceList.get(entryIndex));
                lineChartView.setTooltips(tip);
                lineChartView.showTooltip(tip, true);
                Log.d("TOOLTIP", "set index: " + setIndex + " entry index: " + entryIndex + " bid price: " + bidPriceList.get(entryIndex));
            }
        });

        //method to set on graph clicked
        lineChartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss all tooltips onn graph click
                lineChartView.dismissAllTooltips();
            }
        });

        //animate set
        lineChartView.animateSet(0, new DashAnimation());

        //show graph
        lineChartView.show();
    }
}
