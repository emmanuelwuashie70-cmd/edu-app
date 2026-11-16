package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.UserProfile
import com.example.model.UserRole
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdesuaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveUserProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = AdesuaRepository(firestore, auth)

        val profile = UserProfile(
            userId = uid,
            fullName = "Ama Mensah",
            email = ALICE_EMAIL,
            role = UserRole.STUDENT.apiValue,
            educationLevel = "SHS",
            institutionId = "wghs",
            institutionName = "Wesley Girls' High School",
            formOrLevel = "SHS 2",
            learningAreaOrProgramme = "General Science",
            enrolledUnitIds = listOf("shs_core_math", "shs_bio"),
            onboardingCompleted = true
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveUserProfile(profile) }
        assertTrue(saveResult.isSuccess)

        val loaded = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(loaded)
        assertEquals("Ama Mensah", loaded?.fullName)
    }

    @Test
    fun getUserProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = AdesuaRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveUserProfile(
                UserProfile(
                    userId = aliceUid,
                    fullName = "Ama Mensah",
                    role = UserRole.STUDENT.apiValue,
                    onboardingCompleted = true
                )
            )
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = AdesuaRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getUserProfile(aliceUid) }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun recordQuizAttemptAndBookTutor_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = AdesuaRepository(firestore, auth)

        val attemptRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordQuizAttempt(
                unitId = "shs_core_math",
                unitTitle = "Core Mathematics",
                assessmentTitle = "WASSCE Algebra Drill",
                score = 80,
                correctCount = 4,
                totalQuestions = 5
            )
        }
        assertTrue(attemptRes.isSuccess)

        val attempts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeQuizAttempts().first { it.isNotEmpty() }
        }
        assertEquals(80L, attempts.first().score)

        val bookingRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTutorBooking(
                tutorId = "tutor_kwame",
                tutorName = "Dr. Kwame Boateng",
                subjectTitle = "Elective Physics",
                scheduledDate = "2026-11-22",
                scheduledTime = "17:00 GMT",
                amountGhs = 90.0,
                paymentMethod = "MTN MoMo",
                meetingMode = "Online (Google Meet)"
            )
        }
        assertTrue(bookingRes.isSuccess)

        val bookings = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTutorBookings().first { it.isNotEmpty() }
        }
        assertEquals("CONFIRMED", bookings.first().paymentStatus)
        assertEquals(uid, bookings.first().userId)
    }

    @Test
    fun publishAndQueryResourcesAndForumPosts_authenticated_succeeds() = runBlocking {
        signInTestUser(BOB_EMAIL)
        val repository = AdesuaRepository(firestore, auth)

        val resResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishResource(
                authorName = "Mr. Kwabena Owusu",
                unitId = "shs_bio",
                unitCode = "BIO-SHS2",
                title = "Cellular Respiration Revision",
                summary = "Glycolysis, Krebs cycle, and ATP yield for WASSCE.",
                resourceType = "LECTURE_NOTE",
                isTeacherVerified = true
            )
        }
        assertTrue(resResult.isSuccess)

        val resources = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePublishedResources().first { it.isNotEmpty() }
        }
        assertTrue(resources.any { it.unitCode == "BIO-SHS2" })

        val postResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createForumPost(
                authorName = "Mr. Kwabena Owusu",
                authorRole = "TEACHER",
                unitId = "shs_bio",
                unitCode = "BIO-SHS2",
                content = "Drop your hardest Ecology questions here before Friday's live class!"
            )
        }
        assertTrue(postResult.isSuccess)

        val posts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePublishedForumPosts().first { it.isNotEmpty() }
        }
        assertTrue(posts.any { it.unitCode == "BIO-SHS2" })
    }

    @Test
    fun observePublishedResources_unauthenticated_failsWithIllegalState() = runBlocking {
        auth.signOut()
        val repository = AdesuaRepository(firestore, auth)
        var threw = false
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePublishedResources().first()
            }
        } catch (e: IllegalStateException) {
            threw = true
        }
        assertTrue(threw)
    }

    private companion object {
        const val ALICE_EMAIL = "ama_rule_test@adesua.gh"
        const val BOB_EMAIL = "kwabena_rule_test@adesua.gh"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
