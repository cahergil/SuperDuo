package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities.Utilies;
import barqsoft.footballscores.service.myFetchService;

/**
 * Created by Carlos on 18/10/2015.
 */
public class FootballWidgetProvider extends AppWidgetProvider {
    private static final String BTN_LESS_DATE = "myOnClickTag1";
    private static final String BTN_MORE_DATE = "myOnClickTag2";
    private static final long ONE_DAY=86400000L;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("VILLANUEVA", "onUpdate");
        for (int i = 0; i < appWidgetIds.length; i++) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_football_scores);


            Intent launchActivity=new Intent(context,MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, launchActivity, 0);
            remoteViews.setOnClickPendingIntent(R.id.imgBall,pi);
            remoteViews.setOnClickPendingIntent(R.id.textTitleBabar,pi);

            Intent lauchService=new Intent(context,myFetchService.class);
            PendingIntent pendingIntent=PendingIntent.getService(context,0,lauchService,0);
            remoteViews.setOnClickPendingIntent(R.id.imgUpdate, pendingIntent);

            remoteViews.setOnClickPendingIntent(R.id.btnLessDate,getPendingSelfIntent(context,BTN_LESS_DATE));
            remoteViews.setOnClickPendingIntent(R.id.btnMoreDate,getPendingSelfIntent(context,BTN_MORE_DATE));

            Intent svcIntent = new Intent(context, WidgetRemoteViewService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                remoteViews.setRemoteAdapter(R.id.listFootballWidget, svcIntent);
            } else {
                remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.listFootballWidget,
                        svcIntent);
            }

            remoteViews.setEmptyView(R.id.listFootballWidget, R.id.emptyTextView);

            Intent clickIntent = new Intent(context, MainActivity.class);

            PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.listFootballWidget, clickPI);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);


        }


    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("VILLANUEVA", "onReceive");
        if (BTN_LESS_DATE.equals(intent.getAction())) {
            long timestamp=Utilies.getDateStatus(context);
            timestamp-=ONE_DAY;
            Utilies.setDateStatus(context, timestamp);

        } else if (BTN_MORE_DATE.equals(intent.getAction())) {
            long timestamp=Utilies.getDateStatus(context);
            timestamp+=ONE_DAY;
            Utilies.setDateStatus(context, timestamp);

        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, getClass()));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listFootballWidget);


    }



}
