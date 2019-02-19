package org.myopenproject.esamu.presentation.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.widget.NavDrawerActivity;

public class HomeActivity extends NavDrawerActivity {
    public static final int PAGE_MAIN = 0;
    public static final int PAGE_HISTORY = 1;

    private HomeFragment homeFrag;
    private HistoryFragment historyFrag;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup toolbar and navigation drawer
        setupToolbarAndNavDrawer();

        // Fragments
        homeFrag = new HomeFragment();
        historyFrag = new HistoryFragment();

        // View Pager
        pager = findViewById(R.id.homePager);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}

            @Override
            public void onPageSelected(int i) {
                if (i == 1)
                    homeFrag.reset();
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        // Tabs
        TabLayout tabs = findViewById(R.id.homeTabs);
        tabs.setupWithViewPager(pager);
    }

    public void showPage(int page) {
        pager.setCurrentItem(page);
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 1)
                return getString(R.string.home_tab_history);

            return getString(R.string.home_tab_main);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 1)
                return historyFrag;

            return homeFrag;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
