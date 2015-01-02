package com.elaine.nsliyapplication.input;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.elaine.nsliyapplication.R;

/**
 * Displays a Pronunciation along with a delete button.
 * Created by Elaine on 12/26/2014.
 */
public class PronunciationView extends FrameLayout {
    public PronunciationView(Context context) {
        super(context);
        initView();
    }

    public PronunciationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PronunciationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * Inflates corresponding view into this layout.
     */
    private void initView() {
        View view = inflate(getContext(), R.layout.view_pronunciation,null);
        addView(view);
    }

    /**
     * Sets syllable portion of the pronunciation to the given string.
     * @param syllable - Syllable to be displayed.
     */
    public void setSyllable(String syllable){
        TextView textView = (TextView) findViewById(R.id.syllable);
        textView.setText(syllable);
    }

    /**
     * Sets tone of the pronunciation to the given Tone.
     * @param tone - Tone to be displayed.
     */
    public void setTone(Pronunciation.Tone tone){
        TextView textView = (TextView) findViewById(R.id.tone);
        textView.setText(tone.toString());
    }
}
