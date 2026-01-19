package com.zachvlat.footballscores.ui.components

import androidx.compose.foundation.background
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
fun MatchCard(event: Event, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
        horizontalAlignment = alignment,
        modifier = Modifier.width(120.dp)
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
        
        // Team Name
        Text(
            text = team.Nm,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = if (alignment == Alignment.CenterEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2
        )
        
        // Team Abbreviation
        Text(
            text = team.Abr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = if (alignment == Alignment.CenterEnd) TextAlign.End else TextAlign.Start
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
        Text(
            text = "${event.Tr1} - ${event.Tr2}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Match Status
        StatusBadge(status = event.Eps, minutes = event.ErnInf)
    }
}

@Composable
private fun StatusBadge(status: String, minutes: String?) {
    val (statusText, color) = when (status) {
        "FT" -> "FT" to Color.Gray
        "AET" -> "AET" to Color.Gray
        "HT" -> "HT" to Color.Magenta
        "NS" -> "NS" to Color.Blue
        else -> {
            // Live match with minutes
            val liveMinutes = minutes?.let { "${it}'" } ?: status
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stage.CompN ?: stage.Snm,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            if (!stage.Cnm.isNullOrEmpty() && stage.Cnm != stage.CompN) {
                Text(
                    text = stage.Cnm,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = "${stage.Events.size} matches",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
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