package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.res.Resources;
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
    private ActionMode am;
    private ItemTouchHelper ith;
    private CustomItemClickListener listener;
    private SelectionTracker selectionTracker;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTag("Edit Menu");
            MenuItem delete = menu.findItem(R.id.item_delete);


            return true; // Return false if nothing is done
        }

        public long getSelection(){
            for (Card c : cards) {
                if(c.isSelected){
                    return c.key;
                }
            }
            return 0L;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Selection s = selectionTracker.getSelection();
            //if (s.size() == 0) {
             //   return false;
           // }
            long key =  getSelection();

            int id = item.getItemId();
            stopDrag(key);
            if (id == R.id.item_delete) {
                if (cards.size() != 0) {
                    deleteSelected();
                    mode.finish();
                    am = null;
                    return true;
                }

                return false;
            } else if (id == R.id.item_edit) {


                Card clickedCard = getItem(key);
                int pos = cards.indexOf(clickedCard);
                CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForAdapterPosition(pos);
                v.cv.setTag("edit");

                listener.onItemClick(v.cv, cards.indexOf(clickedCard));
            }
            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            am = null;

            selectionTracker.clearSelection();
        }
    };
    private boolean locked = false;
    public void startDrag(long key){
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForItemId(key);
        ith.startDrag(v);
    }

    public void  stopDrag(long key) {
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForItemId(key);
        simpleItemTouchHelperCallback.clearView(rv,v);
    }
    CardRecyclerViewAdapter(List<Card> cards, CustomItemClickListener listener, RecyclerView rv) {
        this.cards = cards;
        this.listener = listener;
        this.rv = rv;

        setHasStableIds(true);


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

        if(!selectionTracker.hasSelection()){


            rv.smoothScrollToPosition(cards.size()-1);

        }

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
    // returns true if action mode is active

    public void setList(ListIterator li) {
        cards.clear();
        while (li.hasNext()) {
            Card curr = (Card) li.next();
            cards.add(curr);
            notifyItemInserted(cards.size() - 1);
        }
    }

    public void clear() {
        final int size = cards.size();
        cards.clear();
        notifyItemRangeRemoved(0, size);
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

       // selectionTracker.select(id);
        for (Card curr : cards) {
            if (curr.key == id) {

                curr.isSelected = selected;
                return;
            }


        }
        return;
    }

    public void setSelectionTracker(SelectionTracker st) {
        selectionTracker = st;
    }

    public void updateItem(int position, String label, String file) {
        cards.get(position).label = label;
        cards.get(position).photoId = file;

        this.notifyItemChanged(position);
    }

    public void setItemTag(int position, String tag) {
        CardRecyclerViewAdapter.CardViewHolder v = (CardRecyclerViewAdapter.CardViewHolder) rv.findViewHolderForAdapterPosition(position);
        v.cv.setTag(tag);
    }

    public void removeItemFromBack() {
        this.cards.remove(this.cards.size() - 1);
        this.notifyItemRemoved(this.cards.size());

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
            public void onShowPress(MotionEvent e) {
            }

            public boolean onSingleTapUp(MotionEvent e){

                return false;
            }

            public void onLongPress(MotionEvent e) {
               View mainview = (FrameLayout)(((((ViewGroup)((ViewGroup)
                       v.getParent()).getParent()).getParent()).getParent()).getParent());
               FloatingActionButton fab = mainview.findViewById(R.id.add_card_fab);
               fab.setAlpha(locked? .5f:1f);

                if(locked){
                    Toast.makeText(v.getContext(), "Edit Locked Out", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("", "Longpress detected");
                if (am == null) {

                    am = v.startActionMode(mActionModeCallback);

                }
                Card c = cards.get(cvh.getAdapterPosition());

                selectionTracker.select(c.key);
                ith.startDrag(cvh);
                super.onLongPress(e);

            }


            public boolean onDown(MotionEvent e){
                if(locked){
                    Toast.makeText(v.getContext(), "Edit Locked Out", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(am != null) {
                    ith.startDrag(cvh);
                    return true;

                }
                listener.onItemClick(v, cvh.getAdapterPosition());
//

                //selectionTracker.select(c.key);

                return true;
           }
        });


        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                gestureDetector.setIsLongpressEnabled(true);

                    return gestureDetector.onTouchEvent(event);


            }
        });



        return cvh;
    }


    public void onBindViewHolder(final CardViewHolder cardViewHolder, int i) {

        cardViewHolder.label.setText(cards.get(i).label);

        Context c = cardViewHolder.cv.getContext();
        Resources resources = c.getResources();

        final int resourceId = resources.getIdentifier(cards.get(i).photoId, "drawable", c.getPackageName());

        cardViewHolder.image.setImageResource(resourceId);
        Context context = c.getApplicationContext();
        Card card = cards.get(i);
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

    public void stopActionMode() {
        if (am != null) {

            am.finish();
            am = null;
        }
    }

    public boolean actionModeActive(){
        return am != null;
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
            cv = (CardView) itemView.findViewById(R.id.cv);
            label = (TextView) itemView.findViewById(R.id.textView);
            image = (ImageView) itemView.findViewById(R.id.imageView);


        }

        CardItemDetails getItemDetails() {
            return new CardItemDetails(getAdapterPosition(), cards.get(getAdapterPosition()));
        }


    }
}
