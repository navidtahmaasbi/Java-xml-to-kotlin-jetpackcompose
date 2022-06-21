package com.azarpark.watchman.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.payment.parsian.ParsianPayment;
import com.azarpark.watchman.utils.Assistant;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

public class QRScanerActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscaner);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
//            Toast.makeText(QRScanerActivity.this, result.getText(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra(ParsianPayment.QR_DATA,result.getText());
            setResult(ParsianPayment.QR_SCANER_REQUEST_CODE,intent);
            QRScanerActivity.this.finish();
        }));
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}