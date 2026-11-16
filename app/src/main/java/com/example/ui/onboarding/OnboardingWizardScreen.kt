package com.example.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.seed.GhanaCurriculumSeed
import com.example.model.EducationLevel
import com.example.model.UserProfile
import com.example.model.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWizardScreen(
    initialFullName: String,
    isSaving: Boolean,
    errorMessage: String?,
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var fullName by remember { mutableStateOf(initialFullName) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    // Student state
    var educationLevel by remember { mutableStateOf(EducationLevel.SHS) }
    var selectedShsId by remember { mutableStateOf(GhanaCurriculumSeed.shsSchools.first().id) }
    var selectedShsForm by remember { mutableStateOf(GhanaCurriculumSeed.shsForms[1]) }
    var selectedLearningArea by remember { mutableStateOf(GhanaCurriculumSeed.shsLearningAreas.first()) }
    val selectedUnitIds = remember {
        mutableStateListOf(
            "shs_core_math", "shs_eng", "shs_int_sci", "shs_soc_stud",
            "shs_e_math", "shs_physics", "shs_chem", "shs_bio"
        )
    }

    var selectedUniId by remember { mutableStateOf(GhanaCurriculumSeed.universities.first().id) }
    var selectedProgramme by remember { mutableStateOf(GhanaCurriculumSeed.universityProgrammes.first()) }
    var selectedUniLevel by remember { mutableStateOf(GhanaCurriculumSeed.universityLevels[1]) }
    var selectedSemester by remember { mutableStateOf(GhanaCurriculumSeed.universitySemesters.first()) }
    var academicYear by remember { mutableStateOf("2026/2027") }

    // Teacher state
    var teacherInstitutionId by remember { mutableStateOf(GhanaCurriculumSeed.shsSchools.first().id) }
    var teacherSpecialty by remember { mutableStateOf("Elective Mathematics & Physics") }
    var staffVerificationId by remember { mutableStateOf("NTC-GH-2026-4812") }

    // Private Tutor state
    var tutorBio by remember {
        mutableStateOf("Experienced Ghanaian academic coach helping WASSCE and undergraduate learners master core concepts.")
    }
    var tutorCredentials by remember { mutableStateOf("BSc / MPhil, NTC Licensed Educator") }
    var tutorHourlyRate by remember { mutableStateOf("85") }
    var tutorMode by remember { mutableStateOf("Online & In-Person") }

    var validationError by remember { mutableStateOf<String?>(null) }

    if (step == 2) {
        BackHandler { step = 1 }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (step == 2) {
                        IconButton(onClick = { step = 1 }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to role selection"
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Academic Profiling • Step $step of 2",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (step == 1) "Choose Your Adesua Role" else "Personalise Your ${selectedRole.label} Setup",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { if (step == 1) 0.5f else 1f },
                    modifier = Modifier.fillMaxWidth()
                )

                val activeError = validationError ?: errorMessage
                if (!activeError.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                AnimatedContent(targetState = step, label = "onboarding_step") { currentStep ->
                    if (currentStep == 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g., Ama Serwaa Mensah") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_name_input")
                            )

                            RoleOptionCard(
                                selected = selectedRole == UserRole.STUDENT,
                                icon = Icons.Filled.School,
                                title = "Student (SHS / University)",
                                subtitle = "NaCCA SHS subjects, illustrative university courses, AI tutor & WASSCE MCQ practice.",
                                testTag = "role_card_student",
                                onClick = { selectedRole = UserRole.STUDENT }
                            )
                            RoleOptionCard(
                                selected = selectedRole == UserRole.TEACHER,
                                icon = Icons.Filled.MenuBook,
                                title = "Teacher / Lecturer",
                                subtitle = "AI Teacher Copilot for NaCCA lesson notes, rubrics, resources & live classes.",
                                testTag = "role_card_teacher",
                                onClick = { selectedRole = UserRole.TEACHER }
                            )
                            RoleOptionCard(
                                selected = selectedRole == UserRole.PRIVATE_TUTOR,
                                icon = Icons.Filled.PersonSearch,
                                title = "Private Tutor",
                                subtitle = "Offer 1-on-1 tuition in GHS, manage bookings & track earnings.",
                                testTag = "role_card_tutor",
                                onClick = { selectedRole = UserRole.PRIVATE_TUTOR }
                            )

                            Button(
                                onClick = {
                                    if (fullName.isBlank()) {
                                        validationError = "Please enter your full name to continue."
                                    } else {
                                        validationError = null
                                        step = 2
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_next_button")
                            ) {
                                Text("Continue")
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            when (selectedRole) {
                                UserRole.STUDENT -> {
                                    Text(
                                        text = "Educational Level",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        EducationLevel.entries.forEach { level ->
                                            FilterChip(
                                                selected = educationLevel == level,
                                                onClick = {
                                                    educationLevel = level
                                                    selectedUnitIds.clear()
                                                    if (level == EducationLevel.SHS) {
                                                        selectedUnitIds.addAll(
                                                            GhanaCurriculumSeed.unitsForShsArea(selectedLearningArea)
                                                                .map { it.id }
                                                        )
                                                    } else {
                                                        selectedUnitIds.addAll(
                                                            GhanaCurriculumSeed.unitsForUniversityProgramme(selectedProgramme)
                                                                .map { it.id }
                                                        )
                                                    }
                                                },
                                                label = { Text(level.apiValue) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("edu_level_${level.apiValue.lowercase()}")
                                            )
                                        }
                                    }

                                    if (educationLevel == EducationLevel.SHS) {
                                        Text("Select Senior High School", style = MaterialTheme.typography.titleSmall)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GhanaCurriculumSeed.shsSchools.forEach { school ->
                                                FilterChip(
                                                    selected = selectedShsId == school.id,
                                                    onClick = { selectedShsId = school.id },
                                                    label = { Text(school.name) }
                                                )
                                            }
                                        }

                                        Text("Form", style = MaterialTheme.typography.titleSmall)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            GhanaCurriculumSeed.shsForms.forEach { form ->
                                                FilterChip(
                                                    selected = selectedShsForm == form,
                                                    onClick = { selectedShsForm = form },
                                                    label = { Text(form) }
                                                )
                                            }
                                        }

                                        Text("Learning Area (NaCCA)", style = MaterialTheme.typography.titleSmall)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GhanaCurriculumSeed.shsLearningAreas.forEach { area ->
                                                FilterChip(
                                                    selected = selectedLearningArea == area,
                                                    onClick = {
                                                        selectedLearningArea = area
                                                        selectedUnitIds.clear()
                                                        selectedUnitIds.addAll(
                                                            GhanaCurriculumSeed.unitsForShsArea(area).map { it.id }
                                                        )
                                                    },
                                                    label = { Text(area) }
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Core & Elective Subjects (NaCCA Guidelines)",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        val availableShsUnits = GhanaCurriculumSeed.unitsForShsArea(selectedLearningArea)
                                        availableShsUnits.forEach { unit ->
                                            val checked = unit.id in selectedUnitIds
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (checked) selectedUnitIds.remove(unit.id)
                                                        else selectedUnitIds.add(unit.id)
                                                    }
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Checkbox(
                                                    checked = checked,
                                                    onCheckedChange = { isChecked ->
                                                        if (isChecked) {
                                                            if (unit.id !in selectedUnitIds) selectedUnitIds.add(unit.id)
                                                        } else {
                                                            selectedUnitIds.remove(unit.id)
                                                        }
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = unit.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = if (unit.isCore) "${unit.code} • Core Subject" else "${unit.code} • $selectedLearningArea Elective",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text("Select University", style = MaterialTheme.typography.titleSmall)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GhanaCurriculumSeed.universities.forEach { uni ->
                                                FilterChip(
                                                    selected = selectedUniId == uni.id,
                                                    onClick = { selectedUniId = uni.id },
                                                    label = { Text(uni.name) }
                                                )
                                            }
                                        }

                                        Text("Programme", style = MaterialTheme.typography.titleSmall)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GhanaCurriculumSeed.universityProgrammes.forEach { prog ->
                                                FilterChip(
                                                    selected = selectedProgramme == prog,
                                                    onClick = {
                                                        selectedProgramme = prog
                                                        selectedUnitIds.clear()
                                                        selectedUnitIds.addAll(
                                                            GhanaCurriculumSeed.unitsForUniversityProgramme(prog).map { it.id }
                                                        )
                                                    },
                                                    label = { Text(prog) }
                                                )
                                            }
                                        }

                                        Text("Level & Semester", style = MaterialTheme.typography.titleSmall)
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            GhanaCurriculumSeed.universityLevels.forEach { lvl ->
                                                FilterChip(
                                                    selected = selectedUniLevel == lvl,
                                                    onClick = { selectedUniLevel = lvl },
                                                    label = { Text(lvl) }
                                                )
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            GhanaCurriculumSeed.universitySemesters.forEach { sem ->
                                                FilterChip(
                                                    selected = selectedSemester == sem,
                                                    onClick = { selectedSemester = sem },
                                                    label = { Text(sem) }
                                                )
                                            }
                                        }
                                        OutlinedTextField(
                                            value = academicYear,
                                            onValueChange = { academicYear = it },
                                            label = { Text("Academic Year") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Note: University departmental course lists are marked as Illustrative Curriculum pending institution verification.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }

                                UserRole.TEACHER -> {
                                    Text("Select Your Teaching Institution", style = MaterialTheme.typography.titleSmall)
                                    val allInstitutions = GhanaCurriculumSeed.shsSchools + GhanaCurriculumSeed.universities
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        allInstitutions.forEach { inst ->
                                            FilterChip(
                                                selected = teacherInstitutionId == inst.id,
                                                onClick = { teacherInstitutionId = inst.id },
                                                label = { Text(inst.name) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = teacherSpecialty,
                                        onValueChange = { teacherSpecialty = it },
                                        label = { Text("Department / Subject Specialty") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = staffVerificationId,
                                        onValueChange = { staffVerificationId = it },
                                        label = { Text("NTC License / Staff Verification ID") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Badge, contentDescription = null)
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Verification Badge: Your staff ID reference will be displayed alongside published lecture notes and live classes.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                UserRole.PRIVATE_TUTOR -> {
                                    OutlinedTextField(
                                        value = tutorBio,
                                        onValueChange = { tutorBio = it },
                                        label = { Text("Tutor Bio") },
                                        minLines = 3,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = tutorCredentials,
                                        onValueChange = { tutorCredentials = it },
                                        label = { Text("Academic Credentials") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = tutorHourlyRate,
                                        onValueChange = { tutorHourlyRate = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                        label = { Text("Hourly Rate (GHS)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text("Teaching Mode", style = MaterialTheme.typography.titleSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Online Only", "In-Person", "Online & In-Person").forEach { mode ->
                                            FilterChip(
                                                selected = tutorMode == mode,
                                                onClick = { tutorMode = mode },
                                                label = { Text(mode) }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                ) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = {
                                        val draft = buildProfileDraft(
                                            fullName = fullName,
                                            role = selectedRole,
                                            educationLevel = educationLevel,
                                            shsId = selectedShsId,
                                            shsForm = selectedShsForm,
                                            learningArea = selectedLearningArea,
                                            uniId = selectedUniId,
                                            programme = selectedProgramme,
                                            uniLevel = selectedUniLevel,
                                            semester = selectedSemester,
                                            academicYear = academicYear,
                                            selectedUnitIds = selectedUnitIds.toList(),
                                            teacherInstitutionId = teacherInstitutionId,
                                            teacherSpecialty = teacherSpecialty,
                                            staffVerificationId = staffVerificationId,
                                            tutorBio = tutorBio,
                                            tutorCredentials = tutorCredentials,
                                            tutorHourlyRate = tutorHourlyRate.toDoubleOrNull() ?: 80.0,
                                            tutorMode = tutorMode
                                        )
                                        if (selectedRole == UserRole.STUDENT && draft.enrolledUnitIds.isEmpty()) {
                                            validationError = "Please select at least one subject or course."
                                        } else {
                                            validationError = null
                                            onComplete(draft)
                                        }
                                    },
                                    enabled = !isSaving,
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(52.dp)
                                        .testTag("onboarding_finish_button")
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Complete Setup")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildProfileDraft(
    fullName: String,
    role: UserRole,
    educationLevel: EducationLevel,
    shsId: String,
    shsForm: String,
    learningArea: String,
    uniId: String,
    programme: String,
    uniLevel: String,
    semester: String,
    academicYear: String,
    selectedUnitIds: List<String>,
    teacherInstitutionId: String,
    teacherSpecialty: String,
    staffVerificationId: String,
    tutorBio: String,
    tutorCredentials: String,
    tutorHourlyRate: Double,
    tutorMode: String
): UserProfile {
    val allInstitutions = GhanaCurriculumSeed.shsSchools + GhanaCurriculumSeed.universities
    return when (role) {
        UserRole.STUDENT -> {
            if (educationLevel == EducationLevel.SHS) {
                val school = GhanaCurriculumSeed.shsSchools.firstOrNull { it.id == shsId }
                UserProfile(
                    fullName = fullName.trim(),
                    role = role.apiValue,
                    educationLevel = EducationLevel.SHS.apiValue,
                    institutionId = shsId,
                    institutionName = school?.name ?: "Ghanaian SHS",
                    formOrLevel = shsForm,
                    learningAreaOrProgramme = learningArea,
                    academicYear = "2026/2027",
                    enrolledUnitIds = selectedUnitIds.ifEmpty { listOf("shs_core_math", "shs_eng") }
                )
            } else {
                val uni = GhanaCurriculumSeed.universities.firstOrNull { it.id == uniId }
                UserProfile(
                    fullName = fullName.trim(),
                    role = role.apiValue,
                    educationLevel = EducationLevel.UNIVERSITY.apiValue,
                    institutionId = uniId,
                    institutionName = uni?.name ?: "Ghanaian University",
                    formOrLevel = uniLevel,
                    learningAreaOrProgramme = programme,
                    semester = semester,
                    academicYear = academicYear.ifBlank { "2026/2027" },
                    enrolledUnitIds = selectedUnitIds.ifEmpty {
                        GhanaCurriculumSeed.unitsForUniversityProgramme(programme).map { it.id }
                    }
                )
            }
        }
        UserRole.TEACHER -> {
            val inst = allInstitutions.firstOrNull { it.id == teacherInstitutionId }
            UserProfile(
                fullName = fullName.trim(),
                role = role.apiValue,
                institutionId = teacherInstitutionId,
                institutionName = inst?.name ?: "Partner Institution",
                teachingSpecialty = teacherSpecialty.trim(),
                staffVerificationId = staffVerificationId.trim(),
                enrolledUnitIds = listOf("shs_core_math", "shs_bio", "uni_csc204")
            )
        }
        UserRole.PRIVATE_TUTOR -> {
            UserProfile(
                fullName = fullName.trim(),
                role = role.apiValue,
                tutorBio = tutorBio.trim(),
                tutorCredentials = tutorCredentials.trim(),
                tutorHourlyRateGhs = tutorHourlyRate,
                tutorMode = tutorMode,
                enrolledUnitIds = listOf("shs_core_math", "shs_physics", "shs_bio")
            )
        }
    }
}

@Composable
private fun RoleOptionCard(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
