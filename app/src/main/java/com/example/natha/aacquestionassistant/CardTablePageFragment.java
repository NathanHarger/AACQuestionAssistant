package com.example.natha.aacquestionassistant;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import static android.content.res.AssetManager.ACCESS_STREAMING;
import static com.example.natha.aacquestionassistant.TextToSpeechManager.tts;

public class CardTablePageFragment extends Fragment {
    static final int SELECT_IMAGE_REQUEST = 1;
    static final int NEW_VOCAB_REQUEST = 1;
    List<Card> cards = new LinkedList<>();
    CardRecyclerViewAdapter adapter;
    int clickedCardIndex = 0;
    Parcelable recyclerViewState;
    private ItemTouchHelper itemTouchHelper;
    private boolean locked = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parcelable p = null;
        ArrayList al = null;
        if (savedInstanceState != null) {
            p = savedInstanceState.getParcelable("listState");
            al = savedInstanceState.getParcelableArrayList("dataset");
        }

        final View view = inflater.inflate(R.layout.activity_main, container, false);
        final RecyclerView rv = view.findViewById(R.id.cardGrid);

        //Move this up to main actvity
        ImageDatabaseHelper.getInstance(this.getContext());

        adapter = new CardRecyclerViewAdapter(cards, new CustomItemClickListener() {


            @Override
            public void onItemClick(View v, int position) {
                if (v.getTag().equals("edit") && !locked) {

                    clickedCardIndex = position;
                    Intent i = new Intent(v.getContext(), ImageSelectionActivity.class);
                    ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, 0, 0);
                    startActivityForResult(i, SELECT_IMAGE_REQUEST, options.toBundle());

                }else if(v.getTag().equals("new") && !locked){
                    clickedCardIndex = position;
                    Intent i = new Intent(v.getContext(), NewVocabActivity.class);
                    ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, 0, 0);
                    startActivityForResult(i, NEW_VOCAB_REQUEST, options.toBundle());
                } else {
                    Card c = cards.get(position);
                    long keyLastClick = adapter.getSelection();

                    // deselet and stop drag of prev
                    if (keyLastClick != 0L) {
                        adapter.setSelected(keyLastClick, false);
                        adapter.stopDrag(keyLastClick);

                        // if clicked on same clear and return
                        if (keyLastClick == c.key && !locked) {
                            return;
                        }
                    }


                    adapter.setSelected(c.key, true);
                    Log.d("adf", "clicked position:" + position);
                    if (v.getTag().equals("cv") && !c.label.equals("") && locked) {
                        if (c.pronunciation.length() == 0) {
                            TextToSpeechManager.speak(c.label);
                        } else {
                            TextToSpeechManager.speak(c.pronunciation);

                        }
                    }

                 else if (locked) {
                    Toast.makeText(v.getContext(), "Card Edit Locked Out", Toast.LENGTH_SHORT).show();
                }
            }
            }


        }, rv, getContext());

        final ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter, this.getContext());

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);

        if (al != null && p != null) {
            adapter.setList(al.listIterator());
            adapter.notifyDataSetChanged();
        }

        int orientation = getActivity().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rv.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        }

        TextToSpeechManager.initTextToSpeech(getContext());

        rv.setAdapter(adapter);

        touchHelper.attachToRecyclerView(rv);
        adapter.setTouchHelper(touchHelper);
        adapter.setTouchHelperCallback((SimpleItemTouchHelperCallback) callback);

        BottomappbarCallbackInterface bottomappbarCallbackInterface = new BottomappbarCallbackInterface() {
            @Override
            public void addButtonClick() {


                if(!locked){
                    adapter.addItem(new Card("",""));
                } else{
                    Toast.makeText(getContext(), "Add Card Locked Out", Toast.LENGTH_SHORT).show();

                }
            }

            public void toggleUiLockClick(){
                locked = !locked;
                adapter.setLocked(locked);

                if(locked)
                    adapter.clearSelection();
                Log.i("Bottom bar navigation:","" + (locked?"locked":"unlocked" ));
            }

            public void menuClick(int id){
                if(!locked) {
                    adapter.menuClick(id);
                } else{
                    Toast.makeText(getContext(), "Locked Out", Toast.LENGTH_SHORT).show();
                }

            }
        };




        ((CardFragmentActivity)getActivity()).setBottomappbarCallbackInterface(bottomappbarCallbackInterface);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        Parcelable listState = adapter.getLayoutManager().onSaveInstanceState();
        outstate.putParcelable("listState", listState);
        outstate.putParcelableArrayList("dataset", new ArrayList<>(cards));
        //mListState = adapter..onSaveInstanceState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE_REQUEST || requestCode == NEW_VOCAB_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra("name")) {
                    int id = data.getIntExtra("id", -1);
                    String returnValue = data.getStringExtra("name");
                    String returnImage = data.getStringExtra("filename");
                    int resourceLocation = data.getIntExtra("resourceLocation",0);
                    String pronunciation = data.getStringExtra("pronunciation");
                    adapter.setItemTag(clickedCardIndex, "cv");

                    String label = returnValue.replace("_", " ");
                    label = label.replaceAll("[0-9]", "");

                    adapter.updateItem(clickedCardIndex, label, returnImage, resourceLocation,pronunciation,id);
                } else {
                    adapter.setItemTag(clickedCardIndex, "cv");
                }

                if(data.hasExtra("deletedList")){
                    ArrayList<String> deletedVocab = (ArrayList<String>) data.getSerializableExtra("deletedList");
                    adapter.deleteInvalidVocab(deletedVocab);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }


}