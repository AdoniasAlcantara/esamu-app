package org.myopenproject.esamu.presentation.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.domain.EmergencyGateway;
import org.myopenproject.esamu.domain.EmergencyRecord;
import org.myopenproject.esamu.domain.App;

import java.util.List;

public class HistoryFragment extends Fragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().getBus().register(this);
        refresh();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
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

        if (emergencies != null) {
            for (EmergencyRecord e : emergencies) {
                Log.d("TEST", e.getId() + ", " + e.getStatus().name());
            }
        } else {
            Log.d("TEST", "empty :(");
        }
    }
}
