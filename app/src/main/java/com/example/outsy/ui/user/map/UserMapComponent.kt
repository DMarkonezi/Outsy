package com.example.outsy.ui.user.ranking.map

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.content.pm.PackageManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.outsy.ui.user.map.PlaceDetailScreen
import androidx.navigation.NavHostController
import com.example.outsy.viewmodel.UserMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.example.outsy.data.models.Place

@Composable
fun UserMapComponent (
    navController: NavController,
    viewModel: UserMapViewModel = viewModel()
) {
    val context = LocalContext.current

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    val displayedPlaces by viewModel.displayedPlaces.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val initialPosition = userLocation ?: LatLng(44.8176, 20.4569)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            if (granted) {
                fetchUserLocation(fusedLocationClient, context) { location ->
                    userLocation = location
                    location?.let {
                        scope.launch {
                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
                            cameraPositionState.animate(cameraUpdate, 1000)
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            fetchUserLocation(fusedLocationClient, context) { location ->
                userLocation = location
                location?.let {
                    scope.launch {
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
                        cameraPositionState.animate(cameraUpdate, 1000)
                    }
                }
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (selectedPlace != null) {
        PlaceDetailScreen(
            place = selectedPlace!!,
            onBackClick = { selectedPlace = null }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
        ) {
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Tvoja lokacija"
                )
            }

            // Svi dostupni lokali
            displayedPlaces.forEach { place ->
                Marker(
                    state = MarkerState(
                        position = LatLng(place.location.latitude, place.location.longitude)
                    ),
                    title = place.name,
                    snippet = place.category,
                    onClick = {
                        selectedPlace = place
                        true
                    }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationReceived: (LatLng?) -> Unit
) {
    try {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(LatLng(location.latitude, location.longitude))
            } else {
                onLocationReceived(null)
            }
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    } catch (e: SecurityException) {
        onLocationReceived(null)
    }
}