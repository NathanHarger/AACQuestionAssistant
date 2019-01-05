package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{
    private final ItemTouchHelperAdapter mAdapter;
    private final Context context;

    public SimpleItemTouchHelperCallback(
            ItemTouchHelperAdapter adapter, Context context) {
        mAdapter = adapter;
        this.context = context;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        // We only want the active item
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof CardRecyclerViewAdapter.CardViewHolder) {
                CardRecyclerViewAdapter.CardViewHolder itemViewHolder =
                        (CardRecyclerViewAdapter.CardViewHolder) viewHolder;


                //itemViewHolder.cv.setBackgroundColor(0xff707070);
            }
        }

    }


    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {


        if (viewHolder instanceof CardRecyclerViewAdapter.CardViewHolder) {
            CardRecyclerViewAdapter.CardViewHolder itemViewHolder =
                    (CardRecyclerViewAdapter.CardViewHolder) viewHolder;

            CardItemDetails id = ((CardRecyclerViewAdapter.CardViewHolder) viewHolder).getItemDetails();
            Card item = id.getItem();
                if(item.isSelected){
                    return;
                }
                    itemViewHolder.cv.setBackgroundColor(context.getResources().getColor(R.color.cardColor));

        }

        super.clearView(recyclerView, viewHolder);

    }



    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {


        if (viewHolder instanceof CardRecyclerViewAdapter.CardViewHolder) {
            CardRecyclerViewAdapter.CardViewHolder itemViewHolder =
                    (CardRecyclerViewAdapter.CardViewHolder) viewHolder;



                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive) {

                    Resources r = context.getResources();
                    Resources.Theme theme = context.getTheme();

                    Drawable d = VectorDrawableCompat.create(r, R.drawable.outline, theme);

                    itemViewHolder.cv.setBackground(d);
                }
                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive);
            }

    }
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {



        CardRecyclerViewAdapter crva = (CardRecyclerViewAdapter) recyclerView.getAdapter();

        crva.stopActionMode();

        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
