package com.elaine.nsliyapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Allows multiple activities using the same navigation drawer.
 * Derived from kevinvanmierlo on github.com.
 */
public abstract class DrawerActivity extends Activity {

    /**
     * Drawer names to be shown in the drawer sidebar.
     */
    private static final String[] drawerNames = {"Manage Characters","Manage Words"};

    /**
     * DrawerLayout to hold sidebar view
     */
    public DrawerLayout drawerLayout;
    /**
     * ListView to be held in drawer sidebar
     */
    public ListView drawerList;
    /**
     * DrawerToggle to open and close drawer layout.
     */
    private ActionBarDrawerToggle drawerToggle;

    /**
     * If drawer is open or not.
     */
    public boolean isDrawerOpen = false;

    /**
     * Sets up drawer layout on sidebar.
     */
    protected void onCreateDrawer(){
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,android.R.drawable.arrow_down_float,0,0){
            @Override
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                // Set the title to this activity's title when the drawer closes
                getActionBar().setTitle(DrawerActivity.this.getTitle());
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                // Set the title to the application's title when the drawer opens
                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // If opening from closed, set it open
                if(slideOffset > .55 && !isDrawerOpen){
                    onDrawerOpened(drawerView);
                    isDrawerOpen = true;
                } else if(slideOffset < .45 && isDrawerOpen) {
                    // If closing from opened, set it closed
                    onDrawerClosed(drawerView);
                    isDrawerOpen = false;
                }
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        // Set ActionBar colors and settings
        ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.g700)));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Fill drawer ListView with link names and intents
        drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.view_drawer_item,drawerNames));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerList.setItemChecked(position,true);
                Intent intent = null;
                switch(position){
                    case 0:
                        if(!(DrawerActivity.this instanceof ViewCharActivity)){
                            intent = new Intent(DrawerActivity.this, ViewCharActivity.class);
                        }
                        break;
                    case 1:
                        if(!(DrawerActivity.this instanceof ViewWordActivity)) {
                            intent = new Intent(DrawerActivity.this, ViewWordActivity.class);
                        }
                        break;
                }
                if(intent != null){
                    // Set intent to switch without animation in
                    drawerLayout.closeDrawer(drawerList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
                } else {
                    // If same activity as already open, simply close drawer
                    drawerLayout.closeDrawer(drawerList);
                }
            }
        });

        // Open drawer on first opening of activity
        drawerLayout.openDrawer(drawerList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
