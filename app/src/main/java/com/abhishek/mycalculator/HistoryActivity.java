package com.abhishek.mycalculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private LinearLayout emptyStateLayout;
    private Button btnClearHistory;
    private HistoryAdapter historyAdapter;
    private List<CalculationHistory> historyList;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private static final String PREF_NAME = "CalculatorHistory";
    private static final String HISTORY_KEY = "history_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadHistoryFromPrefs();
        updateUI();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        historyList = new ArrayList<>();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("History");
        }
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(historyList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);
    }

    private void loadHistoryFromPrefs() {
        String historyJson = sharedPreferences.getString(HISTORY_KEY, "");
        if (!historyJson.isEmpty()) {
            Type listType = new TypeToken<List<CalculationHistory>>(){}.getType();
            historyList = gson.fromJson(historyJson, listType);
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
        }
    }

    private void saveHistoryToPrefs() {
        String historyJson = gson.toJson(historyList);
        sharedPreferences.edit().putString(HISTORY_KEY, historyJson).apply();
    }

    private void updateUI() {
        if (historyList.isEmpty()) {
            recyclerViewHistory.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            btnClearHistory.setEnabled(false);
        } else {
            recyclerViewHistory.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            btnClearHistory.setEnabled(true);
            historyAdapter.updateHistory(historyList);
        }
    }

    private void setupClickListeners() {
        btnClearHistory.setOnClickListener(v -> clearAllHistory());
    }

    private void clearAllHistory() {
        historyList.clear();
        saveHistoryToPrefs();
        updateUI();
        Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Static method to add calculation to history (called from MainActivity)
    public static void addCalculationToHistory(Context context, String expression, String result) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        // Load existing history
        String historyJson = sharedPreferences.getString(HISTORY_KEY, "");
        List<CalculationHistory> historyList;

        if (!historyJson.isEmpty()) {
            Type listType = new TypeToken<List<CalculationHistory>>(){}.getType();
            historyList = gson.fromJson(historyJson, listType);
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
        } else {
            historyList = new ArrayList<>();
        }

        // Create timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        // Add new calculation to the beginning of the list
        CalculationHistory newHistory = new CalculationHistory(expression, result, timestamp, System.currentTimeMillis());
        historyList.add(0, newHistory);

        // Keep only last 100 calculations
        if (historyList.size() > 100) {
            historyList = historyList.subList(0, 100);
        }

        // Save updated history
        String updatedHistoryJson = gson.toJson(historyList);
        sharedPreferences.edit().putString(HISTORY_KEY, updatedHistoryJson).apply();
    }
}
