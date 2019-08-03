package org.myopenproject.esamu.ui.home;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.data.model.EmergencyRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryRecyclerAdapter extends
        RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<EmergencyRecord> emergencies;
    private OnHistoryItemClickListener listener;

    public HistoryRecyclerAdapter(Context context, OnHistoryItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.item_history, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        EmergencyRecord emergency = emergencies.get(i);

        // Title
        int titleRes = 0;
        int colorRes = 0;

        switch (emergency.getStatus()) {
            case PENDENT:
                titleRes = R.string.history_status_pendent;
                colorRes = R.color.pendent;
                viewHolder.btnDelete.setVisibility(View.GONE);
                break;

            case PROGRESS:
                titleRes = R.string.history_status_progress;
                colorRes = R.color.progress;
                viewHolder.btnDelete.setVisibility(View.GONE);
                break;

            case FINISHED:
                titleRes = R.string.history_status_finished;
                colorRes = R.color.finished;
                viewHolder.btnDelete.setVisibility(View.VISIBLE);
                viewHolder.btnDelete.setOnClickListener(v -> listener.onRemoveClick(
                        emergency.getId(), viewHolder.getAdapterPosition()));
                break;

            case CANCELED:
                titleRes = R.string.history_status_canceled;
                colorRes = R.color.canceled;
                viewHolder.btnDelete.setVisibility(View.VISIBLE);
                viewHolder.btnDelete.setOnClickListener(v -> listener.onRemoveClick(
                        emergency.getId(), viewHolder.getAdapterPosition()));
        }

        int color = ContextCompat.getColor(context, colorRes);
        viewHolder.tvTitle.setText(context.getString(titleRes));
        viewHolder.tvTitle.setTextColor(color);
        viewHolder.drwEdge.setColorFilter(color, PorterDuff.Mode.SRC_OVER);

        // Date time
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - HH:mm", Locale.getDefault());
        String dateTimeStr = formatter.format(emergency.getDateTime());
        viewHolder.tvDateTime.setText(dateTimeStr);

        // Location
        if (emergency.getLocation() != null) {
            viewHolder.tvLocation.setText(emergency.getLocation());
        }

        // Attachment
        if (emergency.getAttachment() >= 0) {
            viewHolder.btnAttach.setVisibility(View.VISIBLE);
            viewHolder.btnAttach.setOnClickListener(v -> listener.onAttachmentClick(
                    emergency.getAttachment(), viewHolder.getAdapterPosition()));
        } else {
            viewHolder.btnAttach.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return emergencies != null ? emergencies.size() : 0;
    }

    public void setEmergencies(List<EmergencyRecord> emergencies) {
        this.emergencies = emergencies;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDateTime;
        TextView tvLocation;
        Button btnAttach;
        ImageView btnDelete;
        Drawable drwEdge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.historyTvTitle);
            tvDateTime = itemView.findViewById(R.id.historyTvDateTime);
            tvLocation = itemView.findViewById(R.id.historyTvLocation);
            btnAttach = itemView.findViewById(R.id.historyBtnAttach);
            btnDelete = itemView.findViewById(R.id.historyBtnDelete);
            drwEdge = itemView.findViewById(R.id.historyEdge).getBackground();
        }
    }

    public interface OnHistoryItemClickListener {
        void onRemoveClick(long id, int index);
        void onAttachmentClick(int attachment, int index);
    }
}
