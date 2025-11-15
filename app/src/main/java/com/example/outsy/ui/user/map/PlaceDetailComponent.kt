package com.example.outsy.ui.user.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.outsy.data.models.Place
import com.example.outsy.data.models.Review
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: Place,
    onBackClick: () -> Unit
) {
    var showReviewForm by remember { mutableStateOf(false) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = { Text(place.name) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Slika
            item {
                if (place.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = place.imageUrl,
                        contentDescription = place.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        Text(
                            "No image available",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Osnovno info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = place.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Category: ${place.category}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "Rating: ${String.format("%.1f", place.rating)} ⭐",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Opis
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = place.description,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            // Reviews sekcija
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Reviews (${reviews.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Dugme za dodavanje review-a
                    Button(
                        onClick = { showReviewForm = !showReviewForm },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(if (showReviewForm) "Hide review form" else "Add review")
                    }

                    // Forma za dodavanje review-a
                    if (showReviewForm) {
                        ReviewFormSkeleton(
                            onCancel = { showReviewForm = false },
                            onSubmit = { rating, comment ->
                                // TODO: Dodaj review u bazu
                                showReviewForm = false
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }

            // Lista review-a
            items(reviews) { review ->
                ReviewItem(review = review)
            }

            // Ako nema review-a
            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reviews yet. Be the first to review!")
                    }
                }
            }

            // Padding na kraju
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ==================== REVIEW FORM ====================
@Composable
fun ReviewFormSkeleton(
    onCancel: () -> Unit,
    onSubmit: (rating: Double, comment: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rating by remember { mutableStateOf(5f) }
    var reviewText by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Rate this place:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                "Rating: ${String.format("%.1f", rating)} ⭐",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Your review") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 4
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onSubmit(rating.toDouble(), reviewText) },
                    modifier = Modifier.weight(1f),
                    enabled = reviewText.isNotEmpty()
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

// ==================== REVIEW ITEM ====================
@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header - Username i rating
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text("${String.format("%.1f", review.rating)} ⭐", fontSize = 14.sp)
            }

            // Datum
            Text(
                text = formatTimestamp(review.createdAt),
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Review tekst
            Text(
                text = review.comment,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==================== HELPER FUNKCIJE ====================
fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}