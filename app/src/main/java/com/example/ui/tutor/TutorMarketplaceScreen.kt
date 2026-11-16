package com.example.ui.tutor

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.seed.GhanaCurriculumSeed
import com.example.model.TutorBooking
import com.example.model.TutorProfile
import com.example.model.UserProfile
import com.example.model.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorMarketplaceScreen(
    activeRole: UserRole,
    profile: UserProfile?,
    bookings: List<TutorBooking>,
    onBookSession: (
        tutorName: String,
        tutorId: String,
        subjectTitle: String,
        scheduledDate: String,
        scheduledTime: String,
        amountGhs: Double,
        paymentMethod: String,
        meetingMode: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(activeRole) {
        mutableIntStateOf(if (activeRole == UserRole.PRIVATE_TUTOR) 1 else 0)
    }
    var searchQuery by remember { mutableStateOf("") }
    var maxRateFilter by remember { mutableStateOf<Double?>(null) }
    var tutorToBook by remember { mutableStateOf<TutorProfile?>(null) }

    val filteredTutors = remember(searchQuery, maxRateFilter) {
        GhanaCurriculumSeed.seedTutors.filter { tutor ->
            val matchesQuery = searchQuery.isBlank() ||
                tutor.fullName.contains(searchQuery, ignoreCase = true) ||
                tutor.institutionExpertise.contains(searchQuery, ignoreCase = true) ||
                tutor.subjects.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesRate = maxRateFilter == null || tutor.hourlyRateGhs <= maxRateFilter!!
            matchesQuery && matchesRate
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Find a Tutor") },
                    modifier = Modifier.testTag("tutor_tab_directory")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Schedule & Earnings") },
                    modifier = Modifier.testTag("tutor_tab_earnings")
                )
            }
        }

        if (selectedTab == 0) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by subject, tutor, or school (e.g., KNUST, Maths)") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tutor_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = maxRateFilter == null,
                        onClick = { maxRateFilter = null },
                        label = { Text("All Rates") }
                    )
                    FilterChip(
                        selected = maxRateFilter == 80.0,
                        onClick = { maxRateFilter = 80.0 },
                        label = { Text("Up to GHS 80/hr") }
                    )
                    FilterChip(
                        selected = maxRateFilter == 100.0,
                        onClick = { maxRateFilter = 100.0 },
                        label = { Text("Up to GHS 100/hr") }
                    )
                }
            }

            if (filteredTutors.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No matching verified tutors found", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Clear your rate or subject filter to see all Ghanaian educators.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(filteredTutors, key = { it.id }) { tutor ->
                    TutorCard(
                        tutor = tutor,
                        onBookClick = { tutorToBook = tutor }
                    )
                }
            }
        } else {
            // Tutor Earnings & Schedule View
            item {
                val myRate = profile?.tutorHourlyRateGhs?.takeIf { it > 0.0 } ?: 85.0
                val bookedTotal = bookings.sumOf { it.amountGhs }
                val totalEarningsGhs = 1420.0 + bookedTotal

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
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondaryContainer
                            )
                            Text(
                                text = "TUTOR EARNINGS & PAYOUTS (GHS)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                        Text(
                            text = "GHS ${"%.2f".format(totalEarningsGhs)}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Hourly Rate: GHS ${myRate.toInt()}/hr • ${profile?.tutorMode?.ifBlank { "Online & In-Person" } ?: "Online & In-Person"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Upcoming Booked Sessions (${bookings.size})",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (bookings.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("No newly booked sessions yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Switch to 'Find a Tutor' to test booking a session with MTN MoMo or Paystack checkout.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(bookings, key = { it.id }) { booking ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.subjectTitle, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${booking.tutorName} • ${booking.scheduledDate} at ${booking.scheduledTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${booking.paymentMethod} • ${booking.meetingMode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "GHS ${booking.amountGhs.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = booking.paymentStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Recent Student Reviews", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "\"My WASSCE Core Maths mock score jumped from C5 to A1 after four sessions!\" — Kwesi A., Prempeh College",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider()
                        Text(
                            text = "\"Clear breakdown of Data Structures graphs and trees before my Level 200 exams.\" — Esi M., UG Legon",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    tutorToBook?.let { tutor ->
        BookTutorDialog(
            tutor = tutor,
            onDismiss = { tutorToBook = null },
            onConfirm = { subject, date, time, method ->
                onBookSession(
                    tutor.fullName,
                    tutor.id,
                    subject,
                    date,
                    time,
                    tutor.hourlyRateGhs,
                    method,
                    tutor.cityOrMode
                )
                tutorToBook = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TutorCard(
    tutor: TutorProfile,
    onBookClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(tutor.fullName, style = MaterialTheme.typography.titleMedium)
                        if (tutor.isVerified) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified tutor",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = tutor.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GHS ${tutor.hourlyRateGhs.toInt()}/hr",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "${tutor.credentials} • Expertise: ${tutor.institutionExpertise}",
                style = MaterialTheme.typography.bodySmall
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tutor.subjects.forEach { subj ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = subj,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${tutor.ratingAvg} (${tutor.totalReviews}) • ${tutor.cityOrMode}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = onBookClick,
                    modifier = Modifier.testTag("book_tutor_${tutor.id}")
                ) {
                    Text("Book Session")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookTutorDialog(
    tutor: TutorProfile,
    onDismiss: () -> Unit,
    onConfirm: (subject: String, date: String, time: String, paymentMethod: String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(tutor.subjects.first()) }
    var selectedDate by remember { mutableStateOf("2026-11-20") }
    var selectedTime by remember { mutableStateOf("17:00 GMT") }
    var selectedPayment by remember { mutableStateOf("MTN MoMo") }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Book ${tutor.fullName}", style = MaterialTheme.typography.titleLarge)
                        Text("1-hour session • GHS ${tutor.hourlyRateGhs.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text("Select Subject", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tutor.subjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) }
                        )
                    }
                }

                Text("Choose Slot", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("2026-11-20", "2026-11-21", "2026-11-23").forEach { d ->
                        FilterChip(
                            selected = selectedDate == d,
                            onClick = { selectedDate = d },
                            label = { Text(d) }
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("16:00 GMT", "17:00 GMT", "19:30 GMT").forEach { t ->
                        FilterChip(
                            selected = selectedTime == t,
                            onClick = { selectedTime = t },
                            label = { Text(t) }
                        )
                    }
                }

                Text("Paystack / Mobile Money Checkout Preview", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("MTN MoMo", "Telecel Cash", "Paystack Card").forEach { method ->
                        FilterChip(
                            selected = selectedPayment == method,
                            onClick = { selectedPayment = method },
                            label = { Text(method) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(selectedSubject, selectedDate, selectedTime, selectedPayment) },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("confirm_booking_button")
                    ) {
                        Text("Confirm GHS ${tutor.hourlyRateGhs.toInt()}")
                    }
                }
            }
        }
    }
}
