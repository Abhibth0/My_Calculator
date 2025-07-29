package com.abhishek.mycalculator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private TextView tvDisplay, tvPrevious;
    private String currentInput = "";
    private String operator = "";
    private double firstOperand = 0;
    private boolean isOperatorPressed = false;
    private boolean isResultShown = false;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##########");
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    // Navigation drawer components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    // History components
    private RecyclerView recyclerViewHistory;
    private LinearLayout emptyStateLayout;
    private Button btnClearHistory;
    private HistoryAdapter historyAdapter;
    private List<CalculationHistory> historyList;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private static final String PREF_NAME = "CalculatorHistory";
    private static final String HISTORY_KEY = "history_list";

    // Currency converter components
    private Spinner spinnerFromCurrency, spinnerToCurrency;
    private EditText etFromAmount;
    private TextView tvToAmount, tvExchangeRate, tvLastUpdated;
    private Button btnConvert, btnSwapCurrencies;
    private Button btnUsdToInr, btnEurToInr, btnGbpToInr, btnInrToUsd;

    // Currency data
    private String[] currencies = {"USD", "INR", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "SGD"};
    private String[] currencyNames = {
            "US Dollar (USD)",
            "Indian Rupee (INR)",
            "Euro (EUR)",
            "British Pound (GBP)",
            "Japanese Yen (JPY)",
            "Australian Dollar (AUD)",
            "Canadian Dollar (CAD)",
            "Swiss Franc (CHF)",
            "Chinese Yuan (CNY)",
            "Singapore Dollar (SGD)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize navigation drawer components
        setupNavigationDrawer();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupButtonClickListeners();
        setupSwipeGesture();

        // Initialize history components
        initializeHistoryComponents();

        // Initialize currency converter components
        initializeCurrencyConverterComponents();
    }

    private void setupNavigationDrawer() {
        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup drawer toggle (hamburger icon)
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initializeViews() {
        tvDisplay = findViewById(R.id.tvDisplay);
        tvPrevious = findViewById(R.id.tvPrevious);
    }

    private void setupButtonClickListeners() {
        // Number buttons
        findViewById(R.id.btn0).setOnClickListener(this);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        findViewById(R.id.btn6).setOnClickListener(this);
        findViewById(R.id.btn7).setOnClickListener(this);
        findViewById(R.id.btn8).setOnClickListener(this);
        findViewById(R.id.btn9).setOnClickListener(this);

        // Operator buttons
        findViewById(R.id.btnAdd).setOnClickListener(this);
        findViewById(R.id.btnSubtract).setOnClickListener(this);
        findViewById(R.id.btnMultiply).setOnClickListener(this);
        findViewById(R.id.btnDivide).setOnClickListener(this);

        // Function buttons
        findViewById(R.id.btnEquals).setOnClickListener(this);
        findViewById(R.id.btnClear).setOnClickListener(this);
        findViewById(R.id.btnDecimal).setOnClickListener(this);
        findViewById(R.id.btnPercent).setOnClickListener(this);
        findViewById(R.id.btnPlusMinus).setOnClickListener(this);
    }

    private void setupSwipeGesture() {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Double tap to copy display content
                copyToClipboard();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        tvDisplay.setOnTouchListener(new View.OnTouchListener() {
            private float startX = 0;
            private float startY = 0;
            private static final int MIN_SWIPE_DISTANCE = 50;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();

                // Handle double tap first
                gestureDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();

                        float deltaX = endX - startX;
                        float deltaY = endY - startY;

                        // Check if it's a horizontal swipe (more horizontal than vertical)
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > MIN_SWIPE_DISTANCE) {
                            if (deltaX > 0) {
                                // Left to right swipe - delete one character
                                handleBackspace();
                                return true;
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void handleBackspace() {
        if (isResultShown) {
            // If result is shown, clear everything
            handleClearInput();
            return;
        }

        if (!currentInput.isEmpty()) {
            if (currentInput.length() == 1) {
                // If only one character left, set to "0"
                currentInput = "";
                tvDisplay.setText("0");
            } else {
                // Remove last character
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                tvDisplay.setText(currentInput);
            }
        }
    }

    @Override
    public void onClick(View v) {

        Button button = (Button) v;
        String buttonText = button.getText().toString();

        int id = v.getId();

        if (id == R.id.btn0 || id == R.id.btn1 || id == R.id.btn2 || id == R.id.btn3 ||
            id == R.id.btn4 || id == R.id.btn5 || id == R.id.btn6 || id == R.id.btn7 ||
            id == R.id.btn8 || id == R.id.btn9) {
            handleNumberInput(buttonText);
        } else if (id == R.id.btnDecimal) {
            handleDecimalInput();
        } else if (id == R.id.btnAdd || id == R.id.btnSubtract || id == R.id.btnMultiply || id == R.id.btnDivide) {
            handleOperatorInput(buttonText);
        } else if (id == R.id.btnEquals) {
            handleEqualsInput();
        } else if (id == R.id.btnClear) {
            handleClearInput();
        } else if (id == R.id.btnPercent) {
            handlePercentInput();
        } else if (id == R.id.btnPlusMinus) {
            handlePlusMinusInput();
        }
    }

    private void animateButtonPress(View button) {
        // Scale down and back up for button press effect
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 0.9f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1.0f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.9f, 1.0f);

        // Alpha animation for press effect
        ObjectAnimator alphaDown = ObjectAnimator.ofFloat(button, "alpha", 1.0f, 0.7f);
        ObjectAnimator alphaUp = ObjectAnimator.ofFloat(button, "alpha", 0.7f, 1.0f);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY, alphaDown);
        scaleDown.setDuration(100);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY, alphaUp);
        scaleUp.setDuration(100);

        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playSequentially(scaleDown, scaleUp);
        fullAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        fullAnimation.start();
    }

    private void handleNumberInput(String number) {
        if (isResultShown) {
            currentInput = "";
            isResultShown = false;
        }

        if (currentInput.equals("0") && !number.equals("0")) {
            currentInput = number;
        } else if (!currentInput.equals("0")) {
            currentInput += number;
        }

        // Display with comma formatting for input
        String displayText = formatInputNumber(currentInput);
        tvDisplay.setText(displayText);
        isOperatorPressed = false;
    }

    private void handleDecimalInput() {
        if (isResultShown) {
            currentInput = "0";
            isResultShown = false;
        }

        if (currentInput.isEmpty()) {
            currentInput = "0";
        }

        if (!currentInput.contains(".")) {
            currentInput += ".";
            tvDisplay.setText(currentInput);
        }
        isOperatorPressed = false;
    }

    private void handleOperatorInput(String op) {
        if (!currentInput.isEmpty() && !isOperatorPressed) {
            if (!operator.isEmpty()) {
                calculateResult();
            } else {
                firstOperand = Double.parseDouble(currentInput);
            }
        }

        operator = convertOperatorSymbol(op);
        isOperatorPressed = true;
        isResultShown = false;

        String displayText = formatNumber(firstOperand) + " " + op;
        tvPrevious.setText(displayText);
        currentInput = "";
    }

    private void handleEqualsInput() {
        if (!operator.isEmpty() && !currentInput.isEmpty()) {
            calculateResult();
            tvPrevious.setText("");
            operator = "";
            isResultShown = true;
        }
    }

    private void handleClearInput() {
        currentInput = "";
        operator = "";
        firstOperand = 0;
        isOperatorPressed = false;
        isResultShown = false;
        tvDisplay.setText("0");
        tvPrevious.setText("");
    }

    private void handlePercentInput() {
        if (!currentInput.isEmpty()) {
            double value = Double.parseDouble(currentInput);

            // If there's a pending operation and first operand exists
            if (!operator.isEmpty() && firstOperand != 0) {
                // Calculate percentage of the first operand
                value = (firstOperand * value) / 100;
            } else {
                // Simple percentage conversion
                value = value / 100;
            }

            currentInput = String.valueOf(value);
            tvDisplay.setText(formatNumber(value));
            isResultShown = false; // Allow further operations
        }
    }

    private void handlePlusMinusInput() {
        if (!currentInput.isEmpty() && !currentInput.equals("0")) {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1);
            } else {
                currentInput = "-" + currentInput;
            }
            tvDisplay.setText(currentInput);
        }
    }

    private void calculateResult() {
        if (currentInput.isEmpty()) return;

        double secondOperand = Double.parseDouble(currentInput);
        double result = 0;
        String expression = "";

        try {
            switch (operator) {
                case "+":
                    result = firstOperand + secondOperand;
                    expression = formatNumber(firstOperand) + " + " + formatNumber(secondOperand);
                    break;
                case "-":
                    result = firstOperand - secondOperand;
                    expression = formatNumber(firstOperand) + " - " + formatNumber(secondOperand);
                    break;
                case "*":
                    result = firstOperand * secondOperand;
                    expression = formatNumber(firstOperand) + " × " + formatNumber(secondOperand);
                    break;
                case "/":
                    if (secondOperand != 0) {
                        result = firstOperand / secondOperand;
                        expression = formatNumber(firstOperand) + " ÷ " + formatNumber(secondOperand);
                    } else {
                        tvDisplay.setText("Error");
                        return;
                    }
                    break;
            }

            firstOperand = result;
            currentInput = String.valueOf(result);
            String formattedResult = formatNumber(result);
            tvDisplay.setText(formattedResult);

            // Save calculation to history
            addCalculationToHistory(expression, formattedResult);

        } catch (Exception e) {
            tvDisplay.setText("Error");
            handleClearInput();
        }
    }

    private String convertOperatorSymbol(String symbol) {
        switch (symbol) {
            case "÷":
                return "/";
            case "×":
                return "*";
            case "−":
                return "-";
            default:
                return symbol;
        }
    }

    private void copyToClipboard() {
        String textToCopy = tvDisplay.getText().toString();
        if (!textToCopy.isEmpty() && !textToCopy.equals("0")) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Calculator Result", textToCopy);
            clipboard.setPrimaryClip(clip);

            // Show copy animation
            animateCopy();

            Toast.makeText(this, "Copied: " + textToCopy, Toast.LENGTH_SHORT).show();
        }
    }

    private void animateCopy() {
        // Scale animation for copy feedback
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvDisplay, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvDisplay, "scaleY", 1.0f, 1.1f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private String formatInputNumber(String input) {
        try {
            // Remove any existing commas first
            String cleanInput = input.replace(",", "");

            if (cleanInput.contains(".")) {
                // Handle decimal numbers
                String[] parts = cleanInput.split("\\.");
                String wholePart = parts[0];
                String decimalPart = parts[1];

                if (wholePart.length() >= 4) {
                    long wholeNumber = Long.parseLong(wholePart);
                    return numberFormat.format(wholeNumber) + "." + decimalPart;
                }
                return cleanInput;
            } else {
                // Handle whole numbers
                if (cleanInput.length() >= 4) {
                    long number = Long.parseLong(cleanInput);
                    return numberFormat.format(number);
                }
                return cleanInput;
            }
        } catch (Exception e) {
            return input;
        }
    }

    private String formatNumber(double number) {
        if (number == (long) number) {
            // For whole numbers, use comma separator for thousands
            long longNumber = (long) number;
            if (Math.abs(longNumber) >= 1000) {
                return numberFormat.format(longNumber);
            } else {
                return String.valueOf(longNumber);
            }
        } else {
            // For decimal numbers
            if (Math.abs(number) >= 1000) {
                numberFormat.setMaximumFractionDigits(10);
                numberFormat.setMinimumFractionDigits(0);
                return numberFormat.format(number);
            } else {
                String formatted = decimalFormat.format(number);
                return formatted;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_calculator) {
            showCalculatorView();
        } else if (id == R.id.nav_currency_converter) {
            showCurrencyConverterView();
        } else if (id == R.id.nav_history) {
            showHistoryView();
        }

        // Close drawer after item selection
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onBackPressed() {
        // First priority: Check if navigation drawer is open
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return;
        }

        // Second priority: Check if currency converter content is visible
        LinearLayout currencyContent = findViewById(R.id.currency_converter_content);
        if (currencyContent != null && currencyContent.getVisibility() == View.VISIBLE) {
            // If currency converter is visible and drawer is closed, go back to calculator
            showCalculatorView();
            return;
        }

        // Third priority: Check if history content is visible
        LinearLayout historyContent = findViewById(R.id.history_content);
        if (historyContent != null && historyContent.getVisibility() == View.VISIBLE) {
            // If history is visible and drawer is closed, go back to calculator
            showCalculatorView();
            return;
        }

        // Last: Default back press (close app)
        super.onBackPressed();
    }

    private void showCurrencyConverterView() {
        // Hide calculator and history content
        LinearLayout calculatorContent = findViewById(R.id.calculator_content);
        if (calculatorContent != null) {
            calculatorContent.setVisibility(View.GONE);
        }

        LinearLayout historyContent = findViewById(R.id.history_content);
        if (historyContent != null) {
            historyContent.setVisibility(View.GONE);
        }

        // Show currency converter content without animation
        LinearLayout currencyContent = findViewById(R.id.currency_converter_content);
        if (currencyContent != null) {
            currencyContent.setVisibility(View.VISIBLE);
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Currency Converter");
        }

        // Update navigation menu selection to currency converter
        navigationView.setCheckedItem(R.id.nav_currency_converter);
    }

    private void showHistoryView() {
        // Hide calculator and currency converter content
        LinearLayout calculatorContent = findViewById(R.id.calculator_content);
        if (calculatorContent != null) {
            calculatorContent.setVisibility(View.GONE);
        }

        LinearLayout currencyContent = findViewById(R.id.currency_converter_content);
        if (currencyContent != null) {
            currencyContent.setVisibility(View.GONE);
        }

        // Show history content without animation
        LinearLayout historyContent = findViewById(R.id.history_content);
        if (historyContent != null) {
            historyContent.setVisibility(View.VISIBLE);
            loadAndDisplayHistory();
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("History");
        }

        // Update navigation menu selection to history
        navigationView.setCheckedItem(R.id.nav_history);
    }

    private void showCalculatorView() {
        // Show calculator content and hide other content
        LinearLayout calculatorContent = findViewById(R.id.calculator_content);
        if (calculatorContent != null) {
            calculatorContent.setVisibility(View.VISIBLE);
        }

        LinearLayout historyContent = findViewById(R.id.history_content);
        if (historyContent != null) {
            historyContent.setVisibility(View.GONE);
        }

        LinearLayout currencyContent = findViewById(R.id.currency_converter_content);
        if (currencyContent != null) {
            currencyContent.setVisibility(View.GONE);
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Calculator");
        }

        // Update navigation menu selection to calculator
        navigationView.setCheckedItem(R.id.nav_calculator);
    }

    private void initializeHistoryComponents() {
        // Initialize RecyclerView for history
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        // Setup SharedPreferences and Gson
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        gson = new Gson();

        // Load history from SharedPreferences
        loadHistory();

        // Setup adapter
        historyAdapter = new HistoryAdapter(historyList);
        recyclerViewHistory.setAdapter(historyAdapter);

        // Show or hide empty state
        updateEmptyState();
    }

    private void loadHistory() {
        // Load history list from SharedPreferences
        String json = sharedPreferences.getString(HISTORY_KEY, null);
        Type type = new TypeToken<ArrayList<CalculationHistory>>() {}.getType();
        historyList = gson.fromJson(json, type);

        if (historyList == null) {
            historyList = new ArrayList<>();
        }
    }

    private void updateEmptyState() {
        if (historyList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
        }
    }

    private void loadAndDisplayHistory() {
        // Reload history from SharedPreferences
        loadHistory();

        // Update adapter with new data
        if (historyAdapter != null) {
            historyAdapter.updateHistory(historyList);
        }

        // Update empty state
        updateEmptyState();

        // Setup clear history button click listener
        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> clearAllHistory());
        }
    }

    private void clearAllHistory() {
        historyList.clear();
        saveHistory();
        historyAdapter.updateHistory(historyList);
        updateEmptyState();
        Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
    }

    private void saveHistory() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(historyList);
        editor.putString(HISTORY_KEY, json);
        editor.apply();
    }

    // Method to add calculation to history (called when equals is pressed)
    public void addCalculationToHistory(String expression, String result) {
        // Create timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        // Create new history entry
        CalculationHistory newHistory = new CalculationHistory(expression, result, timestamp, System.currentTimeMillis());

        // Add to beginning of list
        historyList.add(0, newHistory);

        // Keep only last 100 calculations
        if (historyList.size() > 100) {
            historyList = historyList.subList(0, 100);
        }

        // Save to SharedPreferences
        saveHistory();
    }

    private void initializeCurrencyConverterComponents() {
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency);
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency);
        etFromAmount = findViewById(R.id.etFromAmount);
        tvToAmount = findViewById(R.id.tvToAmount);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwapCurrencies = findViewById(R.id.btnSwapCurrencies);
        btnUsdToInr = findViewById(R.id.btnUsdToInr);
        btnEurToInr = findViewById(R.id.btnEurToInr);
        btnGbpToInr = findViewById(R.id.btnGbpToInr);
        btnInrToUsd = findViewById(R.id.btnInrToUsd);

        // Setup currency spinners with custom layout for better visibility
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencyNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.display_text)); // White text
                textView.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.display_text)); // White text
                textView.setBackgroundColor(getResources().getColor(R.color.background_black)); // Black background
                textView.setPadding(20, 20, 20, 20);
                textView.setTextSize(16);
                return view;
            }
        };

        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromCurrency.setAdapter(currencyAdapter);

        // Create separate adapter for "To" spinner
        ArrayAdapter<String> currencyAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencyNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.display_text)); // White text
                textView.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.display_text)); // White text
                textView.setBackgroundColor(getResources().getColor(R.color.background_black)); // Black background
                textView.setPadding(20, 20, 20, 20);
                textView.setTextSize(16);
                return view;
            }
        };

        currencyAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToCurrency.setAdapter(currencyAdapter2);

        // Set default selection to first currency
        spinnerFromCurrency.setSelection(0); // USD
        spinnerToCurrency.setSelection(1);   // INR

        // Setup Convert button click listener
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCurrencyConversion();
            }
        });

        // Setup Swap button click listener
        btnSwapCurrencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCurrencies();
            }
        });

        // Setup popular conversion buttons
        btnUsdToInr.setOnClickListener(v -> performDirectConversion("USD", "INR"));
        btnEurToInr.setOnClickListener(v -> performDirectConversion("EUR", "INR"));
        btnGbpToInr.setOnClickListener(v -> performDirectConversion("GBP", "INR"));
        btnInrToUsd.setOnClickListener(v -> performDirectConversion("INR", "USD"));
    }

    private void performCurrencyConversion() {
        try {
            String amountText = etFromAmount.getText().toString().trim();
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountText);
            int fromPosition = spinnerFromCurrency.getSelectedItemPosition();
            int toPosition = spinnerToCurrency.getSelectedItemPosition();

            String fromCurrency = currencies[fromPosition];
            String toCurrency = currencies[toPosition];

            // Sample exchange rates (in real app, you'd fetch from API)
            double convertedAmount = convertCurrency(amount, fromCurrency, toCurrency);

            // Display result
            tvToAmount.setText(String.format("%.2f", convertedAmount));

            // Update exchange rate display
            double rate = getExchangeRate(fromCurrency, toCurrency);
            tvExchangeRate.setText(String.format("1 %s = %.4f %s", fromCurrency, rate, toCurrency));

            // Update timestamp
            tvLastUpdated.setText("Last updated: Just now");

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void swapCurrencies() {
        int fromPosition = spinnerFromCurrency.getSelectedItemPosition();
        int toPosition = spinnerToCurrency.getSelectedItemPosition();

        // Swap selections
        spinnerFromCurrency.setSelection(toPosition);
        spinnerToCurrency.setSelection(fromPosition);

        // If there's amount entered, perform conversion automatically
        String amountText = etFromAmount.getText().toString().trim();
        if (!amountText.isEmpty()) {
            performCurrencyConversion();
        }
    }

    private void performDirectConversion(String fromCurrency, String toCurrency) {
        spinnerFromCurrency.setSelection(getIndex(fromCurrency));
        spinnerToCurrency.setSelection(getIndex(toCurrency));

        // Perform conversion
        performCurrencyConversion();
    }

    private double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        double rate = getExchangeRate(fromCurrency, toCurrency);
        return amount * rate;
    }

    private double getExchangeRate(String fromCurrency, String toCurrency) {
        // Sample exchange rates (in real app, you'd fetch from API)
        // All rates are relative to USD
        double[][] exchangeRates = {
            // USD, INR, EUR, GBP, JPY, AUD, CAD, CHF, CNY, SGD
            {1.0, 83.15, 0.92, 0.79, 149.50, 1.52, 1.36, 0.89, 7.23, 1.35}, // USD
            {0.012, 1.0, 0.011, 0.0095, 1.80, 0.018, 0.016, 0.011, 0.087, 0.016}, // INR
            {1.09, 90.45, 1.0, 0.86, 162.80, 1.65, 1.48, 0.97, 7.87, 1.47}, // EUR
            {1.27, 105.38, 1.16, 1.0, 189.24, 1.92, 1.72, 1.13, 9.15, 1.71}, // GBP
            {0.0067, 0.56, 0.0061, 0.0053, 1.0, 0.010, 0.0091, 0.0060, 0.048, 0.0090}, // JPY
            {0.66, 54.70, 0.61, 0.52, 98.36, 1.0, 0.89, 0.59, 4.76, 0.89}, // AUD
            {0.74, 61.14, 0.68, 0.58, 109.93, 1.12, 1.0, 0.66, 5.32, 0.99}, // CAD
            {1.12, 93.48, 1.04, 0.89, 167.98, 1.70, 1.52, 1.0, 8.12, 1.52}, // CHF
            {0.138, 11.50, 0.127, 0.109, 20.68, 0.21, 0.188, 0.123, 1.0, 0.187}, // CNY
            {0.74, 61.59, 0.68, 0.58, 110.74, 1.13, 1.01, 0.66, 5.36, 1.0}  // SGD
        };

        int fromIndex = getIndex(fromCurrency);
        int toIndex = getIndex(toCurrency);

        if (fromIndex == -1 || toIndex == -1) {
            return 1.0; // Default rate if currency not found
        }

        return exchangeRates[fromIndex][toIndex];
    }

    private int getIndex(String currency) {
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currency)) {
                return i;
            }
        }
        return -1;
    }
}
