package com.elaine.nsliyapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.SyllableEntryView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Used to edit previously written characters.
 * Created by Elaine on 1/4/2015.
 */
public class EditDrawActivity extends Activity {

    /**
     * Extra key in intents for file name to be loading from.
     */
    public static final String FILE_EXTRA_NAME = "fileName";
    /**
     * Extra key in intent to return to the ViewCharActivity with the relevant directory name
     */
    public static final String DIRECTORY_RETURN_EXTRA = "directory";
    /**
     * The file directory loading the character to edit from.
     */
    private File directory;
    /**
     * The height of the screen
     */
    private float height=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directory = new File(getIntent().getStringExtra(FILE_EXTRA_NAME));
        setContentView(R.layout.activity_draw);

        // Set ActionBar color
        ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.g700)));
        }
        // Set default result to cancelled
        setResult(RESULT_CANCELED);

        // Set height to metrics given upon opening screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels;
        if(actionBar!=null){
            height -= actionBar.getHeight();
        }
    }

    @Override
    public void onBackPressed() {
        DrawView drawView = (DrawView)findViewById(R.id.draw_view);
        // If changes have been made to the drawing view, prompt user before leaving.
        if(drawView.changed() && !drawView.isEmpty() ) {
            AlertDialog quitDialog = new AlertDialog.Builder(this).setMessage(R.string.quit_edit_dialog)
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_revert) { // If keyboard is not up, reload screen from file
            View view = findViewById(R.id.draw_view);
            if(view.getHeight() > 0.5*height) {
                try {
                    ((DrawView) findViewById(R.id.draw_view)).loadFromDirectory(directory);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ((SyllableEntryView) findViewById(R.id.pronunciation_view)).loadFromDirectory(directory);
            }
            return true;
        } else if (id == R.id.action_undo){ // Remove most recent stroke from screen
            ( (DrawView) findViewById(R.id.draw_view) ).undo();
        } else if (id == R.id.action_save){ // Save character images and pronunciations to file
            if(directory!=null){
                File save = null;
                try {
                    save = ( (DrawView) findViewById(R.id.draw_view ) ).saveCharacter(this, directory);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(save!=null) {
                    ((SyllableEntryView) findViewById(R.id.pronunciation_view)).saveSyllables(directory);

                    // return to previous activity with notification of which directory was modified
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(DIRECTORY_RETURN_EXTRA, directory.getName());
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns the directory associated with this editing activity.
     * @return - The directory being modified
     */
    public File getDirectory(){
        return directory;
    }
}
