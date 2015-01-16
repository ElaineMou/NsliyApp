package com.elaine.nsliyapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elaine.nsliyapplication.input.Pronunciation;
import com.elaine.nsliyapplication.input.SyllableEntryView;
import com.elaine.nsliyapplication.words.ReviewAdapter;
import com.elaine.nsliyapplication.words.ReviewCharView;
import com.elaine.nsliyapplication.words.WordAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity allows review of words by step-by-step process.
 * Created by Elaine on 1/14/2015.
 */
public class ReviewWordActivity extends Activity {

    /**
     * Extras key for file to review.
     */
    public static final String EXTRAS_KEY_WORD_FILE = "wordFile";
    private float scale;

    private File wordFile = null;
    ArrayList<File> characterFolders = new ArrayList<File>();
    ArrayList<Pronunciation> pronunciations = new ArrayList<Pronunciation>();
    String meaning;

    private ReviewAdapter reviewAdapter = null;
    private GridView gridView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_word);

        scale = getResources().getDisplayMetrics().density;

        wordFile = (File) getIntent().getSerializableExtra(EXTRAS_KEY_WORD_FILE);
        loadFromFile();

        Button button = (Button) findViewById(R.id.back_back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewind();
            }
        });
        button = (Button) findViewById(R.id.back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
            }
        });
        button = (Button) findViewById(R.id.forward);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
        button = (Button) findViewById(R.id.forward_forward);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fastForward();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        gridView = (GridView) findViewById(R.id.chars_grid);
        reviewAdapter = new ReviewAdapter(this,characterFolders,pronunciations);
        gridView.setAdapter(reviewAdapter);
    }

    private void rewind(){
    }

    private void previous(){
        ((ReviewCharView)gridView.getChildAt(0).findViewById(R.id.review_char_view)).removeStroke();
    }

    private void next(){
        ((ReviewCharView)gridView.getChildAt(0).findViewById(R.id.review_char_view)).addStroke();
    }

    private void fastForward(){

    }

    protected void loadFromFile(){
        if(wordFile.exists()) {
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(wordFile));
                stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();

                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (stringBuilder != null) {
                File picturesDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                JSONObject jsonObject = null;
                JSONArray jsonChars = null;
                JSONArray jsonPronunciations = null;
                String jsonMeaning = null;

                try {
                    jsonObject = new JSONObject(stringBuilder.toString());
                    jsonChars = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_CHARACTERS);
                    jsonPronunciations = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_PRONUNCIATIONS);
                    jsonMeaning = jsonObject.getString(CreateWordActivity.JSON_KEY_MEANING);
                } catch (JSONException e){
                    e.printStackTrace();
                }
                if(jsonChars !=null) {
                    int length = jsonChars.length();
                    for (int i = 0; i < length; i++) {
                        String characterFolder = "";
                        try {
                            characterFolder = jsonChars.getString(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        File directory = new File(picturesDirectory, characterFolder);
                        if (directory != null && directory.exists()) {
                            characterFolders.add(directory);
                        } else {
                            characterFolders.add(null);
                        }
                    }
                }
                if(jsonPronunciations !=null){
                    int length = jsonPronunciations.length();
                    for(int i=0;i<length;i++) {
                        String pronunciation = null;
                        try {
                            pronunciation = jsonPronunciations.getString(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String[] syllableTone = pronunciation.split(SyllableEntryView.SYLLABLE_TONE_SEPARATOR);
                        if(syllableTone.length == 2) {
                            Pronunciation.Tone tone = Pronunciation.Tone.UNKNOWN;
                            for (Pronunciation.Tone value : Pronunciation.Tone.values()) {
                                if (value.toString().equals(syllableTone[1])) {
                                    tone = value;
                                }
                            }
                            pronunciations.add(new Pronunciation(syllableTone[0], tone));
                        }
                    }
                }
                if(jsonMeaning !=null && !jsonMeaning.isEmpty()){
                    meaning = jsonMeaning;
                    TextView textView = (TextView) findViewById(R.id.meaning_text_view);
                    textView.setText(meaning);
                }
            }
        }
    }
}
