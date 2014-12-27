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

import com.elaine.nsliyapplication.R;

import java.util.ArrayList;

/**
 * @author Elaine Mou
 */
public class SyllableEntryView extends FrameLayout implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    private static final int MAX_NUM_PRONUNCIATIONS = 6;
    ArrayList<Pronunciation> pronunciations = new ArrayList<Pronunciation>();

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

    private void initView(){
        View view = inflate(getContext(), R.layout.view_data_entry,null);
        addView(view);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, Pronunciation.SYLLABLES);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                view.findViewById(R.id.pronounce_field);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnItemClickListener(this);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.tone_group);
        radioGroup.check(R.id.unknown_radio);

        Button button = (Button) view.findViewById(R.id.add_button);
        button.setOnClickListener(this);

        Pronunciation.PronunciationAdapter pronunciationAdapter =
                new Pronunciation.PronunciationAdapter(getContext(),pronunciations);
        GridView pronunciationsLayout = (GridView) view.findViewById(R.id.pronunciation_series);
        pronunciationsLayout.setAdapter(pronunciationAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(getFocusedChild()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getFocusedChild().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch(viewId){
            case R.id.add_button:
                if(pronunciations.size() < MAX_NUM_PRONUNCIATIONS) {
                    AutoCompleteTextView autoCompleteTextView =
                            (AutoCompleteTextView) findViewById(R.id.pronounce_field);
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

                    Pronunciation pronunciation =
                            new Pronunciation(autoCompleteTextView.getText().toString(), tone);
                    pronunciations.add(pronunciation);

                    GridView gridView = (GridView) findViewById(R.id.pronunciation_series);
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                }
                break;
        }
    }
}
