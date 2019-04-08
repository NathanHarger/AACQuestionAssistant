package com.nathan.harger.aacquestionassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RestoreGridRecyclerViewAdapter extends RecyclerView.Adapter<RestoreGridRecyclerViewAdapter.RestoreCardViewHolder> {

    private final CustomItemClickListener listener;
    private final RecyclerView rv;
    private final List<Long> keys;
    private View emptyView;


    RestoreGridRecyclerViewAdapter(CustomItemClickListener listener, RecyclerView rv) {
        this.listener = listener;
        this.rv = rv;
        keys = new LinkedList<>();

        this.setEmptyView(((ConstraintLayout) rv.getParent()).findViewById(R.id.restore_grid_empty_view));

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

    public long getCardFromCardView(View cv) {
        int pos = rv.getChildAdapterPosition(cv);
        return keys.get(pos);
    }

    public long getKey(int position) {
        return keys.get(position);
    }

    public void submitList(List<Long> list) {
        int size = keys.size();
        keys.clear();
        notifyItemRangeRemoved(0, size);
        keys.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    public RestoreCardViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restore_card_layout, viewGroup, false);

        final RestoreCardViewHolder cvh = new RestoreCardViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listener.onItemClick(v, cvh.getAdapterPosition());
            }
        });


        return cvh;
    }

    public void removeItem(long id) {
        if (this.keys.contains(id)) {
            int index = keys.indexOf(id);
            this.keys.remove(id);
            notifyItemRemoved(index);
        }

    }

    @Override
    public int getItemCount() {
        return keys.size();
    }


    @Override
    public void onBindViewHolder(@NonNull final RestoreCardViewHolder cardViewHolder, int i) {
        final int pos = cardViewHolder.getLayoutPosition();
        List<Card> innerCards = ImageDatabaseHelper.getInstance(rv.getContext()).getCardGroup(keys.get(pos));

        final ImageSelectionRecyclerViewAdapter adapter = new ImageSelectionRecyclerViewAdapter(new CustomItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                listener.onItemClick(v, pos);
            }
        }, cardViewHolder.row, false);


        LinearLayoutManager layoutManager = new LinearLayoutManager(cardViewHolder.row.getContext(), RecyclerView.HORIZONTAL, false);
        cardViewHolder.row.setLayoutManager(layoutManager);
        cardViewHolder.row.setAdapter(adapter);
        cardViewHolder.row.setHasFixedSize(true);


        if (i != -1) {
            cardViewHolder.x.setVisibility(View.VISIBLE);
        }

        adapter.submitList(innerCards);
    }

    public static class RestoreCardViewHolder extends RecyclerView.ViewHolder {
        final RecyclerView row;
        final ImageButton x;
        final ViewParent parent;

        RestoreCardViewHolder(View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.inner_restore_grid_layout);
            x = itemView.findViewById(R.id.vocabSetDelete);
            parent = itemView.getParent();
        }
    }


}
