package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.azarpark.watchman.databinding.ActivityPaymentTestBinding;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.saman.SamanPayment;

import java.util.Date;

public class PaymentTestActivity extends AppCompatActivity {

    ActivityPaymentTestBinding binding;
    SamanPayment samanPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        samanPayment = new SamanPayment(getSupportFragmentManager(), this, this, new SamanPayment.SamanPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {

            }

            @Override
            public void getScannerData(int placeID) {

            }

            @Override
            public void onVerifyFinished() {

            }
        });

        binding.submit.setOnClickListener(view -> {

            if (binding.value.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show();
            else{

                samanPayment.testPaymentRequest("IR950610000000700790691854", new Date().toString(), Integer.parseInt(binding.value.getText().toString()));

            }

        });

    }
}