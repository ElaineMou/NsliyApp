package com.elaine.nsliyapplication;

import android.app.Activity;
import android.os.Bundle;

import java.io.File;

/**
 * Activity allows review of words by step-by-step process.
 * Created by Elaine on 1/14/2015.
 */
public class ReviewWordActivity extends Activity {

    /**
     * Extras key for file to review.
     */
    public static final String EXTRAS_KEY_WORD_FILE = "wordFile";

    private File wordFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        wordFile = (File) getIntent().getSerializableExtra(EXTRAS_KEY_WORD_FILE);

    }
}
