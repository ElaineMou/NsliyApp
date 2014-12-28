package com.elaine.nsliyapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.SyllableEntryView;
import com.elaine.nsliyapplication.view.ImageAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by Elaine on 12/27/2014.
 */
public class ViewActivity extends Activity {

    public static final int VIEW_IMAGE_SIZE = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        GridView gridView = (GridView) findViewById(R.id.view_grid);
        ArrayList<File> files = new ArrayList<File>();

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] filesList = dir.listFiles();
        for (File file : filesList) {
            if (file.isDirectory()) {
                File bitmapFile = new File(file,DrawView.DISPLAY_IMAGE_NAME);
                if(bitmapFile.exists()){
                    files.add(bitmapFile);
                }
            }
        }

        ImageAdapter imageAdapter = new ImageAdapter(this,files);
        gridView.setAdapter(imageAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(this,DrawActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
