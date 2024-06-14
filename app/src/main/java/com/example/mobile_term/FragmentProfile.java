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

public class FragmentProfile extends Fragment {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    private TextView consumptionCostTextView;
    private TextView incomeCostTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_profile, container, false);

        // 텍스트 뷰 초기화
        consumptionCostTextView = view.findViewById(R.id.consumption_cost);
        incomeCostTextView = view.findViewById(R.id.income_cost);

        // DB 초기화
        dbHelper = new DBHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 소비와 소득 합계 설정
        setCostSums();

        return view;
    }

    private void setCostSums() {
        // 소비 합계 계산
        int totalConsumption = calculateTotalCost("소비");
        consumptionCostTextView.setText(formatCurrency(totalConsumption) + "원");

        // 소득 합계 계산
        int totalIncome = calculateTotalCost("소득");
        incomeCostTextView.setText(formatCurrency(totalIncome) + "원");
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

    private String formatCurrency(int amount) {
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(amount);
    }
}
