package com.elaine.nsliyapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.SyllableEntryView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Activity used to enter character for the first time.
 */
public class DrawActivity extends Activity {

    /**
     * Key used to access shared preferences for character usage numbers
     */
    public static final String PREFERENCES_FILE_KEY = "preferencesKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        // Set ActionBar color
        ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.g700)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // If there are strokes on the screen, prompt before actually quitting
        if(!((DrawView)findViewById(R.id.draw_view)).isEmpty()) {
            AlertDialog quitDialog = new AlertDialog.Builder(this).setMessage(R.string.quit_draw_dialog)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();

            quitDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_clear) { // Clear the drawing view of strokes
            ( (DrawView) findViewById(R.id.draw_view) ).clear();
            return true;
        } else if (id == R.id.action_undo){ // Remove the most recent stroke from the screen
            ( (DrawView) findViewById(R.id.draw_view) ).undo();
        } else if (id == R.id.action_save){ // Save the character images and pronunciations to a new folder
            File directory = null;
            try {
                directory = ( (DrawView) findViewById(R.id.draw_view ) ).saveCharacter(this, null);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(directory!=null){
                ( (SyllableEntryView) findViewById(R.id.pronunciation_view)).saveSyllables(directory);
            }
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
