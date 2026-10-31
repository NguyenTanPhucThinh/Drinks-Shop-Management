package com.uitdrinkapp.app.ui.customer.storeentry;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Owns only this screen's camera use cases and releases every analyzed frame. */
final class ShopCameraScanner implements AutoCloseable {
    interface Listener {
        void onReady();
        void onQrCode(String rawValue);
        void onCameraError();
        void onReadError();
    }
    private final Context context;
    private final Listener listener;
    private final Executor mainExecutor;
    private final ExecutorService analyzerExecutor = Executors.newSingleThreadExecutor();
    private final BarcodeScanner scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build());
    private ProcessCameraProvider provider;
    private Preview preview;
    private ImageAnalysis analysis;
    private volatile boolean closed;

    ShopCameraScanner(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        mainExecutor = ContextCompat.getMainExecutor(context);
    }
    void start(LifecycleOwner owner, PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            if (closed) return;
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    listener.onCameraError(); return;
                }
                provider = future.get();
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(analyzerExecutor, this::analyze);
                provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
                listener.onReady();
            } catch (Exception exception) {
                if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
                if (!closed) listener.onCameraError();
            }
        }, mainExecutor);
    }
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyze(@NonNull ImageProxy frame) {
        if (closed) { frame.close(); return; }
        Image image = frame.getImage();
        if (image == null) { frame.close(); return; }
        try {
            scanner.process(InputImage.fromMediaImage(image, frame.getImageInfo().getRotationDegrees()))
                    .addOnSuccessListener(mainExecutor, barcodes -> {
                        if (closed) return;
                        for (Barcode barcode : barcodes) {
                            String raw = barcode.getRawValue();
                            if (raw != null && !raw.trim().isEmpty()) { listener.onQrCode(raw); break; }
                        }
                    })
                    .addOnFailureListener(mainExecutor, exception -> { if (!closed) listener.onReadError(); })
                    .addOnCompleteListener(task -> frame.close());
        } catch (RuntimeException exception) {
            frame.close();
            mainExecutor.execute(() -> { if (!closed) listener.onReadError(); });
        }
    }
    @Override public void close() {
        if (closed) return;
        closed = true;
        if (analysis != null) analysis.clearAnalyzer();
        if (provider != null && preview != null && analysis != null) provider.unbind(preview, analysis);
        scanner.close();
        analyzerExecutor.shutdown();
    }
}
