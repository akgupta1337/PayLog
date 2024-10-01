package com.paylog.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class EmailCheckingService extends Service {
    public static final String CHANNEL_ID = "PAYLOG_CHANNEL";
    private static final String TAG = "EmailCheckingService";
    private Handler handler;
    private Runnable mailChecker;
    private final String stringMailHost = "imap.gmail.com";
    private Session session;
    private Store store;
    private Properties properties;
    private final String stringUserName = "your google user name here";
    private final String stringPassword = "your google app password here";
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    DatabaseHelper myDB;
    private boolean isConnected = false;
    public static boolean isStored = true;
    public static Integer amount;
    public static String merchant;
    public static String formattedDate;
    public static String Subpurpose;
    public static String purpose;
    boolean stopServiceScheduled = false;
    private Runnable stopServiceRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        createNotificationChannel1();
        createNotificationChannel();
        myDB = new DatabaseHelper(this);


        // Runnable to stop the service
        stopServiceRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isStored) {
                    handler.removeCallbacks(stopServiceRunnable);
                    stopServiceScheduled = false;
                    return;
                }
                // Create an intent for the service you want to stop
                Intent intent = new Intent(getApplicationContext(), EmailCheckingService.class);
                stopServiceScheduled = false;
                stopService(intent);
                

            }
        };

        // Runnable for checking mail
        Runnable mailChecker = new Runnable() {
            @Override
            public void run() {
                if (AppState.isAppInBackground && !stopServiceScheduled) {
                    handler.postDelayed(stopServiceRunnable, 60000);
                    stopServiceScheduled = true;
                }
                if (isStored) {
                    readGmailInBackground();
                }
                handler.postDelayed(this, 1000); // 1 second delay
            }
        };

        handler.post(mailChecker);

        startForeground(1, getNotification("Tracking Payments :)"));


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("reply_text")) {
            Subpurpose = intent.getStringExtra("reply_text").strip();

            CallGemini.call(Subpurpose, new GeminiCallback() {
                @Override
                public void onResult(String result) {
                    purpose = result;  // Assign the result to purpose
                    addDataInBackground();  // Call this method after getting the result
                }

                @Override
                public void onError(Throwable t) {
                    addDataInBackground();
                    // Handle failure if necessary
                }
            });
        }
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PayLog Channel";
            String description = "Channel for PayLog notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Integer amount, String merchant) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        CharSequence replyLabel = "Enter Purpose Here";
        List<String> categories = myDB.getUniqueSubPurposes(merchant);

        // Default choices if the list is empty
        String[] choices;
        if (categories.isEmpty()) {
            choices = new String[]{"Paratha", "Bus", "Textbooks", "Medicine", "Juice"};
        } else {
            choices = categories.toArray(new String[0]);
        }

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .setChoices(choices)
                .build();

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notification, // Ensure you have this drawable resource
                replyLabel,
                replyPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();

        RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.notification_big_content);
        bigContentView.setTextViewText(R.id.title, "Sent " + formatToINR(Integer.toString(amount)) + " to " + merchant);
        bigContentView.setTextViewText(R.id.content, "Enter Reason for Purchase");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ensure you have this drawable resource
                .setContentTitle("Purchase Notification")
                .setContentText("What's the purpose of this purchase?")
                .setContentIntent(replyPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomBigContentView(bigContentView)
                .setCustomContentView(bigContentView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true)
                .addAction(replyAction);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
    }

    public synchronized void readGmailInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                readGmail();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Any UI updates, if needed
            }
        }.execute();
    }
    public synchronized void readGmail() {
        isStored = false;
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        properties = System.getProperties();
        if (properties == null) {
            return;
        } else {
            properties.setProperty("mail.store.protocol", "imaps");
        }
        Folder folder = null;

        try {
            if (!isConnected) {
                session = Session.getDefaultInstance(properties, null);
                store = session.getStore("imaps");
                store.connect(stringMailHost, stringUserName, stringPassword);
                isConnected = true;
            }

            folder = store.getFolder("DebitUPI");
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();

            if (messages.length == 0) {
                isStored = true;
                return;
            }

            // Define the date format you want
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

            Message message = messages[0];
            String subject = message.getSubject();
            Date receivedDate = message.getReceivedDate();
            String[] subarr = subject.split(" ", 5);

            amount = Integer.parseInt(subarr[2]);
            merchant = subarr[4];
            formattedDate = dateFormat.format(receivedDate);
            purpose = myDB.getMerchantPurpose(merchant);

            showNotification(amount, merchant);
            message.setFlag(Flags.Flag.DELETED, true);


        } catch (NoSuchProviderException e) {
            isConnected = false;
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
            isConnected = false;
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
            } catch (MessagingException e) {
                e.printStackTrace(); // Handle or log the exception
            }
        }
    }


    private void addDataInBackground() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return myDB.insertData(formattedDate, merchant, amount, Subpurpose, purpose);
            }

            @Override
            protected void onPostExecute(Boolean isInserted) {
                isStored=true;
            }
        }.execute();
    }

    public static String formatToINR(String amount2) {
        Integer amount = Integer.parseInt(amount2);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setMaximumFractionDigits(0);
        String formattedAmount = currencyFormat.format(amount);
        return formattedAmount.replace("₹", "₹ ");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mailChecker);
        handler.removeCallbacks(stopServiceRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PayLog is active")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();
    }

    private void createNotificationChannel1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Email Checking Service Channel";
            String description = "Channel for Email Checking Service";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // Ensure visibility on lock screen
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
