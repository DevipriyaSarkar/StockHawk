package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

/**
 * Created by Devipriya on 5/15/2016.
 */

// StocksWidgetDataProvider acts as the adapter for the collection view widget,
// providing RemoteViews to the widget in the getViewAt method.

class StocksWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private ArrayList<String> symbolArrayList;  //array list to hold all symbols
    private ArrayList<String> bidPriceArrayList;    //array list to hold all bid prices

    StocksWidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        //fetch the stock data
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return (symbolArrayList.size());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_row);

        //set texts on the widget row
        row.setTextViewText(R.id.widget_stock_symbol, symbolArrayList.get(position));
        row.setTextViewText(R.id.widget_bid_price, bidPriceArrayList.get(position));

        //launches the graph for the symbol clicked
        Intent i = new Intent();
        Bundle extras = new Bundle();
        extras.putString("selSymbol", symbolArrayList.get(position));
        i.putExtras(extras);
        row.setOnClickFillInIntent(R.id.widget_stock_symbol, i);

        //set content description
        row.setContentDescription(R.id.widget_stock_symbol, String.format("%s%s", context.getString(R.string.list_symbol_content_desc), symbolArrayList.get(position)));
        row.setContentDescription(R.id.widget_bid_price, String.format("%s%s", context.getString(R.string.list_bid_price_content_desc), bidPriceArrayList.get(position)));

        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return (null);
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }

    @Override
    public void onDataSetChanged() {
        //fetch the stock data
        initData();
    }

    private void initData() {

        final long token = Binder.clearCallingIdentity();
        try {
            //retrieve current quotes
            Cursor cur = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[] { "Distinct " + QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE }, QuoteColumns.ISCURRENT + "=?" ,
                    new String[] {String.valueOf(1)}, null);

            if(cur != null) {
                //initialise
                symbolArrayList = new ArrayList<>();
                bidPriceArrayList = new ArrayList<>();
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    String symbol = cur.getString(cur.getColumnIndex("symbol"));
                    String bidPrice = cur.getString(cur.getColumnIndex("bid_price"));
                    Log.d("QUERY", "\nSymbol: " + symbol + ", Bid price: " + bidPrice);
                    //update the array lists
                    symbolArrayList.add(symbol);
                    bidPriceArrayList.add(bidPrice);
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}