package org.myopenproject.esamu.presentation.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.domain.App;
import org.myopenproject.esamu.domain.EmergencyGateway;
import org.myopenproject.esamu.domain.EmergencyRecord;
import org.myopenproject.esamu.presentation.firstaid.FirstAidActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private RecyclerView recycler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        recycler = view.findViewById(R.id.historyRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
        App.getInstance().getBus().register(this);
    }

    @Override
    public void onDestroy() {
        App.getInstance().getBus().unregister(this);
        super.onDestroy();
    }

    // Refresh history by the broadcast way
    @Subscribe
    public void refresh(String msg) {
        if (msg.equals("refreshHistory"))
            refresh();
    }

    public void refresh() {
        List<EmergencyRecord> emergencies = null;

        try (EmergencyGateway gateway = new EmergencyGateway(getContext())) {
            emergencies = gateway.findAll();
        }

        if (emergencies != null)
            recycler.setAdapter(new HistoryAdapter(emergencies));
    }

    private void onAttachmentClicked(int attach) {
        Intent it = new Intent(getContext(), FirstAidActivity.class);
        it.putExtra(FirstAidActivity.PARAM_ATTACH, attach);
        startActivity(it);
    }

    private class HistoryAdapter extends RecyclerView.Adapter<ViewHolder> {
        List<EmergencyRecord> list;

        HistoryAdapter(List<EmergencyRecord> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // Inflate view
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.card_history, viewGroup, false);

            // Create view holder
            return new ViewHolder(view);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            EmergencyRecord record = list.get(i);

            // Title
            int titleRes = 0;
            int colorRes = 0;

            switch (record.getStatus()) {
                case PENDENT:
                    titleRes = R.string.history_card_status_pendent;
                    colorRes = R.color.pendent;
                    break;

                case PROGRESS:
                    titleRes = R.string.history_card_status_progress;
                    colorRes = R.color.progress;
                    break;

                case FINISHED:
                    titleRes = R.string.history_card_status_finished;
                    colorRes = R.color.finished;
                    break;

                case CANCELED:
                    titleRes = R.string.history_card_status_canceled;
                    colorRes = R.color.canceled;
            }

            viewHolder.title.setText(getString(titleRes));
            int color = ContextCompat.getColor(getContext(), colorRes);
            viewHolder.title.setTextColor(color);
            viewHolder.edge.setColorFilter(color, PorterDuff.Mode.SRC_OVER);

            // Date time
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm - dd/MM/yy", Locale.getDefault());
            String dateTime = formatter.format(record.getDateTime());
            viewHolder.dateTime
                    .setText(String.format(getString(R.string.history_card_time), dateTime));

            // Location
            String location = record.getLocation();

            if (location != null)
                viewHolder.location
                        .setText(String.format(getString(R.string.history_card_location), location));

            // Attachment
            int attach = record.getAttachment();

            if (attach >= 0) {
                viewHolder.buttonAttach.setVisibility(View.VISIBLE);
                viewHolder.buttonAttach.setOnClickListener((v -> onAttachmentClicked(attach)));
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView dateTime;
        TextView location;
        Button buttonAttach;
        Drawable edge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.historyCardTitle);
            dateTime = itemView.findViewById(R.id.historyCardTime);
            location = itemView.findViewById(R.id.historyCardLocation);
            buttonAttach = itemView.findViewById(R.id.historyCardAttach);
            edge = itemView.findViewById(R.id.historyCardEdge).getBackground();
        }
    }
}
