package org.myopenproject.esamu.widget;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.util.Dialog;

public abstract class NavDrawerActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;

    @SuppressWarnings("ConstantConditions")
    protected void setupToolbarAndNavDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.navDrawer);

        // Bind drawer layout to toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation drawer events
        NavigationView navContent = findViewById(R.id.navContent);
        navContent.setNavigationItemSelectedListener(this::onNavItemSelected);
    }

    protected boolean onNavItemSelected(MenuItem item) {
        String msg = getString(R.string.error_not_implemented);

        switch (item.getItemId()) {
            case R.id.navHome:
                Dialog.toast(this, msg);
                break;

            case R.id.navSettings:
                Dialog.toast(this, msg);
                break;

            case R.id.navAbout:
                Dialog.toast(this, msg);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
