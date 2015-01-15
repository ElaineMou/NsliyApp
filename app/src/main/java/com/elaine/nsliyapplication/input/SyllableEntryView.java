package com.elaine.nsliyapplication.input;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RadioGroup;

import com.elaine.nsliyapplication.EditDrawActivity;
import com.elaine.nsliyapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * View for user to input and add pronunciations to the character being drawn.
 * @author Elaine Mou
 */
public class SyllableEntryView extends FrameLayout implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    /**
     * Maximum number of pronunciations a single character can have saved.
     */
    public static final int MAX_NUM_PRONUNCIATIONS = 6;
    /**
     * Name of file to hold pronunciations
     */
    public static final String SYLLABLE_FILE_NAME = "pronunciations.txt";
    /**
     * Separator between syllable and tone in text file.
     */
    public static final String SYLLABLE_TONE_SEPARATOR = ",";
    /**
     * Separator between pronunciations in text file.
     */
    public static final String PRONUNCIATION_SEPARATOR = ";";
    /**
     * List of pronunciations currently added by user.
     */
    private ArrayList<Pronunciation> pronunciations = new ArrayList<Pronunciation>();

    public SyllableEntryView(Context context) {
        super(context);
        initView();
    }

    public SyllableEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SyllableEntryView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        initView();
    }

    /**
     * Initializes view contents.
     */
    private void initView(){
        View view = inflate(getContext(), R.layout.view_data_entry,null);
        addView(view);

        // Initializes AutoCompleteTextView to enter syllables into
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, Pronunciation.SYLLABLES);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                view.findViewById(R.id.pronounce_field);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnItemClickListener(this);

        // Initialize radio group to Unknown tone
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.tone_group);
        radioGroup.check(R.id.unknown_radio);

        // Ready add button
        Button button = (Button) view.findViewById(R.id.add_button);
        button.setOnClickListener(this);

        // Set adapter to GridView of PronunciationViews
        Pronunciation.PronunciationAdapter pronunciationAdapter =
                new Pronunciation.PronunciationAdapter(getContext(),pronunciations);
        GridView pronunciationsLayout = (GridView) view.findViewById(R.id.pronunciation_series);
        pronunciationsLayout.setAdapter(pronunciationAdapter);
    }

    @Override
    protected void onAttachedToWindow(){
        if(getContext() instanceof EditDrawActivity){
            loadFromDirectory(((EditDrawActivity) getContext()).getDirectory());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(getFocusedChild()!=null) {
            // Close keyboard upon selecting a syllable
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getFocusedChild().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch(viewId){
            // If Add button is chosen
            case R.id.add_button:
                // If room to add more
                if(pronunciations.size() < MAX_NUM_PRONUNCIATIONS) {
                    AutoCompleteTextView autoCompleteTextView =
                            (AutoCompleteTextView) findViewById(R.id.pronounce_field);
                    String syllable = autoCompleteTextView.getText().toString().replaceAll
                            ("[^a-zA-Z]", "").toLowerCase();
                    if(!syllable.isEmpty()) {
                        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.tone_group);

                        int checkedRadioId = radioGroup.getCheckedRadioButtonId();
                        Pronunciation.Tone tone = Pronunciation.Tone.UNKNOWN;
                        switch (checkedRadioId) {
                            case R.id.first_radio:
                                tone = Pronunciation.Tone.FIRST;
                                break;
                            case R.id.second_radio:
                                tone = Pronunciation.Tone.SECOND;
                                break;
                            case R.id.third_radio:
                                tone = Pronunciation.Tone.THIRD;
                                break;
                            case R.id.fourth_radio:
                                tone = Pronunciation.Tone.FOURTH;
                                break;
                            case R.id.neutral_radio:
                                tone = Pronunciation.Tone.NEUTRAL;
                                break;
                            case R.id.unknown_radio:
                                tone = Pronunciation.Tone.UNKNOWN;
                                break;
                        }

                        // Add new PronunciationView to grid using input data
                        Pronunciation pronunciation =
                                new Pronunciation(syllable, tone);
                        if(!pronunciations.contains(pronunciation)) {
                            pronunciations.add(pronunciation);
                        }

                        GridView gridView = (GridView) findViewById(R.id.pronunciation_series);
                        ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    }
                }
                break;
        }
    }

    /**
     * Save syllables into a file in the given file directory
     * @param directory - File directory to save entries in.
     */
    public void saveSyllables(File directory){
        if(directory.exists()){
            boolean success = true;
            StringBuilder strBuilder = new StringBuilder();
            // Build string of separated pronunciations
            for(Pronunciation pronunciation: pronunciations){
                strBuilder.append(pronunciation.syllable).append(SYLLABLE_TONE_SEPARATOR).
                        append(pronunciation.tone.toString()).append(PRONUNCIATION_SEPARATOR);
            }
            // Create new unique text file and write built string to it
            File textFile = new File(directory, SYLLABLE_FILE_NAME);
            if (textFile.exists()) {
                textFile.delete();
            }
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(textFile);
                outputStream.write(strBuilder.toString().getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            if(!success){
                textFile.delete();
            }
        }
    }

    public void loadFromDirectory(File directory) {
        pronunciations = Pronunciation.getListFromDirectory(directory);
        Pronunciation.PronunciationAdapter pronunciationAdapter =
                new Pronunciation.PronunciationAdapter(getContext(),pronunciations);

        GridView pronunciationsLayout = (GridView) findViewById(R.id.pronunciation_series);
        pronunciationsLayout.setAdapter(pronunciationAdapter);
    }
}
