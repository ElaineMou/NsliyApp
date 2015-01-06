package com.elaine.nsliyapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Main menu screen of app.
 * Created by Elaine on 12/25/2014.
 */
public class MainMenuActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button button = (Button) findViewById(R.id.view_button);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.words_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.view_button:
                Intent intent = new Intent(this, ViewActivity.class);
                startActivity(intent);
                break;
            case R.id.words_button:
                break;
        }
    }
}
