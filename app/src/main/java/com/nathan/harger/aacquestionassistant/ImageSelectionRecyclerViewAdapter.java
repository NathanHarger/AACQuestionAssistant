package com.nathan.harger.aacquestionassistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ImageSelectionRecyclerViewAdapter extends RecyclerView.Adapter<ImageSelectionRecyclerViewAdapter.CardViewHolder> {
    protected final CustomItemClickListener listener;
    private final RecyclerView rv;
    private final List<Card> cards;
    private final boolean removeEnabled;

    ImageSelectionRecyclerViewAdapter(CustomItemClickListener listener, RecyclerView rv, boolean removeEnabled) {
        this.listener = listener;
        this.rv = rv;
        cards = new LinkedList<>();
        this.removeEnabled = removeEnabled;

    }

    public Card getCardFromCardView(View cv) {
        int pos = rv.getChildAdapterPosition(cv);
        return cards.get(pos);
    }

    public Card getItem(int position) {
        return cards.get(position);
    }

    public int getItemCount() {
        return cards.size();
    }

    public void submitList(List<Card> list) {
        int size = cards.size();
        cards.clear();
        notifyItemRangeRemoved(0, size);
        cards.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_selection_card_layout, viewGroup, false);

        final CardViewHolder cvh = new CardViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listener.onItemClick(v, cvh.getAdapterPosition());
            }
        });
        return cvh;
    }

    void remove(Card c) {
        List<Card> result = new LinkedList<>();
        for (Card curr : cards) {
            if (curr.key != c.key) {
                result.add(curr);
            }
        }
        submitList(result);

    }

    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, int i) {
        Card curr = cards.get(i);
        Context c = cardViewHolder.cv.getContext();
        curr.label = curr.label.replace("_", " ");
        if (curr.resourceLocation == 0)
            curr.label = curr.label.replaceAll("[0-9]", "");

        cardViewHolder.noImageLabel.setText(curr.label);


        FileOperations.setImageSource(c, curr, cardViewHolder.image, cardViewHolder.noImageLabel);


        if (!curr.pronunciation.equals("")) {
            curr.label += " (" + curr.pronunciation + ")";
        }
        cardViewHolder.text.setText(curr.label);
        cardViewHolder.deleteCard.setVisibility(curr.resourceLocation == 1 && removeEnabled ? View.VISIBLE : View.GONE);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        final CardView cv;
        final ImageView image;
        final TextView text;
        final ImageButton deleteCard;
        final TextView noImageLabel;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            image = itemView.findViewById(R.id.imageViewImageSelection);
            text = itemView.findViewById(R.id.image_selection_text);
            deleteCard = itemView.findViewById(R.id.imageSelectionDelete);
            noImageLabel = itemView.findViewById(R.id.noImageTextImageSelection);
        }
    }
}
