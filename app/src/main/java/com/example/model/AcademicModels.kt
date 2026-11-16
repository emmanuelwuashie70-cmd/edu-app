package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole(val apiValue: String, val label: String) {
    STUDENT("STUDENT", "Student"),
    TEACHER("TEACHER", "Teacher"),
    PRIVATE_TUTOR("PRIVATE_TUTOR", "Private Tutor");

    companion object {
        fun fromValue(value: String?): UserRole =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: STUDENT
    }
}

enum class EducationLevel(val apiValue: String, val label: String) {
    SHS("SHS", "Senior High School (SHS)"),
    UNIVERSITY("UNIVERSITY", "University / Tertiary");

    companion object {
        fun fromValue(value: String?): EducationLevel? =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
    }
}

enum class ResourceType(val apiValue: String, val label: String) {
    TEXTBOOK("TEXTBOOK", "Textbook"),
    LECTURE_NOTE("LECTURE_NOTE", "Lecture Note"),
    VIDEO("VIDEO", "Video"),
    PAST_QUESTION("PAST_QUESTION", "Past Question"),
    LESSON_PLAN("LESSON_PLAN", "Lesson Plan");

    companion object {
        fun fromValue(value: String?): ResourceType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: LECTURE_NOTE
    }
}

enum class LiveClassStatus(val apiValue: String, val label: String) {
    UPCOMING("UPCOMING", "Upcoming"),
    LIVE("LIVE", "Live Now"),
    ENDED("ENDED", "Ended");

    companion object {
        fun fromValue(value: String?): LiveClassStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: UPCOMING
    }
}

enum class AiExplanationStyle(val label: String, val instruction: String) {
    SHS_1_SIMPLE(
        "Explain like I'm in SHS 1",
        "Use clear, relatable Ghanaian everyday analogies and plain language suited for a Form 1 Senior High student."
    ),
    EXAM_FOCUSED(
        "Exam-focused summary",
        "Provide concise WASSCE / University exam bullet points, key definitions, and common marking scheme pitfalls."
    ),
    STEP_BY_STEP(
        "Step-by-step worked example",
        "Walk through a complete worked example step-by-step with clear formulas and reasoning."
    )
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = UserRole.STUDENT.apiValue,
    val educationLevel: String = "",
    val institutionId: String = "",
    val institutionName: String = "",
    val formOrLevel: String = "",
    val learningAreaOrProgramme: String = "",
    val semester: String = "",
    val academicYear: String = "2026/2027",
    val enrolledUnitIds: List<String> = emptyList(),
    val teachingSpecialty: String = "",
    val staffVerificationId: String = "",
    val tutorBio: String = "",
    val tutorCredentials: String = "",
    val tutorHourlyRateGhs: Double = 0.0,
    val tutorMode: String = "Online & In-Person",
    val onboardingCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> {
        return mapOf(
            "userId" to uid,
            "fullName" to fullName.ifBlank { "Adesua Learner" },
            "email" to email,
            "role" to role,
            "educationLevel" to educationLevel,
            "institutionId" to institutionId,
            "institutionName" to institutionName,
            "formOrLevel" to formOrLevel,
            "learningAreaOrProgramme" to learningAreaOrProgramme,
            "semester" to semester,
            "academicYear" to academicYear,
            "enrolledUnitIds" to enrolledUnitIds.take(20),
            "teachingSpecialty" to teachingSpecialty,
            "staffVerificationId" to staffVerificationId,
            "tutorBio" to tutorBio,
            "tutorCredentials" to tutorCredentials,
            "tutorHourlyRateGhs" to tutorHourlyRateGhs,
            "tutorMode" to tutorMode,
            "onboardingCompleted" to onboardingCompleted,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toUpdateMap(): Map<String, Any> {
        return mapOf(
            "fullName" to fullName.ifBlank { "Adesua Learner" },
            "email" to email,
            "role" to role,
            "educationLevel" to educationLevel,
            "institutionId" to institutionId,
            "institutionName" to institutionName,
            "formOrLevel" to formOrLevel,
            "learningAreaOrProgramme" to learningAreaOrProgramme,
            "semester" to semester,
            "academicYear" to academicYear,
            "enrolledUnitIds" to enrolledUnitIds.take(20),
            "teachingSpecialty" to teachingSpecialty,
            "staffVerificationId" to staffVerificationId,
            "tutorBio" to tutorBio,
            "tutorCredentials" to tutorCredentials,
            "tutorHourlyRateGhs" to tutorHourlyRateGhs,
            "tutorMode" to tutorMode,
            "onboardingCompleted" to onboardingCompleted,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

data class Institution(
    val id: String,
    val name: String,
    val type: EducationLevel,
    val region: String
)

data class CurriculumTopic(
    val id: String,
    val unitId: String,
    val strandOrTopic: String,
    val subStrandOrSubtopic: String,
    val learningObjectives: List<String>
)

data class AcademicUnit(
    val id: String,
    val institutionId: String?,
    val code: String,
    val title: String,
    val level: String,
    val semester: String?,
    val learningAreaOrProgramme: String,
    val isCore: Boolean = false,
    val isVerifiedCurriculum: Boolean = true
)

@IgnoreExtraProperties
data class LearningResource(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val unitId: String = "",
    val unitCode: String = "",
    val title: String = "",
    val summary: String = "",
    val resourceType: String = ResourceType.LECTURE_NOTE.apiValue,
    val isTeacherVerified: Boolean = false,
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class McqQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class PracticeAssessment(
    val id: String,
    val unitId: String,
    val unitCode: String,
    val title: String,
    val durationMinutes: Int,
    val questions: List<McqQuestion>
)

@IgnoreExtraProperties
data class QuizAttempt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val unitId: String = "",
    val unitTitle: String = "",
    val assessmentTitle: String = "",
    val score: Long = 0L,
    val correctCount: Long = 0L,
    val totalQuestions: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LiveClass(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val hostName: String = "",
    val unitId: String = "",
    val unitCode: String = "",
    val title: String = "",
    val scheduledAtLabel: String = "",
    val meetingLink: String = "",
    val status: String = LiveClassStatus.UPCOMING.apiValue,
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class TutorProfile(
    val id: String,
    val fullName: String,
    val headline: String,
    val credentials: String,
    val institutionExpertise: String,
    val subjects: List<String>,
    val hourlyRateGhs: Double,
    val ratingAvg: Double,
    val totalReviews: Int,
    val isVerified: Boolean,
    val availability: String,
    val cityOrMode: String
)

@IgnoreExtraProperties
data class TutorBooking(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tutorId: String = "",
    val tutorName: String = "",
    val subjectTitle: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val amountGhs: Double = 0.0,
    val paymentMethod: String = "MTN MoMo",
    val paymentStatus: String = "CONFIRMED",
    val meetingMode: String = "Online (Google Meet)",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ForumPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = UserRole.STUDENT.apiValue,
    val unitId: String = "",
    val unitCode: String = "",
    val content: String = "",
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LessonPlan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val unitId: String = "",
    val unitTitle: String = "",
    val topic: String = "",
    val gradeOrLevel: String = "",
    val lessonNotes: String = "",
    val assessmentQuestions: String = "",
    val gradingRubric: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val styleLabel: String? = null
)
