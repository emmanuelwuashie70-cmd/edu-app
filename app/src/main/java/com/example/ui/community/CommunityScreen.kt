package com.example.ui.community

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.seed.GhanaCurriculumSeed
import com.example.model.AcademicUnit
import com.example.model.ForumPost
import com.example.model.LiveClass
import com.example.model.LiveClassStatus
import com.example.model.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
    liveClasses: List<LiveClass>,
    forumPosts: List<ForumPost>,
    activeRole: UserRole,
    onPostToForum: (AcademicUnit, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unitChoices = remember { GhanaCurriculumSeed.allAcademicUnits.take(6) }
    var selectedUnitFilter by remember { mutableStateOf<AcademicUnit?>(null) }
    var composeTargetUnit by remember { mutableStateOf(unitChoices.first()) }
    var postDraft by remember { mutableStateOf("") }

    val filteredPosts = remember(forumPosts, selectedUnitFilter) {
        if (selectedUnitFilter == null) forumPosts
        else forumPosts.filter { it.unitId == selectedUnitFilter!!.id }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Live Academic Classes", style = MaterialTheme.typography.titleLarge)
        }

        items(liveClasses, key = { it.id }) { live ->
            val status = LiveClassStatus.fromValue(live.status)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (status == LiveClassStatus.LIVE) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (status == LiveClassStatus.LIVE) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoCall,
                            contentDescription = null,
                            tint = if (status == LiveClassStatus.LIVE) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = if (status == LiveClassStatus.LIVE) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = status.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (status == LiveClassStatus.LIVE) MaterialTheme.colorScheme.onSecondary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = live.unitCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(live.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${live.hostName} • ${live.scheduledAtLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(live.meetingLink)))
                            }
                        }
                    ) {
                        Text(if (status == LiveClassStatus.LIVE) "Join" else "Open")
                    }
                }
            }
        }

        item {
            Text("Course Study Community", style = MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedUnitFilter == null,
                    onClick = { selectedUnitFilter = null },
                    label = { Text("All Courses") }
                )
                unitChoices.forEach { unit ->
                    FilterChip(
                        selected = selectedUnitFilter?.id == unit.id,
                        onClick = {
                            selectedUnitFilter = unit
                            composeTargetUnit = unit
                        },
                        label = { Text(unit.code) }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Post to ${composeTargetUnit.code} Study Group (${activeRole.label})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = postDraft,
                            onValueChange = { postDraft = it },
                            placeholder = { Text("Share a past question tip or ask classmates...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("forum_post_input")
                        )
                        IconButton(
                            onClick = {
                                val text = postDraft
                                postDraft = ""
                                onPostToForum(composeTargetUnit, text)
                            },
                            enabled = postDraft.isNotBlank(),
                            modifier = Modifier.testTag("forum_post_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Publish study post",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        items(filteredPosts, key = { it.id }) { post ->
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Forum,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = post.unitCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = UserRole.fromValue(post.authorRole).label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
