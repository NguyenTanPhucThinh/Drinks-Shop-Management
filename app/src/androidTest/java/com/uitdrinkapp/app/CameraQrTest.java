package com.uitdrinkapp.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.camera.view.PreviewView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.uitdrinkapp.app.data.repository.FakeShopRepository;
import com.uitdrinkapp.app.data.repository.PreferencesShopSessionRepository;
import com.uitdrinkapp.app.domain.JoinShopService;
import com.uitdrinkapp.app.domain.ShopQrCode;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CameraQrTest {
    @Test public void cameraPreviewStreamsAndRestartsAfterRecreation() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                context.getPackageName(), android.Manifest.permission.CAMERA);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.btn_scan_qr)).perform(click());
            assertStreaming(scenario);
            scenario.recreate();
            assertStreaming(scenario);
        }
    }
    private void assertStreaming(ActivityScenario<MainActivity> scenario) throws Exception {
        CountDownLatch streaming = new CountDownLatch(1);
        scenario.onActivity(activity -> {
            PreviewView preview = activity.findViewById(R.id.preview_camera);
            preview.getPreviewStreamState().observe(activity, state -> {
                if (state == PreviewView.StreamState.STREAMING) streaming.countDown();
            });
        });
        assertTrue("Camera must deliver a live preview", streaming.await(20, TimeUnit.SECONDS));
    }
    @Test public void realQrDecoderProducesShopAndTableSession() throws Exception {
        Bitmap bitmap;
        try (InputStream input = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("shop_qr.png")) {
            bitmap = BitmapFactory.decodeStream(input);
        }
        BarcodeScanner scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build());
        try {
            List<Barcode> codes = Tasks.await(scanner.process(InputImage.fromBitmap(bitmap, 0)), 20, TimeUnit.SECONDS);
            assertFalse(codes.isEmpty());
            ShopQrCode qr = ShopQrCode.parse(codes.get(0).getRawValue());
            assertNotNull(qr);
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            FakeShopRepository shops = new FakeShopRepository();
            PreferencesShopSessionRepository sessions = new PreferencesShopSessionRepository(context, shops);
            new JoinShopService(shops, sessions).join(qr.getShopCode(), qr.getTableNumber());
            assertEquals("abc", sessions.getSession().getShopId());
            assertEquals("05", sessions.getSession().getTableNumber());
        } finally { scanner.close(); bitmap.recycle(); }
    }
}
