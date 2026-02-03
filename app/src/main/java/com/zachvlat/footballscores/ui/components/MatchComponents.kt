package com.zachvlat.footballscores.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zachvlat.footballscores.data.model.Event
import com.zachvlat.footballscores.data.model.Stage
import com.zachvlat.footballscores.data.model.Team
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

@Composable
private fun parseColor(colorString: String): Color {
    return try {
        when {
            colorString.startsWith("#") -> {
                Color(android.graphics.Color.parseColor(colorString))
            }
            colorString.matches(Regex("^[0-9A-Fa-f]{6}$")) -> {
                Color(android.graphics.Color.parseColor("#$colorString"))
            }
            else -> {
                Color(android.graphics.Color.parseColor(colorString))
            }
        }
    } catch (e: IllegalArgumentException) {
        // Fallback to a default color if parsing fails
        MaterialTheme.colorScheme.primary
    }
}

@Composable
fun MatchCard(event: Event, onMatchClick: (String) -> Unit = {}, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team 1
            TeamInfo(team = event.T1.first(), alignment = Alignment.End)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Score Section
            ScoreSection(event = event)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Team 2
            TeamInfo(team = event.T2.first(), alignment = Alignment.Start)
        }
    }
}

@Composable
private fun TeamInfo(team: Team, alignment: Alignment.Horizontal) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Team Logo/Placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    color = team.Fc?.let { parseColor(it) }
                        ?: MaterialTheme.colorScheme.primary
                ),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = team.getTeamImageUrl()
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "${team.Nm} logo",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Text(
                    text = team.Abr,
                    color = team.Sc?.let { parseColor(it) }
                        ?: MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Team Name - centered with fixed width
        Text(
            text = team.Nm,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
private fun ScoreSection(event: Event) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Halftime Score if available
        if (!event.Trh1.isNullOrEmpty() && !event.Trh2.isNullOrEmpty()) {
            Text(
                text = "HT: ${event.Trh1}-${event.Trh2}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Main Score
        val displayScore = getDisplayScore(event.Tr1, event.Tr2, event.Eps)
        Text(
            text = displayScore,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Match Status
        StatusBadge(status = event.Eps, minutes = event.Eps, startTime = event.Esd)
    }
}

private fun formatStartTime(timestamp: Long): String {
    return try {
        // Parse YYYYMMDDHHMMSS format
        val timeStr = timestamp.toString()
        if (timeStr.length == 14) {
            val year = timeStr.substring(0, 4).toInt()
            val month = timeStr.substring(4, 6).toInt() - 1 // Calendar months are 0-based
            val day = timeStr.substring(6, 8).toInt()
            val hour = timeStr.substring(8, 10).toInt()
            val minute = timeStr.substring(10, 12).toInt()
            
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, hour, minute)
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.format(calendar.time)
        } else {
            timestamp.toString()
        }
    } catch (e: Exception) {
        timestamp.toString()
    }
}

private fun getDisplayScore(tr1: String?, tr2: String?, status: String): String {
    // Check if scores are null or contain "null" string
    val team1Score = if (tr1 == null || tr1.equals("null", ignoreCase = true) || tr1.isEmpty()) "0" else tr1
    val team2Score = if (tr2 == null || tr2.equals("null", ignoreCase = true) || tr2.isEmpty()) "0" else tr2
    
    // For matches that haven't started (NS status), show "vs" instead of "0-0"
    return if (status == "NS" && team1Score == "0" && team2Score == "0") {
        "vs"
    } else {
        "${team1Score} - ${team2Score}"
    }
}

@Composable
private fun StatusBadge(status: String, minutes: String?, startTime: Long?) {
    val (statusText, color) = when (status) {
        "FT" -> "FT" to Color.Gray
        "AET" -> "AET" to Color.Gray
        "HT" -> "HT" to Color.Magenta
        "NS" -> {
            // Show kickoff time for matches that haven't started
            val timeText = startTime?.let { formatStartTime(it) } ?: "NS"
            timeText to Color.Blue
        }
        else -> {
            // Live match with minutes
            val liveMinutes = minutes?.let { 
                if (it.endsWith("'")) it else "${it}'" 
            } ?: status
            liveMinutes to Color.Green
        }
    }
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = statusText ?: "Unknown",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
    }
}

@Composable
fun ErrorMessage(message: String, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
fun CompetitionHeader(stage: Stage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Competition Badge
            val badgeUrl = stage.getCompetitionBadgeUrl()
            if (badgeUrl != null) {
                AsyncImage(
                    model = badgeUrl,
                    contentDescription = "${stage.CompN ?: stage.Snm} logo",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(32.dp + 12.dp))
            }
            
            // Competition Info
            Column(modifier = Modifier.weight(1f)) {
                 Text(
                     text = stage.CompN ?: stage.Snm,
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
                 
                 if (!stage.Cnm.isNullOrEmpty() && stage.Cnm != stage.CompN) {
                     Text(
                         text = stage.Cnm,
                         style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                     )
                 }
                 
                 Text(
                     text = "${stage.Events.size} matches",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                 )
            }
        }
    }
}

@Composable
fun DateHeader(timestamp: Long) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val dateText = dateFormat.format(Date(timestamp))
    
    Text(
        text = dateText,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp)
    )
}