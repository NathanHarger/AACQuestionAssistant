package com.nathan.harger.aacquestionassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RestoreGridRecyclerViewAdapter extends RecyclerView.Adapter<RestoreGridRecyclerViewAdapter.RestoreCardViewHolder> {

    protected final CustomItemClickListener listener;
    private final RecyclerView rv;
    private final List<Long> keys;


    RestoreGridRecyclerViewAdapter(CustomItemClickListener listener, RecyclerView rv) {
        this.listener = listener;
        this.rv = rv;
        keys = new LinkedList<>();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);

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


        adapter.submitList(innerCards);
    }

    public static class RestoreCardViewHolder extends RecyclerView.ViewHolder {
        RecyclerView row;


        RestoreCardViewHolder(View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.restore_grid_layout);

        }
    }


}
