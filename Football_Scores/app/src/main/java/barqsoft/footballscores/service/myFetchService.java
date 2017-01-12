package barqsoft.footballscores.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities.ConnectionActivity;
import barqsoft.footballscores.Utilities.Utilies;
import barqsoft.footballscores.Utilities.WakeLocker;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.widget.FootballWidgetProvider;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchService extends IntentService
{
    public static final String LOG_TAG = "myFetchService";
    public int mCount=0;
    Handler mHandler;
    public static final String ACTION_DATA_UPDATED ="barqsoft.footballscores.ACTION_DATA_UPDATED";
    public myFetchService()
    {
        super("myFetchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler=new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        setProgressBarWidgetVisibility(View.VISIBLE);
        deleteData();
        getData(getString(R.string.next_days_matches));
        getData(getString(R.string.previous_days_matches));
        updateWidget();
        setProgressBarWidgetVisibility(View.GONE);
        AlarmReceiver.completeWakefulIntent(intent);
        return;
    }

    private void setProgressBarWidgetVisibility(final int viewType){

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews=new RemoteViews(getPackageName(),R.layout.widget_football_scores);
        remoteViews.setViewVisibility(R.id.llprogressBar, viewType);
        ComponentName thisWidget = new ComponentName(this, FootballWidgetProvider.class);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    private void deleteData(){
        Context context=getApplicationContext();
        context.getContentResolver().delete(DatabaseContract.BASE_CONTENT_URI,
                null, null);

    }
    private void getData (String timeFrame)
    {
        //Creating fetch URL
        final String BASE_URL = getString(R.string.base_url); //Base URL
        final String QUERY_TIME_FRAME = getString(R.string.timeframe); //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        //Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod(getString(R.string.get_request));
            m_connection.addRequestProperty(getString(R.string.token_request), getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            JSON_data = buffer.toString();

        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,"Exception here" + e.getMessage());
        }
        finally {
            if(m_connection != null)
            {
                m_connection.disconnect();
            }
            if (reader != null)
            {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG,"Error Closing Stream");
                }
            }
        }
        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data)
                        .getJSONArray(getString(R.string.fixtures));
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }


                processJSONdata(JSON_data, getApplicationContext(), true);
             } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }
    private void processJSONdata (String JSONdata,Context mContext, boolean isReal)
    {

        final String SEASON_LINK = getString(R.string.season_link);
        final String MATCH_LINK = getString(R.string.match_link);
        final String TEAM_LINK=getString(R.string.team_link);
        final String FIXTURES = getString(R.string.fixtures);
        final String LINKS = getString(R.string.links);
        final String SOCCER_SEASON = getString(R.string.soccer_season);
        final String SELF = getString(R.string.self);
        final String MATCH_DATE = getString(R.string.match_date);
        final String HOME_TEAM = getString(R.string.home_team);
        final String AWAY_TEAM = getString(R.string.away_team);
        final String HOME_TEAM_ID = getString(R.string.home_team_id);
        final String AWAY_TEAM_ID = getString(R.string.away_team_id);
        final String RESULT = getString(R.string.result);
        final String HOME_GOALS = getString(R.string.home_goals);
        final String AWAY_GOALS = getString(R.string.away_goals);
        final String MATCH_DAY = getString(R.string.match_day_service);


        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;
        String homeTeamId=null;
        String awayTeamId=null;


        try {

        //for debugging purpouses
//            mHandler.post(new Runnable(){
//
//                @Override
//                public void run() {
//                    Toast.makeText(myFetchService.this,"en Servicio",Toast.LENGTH_SHORT).show();
//                }
//            });
            Log.d("VILLANUEVA","myFetchService");
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            int count=0;
            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < matches.length();i++)
            {

                JSONObject match_data = matches.getJSONObject(i);



                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString(getString(R.string.href));
                League = League.replace(SEASON_LINK, getString(R.string.empty_string));
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                SharedPreferences sharedPreferences= PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                String leaguePref=sharedPreferences.getString(getString(R.string.pref_league_key),
                        getString(R.string.pref_league_default));

                if (League.equals(leaguePref))

                {
                    count++;
                    //get home team crest
                    homeTeamId=match_data.getJSONObject(LINKS).getJSONObject(HOME_TEAM_ID)
                            .getString(getString(R.string.href));
                    homeTeamId=homeTeamId.replace(TEAM_LINK, getString(R.string.empty_string));
                    String homeTeamUrlCrest=getImagePath(homeTeamId);


                    //get away tem crest
                    awayTeamId=match_data.getJSONObject(LINKS).getJSONObject(AWAY_TEAM_ID)
                            .getString(getString(R.string.href));
                    awayTeamId=awayTeamId.replace(TEAM_LINK, getString(R.string.empty_string));
                    String awayTemUrlCrest=getImagePath(awayTeamId);

                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString(getString(R.string.href));
                    match_id = match_id.replace(MATCH_LINK, getString(R.string.empty_string));
                    if(!isReal){
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id=match_id+Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf(getString(R.string.t_charakter)) + 1, mDate.indexOf(getString(R.string.z_charakter)));
                    mDate = mDate.substring(0,mDate.indexOf(getString(R.string.t_charakter)));
                    SimpleDateFormat match_date = new SimpleDateFormat(getString(R.string.date_time_mask));
                    match_date.setTimeZone(TimeZone.getTimeZone(getString(R.string.utc_string)));
                    try {
                        Date parseddate = match_date.parse(mDate+mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat(getString(R.string.date_time_mask_hour_minute));
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(getString(R.string.time_divider)) + 1);
                        mDate = mDate.substring(0,mDate.indexOf(getString(R.string.time_divider)));

                        if(!isReal){
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.date_format_mask));
                            mDate=mformat.format(fragmentdate);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG,e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                     Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL,Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL,Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL,League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY,match_day);
                    match_values.put(DatabaseContract.scores_table.HOME_URL_CREST,homeTeamUrlCrest);
                    match_values.put(DatabaseContract.scores_table.AWAY_URL_CREST,awayTemUrlCrest);


                    values.add(match_values);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI,insert_data);
            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
            //delete old data(today - 3 days)
            Date date=new Date(System.currentTimeMillis()-(3*86400000));
            SimpleDateFormat formatter=new SimpleDateFormat(getString(R.string.date_format_mask));
            String formattedDate=formatter.format(date);
            String selection=DatabaseContract.scores_table.DATE_COL + " <= ?";

            mContext.getContentResolver().delete(DatabaseContract.BASE_CONTENT_URI,
                    selection, new String[]{formattedDate});

        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }

    }

    public void updateWidget(){

        Context context=getApplicationContext();
        Intent dataUpdatedIntent=new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
    private String getImagePath(String teamNumber) {
        String BASE_WIKI_URL=getString(R.string.base_wiki_url);
        BASE_WIKI_URL=BASE_WIKI_URL+teamNumber;
        Uri fetch_build=Uri.parse(BASE_WIKI_URL);
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;

        try {
            mCount++;
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod(getString(R.string.get_request));
            m_connection.addRequestProperty(getString(R.string.token_request),getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            JSON_data = buffer.toString();
           // Log.d("VILLANUEVA",JSON_data);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,"Exception here downloading:"+mCount+"/" + e.getMessage());
        }
        finally {
            if(m_connection != null)
            {
                m_connection.disconnect();
            }
            if (reader != null)
            {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG,"Error Closing Stream");
                }
            }
        }

        try {
            JSONObject jsonObject=new JSONObject(JSON_data);
            String path=jsonObject.getString(getString(R.string.crest_url));
            return path;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static class AlarmReceiver extends WakefulBroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
         //   WakeLocker.acquire(context);
            Intent sendIntent=new Intent(context,myFetchService.class);
         //   context.startService(sendIntent);
            startWakefulService(context,sendIntent);
          //  WakeLocker.release();
            Utilies.setAlarm(context);

        }
    }
}

