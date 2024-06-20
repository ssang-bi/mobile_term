package com.example.mobile_term;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class FragmentProfile extends Fragment {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    private TextView consumptionCostText;
    private TextView incomeCostText;
    private TextView thisMonthConsumptionText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_profile, container, false);

        // 텍스트 뷰 초기화
        consumptionCostText = view.findViewById(R.id.consumption_cost);
        incomeCostText = view.findViewById(R.id.income_cost);
        thisMonthConsumptionText = view.findViewById(R.id.month_consumption_cost);

        // DB 초기화
        dbHelper = new DBHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 소비와 소득 합계 설정
        setCostSums();

        // 이번 달 소비 설정
        setThisMonthConsumption();

        return view;
    }

    private void setCostSums() {
        // 소비 합계 계산
        int totalConsumption = calculateTotalCost("소비");
        consumptionCostText.setText(formatCurrency(totalConsumption));

        // 소득 합계 계산
        int totalIncome = calculateTotalCost("소득");
        incomeCostText.setText(formatCurrency(totalIncome));
    }

    private void setThisMonthConsumption() {
        // 현재 년도와 월 구하기
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0부터 시작하므로 1을 더해줌

        // 이번 달의 첫 날과 마지막 날 구하기
        String startDate = String.format(Locale.getDefault(), "%04d-%02d-01", year, month);
        cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        // 이번 달 소비 합계 계산
        int thisMonthConsumption = calculateTotalCostForDateRange("소비", startDate, endDate);
        thisMonthConsumptionText.setText(formatCurrency(thisMonthConsumption));
    }

    private int calculateTotalCost(String type) {
        int totalCost = 0;
        String query = "SELECT SUM(cost) as total FROM money_log WHERE type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{type});
        if (cursor.moveToFirst()) {
            totalCost = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        return totalCost;
    }

    private int calculateTotalCostForDateRange(String type, String startDate, String endDate) {
        int totalCost = 0;
        String query = "SELECT SUM(cost) as total FROM money_log WHERE type = ? AND date BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{type, startDate, endDate});
        if (cursor.moveToFirst()) {
            totalCost = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        return totalCost;
    }

    private String formatCurrency(int amount) {
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(amount) + "원";
    }
}
