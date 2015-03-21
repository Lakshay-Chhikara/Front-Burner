package com.apprevelations.frontburner;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NavigationDrawerFragment extends Fragment implements OnClickListener {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ScrollView mDrawerScrollView;
    private View mFragmentContainerView;

    public int mCurrentSelectedPosition = R.id.navigation_drawer_home;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private RelativeLayout googlePlusProfileLayout;
    private TextView googlePlusUserName;
    private ImageView googlePlusProfilePic;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerScrollView = (ScrollView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        /*googlePlusProfileLayout =
                (RelativeLayout) mDrawerScrollView.findViewById(R.id.google_plus_profile_layout);*/
        googlePlusProfilePic =
                (ImageView) mDrawerScrollView.findViewById(R.id.google_plus_profile_pic);
        googlePlusUserName = (TextView) mDrawerScrollView.findViewById(R.id.google_plus_user_name);
       // mDrawerScrollView.findViewById(R.id.google_plus_profile_layout).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.navigation_drawer_home).setOnClickListener(this);
       // mDrawerScrollView.findViewById(R.id.navigation_drawer_rate_us).setOnClickListener(this);
        //mDrawerScrollView.findViewById(R.id.navigation_drawer_google_plus_logout)
        //        .setOnClickListener(this);
/*
        mDrawerScrollView.findViewById(R.id.all_events).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.Phoenix).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.Troika).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.Renaissance).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.Excelsior).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.Tatva).setOnClickListener(this);
        mDrawerScrollView.findViewById(R.id.Innova).setOnClickListener(this);*/
      //  mDrawerScrollView.findViewById(R.id.engifest).setOnClickListener(this);
       // mDrawerScrollView.findViewById(R.id.navigation_drawer_home).setSelected(true);

        return mDrawerScrollView;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        int mPreviousSelectedPosition = mCurrentSelectedPosition;
        mCurrentSelectedPosition = v.getId();
        mDrawerScrollView.findViewById(mPreviousSelectedPosition).setSelected(false);
        mDrawerScrollView.findViewById(mCurrentSelectedPosition).setSelected(true);

        switch (v.getId()) {
            /*case R.id.google_plus_profile_layout:
                selectItem(mCurrentSelectedPosition);
                break;
*/
            case R.id.navigation_drawer_home:
                selectItem(mCurrentSelectedPosition);
                break;

            /*case R.id.navigation_drawer_feedback:
                selectItem(mCurrentSelectedPosition);
                break;

            case R.id.navigation_drawer_rate_us:
                selectItem(mCurrentSelectedPosition);
                break;

            case R.id.navigation_drawer_google_plus_logout:
                selectItem(mCurrentSelectedPosition);
                break;*/

            default:
                break;
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.mipmap.shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        /*ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);*/

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.mipmap.ic_action_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
// TODO Auto-generated method stub


                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int selectedTextViewId) {
        mCurrentSelectedPosition = selectedTextViewId;
//        if (mDrawerListView != null) {
//            mDrawerListView.setItemChecked(position, true);
//        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(selectedTextViewId);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    public void setGooglePlusProfilePic(Bitmap googlePlusProfilePic) {
        this.googlePlusProfilePic.setImageBitmap(googlePlusProfilePic);
    }


}
