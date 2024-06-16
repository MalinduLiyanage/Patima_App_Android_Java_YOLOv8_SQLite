package com.onesandzeros.patima;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.onesandzeros.patima.databinding.ActivityImageAcquisitionBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    ImageButton galBtn, cameraBtn;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private Detector detector;
    ImageView imageView;
    private ExecutorService cameraExecutor;
    ConstraintLayout cameraContainer;
    boolean isCameraOn = false, isCameraOff = true, isPredictable = false, tryAgain = false;
    private final int IMAGE_PICK = 100;
    Bitmap bitmap, croppedBitmap, detectedBitmap;
    private ImageCapture imageCapture;
    String capturePath = "", galleryPath = "";
    TextView detected;
    private static final String TAG = "Camera";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    int imgId = 0;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    String latitudeString = null, longitudeString = null;

    Float CONFIDENCE_THRESHOLD = 0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageAcquisitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!allPermissionsGrant()) {
            Intent intent = new Intent(ImageAcquisitionActivity.this, PermissionActivity.class);
            startActivity(intent);
            finish();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);

        CONFIDENCE_THRESHOLD = sharedPreferences.getFloat("CONFIDENCE_THRESHOLD",0);


        galBtn = findViewById(R.id.galleryBtn);
        cameraBtn = findViewById(R.id.captureBtn);
        imageView = findViewById(R.id.imageView);
        cameraContainer = findViewById(R.id.camera_container);
        detected = findViewById(R.id.detect_txt);


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

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCameraOff) {
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
                    } else {
                        Intent intent = new Intent(ImageAcquisitionActivity.this, PermissionActivity.class);
                        startActivity(intent);
                    }
                } else if (isCameraOn) {
                    binding.overlay.clear();
                    captureAndSaveImage();
                    if (detector != null) {
                        detector.shutdown();
                    }
                } else if (isPredictable) {
                    Intent intent = new Intent(ImageAcquisitionActivity.this, ProcessActivity.class);
                    intent.putExtra("imgId", imgId);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else if (tryAgain) {
                    Intent intent = new Intent(ImageAcquisitionActivity.this, ImageAcquisitionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }
        });

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

        int rotation = binding.viewFinder.getDisplay().getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(rotation)
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

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);
            detector.detect(rotatedBitmap);

            imageProxy.close();
        });

        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build();

        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer, imageCapture);
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }
    private void captureAndSaveImage() {

        // Create output options for the captured image
        File photoFile = new File(getOutputDirectory(), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take the picture and save it
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String savedImagePath = photoFile.getAbsolutePath();
                //Toast.makeText(ImageAcquisitionActivity.this, "Image saved at: " + savedImagePath, Toast.LENGTH_LONG).show();
                capturePath = savedImagePath;
                Log.e(TAG, savedImagePath);

                // Load the bitmap from the file path
                Bitmap savedBitmap = BitmapFactory.decodeFile(savedImagePath);

                if (savedBitmap != null) {
                    // Center crop the bitmap
                    int originalWidth = savedBitmap.getWidth();
                    int originalHeight = savedBitmap.getHeight();
                    int imageViewWidth = imageView.getWidth();
                    int imageViewHeight = imageView.getHeight();

                    float scale = Math.max((float) imageViewWidth / originalWidth, (float) imageViewHeight / originalHeight);
                    float scaledWidth = scale * originalWidth;
                    float scaledHeight = scale * originalHeight;
                    float left = (imageViewWidth - scaledWidth) / 2;
                    float top = (imageViewHeight - scaledHeight) / 2;

                    croppedBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(croppedBitmap);

                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    matrix.postTranslate(left, top);
                    canvas.drawBitmap(savedBitmap, matrix, null);

                    // Set the cropped bitmap to the ImageView
                    imageView.setBackgroundResource(R.drawable.bg_placeholder);
                    //cameraBtn.setText("Open Camera");
                    cameraBtn.setBackgroundResource(R.drawable.bg_button_opencamera);
                    stopCamera(); // Stop camera when selecting image from gallery
                    if (cameraContainer.getVisibility() == View.VISIBLE) {
                        cameraContainer.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                    }
                    imageView.setImageBitmap(croppedBitmap);

                    // Pass the cropped bitmap to the detection method
                    isCameraOn = false;
                    loadImageFromFileAndDetect();

                } else {
                    Toast.makeText(ImageAcquisitionActivity.this, "Failed to load and process the image", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load and process the image.");
                }

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Image capture failed: " + exception.getMessage(), exception);
            }
        });

    }
    private void loadImageFromFileAndDetect() {
        detector = new Detector(getBaseContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, ImageAcquisitionActivity.this);
        detector.setup(CONFIDENCE_THRESHOLD);
        Bitmap bitmap = BitmapFactory.decodeFile(capturePath);

        if (bitmap != null) {
            // Center crop the bitmap
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int imageViewWidth = imageView.getWidth();
            int imageViewHeight = imageView.getHeight();

            float scale = Math.max((float) imageViewWidth / originalWidth, (float) imageViewHeight / originalHeight);
            float scaledWidth = scale * originalWidth;
            float scaledHeight = scale * originalHeight;
            float left = (imageViewWidth - scaledWidth) / 2;
            float top = (imageViewHeight - scaledHeight) / 2;

            croppedBitmap = Bitmap.createBitmap(imageViewWidth, imageViewHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(croppedBitmap);

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postTranslate(left, top);
            canvas.drawBitmap(bitmap, matrix, null);

            // Set the cropped bitmap to the ImageView
            imageView.setBackgroundResource(R.drawable.bg_placeholder);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(croppedBitmap);

            // Pass the cropped bitmap to the detection method
            detector.detectGallery(croppedBitmap);

        } else {
            Toast.makeText(ImageAcquisitionActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to load image.");
        }
        if (detector != null){
            detector.shutdown();
            //Toast.makeText(ImageAcquisitionActivity.this, "Detector shutdown", Toast.LENGTH_SHORT).show();
        }
    }
    private File getOutputDirectory() {
        File mediaDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tempCaptures");
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }
        return mediaDir;
    }
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (result.getOrDefault(Manifest.permission.CAMERA, false) && result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false)) {
                        isCameraOff = false;
                        startCamera();
                    }
                }
            }
    );

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();

    }

    @Override
    public void onEmptyDetect() {
        binding.overlay.invalidate();
    }
    public void onEmptyDetectGallery() {
        tryAgain = true;
        detected.setVisibility(View.VISIBLE);
        detected.setGravity(Gravity.CENTER);
        detected.setText("No object detected. Please try again!");
        //cameraBtn.setText("Try Again");
        cameraBtn.setBackgroundResource(R.drawable.bg_button_retry);
        galBtn.setVisibility(View.GONE);
        galBtn.setEnabled(false);
    }
    @Override
    public void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime) {
        runOnUiThread(() -> {
            binding.inferenceTime.setText(inferenceTime + "ms");
            binding.overlay.setResults(boundingBoxes);
            binding.overlay.invalidate();
        });
    }
    /*
    @Override
    public void onDetectGallery(List<BoundingBox> boundingBoxes, long inferenceTime) {
        runOnUiThread(() -> {
            binding.inferenceTime.setText(inferenceTime + "ms");
            List<String> labels = detector.getLabels();
            binding.overlay.setResultsGallery(boundingBoxes, labels);
            binding.overlay.invalidate();

            // Update the bitmap variable with the cropped bitmap before drawing bounding boxes
            if (croppedBitmap != null) {
                bitmap = croppedBitmap; // Make sure croppedBitmap is the correct bitmap that you want to use
                // Display the imageView with the detected bounding boxes
                Bitmap detectedBitmap = drawBoundingBoxes(bitmap.copy(Bitmap.Config.ARGB_8888, true), boundingBoxes, labels);
                imageView.setImageBitmap(detectedBitmap);
            } else if (bitmap != null){
                Bitmap detectedBitmap = drawBoundingBoxes(bitmap.copy(Bitmap.Config.ARGB_8888, true), boundingBoxes, labels);
                imageView.setImageBitmap(detectedBitmap);
            } else {
                Log.e(TAG, "croppedBitmap is null");
            }

            // Create a map to store the count of detections for each label
            Map<String, Integer> labelCounts = new HashMap<>();

            for (BoundingBox box : boundingBoxes) {
                String label = box.getClsName();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
                }
            }

            int headCount = 0, bodyCount = 0;

            // Display count of detections with label names as toast messages
            for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
                String labelName = entry.getKey();
                if(labelName.contains("head")){
                    headCount++;
                }else if(labelName.contains("body")){
                    bodyCount++;
                }
            }
            if(headCount == 0 && bodyCount == 1){
                isPredictable = true;
            }else{
                tryAgain = true;
            }
            //Toast.makeText(ImageAcquisitionActivity.this, "Head count: " + headCount + " Body count: " + bodyCount, Toast.LENGTH_SHORT).show();

        });
        if (detector != null){
            detector.shutdown();
            //Toast.makeText(ImageAcquisitionActivity.this, "Detector shutdown", Toast.LENGTH_SHORT).show();
        }
        if (isPredictable){
            //cameraBtn.setText("Generate!");
            cameraBtn.setBackgroundResource(R.drawable.bg_button_generate);
            galBtn.setVisibility(View.GONE);
            galBtn.setEnabled(false);
        }else if(tryAgain){
            detected.setVisibility(View.VISIBLE);
            //cameraBtn.setText("Try Again");
            cameraBtn.setBackgroundResource(R.drawable.bg_button_retry);
            galBtn.setVisibility(View.GONE);
            galBtn.setEnabled(false);
        }

    }
*/
    @Override
    public void onDetectGallery(List<BoundingBox> boundingBoxes, long inferenceTime) {
        runOnUiThread(() -> {
            binding.inferenceTime.setText(inferenceTime + "ms");
            List<String> labels = detector.getLabels();
            binding.overlay.setResultsGallery(boundingBoxes, labels);
            binding.overlay.invalidate();

            if (croppedBitmap != null) {
                bitmap = croppedBitmap;
                detectedBitmap = drawBoundingBoxes(bitmap.copy(Bitmap.Config.ARGB_8888, true), boundingBoxes, labels);
                imageView.setImageBitmap(detectedBitmap);
            } else if (bitmap != null){
                detectedBitmap = drawBoundingBoxes(bitmap.copy(Bitmap.Config.ARGB_8888, true), boundingBoxes, labels);
                imageView.setImageBitmap(detectedBitmap);
            } else {
                Log.e(TAG, "croppedBitmap is null");
            }

            Map<String, Integer> labelCounts = new HashMap<>();
            for (BoundingBox box : boundingBoxes) {
                String label = box.getClsName();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
                }
            }

            int headCount = 0, bodyCount = 0;
            for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
                String labelName = entry.getKey();
                if (labelName.contains("head")) {
                    headCount++;
                } else if (labelName.contains("body")) {
                    bodyCount++;
                }
            }

            if (headCount == 0 && bodyCount == 1) {
                isPredictable = true;
            } else {
                tryAgain = true;
            }
        });

        if (detector != null) {
            detector.shutdown();
        }

        if (isPredictable) {
            SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
            int userId = sharedPreferences.getInt("userId", -1);

            if (userId != -1) {
                String outputPath = saveBitmapToFile(detectedBitmap);
                String inputImagePath;
                if (outputPath != null) {
                    SQLiteHelper dbHelper = new SQLiteHelper(this);
                    if (!galleryPath.isEmpty()) {
                        inputImagePath = galleryPath; // Assuming this is the path of the input image
                    }else{
                        inputImagePath = capturePath;
                    }
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                    imgId = dbHelper.addImage(userId, outputPath, inputImagePath, timestamp);
                    dbHelper.addImageTag(imgId, latitudeString + ", " + longitudeString);
                    //Toast.makeText(this, imid, Toast.LENGTH_SHORT).show();
                }
            }

            cameraBtn.setBackgroundResource(R.drawable.bg_button_generate);
            galBtn.setVisibility(View.GONE);
            galBtn.setEnabled(false);
        } else if (tryAgain) {
            detected.setVisibility(View.VISIBLE);
            cameraBtn.setBackgroundResource(R.drawable.bg_button_retry);
            galBtn.setVisibility(View.GONE);
            galBtn.setEnabled(false);
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


    private Bitmap drawBoundingBoxes(Bitmap bitmap, List<BoundingBox> boundingBoxes, List<String> labels) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8F);
        paint.setColor(Color.GREEN);
        paint.setTextSize(40);

        for (int i = 0; i < boundingBoxes.size(); i++) {
            BoundingBox box = boundingBoxes.get(i);
            float left = box.getX1() * mutableBitmap.getWidth();
            float top = box.getY1() * mutableBitmap.getHeight();
            float right = box.getX2() * mutableBitmap.getWidth();
            float bottom = box.getY2() * mutableBitmap.getHeight();

            // Draw bounding box
            canvas.drawRect(left, top, right, bottom, paint);

            // Draw label
            String label = labels.get(i);
            //canvas.drawText(label, left, top - 10, paint);
        }

        return mutableBitmap;
    }
    public void selectImage(View view) {
        isCameraOff = false;
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
                //Toast.makeText(this, galleryPath, Toast.LENGTH_LONG).show();
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
