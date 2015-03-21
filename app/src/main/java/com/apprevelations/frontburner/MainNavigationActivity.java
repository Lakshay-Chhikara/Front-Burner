package com.apprevelations.frontburner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.io.InputStream;

public class MainNavigationActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {



    private ActionBar bar;
    // Tab titles
    /* A bug was introduced in Android 4.3 that ignores changes to the Canvas state
     * between multiple calls to super.dispatchDraw() when running with hardware acceleration.
     * To account for this bug, a slightly different approach was taken to fold a
     * static image whereby a bitmap of the original contents is captured and drawn
     * in segments onto the canvas. However, this method does not permit the folding
     * of a TextureView hosting a live camera feed which continuously updates.
     * Furthermore, the sepia effect was removed from the bitmap variation of the
     * demo to simplify the logic when running with this workaround."
     */
    static final boolean IS_JBMR2 = Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2;
    static final boolean IS_ISC = Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    static final boolean IS_GINGERBREAD_MR1 = Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    static LinearLayout mContainer;
    private boolean flag = true;
    int Flag=0;
    private Bundle mBundle = new Bundle();
    DrawerLayout mDrawerLayout;
    android.support.v7.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // getSupportActionBar().show();
        setContentView(R.layout.activity_navigation);

       toolbar =(android.support.v7.widget.Toolbar) findViewById(R.id.toolbarfeed);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
       // getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayUseLogoEnabled(false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mContainer = (LinearLayout) findViewById(R.id.container);

       // restoreActionBar();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (mDrawerLayout));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            new LoadProfileImage().execute(extras.getString("GOOGLE_PICTURE"));
        }
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#455a64")));
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
    }


    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch (position) {
            case R.id.navigation_drawer_home:


                fragment = new MyAccount();
                fragment.setArguments(mBundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
                //mBundle = null;*/

                break;

          /*  case R.id.navigation_drawer_feedback:

                break;

            case R.id.navigation_drawer_rate_us:

                break;*/

            default:
                break;
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
      //  MenuInflater inflater = getMenuInflater();
       // inflater.inflate(R.menu.actionbar_icons, menu);
        /*MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        setupSearchView(mSearchView);
        return super.onCreateOptionsMenu(menu);*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            MenuInflater inflater = getMenuInflater();
        /*    inflater.inflate(R.menu.global, menu);
            MenuItem item = menu.findItem(R.id.action_share);*/


        } else if (!mDrawerLayout.isDrawerVisible(Gravity.START)) {

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            mNavigationDrawerFragment.setGooglePlusProfilePic(getRoundedShape(result));
            if (mNavigationDrawerFragment.mCurrentSelectedPosition == R.id.navigation_drawer_home) {

            }
        }
    }

    private Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        if (scaleBitmapImage != null) {
            int targetWidth;
            int targetHeight;

            if (scaleBitmapImage.getWidth() > scaleBitmapImage.getHeight()) {
                targetWidth = scaleBitmapImage.getHeight();
                targetHeight = scaleBitmapImage.getHeight();
            } else {
                targetWidth = scaleBitmapImage.getWidth();
                targetHeight = scaleBitmapImage.getWidth();
            }

            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                    targetHeight, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(targetBitmap);
            Path path = new Path();
            path.addCircle(((float) targetWidth - 1) / 2,
                    ((float) targetHeight - 1) / 2,
                    (Math.min(((float) targetWidth),
                            ((float) targetHeight)) / 2),
                    Path.Direction.CCW);

            canvas.clipPath(path);
            Bitmap sourceBitmap = scaleBitmapImage;
            canvas.drawBitmap(sourceBitmap,
                    new Rect(0, 0, sourceBitmap.getWidth(),
                            sourceBitmap.getHeight()),
                    new Rect(0, 0, targetWidth, targetHeight), null);
            return targetBitmap;
        } else {
            return null;
        }
    }
}