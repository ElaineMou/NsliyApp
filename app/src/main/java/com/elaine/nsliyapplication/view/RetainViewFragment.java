package com.elaine.nsliyapplication.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.elaine.nsliyapplication.ViewActivity;

/**
 * Fragment that retains memory cache during configuration changes, activity stops, etc.
 * Created by Elaine on 12/28/2014.
 */
public class RetainViewFragment extends Fragment {
    /**
     * Tag for logging.
     */
    private static final String TAG = "RetainViewFragment";
    /**
     * Memory cache to be preserved.
     */
    public BitmapLruCache memoryCache;

    public RetainViewFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Retrieves the last RetainViewFragment from the manager, creates one and adds it if it doesn't exist.
     * @param fm - FragmentManager to check for previous fragments, or to add new ones to
     * @return - Fragment either retrieved or created
     */
    public static RetainViewFragment findOrCreateRetainFragment(FragmentManager fm){
        RetainViewFragment fragment = (RetainViewFragment) fm.findFragmentByTag(TAG);
        // If no previous RetainViewFragment saved, create one and add it.
        if(fragment == null){
            fragment = new RetainViewFragment();
            fm.beginTransaction().add(fragment,TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(getActivity() instanceof ViewActivity){
            // Clear disk cache when this fragment is ultimately destroyed.
            DiskLruImageCache diskCache = ((ViewActivity) getActivity()).getDiskCache();
            if(diskCache!=null){
                diskCache.clearCache();
            }
        }
    }
}
