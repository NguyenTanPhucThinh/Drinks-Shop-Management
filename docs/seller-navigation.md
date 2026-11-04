# Seller navigation shell

Six independent Java Fragments render one centered title each. The bottom bar
uses the agreed light seller palette, including in device night mode. The
notification dot is mock presentation until real notification data is connected.

## Open the seller shell

Select the debug build variant and the shared **Seller** run configuration in
Android Studio. If it has not appeared after project sync, in Run > Edit
Configurations create an Android App configuration for the app module, select
Launch > Specified Activity, and enter:

`com.uitdrinkapp.app.ui.seller.main.SellerMainActivity`

Only the debug manifest exports this Activity for development. The main manifest
keeps it unexported. The customer launcher is still the default app entry point.
Once the debug APK is installed, it can also be opened with:

```powershell
adb shell am start -n com.uitdrinkapp.app/.ui.seller.main.SellerMainActivity
```

## Add screen content

The seller navigation graph maps these stable IDs to feature-owned Fragments:

| Destination ID | Fragment | Package under ui/seller |
| --- | --- | --- |
| sellerHomeFragment | SellerHomeFragment | home |
| sellerNotificationFragment | SellerNotificationFragment | notification |
| sellerOrdersFragment | OrderManagementFragment | orders |
| sellerProductFragment | ProductManagementFragment | product |
| sellerOtherFeaturesFragment | OtherFeaturesFragment | otherfeatures |
| sellerProfileFragment | SellerProfileFragment | profile |

Replace each screen's title-only layout and add its logic inside its own
Fragment. Keep the destination IDs. SellerMainActivity owns tab selection,
tab state restoration and Back behavior. Back returns to Overview, then closes
the Activity. The separate Activity/graph follows the seller assignment.

## Verification

SellerNavigationTest covers all six destinations and their single-TextView
content, repeated taps, Back without accumulated tab history, Activity
recreation and rotation. It also captures screenshots in the target app's
external files directory under screenshots.

The existing HomeJoinFlowTest refers to the removed et_shop_code view, so the
full androidTest source set does not compile without isolating that old test.
For a seller-only run, use this temporary Gradle init script outside the repo:

```groovy
allprojects {
    tasks.withType(JavaCompile).configureEach {
        if (name == 'compileDebugAndroidTestJavaWithJavac') {
            exclude '**/HomeJoinFlowTest.java'
        }
    }
}
```

Build the debug and androidTest APKs with --init-script pointing to that file,
install both APKs, then run:

```powershell
adb shell am instrument -w -r -e class com.uitdrinkapp.app.SellerNavigationTest com.uitdrinkapp.app.test/androidx.test.runner.AndroidJUnitRunner
```

Verified on the Android 35 AVD: all three seller tests pass in both the default
411dp configuration with gesture navigation and the 360dp / 130% font / night
mode configuration with three-button navigation. Debug build and lint complete;
there are no seller lint warnings. The release merged manifest keeps the seller
Activity unexported. Direct debug Activity launch was also checked through adb.

Build and lint reports remain under app/build/reports. Reference screenshots
are in docs/screenshots; the small-screen capture uses 360dp width, 130% font
scale, device night mode and three-button system navigation.
