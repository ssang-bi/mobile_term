package com.example.mobile_term;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FragmentDaily extends Fragment {

    private ListView dailyListView;
    private ArrayList<MyData> moneyLogs;
    private MyAdapter adapter;
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private TextView todayDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_daily, container, false);

        // 오늘 날짜 설정
        todayDate = view.findViewById(R.id.today_date);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        todayDate.setText(currentDate);

        // ListView 초기화
        dailyListView = view.findViewById(R.id.daily_list);
        moneyLogs = new ArrayList<>();
        adapter = new MyAdapter(requireContext(), moneyLogs);
        dailyListView.setAdapter(adapter);

        // DB 초기화
        dbHelper = new DBHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 오늘 날짜의 데이터 로드
        loadData(currentDate);

        // 추가 버튼 클릭 리스너
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> showAddDialog(currentDate));

        return view;
    }

    private void showAddDialog(String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("데이터 추가");

        View dialogView = getLayoutInflater().inflate(R.layout.add_money_log, null);
        builder.setView(dialogView);

        RadioGroup typeRadioGroup = dialogView.findViewById(R.id.type_radio_group);
        EditText costInput = dialogView.findViewById(R.id.cost_input);

        builder.setPositiveButton("저장", (dialog, which) -> {
            int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
            RadioButton selectedTypeButton = dialogView.findViewById(selectedTypeId);
            String type = selectedTypeButton.getText().toString();
            int cost = Integer.parseInt(costInput.getText().toString());

            // DB에 데이터 삽입
            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("type", type);
            values.put("cost", cost);
            db.insert("money_log", null, values);

            // ListView 갱신
            loadData(date);
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void loadData(String date) {
        moneyLogs.clear();
        // DB에서 데이터 로드 (SQL 쿼리)
        String query = "SELECT type, cost FROM money_log WHERE date = ?";
        String[] selectionArgs = { date };
        Cursor cursor = db.rawQuery(query, selectionArgs);
        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            int cost = cursor.getInt(cursor.getColumnIndexOrThrow("cost"));
            moneyLogs.add(new MyData(type, cost));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}
