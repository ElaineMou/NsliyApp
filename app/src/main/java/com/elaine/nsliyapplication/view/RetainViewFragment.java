package com.elaine.nsliyapplication.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.elaine.nsliyapplication.ViewActivity;

/**
 * Created by Elaine on 12/28/2014.
 */
public class RetainViewFragment extends Fragment {
    private static final String TAG = "RetainViewFragment";
    public BitmapLruCache memoryCache;

    public RetainViewFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static RetainViewFragment findOrCreateRetainFragment(FragmentManager fm){
        RetainViewFragment fragment = (RetainViewFragment) fm.findFragmentByTag(TAG);
        if(fragment == null){
            fragment = new RetainViewFragment();
            fm.beginTransaction().add(fragment,TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("RetainViewFragment","DESTROY");
        if(getActivity() instanceof ViewActivity){
            DiskLruImageCache diskCache = ((ViewActivity) getActivity()).getDiskCache();
            if(diskCache!=null){
                diskCache.clearCache();
                Log.v("RetainViewFragment","CLEAR CACHE");
            }
        }
    }
}
