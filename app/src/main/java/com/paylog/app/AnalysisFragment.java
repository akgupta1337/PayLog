package com.paylog.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalysisFragment extends Fragment {

    private PieChart pieChart;
    private LineChart lineChart;
    private DatabaseHelper myDb;
    private String currentDate;
    private Button monthButton;
    private Button todayButton;
    private Button currDateBtnView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_analysis, container, false);

        pieChart = view.findViewById(R.id.PieChart);
        lineChart = view.findViewById(R.id.LineChart);
        monthButton = view.findViewById(R.id.month);
        todayButton = view.findViewById(R.id.today);
        currDateBtnView = view.findViewById(R.id.curr_date);
        myDb = new DatabaseHelper(getContext());
        todayButton.setSelected(true);

        LocalDate Date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        currentDate = Date.format(formatter);

        currDateBtnView.setText(currentDate);
        toggle();


        currDateBtnView.setOnClickListener(view1 -> {
            if (todayButton.isSelected()) {
                showDateSelectionDialog();
            } else{
                showMonthSelectionDialog();
            }
        });
        setUpPieChart(currentDate);
        setUpLineChart(currentDate);
        return view;
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
            todayButton.setSelected(false);
            LocalDate Date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            String currentDate2 = Date.format(formatter);
            currDateBtnView.setText(currentDate2);
            setUpPieChart(currentDate2);
            setUpLineChart(currentDate2);


            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            todayButton.setTranslationZ(-90f);
        });

        todayButton.setOnClickListener(v -> {
            todayButton.setSelected(true);
            currDateBtnView.setText(currentDate);
            setUpPieChart(currentDate);
            setUpLineChart(currentDate);

            todayButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark)));
            todayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            monthButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light)));
            monthButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            todayButton.setTranslationZ(0f);
        });
    }


    private void setUpLineChart(String datee) {
        ArrayList<String> dateLabels = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();
        Cursor cursor = myDb.getTodayData(datee);

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat;
        if(todayButton.isSelected()){
             outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        }
        else{
             outputFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        }

        int index = 0;
        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(cursor.getColumnIndex("DATE"));
                float amount = cursor.getFloat(cursor.getColumnIndex("AMOUNT"));
                try {
                    Date date = inputFormat.parse(dateString);
                    String formattedDate = outputFormat.format(date);
                    dateLabels.add(formattedDate);
                    entries.add(new Entry(index, amount));
                    index++;
                } catch (ParseException e) {
                    e.printStackTrace();
                    // Skip this entry if date parsing fails
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        myDb.close();

        LineDataSet lineDataSet = new LineDataSet(entries, "Money Spent");

        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Format the value as INR
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                format.setMaximumFractionDigits(0);
                format.setMinimumFractionDigits(0);
                return format.format(value);
            }
        });

        // Make sure values are shown on the chart
        lineDataSet.setDrawValues(true);
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawFilled(true); // Enable filling below the line
        lineDataSet.setFillColor(Color.parseColor("#C6FFC1")); // Set fill color
        lineDataSet.setFillAlpha(80); // Set the alpha for fill color
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true); // Draw circles at data points
        lineDataSet.setCircleColor(Color.parseColor("#34656D")); // Circle color
        lineDataSet.setCircleRadius(6f); // Circle radius
        lineDataSet.setValueTextSize(15f); // Size of value text
        lineDataSet.setValueTextColor(Color.BLACK); // Color of value text
        lineDataSet.setDrawValues(true); // Draw values on points
        lineDataSet.setLineWidth(0.12f); // Set the line width (adjust the value as needed)

        lineChart.getXAxis().setDrawGridLines(true);
        lineChart.setDrawBorders(false); // Disable borders around the chart
        lineChart.animateY(2000, Easing.EaseInOutCubic);
        lineChart.setExtraBottomOffset(2f); // Adjust the bottom offset as needed
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setScaleEnabled(false);

        lineChart.setVisibleXRangeMaximum(6);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGridLineWidth(0.2f);
        xAxis.setGridColor(Color.BLUE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set X-axis to bottom
        xAxis.setGranularity(1f); // Set granularity to avoid overlapping labels
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setGridColor(Color.parseColor("#34656D"));

        // Configure Y-axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setGranularity(100f); // Set granularity for Y-axis
        yAxis.setEnabled(false);
        yAxis.setDrawLabels(false);

        lineChart.setExtraLeftOffset(25f);
        lineChart.setExtraRightOffset(25f);
        lineChart.setExtraBottomOffset(10f);

        // If you have a right Y-axis (optional)
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis if not needed

        // Remove description label
        lineChart.getDescription().setEnabled(false);

        // Disable the legend if it's not needed
        lineChart.getLegend().setEnabled(false);

        // Refresh the chart
        lineChart.invalidate();
    }


    private void setUpPieChart(String date) {
        ArrayList<PieEntry> pieEntryList = new ArrayList<>();
        Cursor cursor = myDb.getSubPurposeSumByDate(date);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String subPurpose = cursor.getString(cursor.getColumnIndex("SUB_PURPOSE"));
                    float totalAmount = cursor.getFloat(cursor.getColumnIndex("TOTAL_AMOUNT"));
                    pieEntryList.add(new PieEntry(totalAmount, subPurpose));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "");
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setColors(mycolors.customColors);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setSliceSpace(2);
        pieData.setValueTextSize(12f);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Format the value as INR
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                format.setMaximumFractionDigits(0);
                format.setMinimumFractionDigits(0);
                return format.format(value);
            }
        });        pieChart.setData(pieData);
        pieChart.animateY(1000, Easing.EaseInOutCubic);
        pieChart.setDrawHoleEnabled(true);
        pieDataSet.setDrawValues(true);
        pieChart.setDrawEntryLabels(true);
        pieChart.setHoleRadius(50f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setTransparentCircleRadius(70f);

        int totalValue = 0;
        for (PieEntry entry : pieEntryList) {
            totalValue += entry.getValue();
        }

        pieChart.setCenterText(formatToINR(String.valueOf(totalValue)));
        pieChart.setCenterTextSize(15f);
        pieChart.setCenterTextColor(Color.parseColor("#334443"));
        pieChart.getLegend().setEnabled(false);;


        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieData.setValueTextColor(Color.BLACK);
        pieDataSet.setValueLinePart1OffsetPercentage(50f);
        pieDataSet.setUsingSliceColorAsValueLineColor(true);
        pieDataSet.setValueLinePart1Length(0.7f);
        pieDataSet.setValueLinePart2Length(0.2f);
        pieDataSet.setValueTextColor(Color.parseColor("#334443"));
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieChart.setEntryLabelColor(Color.parseColor("#34656D"));

        float offset = 45.0f;
        pieChart.setExtraTopOffset(offset);
        pieChart.setExtraLeftOffset(offset);
        pieChart.setExtraRightOffset(offset);

        pieChart.setMinAngleForSlices(30f);


        pieChart.invalidate();
    }
    public String formatToINR(String amount2) {
        Integer amount = Integer.parseInt(amount2);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setMaximumFractionDigits(0);
        String formattedAmount = currencyFormat.format(amount);
        return formattedAmount.replace("₹", "₹ ");
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
            setUpPieChart(formattedDate);
            setUpLineChart(formattedDate);


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
            currDateBtnView.setText(formattedDate);
            setUpPieChart(formattedDate);
            setUpLineChart(formattedDate);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }



}