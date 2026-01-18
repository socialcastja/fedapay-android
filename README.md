# FedaPay Android App

A Kotlin/Jetpack Compose Android app for the FedaPay merchant payment system.

## Features

- User & Merchant authentication
- Dashboard with wallet balance and transactions
- POS terminal with keypad (FTK, NFC, QR payment methods)
- Wallet with send/receive functionality
- QR code scanner for payments
- Settings with profile management

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Repository pattern
- **DI:** Hilt
- **Networking:** Retrofit + OkHttp
- **Storage:** Encrypted SharedPreferences
- **Camera:** CameraX + ML Kit (QR scanning)

## Building the APK

### Option 1: GitHub Actions (Recommended)

1. Push this project to a GitHub repository
2. Go to the **Actions** tab in your repository
3. The workflow will automatically build on push to main/master
4. Click on the completed workflow run
5. Download the APK from the **Artifacts** section

### Option 2: Local Build

Requirements:
- JDK 17
- Android SDK

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

The APK will be at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Signing for Release

To create a signed release APK for Play Store:

1. Generate a keystore:
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

2. For GitHub Actions, add these secrets to your repository:
   - `KEYSTORE_BASE64`: Base64 encoded keystore file
   - `KEYSTORE_PASSWORD`: Keystore password
   - `KEY_ALIAS`: Key alias
   - `KEY_PASSWORD`: Key password

To encode your keystore:
```bash
base64 -i my-release-key.jks | pbcopy
```

## Project Structure

```
app/src/main/java/app/fedha/fedapay/
├── FedaPayApplication.kt      # Hilt Application
├── MainActivity.kt            # Main entry point
├── data/
│   ├── api/
│   │   └── FedaPayApi.kt     # Retrofit API interface
│   ├── models/
│   │   ├── User.kt           # Data models
│   │   └── ApiResponses.kt   # API response types
│   └── repository/
│       ├── AuthRepository.kt  # Authentication logic
│       └── WalletRepository.kt # Wallet operations
├── di/
│   └── AppModule.kt          # Hilt dependency injection
└── ui/
    ├── FedaPayApp.kt         # Main navigation
    ├── auth/
    │   ├── LoginScreen.kt
    │   ├── RegisterScreen.kt
    │   └── AuthViewModel.kt
    ├── main/
    │   ├── DashboardScreen.kt
    │   ├── WalletScreen.kt
    │   ├── SettingsScreen.kt
    │   └── *ViewModel.kt
    ├── merchant/
    │   ├── POSScreen.kt
    │   └── QRScannerScreen.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## API Configuration

The API base URL is configured in `di/AppModule.kt`:
```kotlin
private const val BASE_URL = "https://fedha.app/api/mobile/index.php/"
```

## License

Proprietary - FedaPay
