package barqsoft.footballscores.Utilities;

import android.app.Application;
import android.content.Context;

/**
 * Created by Carlos on 15/10/2015.
 */
public class ContextWrapper extends Application {

    private static Context context;
    public void onCreate(){
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getCustomAppContext(){
        return context;
    }

}
