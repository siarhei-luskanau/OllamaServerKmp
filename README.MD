# compose-multiplatform-template

### Android
To run the application on android device/emulator:
- open project in Android Studio and run imported android run configuration

To build the application bundle:
- run `./gradlew app:androidApp:assembleDebug app:androidApp:assembleRelease`
- find `.apk` file in `app/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- Run android UI tests on the virtual device: `./gradlew managedVirtualDeviceDebugAndroidTest managedVirtualDeviceAndroidDeviceTest -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect`

### iOS
To run the application on iPhone device/simulator:
- Open `iosApp/iosApp.xcproject` in Xcode and run standard configuration
- Or use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) for Android Studio
- Run iOS simulator UI tests: `./gradlew iosSimulatorArm64Test`

### Code style and linting
- launch ktlintFormat: `./gradlew ktlintFormat`
- launch ktlint, detekt and lint checks: `./gradlew ktlintCheck detekt lint`

### Screenshot Testing
Record a screenshot: `./gradlew recordRoborazzi`
