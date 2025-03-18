package thanh.karaokevitinh;

import static thanh.karaokevitinh.R.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScannerActivity extends AppCompatActivity {

    private Button scanButton;
    private TextView resultTextView;
    private final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    ActivityResultLauncher<ScanOptions> barcodeLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_qr);

        scanButton = findViewById(R.id.scanButton);
        resultTextView = findViewById(R.id.resultTextView);

        // Set up the ActivityResultLauncher
        barcodeLauncher = registerForActivityResult(new ScanContract(),
                result -> {
                    if (result.getContents() != null) {
                        // Handle the scanned result here
                        resultTextView.setText(result.getContents());
                    }
                });

        // Set an onClickListener to the button
        scanButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startScanner();
            } else {
                requestCameraPermission();
            }
        });
    }

    // Function to start the scanner
    public void startScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barcodeLauncher.launch(options);
    }

    // Method to check camera permission
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Method to request camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
