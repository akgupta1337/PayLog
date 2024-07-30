package com.paylog.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class ExportFragment extends Fragment {
    private Button monthButton;
    private Button todayButton;
    private Button currDateBtnView;
    private TableLayout tableLayout;
    private Button exportButton;
    String currentDate;
    DatabaseHelper myDB;
    private String exportDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_export, container, false);

        monthButton = view.findViewById(R.id.month);
        todayButton = view.findViewById(R.id.today);
        tableLayout = view.findViewById(R.id.tableLayout);
        currDateBtnView = view.findViewById(R.id.curr_date);
        exportButton = view.findViewById(R.id.export_button);
        myDB = new DatabaseHelper(view.getContext());

        todayButton.setSelected(true);
        LocalDate Date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        currentDate = Date.format(formatter);
        currDateBtnView.setText(currentDate);
        exportDate = currentDate;


        toggle();
        DisplayTableContent();

        currDateBtnView.setOnClickListener(view1 -> {
            if (todayButton.isSelected()) {
                showDateSelectionDialog();
            } else{
                showMonthSelectionDialog();
            }
        });

        exportButton.setOnClickListener(view1 -> {
                        myDB.exportData(getContext(),exportDate);
                        DisplayTableContent();
                }
        );
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        DisplayTableContent();
    }



    public void DisplayTableContent(){
        tableLayout.removeAllViews();
        TableRow tableRow2 = new TableRow(getContext());
        TextView TextView = new TextView(getContext());

        TextView.setText("Recent Exports: ");
        TextView.setTextColor(Color.parseColor("#176B87"));
        TextView.setTextSize(22);
        TextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));
        tableRow2.addView(TextView);
        tableLayout.addView(tableRow2);


        File folder = new File(getContext().getFilesDir(), "PayLog_Exports");
        File[] files = folder.listFiles();
        if (files != null) {
            if (files.length == 0) {
                TableRow tableRow = new TableRow(getContext());
                TextView noFilesTextView = new TextView(getContext());
                noFilesTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));
                noFilesTextView.setTextSize(20);
                noFilesTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark));
                noFilesTextView.setText("No Files Found!");
                tableRow.addView(noFilesTextView);
                tableLayout.addView(tableRow);
            } else {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".csv")) {
                        TableRow tableRow = new TableRow(getContext());
                        Button fileNameButton = new Button(getContext());
                        fileNameButton.setOnClickListener(view -> myDB.openFile(file));
                        fileNameButton.setText(file.getName());
                        fileNameButton.setTextColor(ContextCompat.getColor(getContext(), R.color.dark2));
                        fileNameButton.setTextSize(20);
                        fileNameButton.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));
                        fileNameButton.setBackgroundColor(Color.TRANSPARENT);
                        fileNameButton.setPaintFlags(fileNameButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        fileNameButton.setTypeface(fileNameButton.getTypeface(), Typeface.ITALIC);
                        fileNameButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_open_in_new_24, 0);
                        fileNameButton.setTextAlignment(2);


                        tableRow.addView(fileNameButton);
                        tableLayout.addView(tableRow);
                    }
                }
            }
        } else {
            TableRow tableRow = new TableRow(getContext());
            TextView errorTextView = new TextView(getContext());
            errorTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.baloo));
            errorTextView.setTextSize(20);
            errorTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark));
            errorTextView.setText("Unable to access folder.");
            tableRow.addView(errorTextView);
            tableLayout.addView(tableRow);
        }


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
            exportDate = currentDate2;
            todayButton.setSelected(false);
            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            todayButton.setTranslationZ(-90f);
        });

        todayButton.setOnClickListener(v -> {
            exportDate = currentDate;
            currDateBtnView.setText(currentDate);
            todayButton.setSelected(true);
            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark2)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            todayButton.setTranslationZ(0f);
        });
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
            currDateBtnView.setText(formattedDate);
            exportDate = formattedDate;
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
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
            currDateBtnView.setText(formattedDate);
            exportDate = formattedDate;


        }, year, month, day);

        datePickerDialog.show();
    }

}