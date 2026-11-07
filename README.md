# TORQUE — Every Ride. Better Care.

TORQUE is a production-grade native Android application developed for a professional bike repair and maintenance workshop. It provides a real-time, cloud-connected experience for motorcycle owners (customers) and the workshop owner (admin).

---

## Key Features

### For Customers
- **Google Sign-In**: Secure authentication using Jetpack Credential Manager and Firebase Authentication.
- **Service Catalog**: Live workshop service packages loaded directly from Cloud Firestore with pricing, category filters, and duration estimates.
- **Service Booking**: Comprehensive booking form capturing bike brand, model, registration plate number, issue description, and preferred appointment time slots.
- **Live Booking Tracker**: Real-time status updates reflecting workshop progress (**Pending** ➔ **Accepted** ➔ **In Service** ➔ **Completed**).
- **Direct WhatsApp Chat**: One-tap WhatsApp deep link opening a pre-filled chat with the workshop master mechanic.
- **Rider Profile**: Profile management, booking history, and sign-out.

### For Workshop Owner (Admin)
- **Restricted Access**: Admin console secured via Google identity verification and hardened Cloud Firestore Security Rules against `muzamillp20@gmail.com`.
- **Live Statistics Dashboard**: Real-time counters for Total, Pending, Accepted, In-Service, Completed, and Declined bookings.
- **Appointment Management**: Search, filter, approve, decline, and advance booking statuses with workshop notes.
- **Service Catalog Management**: Add, edit, toggle active status, or delete workshop service packages.
- **Customer Directory**: View all registered riders and their booking records.
- **Workshop Settings**: Update workshop name, physical address, customer-facing voice phone, and WhatsApp contact number.

---

## Technical Architecture

- **Language & Platform**: Kotlin 2.2, Android SDK 36, Jetpack Compose, Material 3.
- **Architecture**: MVVM with Repository Pattern, Kotlin Coroutines, and reactive `StateFlow`.
- **Cloud Backend**: Google Cloud Firestore Enterprise edition and Firebase Authentication.
- **Security**: Zero-Trust Firestore Security Rules (`firestore.rules`) enforcing caller verification, strict property schemas, and role isolation.
- **Navigation**: Jetpack Navigation Compose with Predictive Back support (`BackHandler`).

---

## Firebase Configuration

This applet has been provisioned with:
- **Firebase Project ID**: `gen-lang-client-0309718152`
- **Firestore Database ID**: `ai-studio-android-torque-ba1bc479-57d3-4f44-a4f4-7bb0e7706c75`
- **Application ID**: `com.torque.bikecare`
- **Debug SHA-1**: `F9:E7:0C:30:4E:4F:D6:37:D4:C1:98:CF:53:B6:2B:79:BC:8D:C4:90`

### Administrator Account
The designated workshop owner email is configured as:
```
muzamillp20@gmail.com
```
Signing in with this Google account unlocks the Workshop Owner Dashboard and Admin menu.
