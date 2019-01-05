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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class CardTablePageFragment extends Fragment {
    static final int SELECT_IMAGE_REQUEST = 1;
    List<Card> cards = new LinkedList<Card>();
    CardRecyclerViewAdapter adapter;
    int clickedCardIndex = 0;
    Parcelable recyclerViewState;
    private SelectionTracker mSelectionTracker;
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
        final RecyclerView rv = (RecyclerView) view.findViewById(R.id.cardGrid);

        //Move this up to main actvity
        ImageDatabaseHelper.getInstance(this.getContext());

        adapter = new CardRecyclerViewAdapter(cards, new CustomItemClickListener() {


            @Override
            public void onItemClick(View v, int position) {
                Card c = cards.get(position);
                mSelectionTracker.select(c.key);

                Log.d("adf", "clicked position:" + position);
                if (v.getTag().equals("cv")) {
                    if(!c.label.equals("")) {
                       // mSelectionTracker.select(c.key);
                        TextToSpeechManager.speak(c.label);
                    }
                } else if (!locked) {

                    clickedCardIndex = position;
                    Intent i = new Intent(v.getContext(), ImageSelectionActivity.class);
                    ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, 0, 0);
                    startActivityForResult(i, SELECT_IMAGE_REQUEST, options.toBundle());
                    adapter.stopActionMode();


                } else {
                    Toast.makeText(v.getContext(), "Card Edit Locked Out", Toast.LENGTH_SHORT).show();
                }
            }

        }, rv);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter, this.getContext());

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

        TextToSpeechManager.initTextToSpeech(view.getContext());

        rv.setAdapter(adapter);


        mSelectionTracker = new SelectionTracker.Builder<>(
                "string-items-selection", rv, new CardItemKeyProvider(1, cards), new CardItemDetailsLookup(rv), StorageStrategy.createLongStorage()).withSelectionPredicate(SelectionPredicates.<Long>createSelectSingleAnything()).build();

        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onItemStateChanged(@NonNull Object key, boolean selected) {

                super.onItemStateChanged(key, selected);
                Log.i("ItemStateChanged", "" + key + " to " + selected);

                adapter.setSelected((long) key, selected);
                if(!adapter.actionModeActive()){


                    return;
                }

                if(selected){
                    adapter.startDrag((long)key);

                } else {
                    adapter.stopDrag((long) key);

                }


            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
                Log.i("Selection: ", "onSelectionRefresh()");

            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                Log.i("Selection: ", "onSelectionChanged()");

            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
            }
        });

        adapter.setSelectionTracker(mSelectionTracker);

        touchHelper.attachToRecyclerView(rv);
        adapter.setTouchHelper(touchHelper);
        adapter.setTouchHelperCallback((SimpleItemTouchHelperCallback) callback);
        ((CardFragmentActivity)getActivity()).setBottomappbarCallbackInterface(new BottomappbarCallbackInterface() {
            @Override
            public void addButtonClick() {


                if(!locked && !adapter.actionModeActive()){
                    adapter.addItem(new Card("",""));
                } else{
                    Toast.makeText(getContext(), "Add Card Locked Out", Toast.LENGTH_SHORT).show();

                }
            }

            public void toggleUiLockClick(){
                locked = !locked;
                adapter.setLocked(locked);
                Log.i("Bottom bar navigation:","" + (locked?"locked":"unlocked" ));
            }
        });

        if (cards.size() == 0) {

            adapter.addItem(new Card("b", "b"));
            adapter.addItem(new Card("c", "c"));
            adapter.addItem(new Card("a", "a"));
            adapter.addItem(new Card("d", "d"));
        }
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
        if (requestCode == SELECT_IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra("name")) {
                    String returnValue = data.getStringExtra("name");
                    String returnImage = data.getStringExtra("filename");
                    if (returnImage.equals("") || returnValue.equals("")) {
                        adapter.setItemTag(clickedCardIndex, "cv");
                        return;
                    }
                    String label = returnValue.replace("_", " ");
                    label = label.replaceAll("[0-9]", "");

                    adapter.updateItem(clickedCardIndex, label, returnImage);
                } else {
                    adapter.setItemTag(clickedCardIndex, "cv");
                }
            }
        }
    }
}

