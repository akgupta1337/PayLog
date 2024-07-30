package com.paylog.app;


import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    DatabaseHelper myDB;
    private TextView myTextView;
    private Button monthButton;
    private Button todayButton;
    private Button currDateBtnView;
    private TableLayout tableLayout;
    private  Button payNow;
    String currentDate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        myTextView = view.findViewById(R.id.toolbar_money);
        monthButton = view.findViewById(R.id.month);
        todayButton = view.findViewById(R.id.today);
        tableLayout = view.findViewById(R.id.tableLayout);
        currDateBtnView = view.findViewById(R.id.curr_date);
        payNow = view.findViewById(R.id.payNow);
        payNow.setOnClickListener(view1->{
            Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.phonepe.app");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(getContext(), "PhonePe app is not installed", Toast.LENGTH_SHORT).show();
            }
        });


        myDB = new DatabaseHelper(view.getContext());
        todayButton.setSelected(true);
        LocalDate Date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        currentDate = Date.format(formatter);

        currDateBtnView.setText(currentDate);


        toggle();
        startCountAnimation(currentDate);
        DisplayTableContent(currentDate);

        currDateBtnView.setOnClickListener(view1 -> {
            if (todayButton.isSelected()) {
                showDateSelectionDialog();
            } else{
                showMonthSelectionDialog();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppState.isAppInBackground = false;
        startEmailCheckingService();
        DisplayTableContent(currentDate);
        startCountAnimation(currentDate);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppState.isAppInBackground = true;
    }

    private void startEmailCheckingService() {
        Intent serviceIntent = new Intent(getActivity(), EmailCheckingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(serviceIntent);
        }
    }


    public void DisplayTableContent(String date){
        tableLayout.removeAllViews();

        TableRow tableRow2 = new TableRow(getContext());
        TextView TextView = new TextView(getContext());

        TextView.setText("Recent Transactions: ");
        TextView.setTextColor(Color.parseColor("#34656D"));
        TextView.setTextSize(22);
        TextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));

        tableRow2.addView(TextView);
        tableLayout.addView(tableRow2);
        Cursor cursor = myDB.getTodayData(date);
        if (cursor.getCount() == 0) {
            cursor.close();
            TableRow tableRow = new TableRow(getContext());
            TextView purposeTextView = new TextView(getContext());
            purposeTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));
            purposeTextView.setTextSize(20);
            purposeTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark));
            purposeTextView.setText("No Purchase!");
            tableRow.addView(purposeTextView);
            tableLayout.addView(tableRow);
            return;
        }
        while (cursor.moveToNext())
        {
            String purpose = cursor.getString(3);
            String amount = formatToINR(cursor.getString(2));
            TableRow tableRow = new TableRow(getContext());
            TextView purposeTextView = new TextView(getContext());
            TextView amountTextView = new TextView(getContext());

            purposeTextView.setText(purpose);
            purposeTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark));
            purposeTextView.setTextSize(20);
            purposeTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));
            amountTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));

            amountTextView.setText(amount);
            amountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark));
            amountTextView.setTextSize(20);

            tableRow.addView(purposeTextView);
            tableRow.addView(amountTextView);
            tableLayout.addView(tableRow);
        }
            cursor.close();
    }


    public void toggle(){

        // Set default states
        if(todayButton.isSelected()){
            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }


        // Set click listeners
        monthButton.setOnClickListener(v -> {
            LocalDate Date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            String currentDate2 = Date.format(formatter);
            currDateBtnView.setText(currentDate2);
            DisplayTableContent(currentDate2);
            startCountAnimation(currentDate2);


            todayButton.setSelected(false);
            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            todayButton.setTranslationZ(-90f);
        });

        todayButton.setOnClickListener(v -> {
            todayButton.setSelected(true);
            DisplayTableContent(currentDate);
            currDateBtnView.setText(currentDate);
            startCountAnimation(currentDate);

            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            todayButton.setTranslationZ(0f);
        });
    }

    public static String formatToINR(String amount2) {
        Integer amount = Integer.parseInt(amount2);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setMaximumFractionDigits(0);
        String formattedAmount = currencyFormat.format(amount);
        return formattedAmount.replace("₹", "₹ ");
    }


    private void startCountAnimation(String date) {
        Integer amount = myDB.getTotalSpendingDateWise(date);
        ValueAnimator animator = ValueAnimator.ofInt(0, amount);
        animator.setDuration(700);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                myTextView.setText(formatToINR(animation.getAnimatedValue().toString()));
            }
        });
        animator.start();
    }

    private void showDateSelectionDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            // Create a Calendar object with the selected date
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);



            // Format the date as "10 Jul 2024"
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedDate.getTime());
            DisplayTableContent(formattedDate);
            currDateBtnView.setText(formattedDate);
            startCountAnimation(formattedDate);


        }, year, month, day);

        datePickerDialog.show();
    }

    public void showMonthSelectionDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_year_month_picker, null);
        builder.setView(dialogView);

        NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);
        NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);

        // Set up the year picker
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(2100);
        yearPicker.setValue(year);

        // Set up the month picker
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        monthPicker.setValue(month);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int selectedYear = yearPicker.getValue();
            int selectedMonth = monthPicker.getValue();

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, 1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedDate.getTime());
            DisplayTableContent(formattedDate);
            currDateBtnView.setText(formattedDate);
            startCountAnimation(formattedDate);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }



}
