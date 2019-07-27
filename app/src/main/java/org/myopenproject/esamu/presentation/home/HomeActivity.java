package org.myopenproject.esamu.presentation.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.presentation.settings.AboutActivity;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.widget.NavDrawerActivity;

public class HomeActivity extends NavDrawerActivity
{
    public static final int PAGE_MAIN = 0;
    public static final int PAGE_HISTORY = 1;

    private HomeFragment homeFrag;
    private HistoryFragment historyFrag;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}

            @Override
            public void onPageSelected(int i)
            {
                switch (i) {
                    case PAGE_HISTORY:
                        homeFrag.reset();
                        setSelectedItem(i);
                        break;

                    case PAGE_MAIN:
                        setSelectedItem(i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        // Tabs
        TabLayout tabs = findViewById(R.id.homeTabs);
        tabs.setupWithViewPager(pager);
    }

    @Override
    protected void onStop()
    {
        homeFrag.reset();
        super.onStop();
    }

    @Override
    protected boolean onNavItemSelected(MenuItem item)
    {
        String msg = getString(R.string.error_not_implemented);

        switch (item.getItemId()) {
            case R.id.navHome:
                pager.setCurrentItem(PAGE_MAIN);
                break;

            case R.id.navHistory:
                pager.setCurrentItem(PAGE_HISTORY);
                break;

            case R.id.navProfile:
                Dialog.toast(this, msg);
                break;

            case R.id.navAbout:
                startActivity(AboutActivity.class);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showPage(int page)
    {
        pager.setCurrentItem(page);
    }

    private class TabsAdapter extends FragmentPagerAdapter
    {
        TabsAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position)
        {
            if (position == 1) {
                return getString(R.string.home_tab_history);
            }

            return getString(R.string.home_tab_main);
        }

        @Override
        public Fragment getItem(int i)
        {
            if (i == 1) {
                return historyFrag;
            }

            return homeFrag;
        }

        @Override
        public int getCount()
        {
            return 2;
        }
    }
}
