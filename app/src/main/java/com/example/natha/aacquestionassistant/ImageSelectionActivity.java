package com.example.natha.aacquestionassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.res.AssetManager.ACCESS_STREAMING;

public class ImageSelectionActivity extends AppCompatActivity {
    final boolean BUILD_FROM_FILE = true;
    List<Card> images = new LinkedList<>();
    ImageSelectionRecyclerViewAdapter adapter;
    ImageDatabaseHelper idh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_selection_layout);

        idh = ImageDatabaseHelper.getInstance(ImageSelectionActivity.this);

        RecyclerView rv = findViewById(R.id.imageSelectionGrid);

        adapter = new ImageSelectionRecyclerViewAdapter(images, new CustomItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                Log.d("adf", "clicked position:" + position);
                // do what ever you want to do with it

                submit_photo(position);

            }
        }
        );

        GridLayoutManager glm = new GridLayoutManager(getApplicationContext(), 4);
        rv.setLayoutManager(glm);
        rv.setAdapter(adapter);


        EditText editText = findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.equals("") || (count < 3 && count != 1)) {
                    return;
                }
                String searchQuery = s.toString().replaceAll(" ", "_");
                Log.d("Image Selection: ", "field changed");
                List<Card> r = new LinkedList<>();
                idh.searchImages(searchQuery, r);//, preFetch);

                adapter.submitList(r);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        if (BUILD_FROM_FILE && idh.getSize() == 0) {
            setupDB();
        }
    }

    public void setupDB() {
        String line = "";
        String split = ",";
        BufferedReader b = null;
        try {
            // read in core vocab
            InputStream s = getAssets().open("symbol-info.csv", ACCESS_STREAMING);
            b = new BufferedReader(new InputStreamReader(s));

            // read header
            b.readLine();
            while ((line = b.readLine()) != null) {
                idh.addImage(new Card(line.split(split)));
            }

            // read in custom vocab
            FileOperations.readNewVocab(getApplicationContext() , idh);
        } catch (FileNotFoundException e) {
            Log.e("CSV parsing: ", String.valueOf(e.getStackTrace()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    Log.e("CSV parsing: ", String.valueOf(e.getStackTrace()));
                }
            }
        }
    }

    public void submit_photo(int position) {
        Card curr = adapter.getItem(position);
        String i = curr.label;

        Intent output = new Intent();
        output.putExtra("name", i);
        output.putExtra("filename", i);
        output.putExtra("resourceLocation", curr.resourceLocation);
        output.putExtra("pronunciation", curr.pronunciation);
        setResult(Activity.RESULT_OK, output);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent output = new Intent();

        setResult(Activity.RESULT_OK, output);
        finish();
    }


}
