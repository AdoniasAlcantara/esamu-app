package org.myopenproject.esamu.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.ui.settings.AboutActivity;

public class HomeActivity extends AppCompatActivity implements OnHomeInteractionListener {
    public static final String PARAM_DEFAULT_PAGE = "defaultPage";

    private MainFragment homeFrag;
    private HistoryFragment historyFrag;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fragments
        homeFrag = new MainFragment();
        historyFrag = new HistoryFragment();

        // View Pager
        pager = findViewById(R.id.homePager);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(new TabsAdapter(getSupportFragmentManager()));

        // Tabs
        TabLayout tabs = findViewById(R.id.homeTabs);
        tabs.setupWithViewPager(pager);

        int defaultPage = getIntent()
                .getIntExtra(PARAM_DEFAULT_PAGE, OnHomeInteractionListener.PAGE_MAIN);

        pager.setCurrentItem(defaultPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homeMenuSettings:
                // TODO
                break;

            case R.id.homeMenuAbout:
                startActivity(new Intent(this, AboutActivity.class));
        }

        return true;
    }

    @Override
    protected void onStop() {
        homeFrag.reset();
        super.onStop();
    }

    @Override
    public void showPage(int page) {
        pager.setCurrentItem(page);
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == OnHomeInteractionListener.PAGE_HISTORY) {
                return getString(R.string.home_tab_history);
            }

            return getString(R.string.home_tab_main);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == OnHomeInteractionListener.PAGE_HISTORY) {
                return historyFrag;
            }

            return homeFrag;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
