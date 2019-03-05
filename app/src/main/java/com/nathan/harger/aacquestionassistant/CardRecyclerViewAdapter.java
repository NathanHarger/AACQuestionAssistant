package com.nathan.harger.aacquestionassistant;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class CardRecyclerViewAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<CardRecyclerViewAdapter.CardViewHolder> implements ItemTouchHelperAdapter {
    private final RecyclerView rv;
    private final List<Card> cards;
    private final CustomItemClickListener listener;
    private final Context context;
    private ItemTouchHelper ith;
    private boolean locked = false;
    private View emptyView;
    private SimpleItemTouchHelperCallback simpleItemTouchHelperCallback;
    private ArrayList<Card> deletedVocab = new ArrayList<>();

    CardRecyclerViewAdapter(List<Card> cards, CustomItemClickListener listener, RecyclerView rv, Context context) {
        this.cards = cards;
        this.listener = listener;
        this.rv = rv;
        setHasStableIds(true);
        this.context = context;

        this.setEmptyView(((ConstraintLayout) rv.getParent()).findViewById(R.id.empty_view));
        RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkIfEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkIfEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkIfEmpty();
            }
        };
        registerAdapterDataObserver(observer);
    }

    public void submitList(ArrayList<Card> list) {
        //mDiffer.submitList(list);
        int size = cards.size();
        cards.clear();
        notifyItemRangeRemoved(0, size);

        cards.addAll(list);
        notifyDataSetChanged();

    }

    void menuClick(int id) {
        long key = getSelection();

        if (key != 0L) {
            stopDrag(key);

            if (id == R.id.item_delete) {
                if (cards.size() != 0) {
                    deleteSelected();
                    return;
                }

            } else if (id == R.id.item_edit) {

                startMenuProcessOnCard(key, "edit");
                return;
            }
        }


        if (id == R.id.new_item_create) {
            if (key != 0) {
                startMenuProcessOnCard(key, "new");
            } else {
                Intent createVocabIntent = new Intent(context, NewVocabActivity.class);

                context.startActivity(createVocabIntent);
            }
        } else if (id == R.id.add_card) {

            this.addItem(new Card());
        }

        // items clicked when no card selected
        if (id == R.id.item_delete) {
            Toast.makeText(context, "Select Card to Delete", Toast.LENGTH_LONG).show();
        } else if (id == R.id.item_edit) {
            Toast.makeText(context, "Select Card to Edit", Toast.LENGTH_LONG).show();

        }
    }

    private void startMenuProcessOnCard(long key, String tag) {
        Card clickedCard = getItem(key);
        int pos = cards.indexOf(clickedCard);
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForAdapterPosition(pos);
        assert v != null;
        v.cv.setTag(tag);
        listener.onItemClick(v.cv, pos);
    }

    long getSelection() {
        for (Card c : cards) {
            if (c.isSelected) {
                return c.key;
            }
        }
        return 0L;
    }

    void stopDrag(long key) {
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForItemId(key);

        simpleItemTouchHelperCallback.clearView(rv, v);
    }

    private void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    private void checkIfEmpty() {

        if (emptyView != null) {
            final boolean emptyViewVisible = getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
        }
    }

    void setTouchHelper(ItemTouchHelper ith) {
        this.ith = ith;
    }

    void setTouchHelperCallback(SimpleItemTouchHelperCallback simpleItemTouchHelperCallback) {
        this.simpleItemTouchHelperCallback = simpleItemTouchHelperCallback;
    }

    private void addItem(Card c) {
        if (!(cards.size() < 6)) {
            return;
        }
        cards.add(c);
        this.notifyDataSetChanged();
        long key = getSelection();
        setSelected(key, false);
        rv.smoothScrollToPosition(cards.size() - 1);
    }

    private Card getItem(long key) {
        for (Card c : cards) {
            if (c.key == key) {
                return c;
            }
        }
        return null;
    }

    RecyclerView.LayoutManager getLayoutManager() {
        return rv.getLayoutManager();
    }

    public void setList(ListIterator li) {
        cards.clear();
        while (li.hasNext()) {
            Card curr = (Card) li.next();
            cards.add(curr);
            notifyItemInserted(cards.size() - 1);
        }
    }

    void clearSelection() {

        long key = getSelection();

        if (key != 0L) {
            setSelected(key, false);
            stopDrag(key);
        }
    }

    private void deleteSelected() {

        Iterator i = cards.iterator();
        Card c = null;
        while (i.hasNext()) {
            Card currCard = (Card) i.next();
            if (currCard.isSelected) {
                c = currCard;
                break;
            }
        }

        if (c != null) {
            int index = cards.indexOf(c);
            cards.remove(index);
            notifyItemRemoved(index);
        }
    }

    void setSelected(long id, boolean selected) {

        for (int i = 0; i < cards.size(); i++) {
            Card curr = cards.get(i);

            if (curr.key == id) {

                Context c = rv.getContext();
                curr.isSelected = selected;
                cards.set(i, curr);
                CardViewHolder cardViewHolder = (CardViewHolder) rv.findViewHolderForItemId(curr.key);
                if (cardViewHolder != null) {
                    if (selected) {

                        Resources r = c.getResources();
                        Resources.Theme theme = c.getTheme();

                        Drawable d = VectorDrawableCompat.create(r, R.drawable.outline, theme);

                        cardViewHolder.cv.setBackground(d);
                    } else {

                        cardViewHolder.cv.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        }
    }


    void updateItem(int position, String label, String file, int resourceLocation, String pronunciation, int id) {
        Card item = cards.get(position);
        item.id = id;
        item.label = label;
        item.photoId = file;
        item.resourceLocation = resourceLocation;
        item.pronunciation = pronunciation;
        this.notifyItemChanged(position);
    }

    void setItemTag(int position) {

        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForAdapterPosition(position);
        if (v == null) throw new AssertionError();
        v.cv.setTag("cv");
    }

    public int getItemCount() {
        return cards.size();
    }

    void setLocked(boolean locked) {

        this.locked = locked;

    }

    public long getItemId(int position) {
        return cards.get(position).key;
    }


    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        final CardViewHolder cvh = new CardViewHolder(v);
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            public boolean onDown(MotionEvent e) {

                int pos = cvh.getAdapterPosition();

                if (!locked) {
                    ith.startDrag(cvh);
                }
                listener.onItemClick(v, pos);
                return false;
            }
        });

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        return cvh;
    }

    private void deleteCard(Card c) {
        int index = cards.indexOf(c);
        cards.remove(c);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, cards.size());
    }

    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, int i) {
        Card card = cards.get(i);

        //if(deletedVocab.contains(cards.get(i).id)){
        //    card.label = "";
        //  cards.set(i,card);
//
        //    }
        cardViewHolder.label.setText(card.label);

        Context c = cardViewHolder.cv.getContext();
        Resources resources = c.getResources();
        cardViewHolder.noImageLabel.setText(card.label);

        FileOperations.setImageSource(c, card, cardViewHolder.image, cardViewHolder.noImageLabel);


        if (card.isSelected) {
            cardViewHolder.cv.setSelected(true);
            Resources.Theme theme = context.getTheme();

            Drawable d = VectorDrawableCompat.create(resources, R.drawable.outline, theme);

            cardViewHolder.cv.setBackground(d);
        } else {
            cardViewHolder.cv.setSelected(false);
            cardViewHolder.cv.setBackground(new ColorDrawable(resources.getColor(R.color.cardColor)));
        }
    }

    public void setInvalidVocab(ArrayList<Card> deletedVocab) {
        this.deletedVocab = deletedVocab;

    }

    public void deleteInvalidVocab() {
        if (deletedVocab == null) {
            return;
        }
        ArrayList<Card> cardsToDelete = new ArrayList<>();
        for (Card curr : deletedVocab) {
            for (int i = 0; i < cards.size(); i++) {
                Card currCard = cards.get(i);
                if (currCard.equals(curr)) {
                    cardsToDelete.add(curr);
                }
            }
        }
        for (Card i : cardsToDelete) {
            deleteCard(i);

        }
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(cards, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(cards, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        final CardView cv;
        final TextView label;
        final ImageView image;
        final boolean isSelected;
        final TextView noImageLabel;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            label = itemView.findViewById(R.id.textView);
            image = itemView.findViewById(R.id.imageView);
            noImageLabel = itemView.findViewById(R.id.noImageText);
            isSelected = false;
        }

        CardItemDetails getItemDetails() {
            return new CardItemDetails(getAdapterPosition(), cards.get(getAdapterPosition()));
        }
    }
}
