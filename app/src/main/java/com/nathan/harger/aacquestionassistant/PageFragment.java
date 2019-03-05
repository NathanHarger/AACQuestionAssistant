package com.nathan.harger.aacquestionassistant;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class PageFragment extends Fragment {

    private static final String ARG_PAGE = "ARG_PAGE";
    protected int mPage;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }


}

