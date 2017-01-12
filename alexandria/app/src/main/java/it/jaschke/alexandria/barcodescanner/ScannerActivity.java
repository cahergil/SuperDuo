package it.jaschke.alexandria.barcodescanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import it.jaschke.alexandria.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Carlos on 27/10/2015.
 */
public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here

        Intent resultIntent=new Intent();
        resultIntent.putExtra(getString(R.string.result_extra),rawResult.getText());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}