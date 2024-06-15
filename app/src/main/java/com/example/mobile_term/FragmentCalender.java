package com.example.mobile_term;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FragmentCalender extends Fragment {

    private CalendarView calendarView;
    private ListView calenderListView;
    private ArrayList<MyData> moneyLogs;
    private MyAdapter adapter;
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_calender, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        calenderListView = view.findViewById(R.id.calender_list);
        moneyLogs = new ArrayList<MyData>();
        adapter = new MyAdapter(requireContext(), moneyLogs);
        calenderListView.setAdapter(adapter);

        dbHelper = new DBHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 현재 날짜를 설정하고 로드
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadData(currentDate);
        calendarView.setDate(System.currentTimeMillis(), false, true);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // 날짜 형식을 yyyy-MM-dd로 맞춤
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadData(selectedDate);
        });

        return view;
    }

    private void loadData(String date) {
        moneyLogs.clear();
        String query = "SELECT type, cost, detail FROM money_log WHERE date = ?";
        String[] selectionArgs = { date };
        Cursor cursor = db.rawQuery(query, selectionArgs);
        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            int cost = cursor.getInt(cursor.getColumnIndexOrThrow("cost"));
            String detail = cursor.getString(cursor.getColumnIndexOrThrow("detail"));
            moneyLogs.add(new MyData(type, cost, detail));
        }
        cursor.close();
        adapter.notifyDataSetChanged();

        if (moneyLogs.isEmpty()) {
            Toast.makeText(requireContext(), "해당 날짜에 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
