package com.elaine.nsliyapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.SyllableEntryView;

import java.io.File;

public class DrawActivity extends Activity {

    public static final String PREFERENCES_FILE_KEY = "preferencesKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            ( (DrawView) findViewById(R.id.draw_view) ).clear();
            return true;
        } else if (id == R.id.action_undo){
            ( (DrawView) findViewById(R.id.draw_view) ).undo();
        } else if (id == R.id.action_save){
            File directory = ( (DrawView) findViewById(R.id.draw_view ) ).saveCharacter(this, null);
            if(directory!=null){
                ( (SyllableEntryView) findViewById(R.id.pronunciation_view)).saveSyllables(directory);
            }
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
