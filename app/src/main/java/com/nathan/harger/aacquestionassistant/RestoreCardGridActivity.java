package com.nathan.harger.aacquestionassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RestoreCardGridActivity extends ImageSelectionActivity {

    private RestoreGridRecyclerViewAdapter adapter;

    protected void submit_photo(int position) {

        long clicked_list = adapter.getKey(position);
        List<Card> cards = idh.getCardGroup(clicked_list);
        Intent output = new Intent();
        output.putExtra("new_list", "new_list");

        output.putExtra("list", (Serializable) cards);
        setResult(Activity.RESULT_OK, output);
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_card_layout);
        rv = findViewById(R.id.restore_grid_layout);
        Parcelable p = null;
        ArrayList al = null;
        if (savedInstanceState != null) {
            p = savedInstanceState.getParcelable("listState");
            al = savedInstanceState.getParcelableArrayList("dataset");
        }


        rv.setLayoutManager(new LinearLayoutManager(rv.getContext(), RecyclerView.VERTICAL, false));


        idh = ImageDatabaseHelper.getInstance(RestoreCardGridActivity.this);

        adapter = new RestoreGridRecyclerViewAdapter(new CustomItemClickListener() {

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


        adapter.submitList(idh.getCardSets());
    }
}
