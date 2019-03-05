package com.nathan.harger.aacquestionassistant;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import androidx.recyclerview.widget.GridLayoutManager;

public class OnlineImageSelectionActivity extends ImageSelectionActivity {
    protected ImageSelectionRecyclerViewAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_image_selection_layout);
        rv = findViewById(R.id.onlineImageSelectionGrid);
        Parcelable p = null;
        ArrayList al = null;
        if (savedInstanceState != null) {
            p = savedInstanceState.getParcelable("listState");
            al = savedInstanceState.getParcelableArrayList("dataset");
        }

        String search = getIntent().getExtras().getString("search");

        int orientation = rv.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv.setLayoutManager(new GridLayoutManager(rv.getContext(), 3));
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rv.setLayoutManager(new GridLayoutManager(rv.getContext(), 4));
        }

        idh = ImageDatabaseHelper.getInstance(OnlineImageSelectionActivity.this);

        adapter = new ImageSelectionRecyclerViewAdapter(null, new CustomItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                Log.d("adf", "clicked position:" + position);
                submit_photo(position);

            }
        }, rv
        );

        if (al != null && p != null) {
            adapter.submitList(al);
            //adapter.notifyDataSetChanged();
        }
        rv.setAdapter(adapter);


        getImages(search);
    }

    protected void submit_photo(int position) {
        Card curr = adapter.getItem(position);
        String i = curr.label;
        Intent output = new Intent();
        output.putExtra("url", ((OnlineImageCard) curr).url);
        setResult(Activity.RESULT_OK, output);
        finish();
    }

    private void getImages(final String search) {
        if (search.equals("")) {
            return;
        }
        GetWikiImagesTask getWikiImagesTask = new GetWikiImagesTask(adapter, search);
        getWikiImagesTask.execute();
    }
}
