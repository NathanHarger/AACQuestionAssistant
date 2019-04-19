package com.nathan.harger.aacquestionassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

public class YesNoPageFragment extends PageFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.yes_no_layout, container, false);
        CardView yes = v.findViewById(R.id.cv);
        yes.setBackgroundColor(getResources().getColor(R.color.green));
        CardView no = v.findViewById(R.id.cv1);
        no.setBackgroundColor(getResources().getColor(R.color.red));
        return v;
    }


}


