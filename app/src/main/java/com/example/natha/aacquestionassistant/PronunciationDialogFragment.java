package com.example.natha.aacquestionassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class PronunciationDialogFragment extends DialogFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        Bundle b = getArguments();
        String vocab = b.getString("vocab");

        View v = inflater.inflate(R.layout.pronunciation_layout, container, false);
        EditText editText = v.findViewById(R.id.pronunciation);

        editText.setText(vocab);


         return v;
    }




}
