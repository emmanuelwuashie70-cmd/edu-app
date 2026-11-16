const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_123";
const BOB_UID = "bob_teacher_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

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

// 1. Unauthenticated bounds
test("Unauthenticated caller cannot read or write users, resources, live_classes, or forum_posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("resources").where("isPublished", "==", true).get());
  await assertFails(unauthDb.collection("live_classes").where("isPublished", "==", true).get());
  await assertFails(unauthDb.collection("forum_posts").where("isPublished", "==", true).get());
});

// 2. PII & Cross-user isolation on /users/{userId}
test("User can create and read their own profile, but cannot read or modify another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Ama Mensah",
      email: "ama@example.com",
      role: "STUDENT",
      educationLevel: "SHS",
      institutionId: "wghs",
      institutionName: "Wesley Girls' High School",
      formOrLevel: "SHS 2",
      learningAreaOrProgramme: "General Science",
      enrolledUnitIds: ["shs_core_math", "shs_bio"],
      onboardingCompleted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ fullName: "Hijacked" })
  );
});

// 3. Shadow update / invalid role / ghost field rejection
test("Rejects profile with ADMIN role or undeclared ghost field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Ama Mensah",
      role: "ADMIN",
      onboardingCompleted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Ama Mensah",
      role: "STUDENT",
      isVerifiedAdmin: true,
      onboardingCompleted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

// 4. Owner-scoped subcollections (quiz_attempts, lesson_plans, tutor_bookings)
test("Subcollections isolate data strictly to owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("quiz_attempts")
      .doc("attempt_1")
      .set({
        userId: ALICE_UID,
        unitId: "shs_core_math",
        unitTitle: "Core Mathematics",
        assessmentTitle: "Quadratic Equations Drill",
        score: 80,
        correctCount: 4,
        totalQuestions: 5,
        createdAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("quiz_attempts").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("quiz_attempts").get()
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tutor_bookings")
      .doc("booking_1")
      .set({
        userId: ALICE_UID,
        tutorId: "tutor_kwame",
        tutorName: "Dr. Kwame Boateng",
        subjectTitle: "Elective Physics",
        scheduledDate: "2026-11-20",
        scheduledTime: "16:00 GMT",
        amountGhs: 85.0,
        paymentMethod: "MTN MoMo",
        paymentStatus: "CONFIRMED",
        meetingMode: "Online (Zoom/Meet)",
        createdAt: ts,
      })
  );
});

// 5. Query alignment & author ownership on shared collections
test("Shared collections enforce isPublished filter on list and authorId ownership on writes", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  // Spoofed authorId fails
  await assertFails(
    aliceDb.collection("resources").doc("res_spoof").set({
      authorId: BOB_UID,
      authorName: "Bob",
      unitId: "shs_bio",
      unitCode: "BIO-SHS2",
      title: "Cell Biology Notes",
      summary: "Mitosis and meiosis summary.",
      resourceType: "LECTURE_NOTE",
      isTeacherVerified: true,
      isPublished: true,
      createdAt: ts,
    })
  );

  // Valid create by Bob
  await assertSucceeds(
    bobDb.collection("resources").doc("res_1").set({
      authorId: BOB_UID,
      authorName: "Mr. Bob Owusu",
      unitId: "shs_bio",
      unitCode: "BIO-SHS2",
      title: "Cell Biology Notes",
      summary: "Mitosis and meiosis summary.",
      resourceType: "LECTURE_NOTE",
      isTeacherVerified: true,
      isPublished: true,
      createdAt: ts,
    })
  );

  // Unfiltered list fails (rules are not filters)
  await assertFails(aliceDb.collection("resources").get());

  // Aligned filtered list succeeds
  await assertSucceeds(
    aliceDb.collection("resources").where("isPublished", "==", true).get()
  );

  // Alice cannot delete Bob's resource
  await assertFails(aliceDb.collection("resources").doc("res_1").delete());

  // Forum post create & query
  await assertSucceeds(
    aliceDb.collection("forum_posts").doc("post_1").set({
      authorId: ALICE_UID,
      authorName: "Ama Mensah",
      authorRole: "STUDENT",
      unitId: "shs_bio",
      unitCode: "BIO-SHS2",
      content: "Can anyone share past WASSCE questions on osmosis?",
      isPublished: true,
      createdAt: ts,
    })
  );
  await assertFails(aliceDb.collection("forum_posts").get());
  await assertSucceeds(
    bobDb.collection("forum_posts").where("isPublished", "==", true).get()
  );
});
