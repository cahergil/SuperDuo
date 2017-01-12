package it.jaschke.alexandria.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.services.BookService;

/**
 * Created by Carlos on 01/10/2015.
 */
public class Utility {

    @SuppressWarnings("ResourceType")
    public static @BookService.NetStatus int  getNetworkStatus(Context c){
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(c);
        int status=sp.getInt(c.getString(R.string.pref_net_status_key),BookService.NET_STATUS_UNKNOWN);
        return status;

    }
}
