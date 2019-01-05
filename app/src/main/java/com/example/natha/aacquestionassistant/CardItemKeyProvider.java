package com.example.natha.aacquestionassistant;

//https://github.com/guenodz/recyclerview-selection-demo/blob/master/app/src/main/java/me/guendouz/recyclerview_item_selection/StringItemKeyProvider.java

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemKeyProvider;

/**
 * A basic implementation of {@link ItemKeyProvider} for String items.
 */

public class CardItemKeyProvider extends ItemKeyProvider<Long> {

    private List<Card> items;

    public CardItemKeyProvider(int scope, List<Card> items) {
        super(scope);
        this.items = items;
    }


    @Override
    public Long getKey(int position) {
        return items.get(position).key;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).key == key) {
                return i;
            }
        }

        return -1;
    }
}