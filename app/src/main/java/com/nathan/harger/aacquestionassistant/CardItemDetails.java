package com.nathan.harger.aacquestionassistant;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;


/**
 * An {@link ItemDetailsLookup.ItemDetails} that holds details about a {@link String} item like its position and its value.
 */

class CardItemDetails extends ItemDetailsLookup.ItemDetails {

    private final int position;
    private final Card item;

    public CardItemDetails(int position, Card item) {
        this.position = position;
        this.item = item;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Object getSelectionKey() {
        return item.key;
    }

    public Card getItem() {
        return item;
    }
}

