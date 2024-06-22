package com.example.mobile_term;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FragmentCalender extends Fragment {

    private CalendarView calendarView;
    private TextView dayConsumption;
    private TextView dayIncome;
    private Button dayLogButton;
    private ArrayList<MyData> moneyLogs;
    private MyAdapter adapter;
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_calender, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        dayConsumption = view.findViewById(R.id.day_consumption);
        dayIncome = view.findViewById(R.id.day_income);
        dayLogButton = view.findViewById(R.id.day_log);

        moneyLogs = new ArrayList<>();
        adapter = new MyAdapter(requireContext(), moneyLogs);

        dbHelper = new DBHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 현재 날짜를 설정하고 로드
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadData(selectedDate);
        calendarView.setDate(System.currentTimeMillis(), false, true);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // 날짜 형식을 yyyy-MM-dd로 맞춤
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadData(selectedDate);
        });

        dayLogButton.setOnClickListener(v -> showDayLogDialog());

        return view;
    }

    private void loadData(String date) {
        moneyLogs.clear();

        int totalConsumption = 0;
        int totalIncome = 0;

        String query = "SELECT type, cost, detail FROM money_log WHERE date = ?";
        String[] selectionArgs = { date };
        Cursor cursor = db.rawQuery(query, selectionArgs);
        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            int cost = cursor.getInt(cursor.getColumnIndexOrThrow("cost"));
            String detail = cursor.getString(cursor.getColumnIndexOrThrow("detail"));
            moneyLogs.add(new MyData(type, cost, detail));

            if (type.equals("소비")) totalConsumption += cost;
            else if (type.equals("소득")) totalIncome += cost;
        }
        cursor.close();

        dayConsumption.setText("소비 : " + formatCurrency(totalConsumption) + "원");
        dayIncome.setText("소득 : " + formatCurrency(totalIncome) + "원");

        adapter.notifyDataSetChanged();

        if (moneyLogs.isEmpty()) {
            Toast.makeText(requireContext(), "해당 날짜에 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDayLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("소득/소비 내역");

        View dialogView = getLayoutInflater().inflate(R.layout.money_log_dialog, null);
        ListView dialogListView = dialogView.findViewById(R.id.day_money_log);
        MyAdapter dialogAdapter = new MyAdapter(requireContext(), moneyLogs);
        dialogListView.setAdapter(dialogAdapter);

        builder.setView(dialogView);
        builder.setPositiveButton("닫기", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private String formatCurrency(int amount) {     // 입력 값을 화폐 단위로 표시
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(amount);
    }
}
