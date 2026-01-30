package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PaymentCheckoutFragment extends Fragment {

    private RadioGroup radioGroupPaymentMethod;
    private LinearLayout layoutCardDetails;
    private Button btnCompletePayment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_checkout, container, false);

        initializeViews(view);
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        radioGroupPaymentMethod = view.findViewById(R.id.radio_group_payment_method);
        layoutCardDetails = view.findViewById(R.id.layout_card_details);
        btnCompletePayment = view.findViewById(R.id.btn_complete_payment);
    }

    private void setupEventListeners() {
        radioGroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                String paymentMethod = selectedRadioButton.getText().toString();

                // Show card details only for credit/debit card option
                if (paymentMethod.equals("Credit/Debit Card")) {
                    layoutCardDetails.setVisibility(View.VISIBLE);
                } else {
                    layoutCardDetails.setVisibility(View.GONE);
                }
            }
        });

        btnCompletePayment.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        // Get selected payment method
        int selectedId = radioGroupPaymentMethod.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = radioGroupPaymentMethod.findViewById(selectedId);
        String paymentMethod = selectedRadioButton.getText().toString();

        // Validate payment details based on method
        if (paymentMethod.equals("Credit/Debit Card")) {
            // In a real app, validate card details here
            Toast.makeText(getContext(), "Processing payment with " + paymentMethod + "...", Toast.LENGTH_SHORT).show();
        } else if (paymentMethod.equals("PayPal")) {
            Toast.makeText(getContext(), "Redirecting to PayPal for payment...", Toast.LENGTH_SHORT).show();
        } else if (paymentMethod.equals("Insurance")) {
            Toast.makeText(getContext(), "Processing insurance claim...", Toast.LENGTH_SHORT).show();
        }

        // Simulate payment processing
        simulatePaymentProcessing(paymentMethod);
    }

    private void simulatePaymentProcessing(String paymentMethod) {
        // In a real app, this would connect to a payment gateway
        Toast.makeText(getContext(), "Payment processed successfully using " + paymentMethod + "!", Toast.LENGTH_LONG).show();
    }
}