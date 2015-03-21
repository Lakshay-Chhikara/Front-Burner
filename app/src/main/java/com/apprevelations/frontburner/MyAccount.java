package com.apprevelations.frontburner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MyAccount extends Fragment {

    static final String LOG_TAG = "SlidingTabsBasicFragment";


    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;
    CharSequence Titles[]={"Question Asked","Upvoted","Comment"};
    int Numboftabs =3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.testing, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter(getFragmentManager(),Titles,Numboftabs));
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        Toolbar toolbar=(Toolbar)view.findViewById(R.id.toolbar);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("");
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.white);    //define any color in xml resources and set it here, I have used white
            }

            @Override
            public int getDividerColor(int position) {
                return getResources().getColor(R.color.transparent);
            }

        });
        return view;
    }

    public class SamplePagerAdapter extends FragmentPagerAdapter {

        CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
        int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

        // Build a Constructor and assign the passed Values to appropriate values in the class
        public SamplePagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
            super(fm);

            this.Titles = mTitles;
            this.NumbOfTabs = mNumbOfTabsumb;

        }

        //This method return the fragment for the every position in the View Pager
        @Override
        public Fragment getItem(int position) {

            if(position == 0) // if the position is 0 we are returning the First tab
            {
                QAsked fr=new QAsked();
               return fr;
            }
            else if(position == 1) // if the position is 0 we are returning the First tab
            {
                CommentedFragment fr=new CommentedFragment();
                return fr;
            }
            else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
            {
                Upvote fr=new Upvote();
                return fr;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Titles[position];
        }

        @Override
        public int getCount() {
            return NumbOfTabs;
        }

    }
}
