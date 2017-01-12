package barqsoft.footballscores.Utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.service.myFetchService;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{

     public static final int SERIE_A = 401;
     public static final int PREMIER_LEGAUE = 398;
     public static final int CHAMPIONS_LEAGUE = 405;
     public static final int PRIMERA_DIVISION = 399;
     public static final int SEGUNDA_DIVISION=400;
     public static final int BUNDESLIGA1 = 394;
     public static final int BUNDESLIGA2=395;
     public static final int BUNDESLIGA3=403;
     public static final int LIGUE1=396;
     public static final int LIGUE2=397;
     public static final int EREDIVISIE = 404;
     public static final int ALARM_TIME=1000*60*60*12;

    public static String getLeagueFromPreferences(Context context){
        SharedPreferences sharedPreferences= PreferenceManager
                .getDefaultSharedPreferences(context);
        String league=sharedPreferences.getString(context.getString(R.string.pref_league_key),
                context.getString(R.string.pref_league_default));
        return league;

    }
    public static String getLeague(int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return ContextWrapper.getCustomAppContext().getString(R.string.serie_a);
            case PREMIER_LEGAUE : return ContextWrapper.getCustomAppContext().getString(R.string.premier_league);
            case CHAMPIONS_LEAGUE : return ContextWrapper.getCustomAppContext().getString(R.string.championsleague);
            case PRIMERA_DIVISION : return ContextWrapper.getCustomAppContext().getString(R.string.primeradivision);
            case SEGUNDA_DIVISION : return ContextWrapper.getCustomAppContext().getString(R.string.segundadivision);
            case LIGUE1:return ContextWrapper.getCustomAppContext().getString(R.string.ligue1);
            case LIGUE2: return ContextWrapper.getCustomAppContext().getString(R.string.ligue2);
            case BUNDESLIGA1 : return ContextWrapper.getCustomAppContext().getString(R.string.bundesliga1);
            case BUNDESLIGA2: return ContextWrapper.getCustomAppContext().getString(R.string.bundesliga2);
            case BUNDESLIGA3: return ContextWrapper.getCustomAppContext().getString(R.string.bundesliga3);
            case EREDIVISIE: return ContextWrapper.getCustomAppContext().getString(R.string.eredivisie);
            default: return ContextWrapper.getCustomAppContext().getString(R.string.not_known_league);
        }
    }
    public static String getMatchDay(int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return ContextWrapper.getCustomAppContext().getString(R.string.group_stage);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return ContextWrapper.getCustomAppContext().getString(R.string.first_knockout_round);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return ContextWrapper.getCustomAppContext().getString(R.string.quarter_final);
            }
            else if(match_day == 11 || match_day == 12)
            {
                return ContextWrapper.getCustomAppContext().getString(R.string.semi_final);
            }
            else
            {
                return ContextWrapper.getCustomAppContext().getString(R.string.final_);
            }
        }
        else
        {
            return ContextWrapper.getCustomAppContext().getString(R.string.match_day) + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }


    public static boolean isRtlEnabled(Context context){
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        boolean status=sharedPreferences.getBoolean(context.getString(R.string.rtl_status),false);
        return status;
    }
    public static void setRtlStatus(Context context,boolean rtlStatus){
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.rtl_status),rtlStatus);
        editor.apply();

    }
    public static void setDateStatus(Context context,long timestamp) {
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(context.getString(R.string.date_status), timestamp);
        editor.apply();

    }
    public static long getDateStatus(Context context){
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        long millis=sharedPreferences.getLong(context.getString(R.string.date_status), 0L);
        return millis;
    }
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=cm.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnectedOrConnecting();
    }
    public static void setAlarm(Context context){
        Intent alarmIntent=new Intent(context,myFetchService.AlarmReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(context,0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(android.os.Build.VERSION.SDK_INT>=19) {
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_TIME, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_TIME, pi);
        }

    }
}
