package org.myopenproject.esamu.domain;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.onesignal.OneSignal;
import com.squareup.otto.Bus;

import org.myopenproject.esamu.common.UserDto;

public class App extends Application {
    private static App singleton;
    private UserDto user;
    private Bus bus;

    public static App getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        bus = new Bus();

        // Initialize OneSignal notifications
        NotificationReceiver receiver = new NotificationReceiver();
        OneSignal.startInit(this)
                .setNotificationReceivedHandler(receiver)
                .setNotificationOpenedHandler(receiver)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    public boolean isUserRegistered() {
        return getUser() != null;
    }

    public UserDto getUser() {
        if (user != null)
            return user;

        // Restore user from SharedPreferences
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("user_credentials", Context.MODE_PRIVATE);

        if (prefs.contains("id")) {
            user = new UserDto();
            user.setId(prefs.getString("id", ""));
            user.setName(prefs.getString("name", ""));
            user.setPhone(prefs.getString("phone", ""));
            user.setNotificationKey(prefs.getString("notification", ""));
        }

        return user;
    }

    public void saveUser(UserDto user) {
        this.user = user;

        // Persist user to SharedPreferences
        SharedPreferences.Editor editor = getApplicationContext()
                .getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                .edit();

        editor.putString("id", user.getId());
        editor.putString("name", user.getName());
        editor.putString("phone", user.getName());
        editor.putString("notification", user.getNotificationKey());
        editor.apply();
    }

    public Bus getBus() {
        return bus;
    }
}
