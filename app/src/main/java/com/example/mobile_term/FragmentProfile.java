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

    private TextView consumptionCostText;
    private TextView incomeCostText;
    private TextView savingCostText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_profile, container, false);

        // 텍스트 뷰 초기화
        consumptionCostText = view.findViewById(R.id.consumption_cost);
        incomeCostText = view.findViewById(R.id.income_cost);
        savingCostText = view.findViewById(R.id.saving_cost);

        // DB 초기화
        dbHelper = new DBHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // 소비와 소득 합계 설정
        setCostSums();


        return view;
    }

    private void setCostSums() {
        // 소비 합계 계산
        int totalConsumption = calculateAvgCost("소비");
        consumptionCostText.setText(formatCurrency(totalConsumption));

        // 소득 합계 계산
        int totalIncome = calculateAvgCost("소득");
        incomeCostText.setText(formatCurrency(totalIncome));

        // 저축 합계 계산
        int totalSaving = 0;
        if (calculateTotalCost("소득") > calculateTotalCost("소비"))
            totalSaving = calculateTotalCost("소득") - calculateTotalCost("소비");
        savingCostText.setText(formatCurrency(totalSaving));
    }

    private int calculateAvgCost(String type) { // 소득, 소비 평균 구하기
        int costAvg = 0;
        String query = "SELECT AVG(cost) as total FROM money_log WHERE type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{type});
        if (cursor.moveToFirst()) {
            costAvg = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        return costAvg;
    }

    private int calculateTotalCost(String type) {   // 총 저축 액 구하기
        int totalCost = 0;
        String query = "SELECT SUM(cost) as total FROM money_log WHERE type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{type});
        if (cursor.moveToFirst()) {
            totalCost = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        return totalCost;
    }

    private String formatCurrency(int amount) {     // 입력 값을 화폐 단위로 표시
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(amount) + "원";
    }
}
