package com.elaine.nsliyapplication.drawing;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.elaine.nsliyapplication.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DrawFragment extends Fragment {


    public DrawFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_draw, container, false);
        return rootView;
    }


}
