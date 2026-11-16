package com.example.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.seed.GhanaCurriculumSeed
import com.example.model.AcademicUnit
import com.example.model.AiExplanationStyle
import com.example.model.LearningResource
import com.example.model.PracticeAssessment
import com.example.model.QuizAttempt
import com.example.model.ResourceType
import com.example.model.UserProfile
import com.example.viewmodel.AiTutorState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentDashboardScreen(
    profile: UserProfile?,
    resources: List<LearningResource>,
    quizAttempts: List<QuizAttempt>,
    aiTutorState: AiTutorState,
    onAskAiTutor: (AcademicUnit, String, AiExplanationStyle, String, String) -> Unit,
    onSubmitQuizAttempt: (unitId: String, unitTitle: String, assessmentTitle: String, correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val enrolledUnits = remember(profile) {
        val ids = profile?.enrolledUnitIds.orEmpty()
        if (ids.isNotEmpty()) {
            GhanaCurriculumSeed.allAcademicUnits.filter { it.id in ids }
                .ifEmpty { GhanaCurriculumSeed.shsCoreUnits }
        } else {
            GhanaCurriculumSeed.shsCoreUnits
        }
    }

    var selectedUnit by remember(enrolledUnits) { mutableStateOf(enrolledUnits.first()) }
    var resourceFilter by remember { mutableStateOf<String?>(null) }
    var showAiTutorSheet by remember { mutableStateOf(false) }
    var activeQuiz by remember { mutableStateOf<PracticeAssessment?>(null) }

    val hasUnverifiedUnits = enrolledUnits.any { !it.isVerifiedCurriculum }
    val filteredResources = remember(resources, resourceFilter) {
        if (resourceFilter == null) resources
        else resources.filter { it.resourceType == resourceFilter }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Academic Profile Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Text(
                            text = profile?.institutionName?.ifBlank { "Ghanaian Academic Track" }
                                ?: "Ghanaian Academic Track",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = "Akwaaba, ${profile?.fullName?.ifBlank { "Scholar" } ?: "Scholar"}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = listOfNotNull(
                            profile?.formOrLevel?.takeIf { it.isNotBlank() },
                            profile?.learningAreaOrProgramme?.takeIf { it.isNotBlank() },
                            profile?.semester?.takeIf { it.isNotBlank() }
                        ).joinToString(" • ").ifBlank { "NaCCA SHS Curriculum • 2026/2027" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showAiTutorSheet = true },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_ai_tutor_button")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask AI Personalised Tutor (${selectedUnit.code})")
                    }
                }
            }
        }

        // 2. Illustrative Curriculum Banner if unverified university courses are present
        if (hasUnverifiedUnits) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Curriculum notice",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(R.string.illustrative_curriculum_banner),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // 3. My Curriculum Horizontal Selector & Topics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "My Curriculum (${enrolledUnits.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(enrolledUnits, key = { it.id }) { unit ->
                        val isSelected = unit.id == selectedUnit.id
                        Card(
                            onClick = { selectedUnit = unit },
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .width(220.dp)
                                .testTag("curriculum_unit_${unit.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (unit.isVerifiedCurriculum) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = unit.code,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (unit.isVerifiedCurriculum) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (!unit.isVerifiedCurriculum) {
                                        Text(
                                            text = "Illustrative",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                Text(
                                    text = unit.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2
                                )
                                Text(
                                    text = unit.learningAreaOrProgramme,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Curriculum topics for selected unit
                val topics = GhanaCurriculumSeed.topicsByUnitId[selectedUnit.id].orEmpty()
                if (topics.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "NaCCA / Syllabus Topics • ${selectedUnit.title}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            topics.forEach { topic ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${topic.strandOrTopic}: ${topic.subStrandOrSubtopic}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    topic.learningObjectives.forEach { obj ->
                                        Text(
                                            text = "• $obj",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Interactive MCQ Practice Quizzes
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "MCQ Practice Quizzes",
                    style = MaterialTheme.typography.titleLarge
                )
                GhanaCurriculumSeed.seedAssessments.forEach { quiz ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Quiz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = quiz.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${quiz.unitCode} • ${quiz.questions.size} questions • ${quiz.durationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { activeQuiz = quiz },
                                modifier = Modifier.testTag("start_quiz_${quiz.id}")
                            ) {
                                Text("Practice")
                            }
                        }
                    }
                }

                if (quizAttempts.isNotEmpty()) {
                    Text(
                        text = "Recent Quiz Scores",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    quizAttempts.take(3).forEach { attempt ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(attempt.assessmentTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${attempt.correctCount}/${attempt.totalQuestions} correct", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = "${attempt.score}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Filterable Resource Library
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Resource Library",
                    style = MaterialTheme.typography.titleLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = resourceFilter == null,
                        onClick = { resourceFilter = null },
                        label = { Text("All") }
                    )
                    listOf(
                        ResourceType.LECTURE_NOTE,
                        ResourceType.PAST_QUESTION,
                        ResourceType.TEXTBOOK
                    ).forEach { type ->
                        FilterChip(
                            selected = resourceFilter == type.apiValue,
                            onClick = { resourceFilter = type.apiValue },
                            label = { Text(type.label) }
                        )
                    }
                }
            }
        }

        items(filteredResources, key = { it.id }) { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${res.unitCode} • ${ResourceType.fromValue(res.resourceType).label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (res.isTeacherVerified) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Teacher verified",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Text(text = res.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = res.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "By ${res.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showAiTutorSheet) {
        AiTutorBottomSheet(
            unit = selectedUnit,
            levelLabel = profile?.formOrLevel?.ifBlank { selectedUnit.level } ?: selectedUnit.level,
            aiTutorState = aiTutorState,
            onAsk = onAskAiTutor,
            onDismiss = { showAiTutorSheet = false }
        )
    }

    activeQuiz?.let { quiz ->
        PracticeQuizDialog(
            quiz = quiz,
            onDismiss = { activeQuiz = null },
            onComplete = { correctCount ->
                onSubmitQuizAttempt(
                    quiz.unitId,
                    quiz.title,
                    quiz.title,
                    correctCount,
                    quiz.questions.size
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AiTutorBottomSheet(
    unit: AcademicUnit,
    levelLabel: String,
    aiTutorState: AiTutorState,
    onAsk: (AcademicUnit, String, AiExplanationStyle, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStyle by remember { mutableStateOf(AiExplanationStyle.SHS_1_SIMPLE) }
    var topicInput by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text("AI Personalised Tutor", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Scoped to ${unit.code} • ${unit.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiExplanationStyle.entries.forEach { style ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        label = { Text(style.label) }
                    )
                }
            }

            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                label = { Text("Optional Topic (e.g., Circle Theorems, Osmosis)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (aiTutorState.messages.isEmpty() && aiTutorState.streamingReply.isBlank() && !aiTutorState.isLoading) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ask any question about ${unit.title}. Try: \"Walk me through a WASSCE-style question on this topic.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
                aiTutorState.messages.forEach { msg ->
                    Surface(
                        color = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (msg.isUser) "You • ${msg.styleLabel.orEmpty()}" else "Adesua AI Tutor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (aiTutorState.streamingReply.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Adesua AI Tutor (streaming...)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(aiTutorState.streamingReply, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else if (aiTutorState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Thinking with Gemini...", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (!aiTutorState.error.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = aiTutorState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text("Ask a question...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_tutor_input")
                )
                IconButton(
                    onClick = {
                        val q = questionInput
                        questionInput = ""
                        onAsk(unit, topicInput, selectedStyle, q, levelLabel)
                    },
                    enabled = questionInput.isNotBlank() && !aiTutorState.isLoading,
                    modifier = Modifier.testTag("ai_tutor_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send question",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeQuizDialog(
    quiz: PracticeAssessment,
    onDismiss: () -> Unit,
    onComplete: (correctCount: Int) -> Unit
) {
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var submitted by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(quiz.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${quiz.unitCode} • Select the best answer for each question",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()

                quiz.questions.forEachIndexed { qIndex, question ->
                    val chosen = selectedAnswers[qIndex]
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${qIndex + 1}. ${question.prompt}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        question.options.forEachIndexed { optIndex, option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !submitted) {
                                        selectedAnswers[qIndex] = optIndex
                                    }
                            ) {
                                RadioButton(
                                    selected = chosen == optIndex,
                                    onClick = if (!submitted) {
                                        { selectedAnswers[qIndex] = optIndex }
                                    } else null
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        submitted && optIndex == question.correctIndex -> MaterialTheme.colorScheme.primary
                                        submitted && chosen == optIndex && optIndex != question.correctIndex -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        if (submitted) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Explanation: ${question.explanation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                if (submitted) {
                    val correctCount = quiz.questions.indices.count { i ->
                        selectedAnswers[i] == quiz.questions[i].correctIndex
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Score: $correctCount / ${quiz.questions.size} (${(correctCount * 100) / quiz.questions.size}%)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (submitted) "Close" else "Cancel")
                    }
                    if (!submitted) {
                        Button(
                            onClick = {
                                submitted = true
                                val correctCount = quiz.questions.indices.count { i ->
                                    selectedAnswers[i] == quiz.questions[i].correctIndex
                                }
                                onComplete(correctCount)
                            },
                            enabled = selectedAnswers.size == quiz.questions.size,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_quiz_button")
                        ) {
                            Text("Grade Quiz")
                        }
                    }
                }
            }
        }
    }
}
