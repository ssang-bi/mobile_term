package com.example.mobile_term;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

        // 리스트뷰 아이템 롱클릭 리스너 추가
        dailyListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            showDeleteDialog(position, currentDate);
            return true;
        });

        return view;
    }

    private void showAddDialog(String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("데이터 추가");

        View dialogView = getLayoutInflater().inflate(R.layout.add_money_log, null);
        builder.setView(dialogView);

        RadioGroup typeRadioGroup = dialogView.findViewById(R.id.type_radio_group);
        EditText costInput = dialogView.findViewById(R.id.cost_input);
        EditText detailInput = dialogView.findViewById(R.id.detail_input);

        costInput.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    costInput.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[,]", "");
                    double parsed = Double.parseDouble(cleanString);
                    String formatted = NumberFormat.getNumberInstance(Locale.getDefault()).format(parsed);

                    current = formatted;
                    costInput.setText(formatted);
                    costInput.setSelection(formatted.length());

                    costInput.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        builder.setPositiveButton("저장", null); // 초기에는 저장 버튼 비활성화 상태로 설정

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                // 저장 버튼 클릭 시 처리할 내용
                int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedTypeButton = dialogView.findViewById(selectedTypeId);
                String type = selectedTypeButton.getText().toString();

                String costText = costInput.getText().toString().trim();
                if (costText.isEmpty()) {
                    Toast.makeText(requireContext(), "금액을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return; // 금액 입력란이 비어있으면 저장하지 않고 리턴
                }

                int cost = Integer.parseInt(costText.replaceAll("[,]", ""));
                String detail = detailInput.getText().toString();

                // DB에 데이터 삽입
                ContentValues values = new ContentValues();
                values.put("date", date);
                values.put("type", type);
                values.put("cost", cost);
                values.put("detail", detail);
                db.insert("money_log", null, values);

                // ListView 갱신
                loadData(date);

                // 다이얼로그 닫기
                dialog.dismiss();
            });
        });

        builder.setNegativeButton("취소", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.show();
    }

    private void showDeleteDialog(int position, String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("삭제 확인");
        builder.setMessage("이 항목을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제", (dialog, which) -> {
            MyData data = moneyLogs.get(position);
            String whereClause = "date = ? AND type = ? AND cost = ?";
            String[] whereArgs = { date, data.type, String.valueOf(data.cost) };
            db.delete("money_log", whereClause, whereArgs);
            // ListView 갱신
            loadData(date);
            Toast.makeText(requireContext(), "항목이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void loadData(String date) {
        moneyLogs.clear();
        // DB에서 데이터 로드 (SQL 쿼리)
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
    }
}
