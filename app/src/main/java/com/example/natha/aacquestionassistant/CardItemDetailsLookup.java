package com.example.natha.aacquestionassistant;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;


public class CardItemDetailsLookup extends ItemDetailsLookup {

    private final RecyclerView mRecyclerView;

    CardItemDetailsLookup(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails getItemDetails(@NonNull MotionEvent e) {
        View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
            if (holder instanceof CardRecyclerViewAdapter.CardViewHolder) {
                return ((CardRecyclerViewAdapter.CardViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}

