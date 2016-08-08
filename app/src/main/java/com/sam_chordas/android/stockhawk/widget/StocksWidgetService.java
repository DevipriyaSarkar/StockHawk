package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Devipriya on 5/15/2016.
 */

//StocksWidgetService is the {@link RemoteViewsService} that will return our RemoteViewsFactory

public class StocksWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        return (new StocksWidgetDataProvider(this.getApplicationContext(), intent));
    }

}