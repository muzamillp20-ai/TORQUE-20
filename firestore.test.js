const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const CUSTOMER_UID = "customer_alice_123";
const CUSTOMER_BOB_UID = "customer_bob_456";
const ADMIN_UID = "admin_father_789";
const ADMIN_EMAIL = "muzamillp20@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- Test 1: Unauthenticated user rejections ---
test("Unauthenticated user: cannot read services, bookings or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("services").get());
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("users").doc("test").get());
});

// --- Test 2: Customer user profile and role escalation protection ---
test("Customer: can create own profile with role CUSTOMER, cannot self-escalate to ADMIN", async () => {
  const customerDb = testEnv.authenticatedContext(CUSTOMER_UID, {
    email: "alice@test.com",
    email_verified: true,
  }).firestore();

  // Valid customer creation
  await assertSucceeds(
    customerDb.collection("users").doc(CUSTOMER_UID).set({
      uid: CUSTOMER_UID,
      name: "Alice Rider",
      email: "alice@test.com",
      role: "CUSTOMER",
      createdAt: new Date(),
    })
  );

  // Attempt role escalation to ADMIN
  await assertFails(
    customerDb.collection("users").doc("attacker_uid").set({
      uid: "attacker_uid",
      name: "Attacker",
      email: "attacker@test.com",
      role: "ADMIN",
      createdAt: new Date(),
    })
  );
});

// --- Test 3: Customer creating a booking ---
test("Customer: can create a booking with status PENDING and own customerUid", async () => {
  const customerDb = testEnv.authenticatedContext(CUSTOMER_UID, {
    email: "alice@test.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    customerDb.collection("bookings").doc("booking_001").set({
      bookingId: "booking_001",
      customerUid: CUSTOMER_UID,
      customerName: "Alice Rider",
      customerEmail: "alice@test.com",
      customerPhone: "+1234567890",
      serviceId: "srv_gen_service",
      serviceName: "Full Periodic Service",
      bikeBrand: "Yamaha",
      bikeModel: "MT-15",
      registrationNumber: "MH02AB1234",
      problemDescription: "Chain slack and brake pad replacement",
      preferredDate: "2026-11-10",
      preferredTime: "10:00 AM",
      status: "PENDING",
      createdAt: new Date(),
    })
  );

  // Cannot create booking with someone else's UID
  await assertFails(
    customerDb.collection("bookings").doc("booking_spoof").set({
      bookingId: "booking_spoof",
      customerUid: "someone_else_uid",
      customerName: "Alice Rider",
      customerEmail: "alice@test.com",
      customerPhone: "+1234567890",
      serviceId: "srv_gen_service",
      serviceName: "Full Periodic Service",
      bikeBrand: "Yamaha",
      bikeModel: "MT-15",
      registrationNumber: "MH02AB1234",
      problemDescription: "Issue",
      preferredDate: "2026-11-10",
      preferredTime: "10:00 AM",
      status: "PENDING",
      createdAt: new Date(),
    })
  );

  // Cannot create booking directly with status ACCEPTED
  await assertFails(
    customerDb.collection("bookings").doc("booking_invalid_status").set({
      bookingId: "booking_invalid_status",
      customerUid: CUSTOMER_UID,
      customerName: "Alice Rider",
      customerEmail: "alice@test.com",
      customerPhone: "+1234567890",
      serviceId: "srv_gen_service",
      serviceName: "Full Periodic Service",
      bikeBrand: "Yamaha",
      bikeModel: "MT-15",
      registrationNumber: "MH02AB1234",
      problemDescription: "Issue",
      preferredDate: "2026-11-10",
      preferredTime: "10:00 AM",
      status: "ACCEPTED",
      createdAt: new Date(),
    })
  );
});

// --- Test 4: Booking privacy and isolation ---
test("Customer: cannot read another customer's booking", async () => {
  // Seed Bob's booking
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bookings").doc("bob_booking").set({
      bookingId: "bob_booking",
      customerUid: CUSTOMER_BOB_UID,
      customerName: "Bob Speed",
      customerEmail: "bob@test.com",
      customerPhone: "+1987654321",
      serviceId: "srv_oil",
      serviceName: "Engine Oil Flush",
      bikeBrand: "Honda",
      bikeModel: "CBR",
      registrationNumber: "DL04CD5678",
      problemDescription: "Oil change",
      preferredDate: "2026-11-12",
      preferredTime: "11:00 AM",
      status: "PENDING",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(CUSTOMER_UID, {
    email: "alice@test.com",
    email_verified: true,
  }).firestore();

  await assertFails(aliceDb.collection("bookings").doc("bob_booking").get());
});

// --- Test 5: Admin service management ---
test("Admin: can create, update, and manage services; Customer cannot modify services", async () => {
  const customerDb = testEnv.authenticatedContext(CUSTOMER_UID, {
    email: "alice@test.com",
    email_verified: true,
  }).firestore();

  // Customer cannot create service
  await assertFails(
    customerDb.collection("services").doc("srv_brake").set({
      serviceId: "srv_brake",
      name: "Brake Overhaul",
      description: "Complete brake pad and rotor servicing",
      price: 49.99,
      category: "Brakes",
      durationMinutes: 45,
      isActive: true,
      createdAt: new Date(),
    })
  );

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Admin can create service
  await assertSucceeds(
    adminDb.collection("services").doc("srv_brake").set({
      serviceId: "srv_brake",
      name: "Brake Overhaul",
      description: "Complete brake pad and rotor servicing",
      price: 49.99,
      category: "Brakes",
      durationMinutes: 45,
      isActive: true,
      createdAt: new Date(),
    })
  );
});

// --- Test 6: Admin managing bookings and status changes ---
test("Admin: can read all bookings and update status to ACCEPTED or IN_SERVICE", async () => {
  // Seed Alice's booking
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bookings").doc("alice_booking").set({
      bookingId: "alice_booking",
      customerUid: CUSTOMER_UID,
      customerName: "Alice Rider",
      customerEmail: "alice@test.com",
      customerPhone: "+1234567890",
      serviceId: "srv_gen_service",
      serviceName: "Full Periodic Service",
      bikeBrand: "Yamaha",
      bikeModel: "MT-15",
      registrationNumber: "MH02AB1234",
      problemDescription: "Chain maintenance",
      preferredDate: "2026-11-10",
      preferredTime: "10:00 AM",
      status: "PENDING",
      createdAt: new Date(),
    });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Admin can read Alice's booking
  await assertSucceeds(adminDb.collection("bookings").doc("alice_booking").get());

  // Admin updates status to ACCEPTED
  await assertSucceeds(
    adminDb.collection("bookings").doc("alice_booking").update({
      status: "ACCEPTED",
      statusNote: "Bring the bike at 10 AM",
      updatedAt: new Date(),
    })
  );
});
