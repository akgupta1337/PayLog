package com.paylog.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PAY_LOG_DB";
    private static final String TABLE_NAME = "PAY_LOG_DATA";
    private static final String Date = "DATE";
    private static final String Merchant = "MERCHANT";
    private static final String Amount = "AMOUNT";
    private static final String Purpose = "PURPOSE";
    private static final String Sub_Purpose = "SUB_PURPOSE";
    private static final Map<String, String> MONTH_MAP = new HashMap<>();
    private Context context;
    static {
        MONTH_MAP.put("January", "Jan");
        MONTH_MAP.put("February", "Feb");
        MONTH_MAP.put("March", "Mar");
        MONTH_MAP.put("April", "Apr");
        MONTH_MAP.put("May", "May");
        MONTH_MAP.put("June", "Jun");
        MONTH_MAP.put("July", "Jul");
        MONTH_MAP.put("August", "Aug");
        MONTH_MAP.put("September", "Sep");
        MONTH_MAP.put("October", "Oct");
        MONTH_MAP.put("November", "Nov");
        MONTH_MAP.put("December", "Dec");
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(DATE TEXT PRIMARY KEY, MERCHANT TEXT, AMOUNT INTERGER, SUB_PURPOSE TEXT, PURPOSE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    private boolean isEntryExists2(SQLiteDatabase db, String query, String[] selectionArgs) {
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.rawQuery(query, selectionArgs);
            exists = (cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception or handle it as needed
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return exists;
    }

    public boolean insertData(String date, String merchant, Integer amount, String subPurpose, String purpose) {

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + Date + " = ?";
        if (isEntryExists2(db, query, new String[]{date})) {
            return false;
        }


        ContentValues values = new ContentValues();
        values.put(Date, date);
        values.put(Merchant, merchant);
        values.put(Amount, amount);
        values.put(Sub_Purpose, subPurpose);
        values.put(Purpose, purpose);

        long var = db.insert(TABLE_NAME, null, values);
        return var != -1;
    }



    public String getMerchantPurpose(String merchant) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + Purpose + " FROM " + TABLE_NAME + " WHERE " + Merchant + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{merchant});

        if (cursor.moveToFirst()) {
            String purpose = cursor.getString(0);
            cursor.close();
            return purpose;
        } else {
            cursor.close();
            return null;
        }
    }

    public String getMerchantSubPurpose(String merchant) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + Sub_Purpose + " FROM " + TABLE_NAME + " WHERE " + Merchant + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{merchant});

        if (cursor.moveToFirst()) {
            String Sub_purpose = cursor.getString(0);
            cursor.close();
            return Sub_purpose;
        } else {
            cursor.close();
            return null;
        }
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

    public boolean isEntryExists(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public Cursor getTotalSpentByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DATE, SUM(AMOUNT) as AMOUNT " +
                "FROM " + TABLE_NAME + " " +
                "WHERE DATE LIKE '%" + date + "%' " +  // Using LIKE for the date comparison
                "GROUP BY DATE";
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }




    public Cursor getTodayData(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + Date + " LIKE '%" + date + "%'";
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }
    public String getFormattedTodayData(String currentDate) {

        Cursor cursor = getTodayData(currentDate);
        StringBuilder formattedData = new StringBuilder();

        // Iterate through the cursor and format the data
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex("DATE")).trim();
                String merchant = cursor.getString(cursor.getColumnIndex("MERCHANT")).trim();
                String amount = cursor.getString(cursor.getColumnIndex("AMOUNT")).trim();
                String purpose = cursor.getString(cursor.getColumnIndex("PURPOSE")).trim();
                String sub_purpose = cursor.getString(cursor.getColumnIndex("SUB_PURPOSE")).trim();


                formattedData.append("Date: ").append(date)
                        .append(", Merchant: ").append(merchant)
                        .append(", Amount: ").append(amount)
                        .append(", Purpose: ").append(purpose)
                        .append(", Sub_Purpose: ").append(sub_purpose)
                        .append("\n");
            } while (cursor.moveToNext());
        }

        cursor.close(); // Close the cursor to prevent memory leaks
        return formattedData.toString(); // Return the formatted string
    }


    public Cursor getSubPurposeSumByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT PURPOSE, SUM(AMOUNT) as TOTAL_AMOUNT " +
                "FROM " + TABLE_NAME + " " +
                "WHERE DATE LIKE '%" + date + "%' " +
                "GROUP BY PURPOSE";
        Cursor cursor =  db.rawQuery(query, null);
        return  cursor;
    }


    public List<String> getUniqueSubPurposes(String merchantName) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT " + Sub_Purpose + " FROM " + TABLE_NAME + " WHERE " + Merchant + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{merchantName});

        if (cursor.moveToFirst()) {
            do {
                String subPurpose = cursor.getString(cursor.getColumnIndex(Sub_Purpose));
                categories.add(subPurpose);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }



    public Integer getTotalSpendingDateWise(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT SUM(amount) as total FROM " + TABLE_NAME + " WHERE " + Date + " LIKE '%" + date + "%'";
        Cursor cursor = db.rawQuery(query, null);

        int totalSpending = 0;
        if (cursor.moveToFirst()) {
            totalSpending = cursor.getInt(cursor.getColumnIndex("total"));
        }
        cursor.close();
        return totalSpending;
    }

    public void exportData(Context context, String date) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + Date + " LIKE '%" + date + "%'";
        if(!isEntryExists(query)){
            Toast.makeText(context, "No data found for the selected date", Toast.LENGTH_SHORT).show();
            return;

        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        File folder = new File(context.getFilesDir(), "PayLog_Exports");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH.mm");
        String currentTime = time.format(formatter);
        String filename = currentTime + "_" + date;


        String formattedFileName;
        if (!filename.endsWith(".csv")) {
            formattedFileName = filename + ".csv";
        } else {
            formattedFileName = filename;
        }
        File file = new File(folder, formattedFileName);
        // Check if the file already exists
        if (file.exists()) {
            Toast.makeText(context, "A file with the same name already exists", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            // Write the header row
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                fileWriter.append(cursor.getColumnName(i));
                if (i < cursor.getColumnCount() - 1) {
                    fileWriter.append(",");
                }
            }
            fileWriter.append("\n");

            // Write data rows
            while (cursor.moveToNext()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    fileWriter.append(cursor.getString(i));
                    if (i < cursor.getColumnCount() - 1) {
                        fileWriter.append(",");
                    }
                }
                fileWriter.append("\n");
            }

            cursor.close();
        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
            Toast.makeText(context, "Error exporting data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void openFile(File file) {
        // Use the context passed to the constructor
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);

        // Create an intent to view the file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getMimeType(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Check if there is an app that can handle the intent
      
            context.startActivity(intent);


    }

    private String getMimeType(File file) {
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(file.getName());
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }


}
