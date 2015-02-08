package com.elaine.nsliyapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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

    private static final String[] drawerNames = {"Manage Characters","Manage Words"};

    public DrawerLayout drawerLayout;
    public ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    public boolean isDrawerOpen = false;

    protected void onCreateDrawer(){
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,android.R.drawable.arrow_down_float,0,0){
            @Override
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                getActionBar().setTitle(DrawerActivity.this.getTitle());
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if(slideOffset > .55 && !isDrawerOpen){
                    onDrawerOpened(drawerView);
                    isDrawerOpen = true;
                } else if(slideOffset < .45 && isDrawerOpen) {
                    onDrawerClosed(drawerView);
                    isDrawerOpen = false;
                }
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

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
                    drawerLayout.closeDrawer(drawerList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
                }
            }
        });

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
