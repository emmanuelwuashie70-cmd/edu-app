package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AdesuaRepository
import com.example.data.repository.AiEducationRepository
import com.example.data.repository.GeneratedCopilotPackage
import com.example.data.seed.GhanaCurriculumSeed
import com.example.model.AcademicUnit
import com.example.model.AiChatMessage
import com.example.model.AiExplanationStyle
import com.example.model.ForumPost
import com.example.model.LearningResource
import com.example.model.LessonPlan
import com.example.model.LiveClass
import com.example.model.QuizAttempt
import com.example.model.TutorBooking
import com.example.model.UserProfile
import com.example.model.UserRole
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(val profile: UserProfile?) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

data class AiTutorState(
    val messages: List<AiChatMessage> = emptyList(),
    val streamingReply: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TeacherCopilotState(
    val isLoading: Boolean = false,
    val generated: GeneratedCopilotPackage? = null,
    val error: String? = null,
    val savedConfirmation: String? = null
)

class AdesuaViewModel(
    private val repository: AdesuaRepository,
    private val aiRepository: AiEducationRepository,
    private val currentUserId: String,
    private val userDisplayName: String,
    private val userEmail: String
) : ViewModel() {

    val profileState: StateFlow<ProfileUiState> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, ProfileUiState> { ProfileUiState.Ready(it) }
        .catch { error ->
            val fsError = unwrapFirestoreError(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Profile stream error", root)
            emit(ProfileUiState.Error(root.message ?: "Unable to load academic profile."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ProfileUiState.Loading
        )

    val quizAttempts: StateFlow<List<QuizAttempt>> = repository.observeQuizAttempts()
        .catch { error ->
            if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
            Log.w(TAG, "Quiz attempts stream error", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val lessonPlans: StateFlow<List<LessonPlan>> = repository.observeLessonPlans()
        .catch { error ->
            if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
            Log.w(TAG, "Lesson plans stream error", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val tutorBookings: StateFlow<List<TutorBooking>> = repository.observeTutorBookings()
        .catch { error ->
            if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
            Log.w(TAG, "Tutor bookings stream error", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val publishedResources: StateFlow<List<LearningResource>> = repository.observePublishedResources()
        .map { remote ->
            val remoteIds = remote.map { it.id }.toSet()
            remote + GhanaCurriculumSeed.seedResources.filterNot { it.id in remoteIds }
        }
        .catch { error ->
            if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
            Log.w(TAG, "Resources stream error, using seed catalog", error)
            emit(GhanaCurriculumSeed.seedResources)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            GhanaCurriculumSeed.seedResources
        )

    val publishedLiveClasses: StateFlow<List<LiveClass>> = repository.observePublishedLiveClasses()
        .map { remote ->
            val remoteIds = remote.map { it.id }.toSet()
            remote + GhanaCurriculumSeed.seedLiveClasses.filterNot { it.id in remoteIds }
        }
        .catch { error ->
            if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
            Log.w(TAG, "Live classes stream error, using seed catalog", error)
            emit(GhanaCurriculumSeed.seedLiveClasses)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            GhanaCurriculumSeed.seedLiveClasses
        )

    val publishedForumPosts: StateFlow<List<ForumPost>> = repository.observePublishedForumPosts()
        .map { remote ->
            val remoteIds = remote.map { it.id }.toSet()
            remote + GhanaCurriculumSeed.seedForumPosts.filterNot { it.id in remoteIds }
        }
        .catch { error ->
            if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
            Log.w(TAG, "Forum stream error, using seed catalog", error)
            emit(GhanaCurriculumSeed.seedForumPosts)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            GhanaCurriculumSeed.seedForumPosts
        )

    private val _demoRoleOverride = MutableStateFlow<UserRole?>(null)
    val demoRoleOverride: StateFlow<UserRole?> = _demoRoleOverride.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    private val _isSavingOnboarding = MutableStateFlow(false)
    val isSavingOnboarding: StateFlow<Boolean> = _isSavingOnboarding.asStateFlow()

    private val _aiTutorState = MutableStateFlow(AiTutorState())
    val aiTutorState: StateFlow<AiTutorState> = _aiTutorState.asStateFlow()

    private val _copilotState = MutableStateFlow(TeacherCopilotState())
    val copilotState: StateFlow<TeacherCopilotState> = _copilotState.asStateFlow()

    fun switchDemoRole(role: UserRole) {
        _demoRoleOverride.value = role
    }

    fun clearBannerMessages() {
        _actionError.value = null
        _actionSuccess.value = null
    }

    fun saveOnboardingProfile(draft: UserProfile) {
        viewModelScope.launch {
            _isSavingOnboarding.value = true
            _actionError.value = null
            val profileToSave = draft.copy(
                userId = currentUserId,
                fullName = draft.fullName.ifBlank { userDisplayName.ifBlank { "Adesua Learner" } },
                email = userEmail,
                onboardingCompleted = true
            )
            val result = repository.saveUserProfile(profileToSave)
            _isSavingOnboarding.value = false
            result.onSuccess {
                _demoRoleOverride.value = UserRole.fromValue(profileToSave.role)
                _actionSuccess.value = "Academic profile saved to cloud."
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Failed to save onboarding profile."
            }
        }
    }

    fun askAiTutor(
        unit: AcademicUnit,
        topic: String,
        style: AiExplanationStyle,
        question: String,
        levelLabel: String
    ) {
        if (question.isBlank() || _aiTutorState.value.isLoading) return
        val userMessage = AiChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = question.trim(),
            styleLabel = style.label
        )
        _aiTutorState.value = _aiTutorState.value.copy(
            messages = _aiTutorState.value.messages + userMessage,
            streamingReply = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            var latestReply = ""
            try {
                aiRepository.askTutorStream(
                    subjectTitle = "${unit.code} - ${unit.title}",
                    levelLabel = levelLabel.ifBlank { unit.level },
                    topic = topic,
                    style = style,
                    question = question.trim()
                ).collect { accumulated ->
                    latestReply = accumulated
                    _aiTutorState.value = _aiTutorState.value.copy(
                        streamingReply = accumulated
                    )
                }
                val aiMsg = AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = latestReply,
                    styleLabel = style.label
                )
                _aiTutorState.value = _aiTutorState.value.copy(
                    messages = _aiTutorState.value.messages + aiMsg,
                    streamingReply = "",
                    isLoading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiTutorState.value = _aiTutorState.value.copy(
                    isLoading = false,
                    streamingReply = "",
                    error = e.localizedMessage ?: "AI Tutor request failed."
                )
            }
        }
    }

    fun generateCopilotLesson(
        unit: AcademicUnit,
        topic: String,
        gradeOrLevel: String,
        durationMinutes: Int
    ) {
        if (topic.isBlank()) {
            _copilotState.value = _copilotState.value.copy(error = "Enter a NaCCA strand or course topic first.")
            return
        }
        viewModelScope.launch {
            _copilotState.value = TeacherCopilotState(isLoading = true)
            try {
                val pkg = aiRepository.generateTeacherCopilotPackage(
                    unitTitle = "${unit.code}: ${unit.title}",
                    strandOrTopic = topic.trim(),
                    gradeOrLevel = gradeOrLevel,
                    durationMinutes = durationMinutes
                )
                _copilotState.value = TeacherCopilotState(
                    isLoading = false,
                    generated = pkg
                )
                repository.saveLessonPlan(
                    unitId = unit.id,
                    unitTitle = unit.title,
                    topic = topic.trim(),
                    gradeOrLevel = gradeOrLevel,
                    lessonNotes = pkg.lessonNotes,
                    assessmentQuestions = pkg.assessmentQuestions,
                    gradingRubric = pkg.gradingRubric
                ).onSuccess {
                    _copilotState.value = _copilotState.value.copy(
                        savedConfirmation = "Saved lesson plan & rubric to your Teacher Vault."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _copilotState.value = TeacherCopilotState(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to generate lesson plan."
                )
            }
        }
    }

    fun recordQuizAttempt(
        unitId: String,
        unitTitle: String,
        assessmentTitle: String,
        correctCount: Int,
        totalQuestions: Int
    ) {
        val score = if (totalQuestions > 0) ((correctCount * 100) / totalQuestions) else 0
        viewModelScope.launch {
            repository.recordQuizAttempt(
                unitId = unitId,
                unitTitle = unitTitle,
                assessmentTitle = assessmentTitle,
                score = score,
                correctCount = correctCount,
                totalQuestions = totalQuestions
            ).onSuccess {
                _actionSuccess.value = "Quiz score ($score%) saved to your academic record."
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Failed to save quiz score."
            }
        }
    }

    fun publishResource(
        authorName: String,
        unit: AcademicUnit,
        title: String,
        summary: String,
        resourceType: String
    ) {
        if (title.isBlank() || summary.isBlank()) {
            _actionError.value = "Please enter both a resource title and summary."
            return
        }
        viewModelScope.launch {
            repository.publishResource(
                authorName = authorName.ifBlank { userDisplayName.ifBlank { "Adesua Teacher" } },
                unitId = unit.id,
                unitCode = unit.code,
                title = title.trim(),
                summary = summary.trim(),
                resourceType = resourceType,
                isTeacherVerified = true
            ).onSuccess {
                _actionSuccess.value = "Resource published to students."
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Failed to publish resource."
            }
        }
    }

    fun scheduleLiveClass(
        hostName: String,
        unit: AcademicUnit,
        title: String,
        scheduledAtLabel: String,
        meetingLink: String
    ) {
        if (title.isBlank() || scheduledAtLabel.isBlank()) {
            _actionError.value = "Enter a class title and schedule time."
            return
        }
        viewModelScope.launch {
            repository.scheduleLiveClass(
                hostName = hostName.ifBlank { userDisplayName.ifBlank { "Adesua Educator" } },
                unitId = unit.id,
                unitCode = unit.code,
                title = title.trim(),
                scheduledAtLabel = scheduledAtLabel.trim(),
                meetingLink = meetingLink.ifBlank { "https://meet.adesua.gh/${unit.code.lowercase()}" }
            ).onSuccess {
                _actionSuccess.value = "Live class scheduled."
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Failed to schedule live class."
            }
        }
    }

    fun bookTutorSession(
        tutorName: String,
        tutorId: String,
        subjectTitle: String,
        scheduledDate: String,
        scheduledTime: String,
        amountGhs: Double,
        paymentMethod: String,
        meetingMode: String
    ) {
        viewModelScope.launch {
            repository.createTutorBooking(
                tutorId = tutorId,
                tutorName = tutorName,
                subjectTitle = subjectTitle,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                amountGhs = amountGhs,
                paymentMethod = paymentMethod,
                meetingMode = meetingMode
            ).onSuccess {
                _actionSuccess.value = "Session booked with $tutorName (GHS ${amountGhs.toInt()} via $paymentMethod)."
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Failed to confirm booking."
            }
        }
    }

    fun postToForum(
        authorName: String,
        authorRole: UserRole,
        unit: AcademicUnit,
        content: String
    ) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.createForumPost(
                authorName = authorName.ifBlank { userDisplayName.ifBlank { "Adesua Member" } },
                authorRole = authorRole.apiValue,
                unitId = unit.id,
                unitCode = unit.code,
                content = content.trim()
            ).onSuccess {
                _actionSuccess.value = "Posted to ${unit.code} study thread."
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Failed to publish post."
            }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    private companion object {
        const val TAG = "AdesuaVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
