package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities.GetImage;
import barqsoft.footballscores.Utilities.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by Carlos on 19/10/2015.
 */
public class ListProviderFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context ctxt = null;
    private int appWidgetId;
    private static String[] items_home;
    private static String[] images_home;
    private static String[] items_away;
    private static String[] images_away;
    private static String[] score;
    private static String[] time;
    Cursor data;

    public ListProviderFactory(Context context, Intent intent) {
        this.ctxt = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }


    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }

        final long identityToken = Binder.clearCallingIdentity();
        Uri matchesScoresUri= DatabaseContract.scores_table.buildScoreWithDate();
        // for testing new Date(System.currentTimeMillis()+(86400000));
        Date timestamp=new Date(Utilies.getDateStatus(ctxt));
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(ctxt.getString(R.string.date_mask));
        String date=simpleDateFormat.format(timestamp);
        String [] selectionArgs=new String[]{date};
        data=ctxt.getContentResolver().query(matchesScoresUri,null,null,selectionArgs,null);

        if (data==null){
            return;
        }
        items_home=new String[data.getCount()];
        images_home=new String[data.getCount()];

        items_away=new String[data.getCount()];
        images_away=new String[data.getCount()];
        score=new String[data.getCount()];
        time=new String[data.getCount()];
        String home,away,home_goals,away_goals,img_home,img_away;
        int i=0;
        while(data.moveToNext()) {
            home=data.getString(data.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
            home_goals=data.getString(data.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
            away=data.getString(data.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
            away_goals=data.getString(data.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));
            img_home=data.getString(data.getColumnIndex(DatabaseContract.scores_table.HOME_URL_CREST));
            img_away=data.getString(data.getColumnIndex(DatabaseContract.scores_table.AWAY_URL_CREST));
            items_home[i]=home;
            items_away[i]=away;
            images_home[i]=img_home;
            images_away[i]=img_away;
            if( home_goals.equals("-1") &&  away_goals.equals("-1")) {
                score[i]="-";
            } else {
                score[i] = home_goals + "-" + away_goals;
            }

            if(matchFinished(Utilies.getDateStatus(ctxt))) {
                time[i]=ctxt.getString(R.string.match_finished);
            } else {
                time[i] = data.getString(data.getColumnIndex(DatabaseContract.scores_table.TIME_COL));
            }
            i++;
        }
        //change ligue and date in widget "toolbar"
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctxt);
        RemoteViews view=new RemoteViews(ctxt.getPackageName(),R.layout.widget_football_scores);

        view.setTextViewText(R.id.textTitleBabar,
                Utilies.getLeague(Integer.parseInt(Utilies.getLeagueFromPreferences(ctxt))));


        view.setTextViewText(R.id.textDate, date);



        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            view.setContentDescription(R.id.imgBall,ctxt.getString(R.string.impaired_logo));
            view.setContentDescription(R.id.textTitleBabar,
                    Utilies.getLeague(Integer.parseInt(Utilies.getLeagueFromPreferences(ctxt))));
            view.setContentDescription(R.id.imgUpdate,ctxt.getString(R.string.impaired_widget_update_widget_image));
            view.setContentDescription(R.id.btnLessDate,ctxt.getString(R.string.impaired_widget_decrease_date));
            view.setContentDescription(R.id.btnMoreDate,ctxt.getString(R.string.impaired_widget_increase_date));
            view.setContentDescription(R.id.textDate,date);


        }


        ComponentName thisWidget = new ComponentName(ctxt, FootballWidgetProvider.class);
        appWidgetManager.updateAppWidget(thisWidget, view);
        Binder.restoreCallingIdentity(identityToken);



    }
    private boolean matchFinished(long date) {
        Time t=new Time();
        t.setToNow();
        int currentJulianDay=Time.getJulianDay(System.currentTimeMillis(),t.gmtoff);
        int julianDay=Time.getJulianDay(date,t.gmtoff);
        if(currentJulianDay>julianDay) {
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return  data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }   

        RemoteViews row=new RemoteViews(ctxt.getPackageName(),
                R.layout.widget_row);


        row.setTextViewText(R.id.text_home, items_home[position]);
        GetImage.load(ctxt, images_home[position], row, R.id.img_home);


        row.setTextViewText(R.id.text_score,score[position]);

        if (time[position].equals("FINISHED")) {
            row.setTextColor(R.id.text_date, Color.RED);
        } else {
            row.setTextColor(R.id.text_date, Color.GREEN);
        }
        row.setTextViewText(R.id.text_date, String.valueOf(time[position]));

        row.setTextViewText(R.id.text_away, items_away[position]);
        GetImage.load(ctxt, images_away[position], row, R.id.img_away);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            row.setContentDescription(R.id.img_home, ctxt.getString(R.string.impaired_home_crest));
            row.setContentDescription(R.id.img_away, ctxt.getString(R.string.impaired_away_crest));
            row.setContentDescription(R.id.text_score,ctxt.getString(R.string.impaired_score_text)+score[position]);
            row.setContentDescription(R.id.text_date,ctxt.getString(R.string.impaired_time_match)+time[position]);
        }

       // Intent i=new Intent();
      //  Bundle extras=new Bundle();


        //row.setOnClickFillInIntent(R.id.text_home, i);


        return(row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
