package com.example.ui.teacher

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.seed.GhanaCurriculumSeed
import com.example.model.AcademicUnit
import com.example.model.LessonPlan
import com.example.model.ResourceType
import com.example.model.UserProfile
import com.example.viewmodel.TeacherCopilotState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherPortalScreen(
    profile: UserProfile?,
    copilotState: TeacherCopilotState,
    savedLessonPlans: List<LessonPlan>,
    onGenerateCopilot: (AcademicUnit, String, String, Int) -> Unit,
    onPublishResource: (AcademicUnit, String, String, String) -> Unit,
    onScheduleLiveClass: (AcademicUnit, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val units = remember { GhanaCurriculumSeed.allAcademicUnits }
    var selectedUnit by remember { mutableStateOf(units.first()) }

    // Copilot inputs
    var topicInput by remember { mutableStateOf("Quadratic Equations & Discriminant Analysis") }
    var gradeLevel by remember { mutableStateOf("SHS 2") }
    var durationMinutes by remember { mutableIntStateOf(60) }

    // Resource Manager inputs
    var resourceTitle by remember { mutableStateOf("") }
    var resourceSummary by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf(ResourceType.LECTURE_NOTE) }

    // Live class inputs
    var classTitle by remember { mutableStateOf("") }
    var classTimeLabel by remember { mutableStateOf("Thu • 17:00 GMT") }
    var classLink by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TEACHER PORTAL • ${profile?.institutionName?.ifBlank { "Ghanaian Educator" } ?: "Ghanaian Educator"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        text = "Welcome, ${profile?.fullName?.ifBlank { "Educator" } ?: "Educator"}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = profile?.teachingSpecialty?.ifBlank { "NaCCA Curriculum & Exam Preparation" }
                            ?: "NaCCA Curriculum & Exam Preparation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("AI Teacher Copilot") },
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_copilot")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Class & Resources") },
                    icon = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_resources")
                )
            }
        }

        item {
            Text("Target Subject / Course", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                units.take(8).forEach { unit ->
                    FilterChip(
                        selected = selectedUnit.id == unit.id,
                        onClick = { selectedUnit = unit },
                        label = { Text("${unit.code}: ${unit.title}") }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Generate NaCCA Lesson Notes & Assessment Rubric",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            label = { Text("NaCCA Strand or University Topic") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("copilot_topic_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SHS 1", "SHS 2", "SHS 3", "Level 200").forEach { lvl ->
                                FilterChip(
                                    selected = gradeLevel == lvl,
                                    onClick = { gradeLevel = lvl },
                                    label = { Text(lvl) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(45, 60, 90).forEach { mins ->
                                FilterChip(
                                    selected = durationMinutes == mins,
                                    onClick = { durationMinutes = mins },
                                    label = { Text("$mins mins") }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                onGenerateCopilot(selectedUnit, topicInput, gradeLevel, durationMinutes)
                            },
                            enabled = !copilotState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_copilot_button")
                        ) {
                            if (copilotState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating with Gemini...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Lesson Plan & Rubric")
                            }
                        }

                        if (!copilotState.error.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = copilotState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (!copilotState.savedConfirmation.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = copilotState.savedConfirmation,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            copilotState.generated?.let { pkg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Generated Lesson Package", style = MaterialTheme.typography.titleLarge)
                            HorizontalDivider()
                            Text("Lesson Notes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text(pkg.lessonNotes, style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider()
                            Text("Assessment Questions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text(pkg.assessmentQuestions, style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider()
                            Text("Grading Rubric", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text(pkg.gradingRubric, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (savedLessonPlans.isNotEmpty()) {
                item {
                    Text("Saved Lesson Vault (${savedLessonPlans.size})", style = MaterialTheme.typography.titleLarge)
                }
                items(savedLessonPlans, key = { it.id }) { plan ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("${plan.unitTitle} • ${plan.gradeOrLevel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(plan.topic, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = plan.lessonNotes.take(180) + if (plan.lessonNotes.length > 180) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // Class & Resource Manager
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Publish Lecture Note / Past Question", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                ResourceType.LECTURE_NOTE,
                                ResourceType.PAST_QUESTION,
                                ResourceType.TEXTBOOK,
                                ResourceType.LESSON_PLAN
                            ).forEach { rt ->
                                FilterChip(
                                    selected = resourceType == rt,
                                    onClick = { resourceType = rt },
                                    label = { Text(rt.label) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = resourceTitle,
                            onValueChange = { resourceTitle = it },
                            label = { Text("Resource Title") },
                            placeholder = { Text("e.g., WASSCE 2025 Core Maths Paper 2 Guide") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_resource_title")
                        )
                        OutlinedTextField(
                            value = resourceSummary,
                            onValueChange = { resourceSummary = it },
                            label = { Text("Summary & Key Takeaways") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onPublishResource(selectedUnit, resourceTitle, resourceSummary, resourceType.apiValue)
                                resourceTitle = ""
                                resourceSummary = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("publish_resource_button")
                        ) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish to Student Library")
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Schedule Live Class", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = classTitle,
                            onValueChange = { classTitle = it },
                            label = { Text("Session Title") },
                            placeholder = { Text("e.g., Mensuration & Circle Theorems Q&A") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = classTimeLabel,
                            onValueChange = { classTimeLabel = it },
                            label = { Text("Date & Time (GMT)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = classLink,
                            onValueChange = { classLink = it },
                            label = { Text("Meeting Link (optional)") },
                            placeholder = { Text("https://meet.adesua.gh/...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onScheduleLiveClass(selectedUnit, classTitle, classTimeLabel, classLink)
                                classTitle = ""
                                classLink = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("schedule_live_class_button")
                        ) {
                            Icon(Icons.Filled.VideoCall, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule Live Class")
                        }
                    }
                }
            }
        }
    }
}
