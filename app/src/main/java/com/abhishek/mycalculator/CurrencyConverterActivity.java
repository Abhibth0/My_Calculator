package com.abhishek.mycalculator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CurrencyConverterActivity extends AppCompatActivity {

    private Spinner spinnerFromCurrency, spinnerToCurrency;
    private EditText etFromAmount;
    private TextView tvToAmount, tvExchangeRate, tvLastUpdated;
    private Button btnConvert, btnSwapCurrencies;
    private Button btnUsdToInr, btnEurToInr, btnGbpToInr, btnInrToUsd;

    private List<Currency> currencyList;
    private ArrayAdapter<Currency> currencyAdapter;
    private Map<String, Double> exchangeRates;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        initializeViews();
        setupToolbar();
        setupCurrencies();
        setupExchangeRates();
        setupSpinners();
        setupClickListeners();
        updateLastUpdatedTime();
    }

    private void initializeViews() {
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

        decimalFormat = new DecimalFormat("#,##0.00");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Currency Converter");
        }
    }

    private void setupCurrencies() {
        currencyList = new ArrayList<>();
        currencyList.add(new Currency("USD", "US Dollar", "$"));
        currencyList.add(new Currency("EUR", "Euro", "€"));
        currencyList.add(new Currency("GBP", "British Pound", "£"));
        currencyList.add(new Currency("INR", "Indian Rupee", "₹"));
        currencyList.add(new Currency("JPY", "Japanese Yen", "¥"));
        currencyList.add(new Currency("AUD", "Australian Dollar", "A$"));
        currencyList.add(new Currency("CAD", "Canadian Dollar", "C$"));
        currencyList.add(new Currency("CHF", "Swiss Franc", "Fr"));
        currencyList.add(new Currency("CNY", "Chinese Yuan", "¥"));
        currencyList.add(new Currency("SGD", "Singapore Dollar", "S$"));
    }

    private void setupExchangeRates() {
        // Sample exchange rates (in real app, you would fetch from API)
        exchangeRates = new HashMap<>();

        // USD as base currency
        exchangeRates.put("USD_EUR", 0.85);
        exchangeRates.put("USD_GBP", 0.73);
        exchangeRates.put("USD_INR", 83.15);
        exchangeRates.put("USD_JPY", 149.50);
        exchangeRates.put("USD_AUD", 1.52);
        exchangeRates.put("USD_CAD", 1.36);
        exchangeRates.put("USD_CHF", 0.88);
        exchangeRates.put("USD_CNY", 7.23);
        exchangeRates.put("USD_SGD", 1.34);

        // EUR rates
        exchangeRates.put("EUR_USD", 1.18);
        exchangeRates.put("EUR_GBP", 0.86);
        exchangeRates.put("EUR_INR", 97.89);

        // GBP rates
        exchangeRates.put("GBP_USD", 1.37);
        exchangeRates.put("GBP_EUR", 1.16);
        exchangeRates.put("GBP_INR", 113.97);

        // INR rates
        exchangeRates.put("INR_USD", 0.012);
        exchangeRates.put("INR_EUR", 0.010);
        exchangeRates.put("INR_GBP", 0.0088);

        // Add more rates as needed
    }

    private void setupSpinners() {
        currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencyList);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFromCurrency.setAdapter(currencyAdapter);
        spinnerToCurrency.setAdapter(currencyAdapter);

        // Set default selections
        spinnerFromCurrency.setSelection(0); // USD
        spinnerToCurrency.setSelection(3); // INR

        // Setup spinner listeners
        spinnerFromCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExchangeRateDisplay();
                convertCurrency();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerToCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExchangeRateDisplay();
                convertCurrency();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        btnConvert.setOnClickListener(v -> convertCurrency());

        btnSwapCurrencies.setOnClickListener(v -> swapCurrencies());

        // Popular conversion buttons
        btnUsdToInr.setOnClickListener(v -> setPopularConversion("USD", "INR"));
        btnEurToInr.setOnClickListener(v -> setPopularConversion("EUR", "INR"));
        btnGbpToInr.setOnClickListener(v -> setPopularConversion("GBP", "INR"));
        btnInrToUsd.setOnClickListener(v -> setPopularConversion("INR", "USD"));

        // Text watcher for auto conversion
        etFromAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                convertCurrency();
            }
        });
    }

    private void convertCurrency() {
        String amountStr = etFromAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            tvToAmount.setText("0.00");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Currency fromCurrency = (Currency) spinnerFromCurrency.getSelectedItem();
            Currency toCurrency = (Currency) spinnerToCurrency.getSelectedItem();

            if (fromCurrency != null && toCurrency != null) {
                double convertedAmount = performConversion(amount, fromCurrency.getCode(), toCurrency.getCode());
                tvToAmount.setText(decimalFormat.format(convertedAmount));
            }
        } catch (NumberFormatException e) {
            tvToAmount.setText("0.00");
        }
    }

    private double performConversion(double amount, String fromCode, String toCode) {
        if (fromCode.equals(toCode)) {
            return amount;
        }

        String rateKey = fromCode + "_" + toCode;
        Double rate = exchangeRates.get(rateKey);

        if (rate != null) {
            return amount * rate;
        } else {
            // Try reverse conversion
            String reverseKey = toCode + "_" + fromCode;
            Double reverseRate = exchangeRates.get(reverseKey);
            if (reverseRate != null) {
                return amount / reverseRate;
            }
        }

        // Fallback: convert through USD
        if (!fromCode.equals("USD") && !toCode.equals("USD")) {
            double usdAmount = performConversion(amount, fromCode, "USD");
            return performConversion(usdAmount, "USD", toCode);
        }

        return amount; // No conversion available
    }

    private void swapCurrencies() {
        int fromPosition = spinnerFromCurrency.getSelectedItemPosition();
        int toPosition = spinnerToCurrency.getSelectedItemPosition();

        spinnerFromCurrency.setSelection(toPosition);
        spinnerToCurrency.setSelection(fromPosition);

        // Also swap the amounts
        String fromAmount = etFromAmount.getText().toString();
        String toAmount = tvToAmount.getText().toString();

        if (!toAmount.equals("0.00") && !toAmount.isEmpty()) {
            // Remove formatting from toAmount
            String cleanAmount = toAmount.replace(",", "");
            etFromAmount.setText(cleanAmount);
        }
    }

    private void setPopularConversion(String fromCode, String toCode) {
        // Find positions of currencies
        int fromPosition = findCurrencyPosition(fromCode);
        int toPosition = findCurrencyPosition(toCode);

        if (fromPosition != -1 && toPosition != -1) {
            spinnerFromCurrency.setSelection(fromPosition);
            spinnerToCurrency.setSelection(toPosition);

            if (etFromAmount.getText().toString().isEmpty()) {
                etFromAmount.setText("1");
            }
        }
    }

    private int findCurrencyPosition(String currencyCode) {
        for (int i = 0; i < currencyList.size(); i++) {
            if (currencyList.get(i).getCode().equals(currencyCode)) {
                return i;
            }
        }
        return -1;
    }

    private void updateExchangeRateDisplay() {
        Currency fromCurrency = (Currency) spinnerFromCurrency.getSelectedItem();
        Currency toCurrency = (Currency) spinnerToCurrency.getSelectedItem();

        if (fromCurrency != null && toCurrency != null) {
            double rate = performConversion(1.0, fromCurrency.getCode(), toCurrency.getCode());
            String rateText = "1 " + fromCurrency.getCode() + " = " +
                             decimalFormat.format(rate) + " " + toCurrency.getCode();
            tvExchangeRate.setText(rateText);
        }
    }

    private void updateLastUpdatedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvLastUpdated.setText("Last updated: " + currentTime);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
