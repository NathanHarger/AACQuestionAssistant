package com.nathan.harger.aacquestionassistant;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static com.nathan.harger.aacquestionassistant.TextToSpeechManager.tts;

public class CardTablePageFragment extends Fragment {
    private static final int SELECT_IMAGE_REQUEST = 1;
    private static final int NEW_VOCAB_REQUEST = 1;
    public static Bundle mBundleRecyclerViewState;
    private final List<Card> cards = new LinkedList<>();
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private CardRecyclerViewAdapter adapter;
    private List<Card> newList = new LinkedList<>();
    private int clickedCardIndex;
    private boolean locked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

        ImageDatabaseHelper.getInstance(this.getContext());

        adapter = new CardRecyclerViewAdapter(cards, new CustomItemClickListener() {


            @Override
            public void onItemClick(View v, int position) {
                if (v.getTag().equals("edit") && !locked) {

                    clickedCardIndex = position;
                    Intent i = new Intent(v.getContext(), ImageSelectionActivity.class);
                    ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, 0, 0);
                    startActivityForResult(i, SELECT_IMAGE_REQUEST, options.toBundle());

                } else if (v.getTag().equals("new") && !locked) {
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
                    } else if (locked) {
                        Toast.makeText(v.getContext(), "Card Edit Locked Out", Toast.LENGTH_SHORT).show();
                    }
                }
            }


        }, rv, getContext());

        final SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(adapter, this.getContext());

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
        adapter.setTouchHelperCallback(callback);

        BottomappbarCallbackInterface bottomappbarCallbackInterface = new BottomappbarCallbackInterface() {

            public void toggleUiLockClick() {
                locked = !locked;
                adapter.setLocked(locked);

                if (locked)
                    adapter.clearSelection();
                Log.i("Bottom bar navigation:", "" + (locked ? "locked" : "unlocked"));
            }

            public void menuClick(int id) {
                if (!locked) {
                    adapter.menuClick(id);
                    if (id == R.id.restore_grid) {
                        Intent i = new Intent(getContext(), RestoreCardGridActivity.class);
                        startActivityForResult(i, 10);

                    }
                } else {

                    Toast.makeText(getContext(), "Locked Out", Toast.LENGTH_SHORT).show();
                }
            }
        };


        ((CardFragmentActivity) getActivity()).setBottomappbarCallbackInterface(bottomappbarCallbackInterface);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outstate) {
        Parcelable listState = adapter.getLayoutManager().onSaveInstanceState();
        outstate.putParcelable("listState", listState);
        outstate.putParcelableArrayList("dataset", new ArrayList<>(cards));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data.hasExtra("new_list")) {
                List<Card> newList = (List<Card>) data.getSerializableExtra("list");
                this.newList = newList;
                adapter.submitList(new ArrayList<Card>(newList));

                return;
            }

            adapter.setItemTag(clickedCardIndex);
            if (data.hasExtra("name")) {
                long id = data.getLongExtra("id", -1);
                String returnValue = data.getStringExtra("name");
                String returnImage = data.getStringExtra("filename");
                int resourceLocation = data.getIntExtra("resourceLocation", 0);
                String pronunciation = data.getStringExtra("pronunciation");
                String label = returnValue.replace("_", " ");
                if (resourceLocation == 0)
                    label = label.replaceAll("[0-9]", "");
                adapter.updateItem(clickedCardIndex, label, returnImage, resourceLocation, pronunciation, id);
            }

            if (data.hasExtra("deletedList")) {
                ArrayList<Card> deletedVocab = (ArrayList<Card>) data.getSerializableExtra("deletedList");
                adapter.setInvalidVocab(deletedVocab);
            }
        }
    }

    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();

        mBundleRecyclerViewState.putParcelableArrayList(KEY_RECYCLER_STATE, new ArrayList<>(cards));

    }

    @Override
    public void onResume() {
        super.onResume();


        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            ArrayList<Card> listState = mBundleRecyclerViewState.getParcelableArrayList(KEY_RECYCLER_STATE);
            adapter.submitList(listState);

        }
        if (newList.size() != 0) {
            adapter.submitList(new ArrayList<Card>(newList));
            newList.clear();
        }
        adapter.deleteInvalidVocab();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}