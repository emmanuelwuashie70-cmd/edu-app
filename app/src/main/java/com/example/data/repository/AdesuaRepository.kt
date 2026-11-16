package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.firebase.OperationType
import com.example.core.firebase.handleFirestoreError
import com.example.model.ForumPost
import com.example.model.LearningResource
import com.example.model.LessonPlan
import com.example.model.LiveClass
import com.example.model.QuizAttempt
import com.example.model.TutorBooking
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdesuaRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Adesua data.")
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> =
        db.collection("users").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                logFlowError(error, OperationType.GET, "users/$uid", uid)
                throw error
            }

    suspend fun getUserProfile(uid: String): Result<UserProfile?> = runCatching {
        val doc = db.collection("users").document(uid).get().await()
        if (doc.exists()) {
            doc.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
            null
        }
    }.onFailure { e ->
        if (e is CancellationException) throw e
        handleFirestoreError(e, OperationType.GET, "users/$uid")
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                docRef.update(profile.toUpdateMap()).await()
            } else {
                docRef.set(profile.toCreateMap(uid)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeQuizAttempts(): Flow<List<QuizAttempt>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/quiz_attempts"
        emitAll(
            db.collection("users").document(uid).collection("quiz_attempts")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuizAttempt::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun recordQuizAttempt(
        unitId: String,
        unitTitle: String,
        assessmentTitle: String,
        score: Int,
        correctCount: Int,
        totalQuestions: Int
    ): Result<String> {
        val uid = requireUserId()
        val attemptId = "attempt_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid)
            .collection("quiz_attempts").document(attemptId)
        val payload = mapOf(
            "userId" to uid,
            "unitId" to unitId,
            "unitTitle" to unitTitle,
            "assessmentTitle" to assessmentTitle,
            "score" to score,
            "correctCount" to correctCount,
            "totalQuestions" to totalQuestions,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(attemptId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeLessonPlans(): Flow<List<LessonPlan>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/lesson_plans"
        emitAll(
            db.collection("users").document(uid).collection("lesson_plans")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LessonPlan::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun saveLessonPlan(
        unitId: String,
        unitTitle: String,
        topic: String,
        gradeOrLevel: String,
        lessonNotes: String,
        assessmentQuestions: String,
        gradingRubric: String
    ): Result<String> {
        val uid = requireUserId()
        val planId = "plan_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid)
            .collection("lesson_plans").document(planId)
        val payload = mapOf(
            "userId" to uid,
            "unitId" to unitId,
            "unitTitle" to unitTitle,
            "topic" to topic.take(200),
            "gradeOrLevel" to gradeOrLevel.take(60),
            "lessonNotes" to lessonNotes.take(15000),
            "assessmentQuestions" to assessmentQuestions.take(15000),
            "gradingRubric" to gradingRubric.take(10000),
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(planId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeTutorBookings(): Flow<List<TutorBooking>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/tutor_bookings"
        emitAll(
            db.collection("users").document(uid).collection("tutor_bookings")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TutorBooking::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createTutorBooking(
        tutorId: String,
        tutorName: String,
        subjectTitle: String,
        scheduledDate: String,
        scheduledTime: String,
        amountGhs: Double,
        paymentMethod: String,
        meetingMode: String
    ): Result<String> {
        val uid = requireUserId()
        val bookingId = "book_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid)
            .collection("tutor_bookings").document(bookingId)
        val payload = mapOf(
            "userId" to uid,
            "tutorId" to tutorId,
            "tutorName" to tutorName,
            "subjectTitle" to subjectTitle,
            "scheduledDate" to scheduledDate,
            "scheduledTime" to scheduledTime,
            "amountGhs" to amountGhs,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to "CONFIRMED",
            "meetingMode" to meetingMode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(bookingId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observePublishedResources(): Flow<List<LearningResource>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("resources")
                .whereEqualTo("isPublished", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LearningResource::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, "resources", uid)
                    throw error
                }
        )
    }

    suspend fun publishResource(
        authorName: String,
        unitId: String,
        unitCode: String,
        title: String,
        summary: String,
        resourceType: String,
        isTeacherVerified: Boolean
    ): Result<String> {
        val uid = requireUserId()
        val resourceId = "res_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("resources").document(resourceId)
        val payload = mapOf(
            "authorId" to uid,
            "authorName" to authorName.ifBlank { "Adesua Educator" },
            "unitId" to unitId,
            "unitCode" to unitCode,
            "title" to title.take(150),
            "summary" to summary.take(1000),
            "resourceType" to resourceType,
            "isTeacherVerified" to isTeacherVerified,
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(resourceId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observePublishedLiveClasses(): Flow<List<LiveClass>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("live_classes")
                .whereEqualTo("isPublished", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LiveClass::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, "live_classes", uid)
                    throw error
                }
        )
    }

    suspend fun scheduleLiveClass(
        hostName: String,
        unitId: String,
        unitCode: String,
        title: String,
        scheduledAtLabel: String,
        meetingLink: String
    ): Result<String> {
        val uid = requireUserId()
        val classId = "cls_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("live_classes").document(classId)
        val payload = mapOf(
            "authorId" to uid,
            "hostName" to hostName.ifBlank { "Adesua Educator" },
            "unitId" to unitId,
            "unitCode" to unitCode,
            "title" to title.take(150),
            "scheduledAtLabel" to scheduledAtLabel.take(80),
            "meetingLink" to meetingLink.take(300),
            "status" to "UPCOMING",
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(classId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observePublishedForumPosts(): Flow<List<ForumPost>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("forum_posts")
                .whereEqualTo("isPublished", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ForumPost::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, "forum_posts", uid)
                    throw error
                }
        )
    }

    suspend fun createForumPost(
        authorName: String,
        authorRole: String,
        unitId: String,
        unitCode: String,
        content: String
    ): Result<String> {
        val uid = requireUserId()
        val postId = "post_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("forum_posts").document(postId)
        val payload = mapOf(
            "authorId" to uid,
            "authorName" to authorName.ifBlank { "Adesua Member" },
            "authorRole" to authorRole,
            "unitId" to unitId,
            "unitCode" to unitCode,
            "content" to content.take(2000),
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(postId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    private fun logFlowError(error: Throwable, op: OperationType, path: String, expectedUid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
