package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.res.Resources;
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
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ImageSelectionRecyclerViewAdapter extends RecyclerView.Adapter<ImageSelectionRecyclerViewAdapter.CardViewHolder> {
    public static final DiffUtil.ItemCallback<Card> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<Card>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull Card oldUser, @NonNull Card newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.key == newUser.key;
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull Card oldUser, @NonNull Card newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.key == (newUser.key);
        }
    };
    final AsyncListDiffer<Card> mDiffer = new AsyncListDiffer(this, DIFF_CALLBACK);

    protected List<Card> images;
    private CustomItemClickListener listener;
    private RecyclerView rv;


    ImageSelectionRecyclerViewAdapter(List<Card> images, CustomItemClickListener listener, RecyclerView rv) {
        this.images = images;
        this.listener = listener;
        this.rv = rv;
    }

    public Card getCardFromCardView(View cv){
        int pos = rv.getChildAdapterPosition(cv);
        return mDiffer.getCurrentList().get(pos);
    }
    public void addItem(Card c) {
        images.add(c);
        this.notifyDataSetChanged();

    }

    public void clearList() {
        final int size = images.size();
        images.clear();
        notifyItemRangeRemoved(0, size);


    }

    public Card getItem(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public void submitList(List<Card> list) {
        mDiffer.submitList(list);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
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

    public void remove(Card c){
        List<Card> result = new LinkedList<>();
        for(Card curr : mDiffer.getCurrentList()){
            if(curr.key != c.key){
                result.add(curr);
            }
        }
        mDiffer.submitList(result);

    }


    public void onBindViewHolder(final CardViewHolder cardViewHolder, int i) {
        Card curr = mDiffer.getCurrentList().get(i);
        Context c = cardViewHolder.cv.getContext();


        FileOperations.setImageSource(c,curr,cardViewHolder.image);


        curr.label = curr.label.replace("_", " ").replaceAll("[0-9]", "");
        if(!curr.pronunciation.equals("")){
            curr.label += " (" + curr.pronunciation + ")";
        }


        cardViewHolder.text.setText(curr.label );

        cardViewHolder.deleteCard.setVisibility(curr.resourceLocation == 1 ? View.VISIBLE : View.GONE);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView image;
        TextView text;
        ImageButton deleteCard;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            image = itemView.findViewById(R.id.imageView);
            text = itemView.findViewById(R.id.image_selection_text);
            deleteCard = itemView.findViewById(R.id.imageSelectionDelete);
        }
    }

}
