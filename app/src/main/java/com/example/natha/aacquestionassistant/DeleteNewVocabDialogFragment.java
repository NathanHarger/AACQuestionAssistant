package com.example.natha.aacquestionassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

public class DeleteNewVocabDialogFragment extends DialogFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.delete_new_vocab_dialog,container,false);
        return v;
    }
}
