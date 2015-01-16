package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.input.Pronunciation;
import com.elaine.nsliyapplication.input.SyllableSoundTask;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Elaine on 1/15/2015.
 */
public class ReviewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<File> charFolders;
    ArrayList<Pronunciation> pronunciations;
    ArrayList<ViewHolder> holders;
    float scale;

    public ReviewAdapter(Context context, ArrayList<File> charFolders, ArrayList<Pronunciation> pronunciations){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.charFolders = charFolders;
        this.pronunciations = pronunciations;
        this.holders = new ArrayList<ViewHolder>();
        int size = charFolders.size();
        for(int i=0;i<size;i++){
            holders.add(new ViewHolder());
        }

        scale = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getCount() {
        return charFolders.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder = holders.get(position);;

        if(convertView==null) {
            view = inflater.inflate(R.layout.view_review_word, parent, false);

            holder.reviewCharView = (ReviewCharView) view.findViewById(R.id.review_char_view);
            holder.reviewCharView.init(charFolders.get(position));

            holder.textView = (TextView) view.findViewById(R.id.pronunciation_text_view);
            holder.textView.setGravity(Gravity.CENTER);
            holder.textView.setText(pronunciations.get(position).syllable + " " + pronunciations.get(position).tone);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView textView = holders.get(position).textView;
                    if (textView != null) {
                        String[] syllableTone = textView.getText().toString().split(" ");
                        new SyllableSoundTask(context).execute(syllableTone);
                    }
                }
            });

            holder.reviewCharView.setLayoutParams(new LinearLayout.LayoutParams((int) (scale * 150), (int) (scale * 150)));

            view.setTag(holder);
        } else {
            return convertView;
        }

        return view;
    }

    public ArrayList<ViewHolder> getHolders(){
        return holders;
    }

    public class ViewHolder{
        public ReviewCharView reviewCharView;
        public TextView textView;
    }
}