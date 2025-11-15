package com.example.outsy.ui.owner.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.outsy.viewmodel.OwnerMapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun AddPlaceDialog(
    userLocation: LatLng?,
    selectedLocation: LatLng?,
    onDismiss: () -> Unit,
    onAddPlace: (name: String, category: String, description: String, bitmap: Bitmap?) -> Unit
) {
    if (userLocation == null || selectedLocation == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error") },
            text = { Text("Location not available") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
        return
    }

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            profileBitmap = bitmap
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new place!") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !isLoading
                ) {
                    Text(if (profileBitmap != null) "Image picked ✓" else "Choose an image")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Caffe, Pub, etc.)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    maxLines = 3,
                    enabled = !isLoading
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isEmpty() || category.isEmpty()) {
                        Toast.makeText(context, "Name and category are required", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    isLoading = true
                    onAddPlace(name, category, description, profileBitmap)
                },
                enabled = !isLoading
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OwnerMapComponent(
    viewModel: OwnerMapViewModel = viewModel()
) {
    val context = LocalContext.current

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showAddPlaceDialog by remember { mutableStateOf(false) }

    val vmUserLocation by viewModel.ownerLocation.collectAsStateWithLifecycle()
    val vmSelectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val placeAdded by viewModel.placeAdded.collectAsStateWithLifecycle()
    val ownerPlaces by viewModel.ownerPlaces.collectAsStateWithLifecycle()

    LaunchedEffect(vmUserLocation) {
        userLocation = vmUserLocation
    }

    LaunchedEffect(vmSelectedLocation) {
        selectedLocation = vmSelectedLocation
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(placeAdded) {
        if (placeAdded) {
            Toast.makeText(context, "Place added successfully!", Toast.LENGTH_SHORT).show()
            viewModel.clearPlaceAdded()
            showAddPlaceDialog = false
        }
    }

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
                        viewModel.setOwnerLocation(it)
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
                    viewModel.setOwnerLocation(it)
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

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            onMapLongClick = { latLng ->
                viewModel.setSelectedLocation(latLng)
                showAddPlaceDialog = true
            }
        ) {
            // TODO Mozda nije najbolje da se stavi marker gde je korisnik??
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Your Location"
                )
            }

            selectedLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "New Place",
                    snippet = "Long press to add"
                )
            }

            ownerPlaces.forEach { place ->
                Marker(
                    state = MarkerState(
                        position = LatLng(place.location.latitude, place.location.longitude)
                    ),
                    title = place.name,
                    snippet = place.category
                )
            }
        }

        if (showAddPlaceDialog) {
            AddPlaceDialog(
                userLocation = userLocation,
                selectedLocation = selectedLocation,
                onDismiss = {
                    showAddPlaceDialog = false
                    viewModel.setSelectedLocation(null)
                },
                onAddPlace = { name, category, description, bitmap ->
                    viewModel.addPlace(name, category, description, bitmap)
                }
            )
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