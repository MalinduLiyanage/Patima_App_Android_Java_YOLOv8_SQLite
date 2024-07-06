package com.onesandzeros.patima;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.onesandzeros.patima.databinding.ActivityImageAcquisitionBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageAcquisitionActivity extends AppCompatActivity implements Detector.DetectorListener {
    private ActivityImageAcquisitionBinding binding;
    private final boolean isFrontCamera = false;
    ImageButton galBtn, cameraBtn, flashBtn, returnBtn;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private Detector detector;
    ImageView imageView;
    private ExecutorService cameraExecutor;
    ConstraintLayout cameraContainer;
    LinearLayout cameraBtnlayout;
    boolean isCameraOn = false, isCameraOff = true, isPredictable = false, tryAgain = false, autoPredict = false;
    private final int IMAGE_PICK = 100;
    Bitmap bitmap, croppedBitmap, detectedBitmap;
    private ImageCapture imageCapture;
    String capturePath = "", galleryPath = "", inputImagePath = "", outputPath = "";
    TextView detected;
    private static final String TAG = "Camera";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    int imgId = 0, isFlash = 0; // Flash Status - 1 = On / 0 = Off
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    String latitudeString = null, longitudeString = null;
    Float CONFIDENCE_THRESHOLD = 0F;
    private CameraControl cameraControl;
    Switch autoPredictSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageAcquisitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        hideSystemUI();

        if (!allPermissionsGrant()) {
            Intent intent = new Intent(ImageAcquisitionActivity.this, PermissionActivity.class);
            startActivity(intent);
            finish();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);

        CONFIDENCE_THRESHOLD = sharedPreferences.getFloat("CONFIDENCE_THRESHOLD",0);

        int userId = sharedPreferences.getInt("userId", -1);

        galBtn = findViewById(R.id.galleryBtn);
        cameraBtn = findViewById(R.id.captureBtn);
        flashBtn = findViewById(R.id.flashBtn);
        imageView = findViewById(R.id.imageView);
        cameraContainer = findViewById(R.id.camera_container);
        detected = findViewById(R.id.detect_txt);
        autoPredictSwitch = findViewById(R.id.mode_switch);
        cameraBtnlayout = findViewById(R.id.camera_btns);
        returnBtn = findViewById(R.id.return_button);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SQLiteHelper dbHelper = new SQLiteHelper(ImageAcquisitionActivity.this);

        flashBtn.setEnabled(false);
        autoPredictSwitch.setEnabled(false);

        ContentValues contentValues = new ContentValues();
        Location location = getLocation(ImageAcquisitionActivity.this); // Implement getLocation() method below
        if (location != null) {
            contentValues.put(MediaStore.Images.Media.LATITUDE, location.getLatitude());
            contentValues.put(MediaStore.Images.Media.LONGITUDE, location.getLongitude());

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            latitudeString = String.valueOf(latitude);
            longitudeString = String.valueOf(longitude);

        }else{
            latitudeString = "No Data";
            longitudeString = "No Data";
        }

        detector = new Detector(getBaseContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, ImageAcquisitionActivity.this);
        detector.setup(CONFIDENCE_THRESHOLD);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCameraOff) {
                    flashBtn.setEnabled(true);
                    flashBtn.setBackgroundResource(R.drawable.bg_button_flashoff);
                    autoPredictSwitch.setVisibility(View.VISIBLE);
                    autoPredictSwitch.setEnabled(true);
                    imageView.setVisibility(View.GONE);
                    cameraBtn.setBackgroundResource(R.drawable.bg_button_capture);
                    cameraContainer.setVisibility(View.VISIBLE);
                    isCameraOn = true;

                    if (cameraExecutor == null || cameraExecutor.isShutdown()) {
                        cameraExecutor = Executors.newSingleThreadExecutor();
                        binding.overlay.invalidate();
                        binding.overlay.clear();
                    }

                    detector = new Detector(getBaseContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, ImageAcquisitionActivity.this);
                    detector.setup(CONFIDENCE_THRESHOLD);

                    if (allPermissionsGrant()) {
                        isCameraOff = false;
                        startCamera();
                        setupTouchListener();
                    } else {
                        Intent intent = new Intent(ImageAcquisitionActivity.this, PermissionActivity.class);
                        startActivity(intent);
                    }
                } else if (isCameraOn) {
                    cameraBtn.setBackgroundResource(R.drawable.bg_button_processing);

                    ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(cameraBtn, "rotation", 0f, 360f);
                    galBtn.setEnabled(false);
                    galBtn.setBackgroundResource(R.drawable.bg_button_gallery_disabled);
                    flashBtn.setEnabled(false);
                    flashBtn.setBackgroundResource(R.drawable.bg_button_flashoff_disabled);
                    cameraBtn.setEnabled(false);
                    autoPredictSwitch.setEnabled(false);
                    binding.viewFinder.setEnabled(false);

                    isCameraOn = false;

                    stopCamera(); // Stop camera when selecting image from gallery
                    if (cameraContainer.getVisibility() == View.VISIBLE) {
                        cameraContainer.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                    }

                    cameraProvider.unbindAll();
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);

                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(bitmap);

                    if(bitmap == null){
                        tryAgain = true;
                        uiChanges();
                        detected.setText("Wait for Camera Initialization!");
                        detected.setTextColor(Color.RED);
                    }else{
                        rotationAnimator.setDuration(2000); // Duration in milliseconds
                        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Repeat indefinitely
                        rotationAnimator.setRepeatMode(ObjectAnimator.RESTART); // Start from the beginning after each rotation
                        rotationAnimator.start();
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        binding.overlay.clear();
                        if(isPredictable){
                            saveDetection(bitmap, detectedBitmap);
                        }
                        if (detector != null) {
                            detector.shutdown();
                        }
                        cameraBtn.setEnabled(true);
                        cameraBtn.setRotation(0f);
                        rotationAnimator.cancel();
                        uiChanges();
                    }, 2000);

                } else if (isPredictable) {
                    if (detector != null) {
                        detector.shutdown();
                    }

                    if(!inputImagePath.isEmpty() || !outputPath.isEmpty()){
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                        imgId = dbHelper.addImage(userId, "https://c1.wallpaperflare.com/preview/263/75/660/buddha-image-buddha-statue-black-and-white.jpg", inputImagePath, timestamp);
                        dbHelper.addImageTag(imgId, latitudeString + ", " + longitudeString);

                        Intent intent = new Intent(ImageAcquisitionActivity.this, ProcessActivity.class);
                        intent.putExtra("imgId", imgId);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();

                    }else{
                        Toast.makeText(ImageAcquisitionActivity.this, "Image saving issue!", Toast.LENGTH_SHORT).show();
                    }


                } else if (tryAgain) {
                    if (detector != null) {
                        detector.shutdown();
                    }
                    Intent intent = new Intent(ImageAcquisitionActivity.this, ImageAcquisitionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }
        });

        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraControl = camera.getCameraControl();
                if(isFlash == 0){
                    //Turn on camera flash
                    isFlash = 1;
                    flashBtn.setBackgroundResource(R.drawable.bg_button_torch);
                    cameraControl.enableTorch(true);
                }else if(isFlash == 1){
                    isFlash = 0;
                    cameraControl.enableTorch(false);
                    flashBtn.setBackgroundResource(R.drawable.bg_button_flashoff);
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                }
            }
        });

        autoPredictSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    autoPredict = true;
                    Toast.makeText(ImageAcquisitionActivity.this, "Auto Predict On", Toast.LENGTH_SHORT).show();
                } else {
                    autoPredict = false;
                    Toast.makeText(ImageAcquisitionActivity.this, "Auto Predict Off", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private Location getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                return location;
            } else {
                return null;
            }
        }
        return null;
    }
    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed.", e);
                retryCameraInitialization();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void retryCameraInitialization() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (cameraProvider == null) {
                Log.e(TAG, "Retrying camera initialization...");
                startCamera();
            }
        }, 2000); // Retry after 2 seconds
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            throw new IllegalStateException("Camera initialization failed.");
        }

        //int rotation = binding.viewFinder.getDisplay().getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_DEFAULT)
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_DEFAULT)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
            Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
            imageProxy.getPlanes()[0].getBuffer().rewind();
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

            if (isFrontCamera) {
                matrix.postScale(-1f, 1f, imageProxy.getWidth(), imageProxy.getHeight());
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, false);
            bitmap = rotatedBitmap;
            detector.detect(rotatedBitmap);

            imageProxy.close();
        });

        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_DEFAULT)
                .build();

        cameraProvider.unbindAll();
        imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer, imageCapture);
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        binding.viewFinder.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                MeteringPointFactory factory = binding.viewFinder.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());

                FocusMeteringAction action = new FocusMeteringAction.Builder(point)
                        .addPoint(point)
                        .build();


                cameraControl = camera.getCameraControl();
                cameraControl.startFocusAndMetering(action).addListener(() -> {

                }, ContextCompat.getMainExecutor(this));
                return true;
            }
            return false;
        });
    }
    private File getOutputDirectory() {
        File mediaDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tempCaptures");
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }
        return mediaDir;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();

    }

    @Override
    public void onEmptyDetect() {
        binding.overlay.invalidate();
        binding.overlay.clear();
        tryAgain = true;
        isPredictable = false;
        detected.setText("No object detected. Please try again!");
    }
    public void onEmptyDetectGallery() {
        isCameraOff = false;
        tryAgain = true;
        uiChanges();
        detected.setText("No object detected. Please try again!");


    }
    @Override
    public void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime, Bitmap originalCapture, Bitmap processedCapture, boolean isautoDetected) {
        runOnUiThread(() -> {
            binding.inferenceTime.setText("Processing Delay : " + inferenceTime + " milliseconds");
            binding.overlay.setResults(boundingBoxes);
            binding.overlay.invalidate();

            bitmap = originalCapture;
            detectedBitmap = processedCapture;

            if(autoPredict){
                if(isautoDetected){
                    isPredictable = true;
                    saveDetection(bitmap, detectedBitmap);
                    uiChanges();
                }
            }else{
                if(isautoDetected){
                    isPredictable = true;
                }else{
                    tryAgain = true;
                    detected.setText("The image is not suitable for processing. Please try a different image.");
                }
            }

        });
    }

    private void saveDetection(Bitmap originalCapture, Bitmap processedCapture) {

        cameraBtnlayout.setBackground(ContextCompat.getDrawable(ImageAcquisitionActivity.this, R.color.colorPrimary));
        isCameraOn = false;

        imageView.setBackgroundResource(R.drawable.bg_placeholder);
        stopCamera(); // Stop camera when selecting image from gallery
        if (cameraContainer.getVisibility() == View.VISIBLE) {
            cameraContainer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }

        cameraProvider.unbindAll();
        imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(processedCapture);


        //File saving part

        File photoFile = new File(getOutputDirectory(), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            originalCapture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            capturePath =  photoFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap", e);
        }

        outputPath = saveBitmapToFile(processedCapture);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            if (outputPath != null) {
                inputImagePath = capturePath;
            }
        }
    }

    @Override
    public void onDetectGallery(List<BoundingBox> boundingBoxes, long inferenceTime, Bitmap processedBitmap, boolean isdetected) {
        runOnUiThread(() -> {
            binding.inferenceTime.setText("Processing Delay : " + inferenceTime + " milliseconds");
            List<String> labels = detector.getLabels();
            binding.overlay.setResultsGallery(boundingBoxes, labels);
            binding.overlay.invalidate();

            imageView.setImageBitmap(processedBitmap);

            if(isdetected){
                isPredictable = true;
                uiChanges();

                SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
                int userId = sharedPreferences.getInt("userId", -1);

                if (userId != -1) {
                    outputPath = saveBitmapToFile(processedBitmap);
                    if (outputPath != null) {
                        if (!galleryPath.isEmpty()) {
                            inputImagePath = galleryPath; // Assuming this is the path of the input image
                        }else{
                            inputImagePath = capturePath;
                        }
                    }
                }

            }else{
                tryAgain = true;
                uiChanges();
            }

        });

        isCameraOff = false;

        if (detector != null) {
            detector.shutdown();
        }
    }

    private void uiChanges() {

        cameraBtnlayout.setBackground(ContextCompat.getDrawable(ImageAcquisitionActivity.this, R.color.colorPrimary));
        galBtn.setVisibility(View.GONE);
        galBtn.setEnabled(false);
        galBtn.setBackgroundResource(R.drawable.bg_button_gallery_disabled);
        flashBtn.setVisibility(View.GONE);
        flashBtn.setEnabled(false);
        flashBtn.setBackgroundResource(R.drawable.bg_button_flashoff_disabled);
        autoPredictSwitch.setVisibility(View.GONE);
        autoPredictSwitch.setEnabled(false);

        if(tryAgain){
            detected.setVisibility(View.VISIBLE);
            detected.setGravity(Gravity.CENTER);
            detected.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            cameraBtn.setBackgroundResource(R.drawable.bg_button_retry);

        }

        if (isPredictable){
            detected.setVisibility(View.INVISIBLE);
            cameraBtn.setBackgroundResource(R.drawable.bg_button_generate);

        }
    }


    private String saveBitmapToFile(Bitmap bitmap) {
        File imageFile = new File(getOutputDirectory(), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_output.jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap", e);
            return null;
        }
    }

    public void selectImage(View view) {
        isCameraOff = true;
        flashBtn.setEnabled(false);
        flashBtn.setBackgroundResource(R.drawable.bg_button_flashoff_disabled);
        autoPredictSwitch.setVisibility(View.INVISIBLE);
        autoPredictSwitch.setEnabled(false);
        detector = new Detector(getBaseContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, ImageAcquisitionActivity.this);
        detector.setup(CONFIDENCE_THRESHOLD);
        imageView.setImageDrawable(getDrawable(R.drawable.bg_placeholder));
        //cameraBtn.setText("Open Camera");
        cameraBtn.setBackgroundResource(R.drawable.bg_button_opencamera);
        stopCamera(); // Stop camera when selecting image from gallery
        if(croppedBitmap != null){
            croppedBitmap = null;
        }
        if (cameraContainer.getVisibility() == View.VISIBLE) {
            cameraContainer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK && data != null) {
            Uri uri = data.getData();
            try {
                String path = uri.getPath();
                path = removeRawSegment(path);
                galleryPath = path;
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);

                // Pass the bitmap to the detection method
                detector.detectGallery(bitmap);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String removeRawSegment(String path) {
        // Check if "/raw/" exists in the path
        int rawIndex = path.indexOf("/raw/");
        if (rawIndex != -1) {
            // Remove "/raw/" and return the modified path
            return path.substring(0, rawIndex) + path.substring(rawIndex + 5);
        }
        // If "/raw/" does not exist, return the original path
        return path;
    }

    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }
    }

    private boolean allPermissionsGrant() {
        String[] REQUIRED_PERMISSIONS_ANDROID_12 = new String[]{
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        String[] REQUIRED_PERMISSIONS_ANDROID_13 = new String[]{
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // API level 33
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_13) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }else{
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_12) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


}
