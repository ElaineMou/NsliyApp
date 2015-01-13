package com.elaine.nsliyapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.SyllableEntryView;

import java.io.File;

/**
 * Used to edit previously written characters.
 * Created by Elaine on 1/4/2015.
 */
public class EditDrawActivity extends Activity {

    public static final String FILE_EXTRA_NAME = "fileName";
    public static final String DIRECTORY_RETURN_EXTRA = "directory";
    File directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directory = new File(getIntent().getStringExtra(FILE_EXTRA_NAME));
        setContentView(R.layout.activity_draw);
        setResult(RESULT_CANCELED);
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
        if (id == R.id.action_revert) {
            ((DrawView) findViewById(R.id.draw_view)).loadFromDirectory(directory);
            ( (SyllableEntryView) findViewById(R.id.pronunciation_view)).loadFromDirectory(directory);
            return true;
        } else if (id == R.id.action_undo){
            ( (DrawView) findViewById(R.id.draw_view) ).undo();
        } else if (id == R.id.action_save){
            if(directory!=null){
                File save = ( (DrawView) findViewById(R.id.draw_view ) ).saveCharacter(this, directory);
                if(save!=null) {
                    ((SyllableEntryView) findViewById(R.id.pronunciation_view)).saveSyllables(directory);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(DIRECTORY_RETURN_EXTRA, directory.getName());
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public File getDirectory(){
        return directory;
    }
}
