package org.myopenproject.esamu.widget;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.myopenproject.esamu.R;

public abstract class NavDrawerActivity extends AppCompatActivity
{
    protected DrawerLayout drawerLayout;
    private NavigationView navView;

    @SuppressWarnings("ConstantConditions")
    protected void setupToolbarAndNavDrawer()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.navDrawer);

        // Bind drawer layout to toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_open,
            R.string.nav_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation drawer events
        navView = findViewById(R.id.navContent);
        navView.setNavigationItemSelectedListener(this::onNavItemSelected);
    }

    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected void setSelectedItem(int item)
    {
        Menu menu = navView.getMenu();

        if (item >= 0 && item < menu.size()) {
            menu.getItem(item).setChecked(true);
        }
    }

    protected void startActivity(Class<? extends Activity> activityClass)
    {
        startActivity(new Intent(this, activityClass));
    }

    protected abstract boolean onNavItemSelected(MenuItem item);
}
