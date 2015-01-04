package com.elaine.nsliyapplication.input;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Plays pronunciation sounds from file in raw folder when executed with String parameters.
 * Execute takes two strings - the syllable, and the tone, which must be 1-4 or else is neutral.
 * Created by Elaine on 1/4/2015.
 */
class SyllableSoundTask extends AsyncTask<String,Void,Boolean> {
    MediaPlayer mediaPlayer;
    Context context;

    public SyllableSoundTask(Context context){
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean success = false;
        // Check that syllable and tone were provided
        if(params.length >= 2) {
            String syllable = params[0];
            String tone = params[1];

            int resId = 0;

            // Only append if numbered tone
            if(tone.matches("[1-4]")){
                syllable += tone;
                resId = context.getResources().getIdentifier(syllable, "raw", context.getPackageName());
            } else if (tone.equals(Pronunciation.Tone.UNKNOWN.toString())){
                // If unknown look for first sound file to play to user
                for(int i=1;i<=4 && resId == 0;i++){
                    resId = context.getResources().getIdentifier(syllable + i, "raw", context.getPackageName());
                }
            }
            if (resId != 0) {
                // Play file if it exists
                mediaPlayer = MediaPlayer.create(context, resId);
                mediaPlayer.start();
                while(mediaPlayer.isPlaying());
                success = true;
            }
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(mediaPlayer!=null) {
            // Release resources
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(!result){
            // Notify user if no sound played
            Toast.makeText(context,"No available sound clip", Toast.LENGTH_SHORT).show();
        }
    }
}