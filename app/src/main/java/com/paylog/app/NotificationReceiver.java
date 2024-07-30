package com.paylog.app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence replyText = remoteInput.getCharSequence(EmailCheckingService.KEY_TEXT_REPLY);

            Intent updateIntent = new Intent(context, EmailCheckingService.class);
            updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            updateIntent.putExtra("reply_text", replyText.toString());
            context.startService(updateIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            // Create an updated notification indicating that the reply was sent
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EmailCheckingService.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Purpose sent")
                    .setContentText("Purchase for " + replyText + " is recorded.")
                    .setAutoCancel(true);

            // Show the updated notification with a new ID
            notificationManager.notify(1, builder.build());
        }
    }
}
