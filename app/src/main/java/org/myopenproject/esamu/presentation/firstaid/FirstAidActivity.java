package org.myopenproject.esamu.presentation.firstaid;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.domain.FirstAidGateway;
import org.myopenproject.esamu.domain.FirstAidItem;

import java.util.List;

public class FirstAidActivity extends AppCompatActivity {
    public static final String PARAM_ATTACH = "attachment";
    private static final String TAG = "FIRST_AID";
    private ViewPager pager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_aid);

        // Get attachment param
        int attach = getIntent().getIntExtra(PARAM_ATTACH, -1);

        if (attach < 0) {
            Log.w(TAG, "Invalid attachment param given. Value: " + attach);
            finish();
            return;
        }

        // Set up pager
        List<FirstAidItem> items = FirstAidGateway.findByGroupId(this, attach);

        if (items == null) {
            Log.w(TAG, "No items found");
            finish();
            return;
        }

        // Set up navigation button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pager = findViewById(R.id.firstAidPager);
        FirstAidAdapter adapter = new FirstAidAdapter(this, items);
        pager.setOffscreenPageLimit(items.size());
        pager.setAdapter(adapter);

        // Set up button events
        View buttonPrevious = findViewById(R.id.firstAidButtonPrevious);
        buttonPrevious.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() - 1));

        View buttonForward = findViewById(R.id.firstAidButtonForward);
        buttonForward.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() + 1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return true;
    }
}
