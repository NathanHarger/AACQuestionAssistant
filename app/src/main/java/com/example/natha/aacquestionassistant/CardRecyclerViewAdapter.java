package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;


public class CardRecyclerViewAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<CardRecyclerViewAdapter.CardViewHolder> implements ItemTouchHelperAdapter {

    protected RecyclerView rv;
    protected List<Card> cards;
    private ItemTouchHelper ith;
    private CustomItemClickListener listener;
    private Context context;
    public void menuClick(int id){
        long key = getSelection();

        if(key != 0L){
            stopDrag(key);

            if (id == R.id.item_delete) {
                if (cards.size() != 0) {
                    deleteSelected();
                    return;
                }

            } else if (id == R.id.item_edit) {
                Card clickedCard = getItem(key);
                int pos = cards.indexOf(clickedCard);
                CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForAdapterPosition(pos);
                v.cv.setTag("edit");

                listener.onItemClick(v.cv, cards.indexOf(clickedCard));
            }
        } else if (id == R.id.new_item_create){

            Intent createVocabIntent = new Intent(context, NewVocabActivity.class);

            context.startActivity(createVocabIntent);

        }
    }
    public boolean getLocked(){
        return locked;
    }
    public long getSelection(){
        for (Card c : cards) {
            if(c.isSelected){
                return c.key;
            }
        }
        return 0L;
    }
    private boolean locked = false;
    public void startDrag(long key){
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForItemId(key);
        ith.startDrag(v);
    }

    public void  stopDrag(long key) {
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForItemId(key);
        simpleItemTouchHelperCallback.clearView(rv,v);
    }
    CardRecyclerViewAdapter(List<Card> cards, CustomItemClickListener listener, RecyclerView rv, Context context) {
        this.cards = cards;
        this.listener = listener;
        this.rv = rv;
        setHasStableIds(true);
        this.context = context;


    }

    public void setTouchHelper(ItemTouchHelper ith) {
        this.ith = ith;
    }

    SimpleItemTouchHelperCallback simpleItemTouchHelperCallback;
    public void setTouchHelperCallback(SimpleItemTouchHelperCallback simpleItemTouchHelperCallback){
        this.simpleItemTouchHelperCallback = simpleItemTouchHelperCallback;
    }

    public void addItem(Card c) {
        cards.add(c);
        this.notifyDataSetChanged();
        long key = getSelection();
        setSelected(key,false);
        rv.smoothScrollToPosition(cards.size()-1);
    }

    public Card getItem(long key) {
        for (Card c : cards) {
            if (c.key == key) {
                return c;
            }
        }
        return null;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
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

    public void clearSelection() {

        long key = getSelection();

        if(key != 0L){
            setSelected(key, false);
            stopDrag(key);
        }
    }

    public void deleteSelected() {

        Iterator i = cards.iterator();
        Card c = null;
        while (i.hasNext()){
            Card currCard = (Card) i.next();
            if(currCard.isSelected){
                c = currCard;
                break;
            }
        }

        if(c!=null) {
            int index = cards.indexOf(c);
            cards.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void setSelected(long id, boolean selected) {

        for (int i = 0; i < cards.size(); i++) {
            Card curr = cards.get(i);

            if (curr.key == id) {
                CardViewHolder cardViewHolder = (CardViewHolder) rv.findViewHolderForItemId(curr.key);
                Context c = rv.getContext();
                curr.isSelected = selected;
                cards.set(i, curr);
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

    public void updateItem(int position, String label, String file, int resourceLocation) {
        Card item = cards.get(position);
        item.label = label;
        item.photoId = file;
        item.resourceLocation = resourceLocation;
        this.notifyItemChanged(position);
    }

    public void setItemTag(int position, String tag) {
      
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForAdapterPosition(position);
        v.cv.setTag(tag);
    }

    public int getItemCount() {
        return cards.size();
    }

    public void setLocked(boolean locked){

        this.locked = locked;

    }
    public long getItemId(int position) {
        return cards.get(position).key;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {

        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);

        ImageView iv = v.findViewById(R.id.imageView);

        final CardViewHolder cvh = new CardViewHolder(v);

        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onContextClick(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {}

            public boolean onSingleTapUp(MotionEvent e){ return false;}

            public void onLongPress(MotionEvent e) {}

            public boolean onDown(MotionEvent e){

               int pos = cvh.getAdapterPosition();
               Card c = cards.get(pos);

                  if(!locked){

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


    public void onBindViewHolder(final CardViewHolder cardViewHolder, int i) {

        cardViewHolder.label.setText(cards.get(i).label);

        Context c = cardViewHolder.cv.getContext();
        Resources resources = c.getResources();
        Card card = cards.get(i);
        FileOperations.setImageSource(c, card,cardViewHolder.image );


        if ( card.isSelected) {
            cardViewHolder.cv.setSelected(true);
            Resources r = context.getResources();
            Resources.Theme theme = context.getTheme();

            Drawable d = VectorDrawableCompat.create(resources, R.drawable.outline, theme);

            cardViewHolder.cv.setBackground(d);
        } else {
            cardViewHolder.cv.setSelected(false);
            cardViewHolder.cv.setBackground(new ColorDrawable(resources.getColor(R.color.cardColor)));
        }
    }

    @Override
    public void onItemDismiss(int position) {
        cards.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

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
        return true;
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView label;
        ImageView image;
        boolean isSelected;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            label = itemView.findViewById(R.id.textView);
            image = itemView.findViewById(R.id.imageView);
        }

        CardItemDetails getItemDetails() {
            return new CardItemDetails(getAdapterPosition(), cards.get(getAdapterPosition()));
        }
    }
}
