package com.elaine.nsliyapplication.drawing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.elaine.nsliyapplication.Pronunciation;
import com.elaine.nsliyapplication.R;

/**
 * @author Elaine Mou
 */
public class DataEntryView extends FrameLayout implements AdapterView.OnItemClickListener {

    public DataEntryView(Context context) {
        super(context);
        initView();
    }

    public DataEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DataEntryView(Context context, AttributeSet attrs, int defStyle){
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(getFocusedChild()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getFocusedChild().getWindowToken(), 0);
        }
    }
}
