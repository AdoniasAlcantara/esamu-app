
package org.myopenproject.esamu.domain;

import android.content.Intent;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;
import org.myopenproject.esamu.App;
import org.myopenproject.esamu.presentation.firstaid.FirstAidActivity;

public class NotificationReceiver implements
        OneSignal.NotificationReceivedHandler,
        OneSignal.NotificationOpenedHandler {
    private static final String TAG = "NOTIFICATION";

    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;

        // Validate data params

        if (data == null) {
            Log.w(TAG, "Notification received but no content");
            return;
        }

        long id = data.optLong("emergency_id");
        int status = data.optInt("status", -1);
        int attach = data.optInt("attach", -1);

        if (id == 0) {
            Log.w(TAG, "Invalid id value");
            return;
        }

        if (status == -1) {
            Log.w(TAG, "Invalid status value");
            return;
        }

        App app = App.getInstance();

        try (EmergencyGateway gateway = new EmergencyGateway(app)) {
            EmergencyRecord record = gateway.find(id);

            if (record != null) {
                record.setStatus(EmergencyRecord.Status.valueOf(status));
                record.setAttachment(attach);
                gateway.update(record);
                app.getBus().post(App.BUS_REFRESH_HISTORY);
            } else {
                Log.w(TAG, "Emergency not found. Id " + id);
            }
        }
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;

        // User tapped action button
        if (actionType == OSNotificationAction.ActionType.ActionTaken
                && result.action.actionID.equals("1")
                && data != null) {
            int attach = data.optInt("attach", -1);

            // When there is any attach, then show up firstAidActivity
            if (attach >= 0) {
                App app = App.getInstance();
                Intent it = new Intent(app, FirstAidActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                it.putExtra(FirstAidActivity.PARAM_ATTACH, attach);
                app.startActivity(it);
            }
        }
    }
}
