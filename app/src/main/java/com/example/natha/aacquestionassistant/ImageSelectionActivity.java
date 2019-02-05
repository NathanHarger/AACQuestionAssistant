package com.example.natha.aacquestionassistant;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.res.AssetManager.ACCESS_STREAMING;
import static android.view.View.GONE;

public class ImageSelectionActivity extends AppCompatActivity {
    final boolean BUILD_FROM_FILE = true;
    List<Card> images = new LinkedList<>();
    ImageSelectionRecyclerViewAdapter adapter;
    ImageDatabaseHelper idh;
    RecyclerView rv;
    ImageSelectionNewVocabDialogFragment imageSelectionNewVocabDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_selection_layout);
         rv = findViewById(R.id.imageSelectionGrid);

        Parcelable p = null;
        ArrayList al = null;
        if (savedInstanceState != null) {
            p = savedInstanceState.getParcelable("listState");
            al = savedInstanceState.getParcelableArrayList("dataset");
        }



        int orientation = rv.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv.setLayoutManager(new GridLayoutManager(rv.getContext(), 3));

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rv.setLayoutManager(new GridLayoutManager(rv.getContext(), 4));
        }


        idh = ImageDatabaseHelper.getInstance(ImageSelectionActivity.this);


        adapter = new ImageSelectionRecyclerViewAdapter(images, new CustomItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                Log.d("adf", "clicked position:" + position);
                // do what ever you want to do with it

                submit_photo(position);

            }
        },rv

        );


        if (al != null && p != null) {
            adapter.submitList(al);
            adapter.notifyDataSetChanged();
        }

        rv.setAdapter(adapter);


        EditText editText = findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }



            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().replaceAll(" ", "_");

                //ensure the grid is visibleh
                findViewById(R.id.imageSelectionGrid).setVisibility(View.VISIBLE );

                if ( (s.length() < 3 && s.length() != 1)) {


                    findViewById(R.id.imageSelectionGrid).setVisibility(View.INVISIBLE );

                    return;
                }


                Log.d("Image Selection: ", "field changed");
                List<Card> r = new LinkedList<>();
                idh.searchImages(searchQuery, r);//, preFetch);


               // findViewById(R.id.imageSelectionGrid).setVisibility(searchQuery.length() == 0? View.GONE : View.VISIBLE);
                if(r.size() ==0 && s.length() >=3){
                    if(imageSelectionNewVocabDialogFragment == null) {
                        imageSelectionNewVocabDialogFragment = new ImageSelectionNewVocabDialogFragment();
                        imageSelectionNewVocabDialogFragment.show(getSupportFragmentManager(), "new");
                    }
                }
                //findViewById(R.id.newWordPrompt).setVisibility(r.size() ==0 && s.length() >=3 ? View.VISIBLE : View.GONE);


                adapter.submitList(r);


            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }
    DeleteNewVocabDialogFragment deleteNewVocabDialogFragment;
    Card deletedCard= null;
public void deleteCustomVocab(View v){

    // only perform the following when the x is clicked v.id is x's id :)

    int id = v.getId();
    if (id == R.id.imageSelectionDelete ){
        CardView rv = (((CardView)((ConstraintLayout)v.getParent()).getParent()));
         deletedCard = adapter.getCardFromCardView(rv);

        deleteNewVocabDialogFragment = new DeleteNewVocabDialogFragment();
        deleteNewVocabDialogFragment.show(getSupportFragmentManager(),"delete");

    } else if(id == R.id.delete_new_vocab_no){
        deleteNewVocabDialogFragment.dismiss();
    } else{
        Log.d("Delete: ", ""+deletedCard.id);
        idh.deleteCustomVocab(deletedCard.id);
        FileOperations.deleteCustomVocab(deletedCard.photoId,v.getContext());
        deleteNewVocabDialogFragment.dismiss();

        adapter.remove(deletedCard);

    }

}
    public void submit_photo(int position) {
        Card curr = adapter.getItem(position);
        String i = curr.label;

        Intent output = new Intent();
        output.putExtra("name", i);
        output.putExtra("filename", curr.photoId);
        output.putExtra("resourceLocation", curr.resourceLocation);
        output.putExtra("pronunciation", curr.pronunciation);
        setResult(Activity.RESULT_OK, output);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        Parcelable listState = rv.getLayoutManager().onSaveInstanceState();
        outstate.putParcelable("listState", listState);
        outstate.putParcelableArrayList("dataset", new ArrayList<>(images));
        //mListState = adapter..onSaveInstanceState();
        super.onSaveInstanceState(outstate);
    }

    @Override
    public void onBackPressed() {
        Intent output = new Intent();

        setResult(Activity.RESULT_OK, output);
        finish();
    }

    public static int CREATE_NEW_VOCAB = 3;
    public void searchImageNewVocab(View v){
        int id = v.getId();
        if( id == R.id.search_new_vocab_yes){

            String word = ((EditText) findViewById(R.id.editText)).getText().toString();
            Intent i = new Intent(v.getContext(), NewVocabActivity.class);
            i.putExtra("word",word);
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, 0, 0);

            startActivityForResult(i, CREATE_NEW_VOCAB, options.toBundle());

        } else if(id == R.id.search_new_vocab_no){
            imageSelectionNewVocabDialogFragment.dismiss();
            imageSelectionNewVocabDialogFragment = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == CREATE_NEW_VOCAB) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra("name")) {
                    int id = data.getIntExtra("id",-1);
                    String returnValue = data.getStringExtra("name");
                    String returnImage = data.getStringExtra("filename");
                    int resourceLocation = data.getIntExtra("resourceLocation", 0);
                    String pronunciation = data.getStringExtra("pronunciation");

                    String label = returnValue.replace("_", " ");
                    label = label.replaceAll("[0-9]", "");

                    Intent output = new Intent();
                    output.putExtra("id",id);
                    output.putExtra("name", label);
                    output.putExtra("filename", returnImage);
                    output.putExtra("resourceLocation", resourceLocation);
                    output.putExtra("pronunciation", pronunciation);

                    setResult(Activity.RESULT_OK, output);
                    finish();

                }
            }
        }
    }

}
