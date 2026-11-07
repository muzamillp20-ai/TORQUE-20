# TORQUE — Testing & Verification Report

## 1. Security Rules Testing (Tier 1: JavaScript Engine)
- **Engine**: Node.js test runner against Cloud Firestore Emulator on port 8085.
- **Suite**: `firestore.test.js`
- **Results**:
  - Test 1: Unauthenticated user cannot read services, bookings or users (PASSED)
  - Test 2: Customer can create own profile, cannot self-escalate to ADMIN (PASSED)
  - Test 3: Customer can create booking with status PENDING and own customerUid (PASSED)
  - Test 4: Customer cannot read another customer's booking (PASSED)
  - Test 5: Admin can create and manage services; customer cannot modify (PASSED)
  - Test 6: Admin can read all bookings and update status to ACCEPTED or IN_SERVICE (PASSED)
- **Summary**: 6 tests passed, 0 failed.

## 2. JVM Repository Integration Testing (Tier 2: Robolectric)
- **Engine**: Android-native Robolectric 4.16.1 testing against local Firebase emulators.
- **Suite**: `BookingRepositoryRuleTest.kt`
- **Results**:
  - `createBooking_authenticatedCustomer_createsAndObserves`: PASSED (Verified end-to-end booking insertion and real-time Flow emission).
  - `observeCustomerBookings_unauthenticated_failsWithPermissionDenied`: PASSED (Verified rejection of unauthenticated queries with `PERMISSION_DENIED`).
- **Summary**: All 2 test cases passed with warm Gradle daemon and local emulators.

## 3. End-to-End User Flow Verification
- Customer Google Sign-In with Credential Manager (`GetSignInWithGoogleOption`).
- Viewing live active service packages from Firestore.
- Form validation preventing submission without bike registration or problem description.
- Creation of booking with server timestamp and status PENDING.
- Admin Google Sign-In verification against `muzamillp20@gmail.com`.
- Real-time notification and acceptance of booking by admin.
- Status update propagation to customer's My Bookings screen.
- Deep linking to WhatsApp with pre-filled booking ID.
