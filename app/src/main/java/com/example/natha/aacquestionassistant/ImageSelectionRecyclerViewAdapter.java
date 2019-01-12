package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ImageSelectionRecyclerViewAdapter extends RecyclerView.Adapter<ImageSelectionRecyclerViewAdapter.CardViewHolder> {
    public static final DiffUtil.ItemCallback<FileInfo> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<FileInfo>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull FileInfo oldUser, @NonNull FileInfo newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.id == newUser.id;
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull FileInfo oldUser, @NonNull FileInfo newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.id == (newUser.id);
        }
    };
    final AsyncListDiffer<FileInfo> mDiffer = new AsyncListDiffer(this, DIFF_CALLBACK);

    protected List<FileInfo> images;
    private CustomItemClickListener listener;

    ImageSelectionRecyclerViewAdapter(List<FileInfo> images, CustomItemClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    public void addItem(FileInfo c) {
        images.add(c);
        this.notifyDataSetChanged();

    }

    public void clearList() {
        final int size = images.size();
        images.clear();
        notifyItemRangeRemoved(0, size);


    }

    public FileInfo getItem(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public void submitList(List<FileInfo> list) {
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

    public void onBindViewHolder(final CardViewHolder cardViewHolder, int i) {
        FileInfo curr = mDiffer.getCurrentList().get(i);
        Context c = cardViewHolder.cv.getContext();


        FileOperations.setImageSource(c,curr,cardViewHolder.image);

        cardViewHolder.text.setText(curr.symbol.replace("_", " ").replaceAll("[0-9]", ""));


    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView image;
        TextView text;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            image = itemView.findViewById(R.id.imageView);
            text = itemView.findViewById(R.id.image_selection_text);
        }
    }
}
