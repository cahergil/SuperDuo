package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.Utilities.ConnectionActivity;
import barqsoft.footballscores.Utilities.Utilies;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment  {
    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];
    private PagerTabStrip mPagerTabStrip;
    private boolean mRtlMode=false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerTabStrip = (PagerTabStrip) rootView.findViewById(R.id.pager_header);
        TextView rtlText = (TextView) rootView.findViewById(R.id.rtlText);
        mPagerTabStrip.setDrawFullUnderline(true);
        mPagerTabStrip.setTabIndicatorColor(Color.YELLOW);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());
        if (rtlText!=null) {
            mRtlMode=true;
        }

        if (mRtlMode==false) {
            for (int i = 0; i < NUM_PAGES; i++) {
                // 1000x60=millisecxmin
                // 1000x60x60=millisecxhour
                // 1000x60x60x24=millisecxday
                Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.date_mask));
                viewFragments[i] = new MainScreenFragment();
                viewFragments[i].setFragmentDate(mformat.format(fragmentdate));
            }
        } else {
            for (int i=4;i>-1;i--) {
                Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                SimpleDateFormat mformat = new SimpleDateFormat(getString(R.string.date_mask));
                viewFragments[4-i] = new MainScreenFragment();
                viewFragments[4-i].setFragmentDate(mformat.format(fragmentdate));

            }
        }
        Utilies.setRtlStatus(getActivity(), mRtlMode);
        if(!Utilies.isNetworkAvailable(getActivity())){
            Intent intentConnection =new Intent(getActivity(), ConnectionActivity.class);
            startActivity(intentConnection);
        }
        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.current_fragment);

        return rootView;
    }


    private class myPageAdapter extends FragmentStatePagerAdapter {
        @Override
        public Fragment getItem(int i) {
            return viewFragments[i];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if (mRtlMode==false) {
                return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * 86400000));
            } else {
                return getDayName(getActivity(), System.currentTimeMillis() + ((2-position) * 86400000));
            }


        }

        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.

            Time t = new Time();
            t.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                return context.getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                return context.getString(R.string.yesterday);
            } else {
                Time time = new Time();
                time.setToNow();
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                SimpleDateFormat dayFormat = new SimpleDateFormat(getString(R.string.weekday_mask));
                return dayFormat.format(dateInMillis);
            }
        }
    }
}
