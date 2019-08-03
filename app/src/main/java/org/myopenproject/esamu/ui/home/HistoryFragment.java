package org.myopenproject.esamu.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.myopenproject.esamu.App;
import org.myopenproject.esamu.R;
import org.myopenproject.esamu.data.model.EmergencyGateway;
import org.myopenproject.esamu.data.model.EmergencyRecord;
import org.myopenproject.esamu.ui.firstaid.FirstAidActivity;

import java.util.List;

public class HistoryFragment extends Fragment implements
        HistoryRecyclerAdapter.OnHistoryItemClickListener {
    private EmergencyGateway gateway;

    private HistoryRecyclerAdapter adapter;
    private List<EmergencyRecord> emergencies;
    private TextView tvEmpty;
    private Handler handler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        gateway = new EmergencyGateway(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        tvEmpty = view.findViewById(R.id.historyTvEmpty);

        // Set up recycler view
        RecyclerView rvHistory = view.findViewById(R.id.historyRecyclerView);
        rvHistory.setHasFixedSize(true);
        rvHistory.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter = new HistoryRecyclerAdapter(container.getContext(), this);
        rvHistory.setAdapter(adapter);

        App.getInstance().getBus().register(this);
        handler = new Handler();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().getBus().unregister(this);
    }

    @Subscribe
    public void refresh(String message) {
        if (message.equals(App.BUS_REFRESH_HISTORY)) {
            refresh();
        }
    }

    private void refresh() {
        emergencies = gateway.findAll();
        adapter.setEmergencies(emergencies);
        updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        // Show a message when the list is empty
        if (emergencies != null && !emergencies.isEmpty()) {
            tvEmpty.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRemoveClick(long id, int index) {
        new Thread(() -> {
            gateway.remove(id);
            emergencies.remove(index);
            handler.post(() -> {
                adapter.notifyItemRemoved(index);
                updateEmptyMessage();
            });
        }).run();

    }

    @Override
    public void onAttachmentClick(int attachment, int index) {
        Intent it = new Intent(getContext(), FirstAidActivity.class);
        it.putExtra(FirstAidActivity.PARAM_ATTACH, attachment);
        startActivity(it);
    }
}
