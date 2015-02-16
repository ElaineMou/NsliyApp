package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.input.Pronunciation;
import com.elaine.nsliyapplication.input.SyllableSoundTask;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

/**
 * Adapter supplies ReviewCharViews to GridView
 * Created by Elaine on 1/15/2015.
 */
public class ReviewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<File> charFolders;
    private ArrayList<Pronunciation> pronunciations;
    private ArrayList<ViewHolder> holders;
    private float scale;

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
        final View view;
        ViewHolder holder = new ViewHolder();

        if(convertView==null) {
            Log.v("ReviewAdapter","Get view #" + position + ", convertView is null");
            view = inflater.inflate(R.layout.view_review_word, parent, false);

            holder.reviewCharView = (ReviewCharView) view.findViewById(R.id.review_char_view);
            holder.reviewCharView.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (scale * ReviewCharView.viewSize), (int) (scale * ReviewCharView.viewSize)));
            holder.reviewCharView.init(charFolders.get(position));

            holder.textView = (TextView) view.findViewById(R.id.pronunciation_text_view);
            holder.textView.setGravity(Gravity.CENTER);

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

            view.setTag(holder);
        } else {
            Log.v("ReviewAdapter","Get view #" + position + ", convertView used");
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        final ViewHolder holderToAdd = holder;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Build.VERSION.SDK_INT < 16){
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                holderToAdd.reviewCharView.makeCanvas();
                try {
                    holderToAdd.reviewCharView.loadValuesFromFile();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                holders.set(position, holderToAdd);
                Log.v("ReviewAdapter","Added holder to holders at " + position);
            }
        });

        holder.textView.setText(pronunciations.get(position).syllable + " " + pronunciations.get(position).tone);
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
