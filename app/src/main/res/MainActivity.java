package com.abhishek.mycalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDisplay, tvPrevious;
    private String currentInput = "";
    private String operator = "";
    private double firstOperand = 0;
    private boolean isOperatorPressed = false;
    private boolean isResultShown = false;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##########");
    private NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupButtonClickListeners();
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

        tvDisplay.setText(currentInput);
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
            value = value / 100;
            currentInput = String.valueOf(value);
            tvDisplay.setText(formatNumber(value));
            isResultShown = true;
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

        try {
            switch (operator) {
                case "+":
                    result = firstOperand + secondOperand;
                    break;
                case "-":
                    result = firstOperand - secondOperand;
                    break;
                case "*":
                    result = firstOperand * secondOperand;
                    break;
                case "/":
                    if (secondOperand != 0) {
                        result = firstOperand / secondOperand;
                    } else {
                        tvDisplay.setText("Error");
                        return;
                    }
                    break;
            }

            firstOperand = result;
            currentInput = String.valueOf(result);
            tvDisplay.setText(formatNumber(result));

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
            String formatted = decimalFormat.format(number);
            // Check if the whole part is >= 1000 for comma formatting
            if (Math.abs(number) >= 1000) {
                numberFormat.setMaximumFractionDigits(10);
                numberFormat.setMinimumFractionDigits(0);
                return numberFormat.format(number);
            }
            return formatted;
        }
    }
}
