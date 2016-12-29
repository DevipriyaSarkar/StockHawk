package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by Devipriya on 5/15/2016.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.StocksGraphActivity;


/**
 * Created by Devipriya on 5/27/2016.
 */

// Implementation of App Widget functionality.

public class StocksWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent serviceIntent = new Intent(context, StocksWidgetService.class);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);

            // Sets the remote adapter used to fill in the list items
            widget.setRemoteAdapter(R.id.widget_list, serviceIntent);
            // Display appropriate message if list view empty
            widget.setEmptyView(R.id.widget_list, R.id.empty_view);

            Intent clickIntent = new Intent(context, StocksGraphActivity.class);
            PendingIntent clickPI = PendingIntent
                    .getActivity(context, 0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            widget.setPendingIntentTemplate(R.id.widget_list, clickPI);

            // Instruct the widget manager to update the widget
            ComponentName component = new ComponentName(context, StocksWidgetProvider.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
            appWidgetManager.updateAppWidget(component, widget);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}