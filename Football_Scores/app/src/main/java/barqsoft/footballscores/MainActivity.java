package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.urlconnection.StethoURLConnectionManager;

import barqsoft.footballscores.Utilities.ConnectionActivity;
import barqsoft.footballscores.Utilities.Utilies;
import barqsoft.footballscores.service.myFetchService;

public class MainActivity extends AppCompatActivity
{
    public static int selected_match_id;
    public static int current_fragment = 2;

    public static String LOG_TAG = "MainActivity";
    private final String save_tag = "Save Test";
    private PagerFragment my_main;
    private Toolbar mToolbar;
    private boolean mFromSettings=false;
    private boolean mFirstLaunch=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar= (Toolbar) findViewById(R.id.app_bar);
        mToolbar.setContentDescription(getString(R.string.impaired_toolbar));
        setSupportActionBar(mToolbar);
        mToolbar.setLogo(R.drawable.ic_ball);
        mToolbar.setLogoDescription(R.string.impaired_logo);
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build()
        );


        Log.d(LOG_TAG, "Reached MainActivity onCreate");

        if (savedInstanceState == null) {
            //set date in pref to today
            Utilies.setDateStatus(this,System.currentTimeMillis());
            //set an alarm that wakes up the devices each 12 hours
            Utilies.setAlarm(this);
            mFirstLaunch=true;
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mFromSettings==true || mFirstLaunch==true){
            if(!Utilies.isNetworkAvailable(this)){
                Intent intentConnection =new Intent(this, ConnectionActivity.class);
                startActivity(intentConnection);
            } else {
                update_scores();
            }
        }

        mFromSettings=false;
        mFirstLaunch=false;
        String league=Utilies.getLeague(Integer.parseInt(Utilies.getLeagueFromPreferences(this)));
        getSupportActionBar().setTitle(league);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        } else if(id==R.id.action_settings) {
            mFromSettings=true;
            Intent start_settings=new Intent(this,SettingsActivity.class);
            startActivity(start_settings);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag, "will save");
        Log.v(save_tag, "fragment: " + String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        Log.v(save_tag, "selected id: " + selected_match_id);
        outState.putInt(getString(R.string.main_current_fragment), my_main.mPagerHandler.getCurrentItem());
        outState.putInt(getString(R.string.main_selected_match), selected_match_id);
        getSupportFragmentManager().putFragment(outState,getString(R.string.main_my_main), my_main);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(save_tag, "will retrive");
        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
        current_fragment = savedInstanceState.getInt(getString(R.string.main_current_fragment));
        selected_match_id = savedInstanceState.getInt(getString(R.string.main_selected_match));
        my_main = (PagerFragment) getSupportFragmentManager().
                getFragment(savedInstanceState, getString(R.string.main_my_main));

        super.onRestoreInstanceState(savedInstanceState);
    }
    private void update_scores()
    {
        Intent service_start = new Intent(this, myFetchService.class);
        startService(service_start);
    }

}
