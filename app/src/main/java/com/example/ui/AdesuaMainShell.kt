package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UserRole
import com.example.ui.auth.signOutUser
import com.example.ui.community.CommunityScreen
import com.example.ui.onboarding.OnboardingWizardScreen
import com.example.ui.student.StudentDashboardScreen
import com.example.ui.teacher.TeacherPortalScreen
import com.example.ui.tutor.TutorMarketplaceScreen
import com.example.viewmodel.AdesuaViewModel
import com.example.viewmodel.ProfileUiState

enum class AdesuaDest(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Workspace", Icons.Filled.School),
    TUTORS("tutors", "Tutors", Icons.Filled.PersonSearch),
    COMMUNITY("community", "Community", Icons.Filled.Forum)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdesuaMainShell(
    viewModel: AdesuaViewModel,
    defaultDisplayName: String
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val demoRoleOverride by viewModel.demoRoleOverride.collectAsStateWithLifecycle()
    val resources by viewModel.publishedResources.collectAsStateWithLifecycle()
    val liveClasses by viewModel.publishedLiveClasses.collectAsStateWithLifecycle()
    val forumPosts by viewModel.publishedForumPosts.collectAsStateWithLifecycle()
    val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val lessonPlans by viewModel.lessonPlans.collectAsStateWithLifecycle()
    val tutorBookings by viewModel.tutorBookings.collectAsStateWithLifecycle()
    val aiTutorState by viewModel.aiTutorState.collectAsStateWithLifecycle()
    val copilotState by viewModel.copilotState.collectAsStateWithLifecycle()
    val isSavingOnboarding by viewModel.isSavingOnboarding.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val actionSuccess by viewModel.actionSuccess.collectAsStateWithLifecycle()

    var forceReOnboard by remember { mutableStateOf(false) }

    when (val state = profileState) {
        ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading your Adesua academic profile...", style = MaterialTheme.typography.bodyMedium)
                }
            }
            return
        }

        is ProfileUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Unable to load profile", style = MaterialTheme.typography.titleLarge)
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
            return
        }

        is ProfileUiState.Ready -> {
            val profile = state.profile
            if (profile == null || !profile.onboardingCompleted || forceReOnboard) {
                OnboardingWizardScreen(
                    initialFullName = profile?.fullName?.ifBlank { defaultDisplayName } ?: defaultDisplayName,
                    isSaving = isSavingOnboarding,
                    errorMessage = actionError,
                    onComplete = { draft ->
                        forceReOnboard = false
                        viewModel.saveOnboardingProfile(draft)
                    }
                )
                return
            }

            val activeRole = demoRoleOverride ?: UserRole.fromValue(profile.role)
            var currentDest by remember { mutableStateOf(AdesuaDest.DASHBOARD) }
            var showRoleMenu by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val credentialManager = remember(context) { CredentialManager.create(context) }

            LaunchedEffect(actionError, actionSuccess) {
                val message = actionError ?: actionSuccess
                if (!message.isNullOrBlank()) {
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearBannerMessages()
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 680.dp

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            title = {
                                Column {
                                    Text(
                                        text = "Adesua • ${activeRole.label}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Demo Role Switcher active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            actions = {
                                Box {
                                    FilterChip(
                                        selected = true,
                                        onClick = { showRoleMenu = true },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.SwapHoriz,
                                                contentDescription = "Switch demo role"
                                            )
                                        },
                                        label = { Text(activeRole.label) },
                                        modifier = Modifier.testTag("demo_role_switcher_button")
                                    )
                                    DropdownMenu(
                                        expanded = showRoleMenu,
                                        onDismissRequest = { showRoleMenu = false }
                                    ) {
                                        UserRole.entries.forEach { role ->
                                            DropdownMenuItem(
                                                text = { Text("${role.label} View") },
                                                onClick = {
                                                    showRoleMenu = false
                                                    viewModel.switchDemoRole(role)
                                                    currentDest = if (role == UserRole.PRIVATE_TUTOR) {
                                                        AdesuaDest.TUTORS
                                                    } else {
                                                        AdesuaDest.DASHBOARD
                                                    }
                                                },
                                                modifier = Modifier.testTag("switch_role_${role.apiValue.lowercase()}")
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { forceReOnboard = true },
                                    modifier = Modifier.testTag("edit_onboarding_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Tune,
                                        contentDescription = "Edit academic onboarding"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        signOutUser(
                                            credentialManager = credentialManager,
                                            scope = scope
                                        )
                                    },
                                    modifier = Modifier.testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Sign out"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            NavigationBar {
                                AdesuaDest.entries.forEach { dest ->
                                    val label = when {
                                        dest == AdesuaDest.DASHBOARD && activeRole == UserRole.TEACHER -> "Teacher Portal"
                                        dest == AdesuaDest.DASHBOARD && activeRole == UserRole.PRIVATE_TUTOR -> "Student View"
                                        dest == AdesuaDest.DASHBOARD -> "Student"
                                        else -> dest.label
                                    }
                                    val icon = if (dest == AdesuaDest.DASHBOARD && activeRole == UserRole.TEACHER) {
                                        Icons.Filled.MenuBook
                                    } else {
                                        dest.icon
                                    }
                                    NavigationBarItem(
                                        selected = currentDest == dest,
                                        onClick = { currentDest = dest },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag("nav_${dest.route}")
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isWideScreen) {
                            NavigationRail {
                                AdesuaDest.entries.forEach { dest ->
                                    val label = if (dest == AdesuaDest.DASHBOARD && activeRole == UserRole.TEACHER) {
                                        "Teacher"
                                    } else {
                                        dest.label
                                    }
                                    NavigationRailItem(
                                        selected = currentDest == dest,
                                        onClick = { currentDest = dest },
                                        icon = { Icon(dest.icon, contentDescription = label) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
                                when (currentDest) {
                                    AdesuaDest.DASHBOARD -> {
                                        if (activeRole == UserRole.TEACHER) {
                                            TeacherPortalScreen(
                                                profile = profile,
                                                copilotState = copilotState,
                                                savedLessonPlans = lessonPlans,
                                                onGenerateCopilot = viewModel::generateCopilotLesson,
                                                onPublishResource = { unit, title, summary, type ->
                                                    viewModel.publishResource(profile.fullName, unit, title, summary, type)
                                                },
                                                onScheduleLiveClass = { unit, title, timeLabel, link ->
                                                    viewModel.scheduleLiveClass(profile.fullName, unit, title, timeLabel, link)
                                                }
                                            )
                                        } else {
                                            StudentDashboardScreen(
                                                profile = profile,
                                                resources = resources,
                                                quizAttempts = quizAttempts,
                                                aiTutorState = aiTutorState,
                                                onAskAiTutor = viewModel::askAiTutor,
                                                onSubmitQuizAttempt = viewModel::recordQuizAttempt
                                            )
                                        }
                                    }

                                    AdesuaDest.TUTORS -> {
                                        TutorMarketplaceScreen(
                                            activeRole = activeRole,
                                            profile = profile,
                                            bookings = tutorBookings,
                                            onBookSession = viewModel::bookTutorSession
                                        )
                                    }

                                    AdesuaDest.COMMUNITY -> {
                                        CommunityScreen(
                                            liveClasses = liveClasses,
                                            forumPosts = forumPosts,
                                            activeRole = activeRole,
                                            onPostToForum = { unit, content ->
                                                viewModel.postToForum(profile.fullName, activeRole, unit, content)
                                            }
                                        )
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
