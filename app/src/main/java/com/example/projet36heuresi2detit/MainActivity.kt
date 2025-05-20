package com.example.projet36heuresi2detit

import MyBluetoothClient
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Bluetooth permission is required to connect to the robot", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkBluetoothPermission()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }

    // ✅ Move this inside the class
    private fun checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }
}



@Composable
fun MainScreen() {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    var showAddRobot by remember { mutableStateOf(false) }
    var showRobotDetails by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Track if we want to show a Snackbar for permission
    var showBluetoothPermissionSnackbar by remember { mutableStateOf(false) }

    val hasBluetoothPermission =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED

    // Show the snackbar if needed
    if (showBluetoothPermissionSnackbar) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Bluetooth permission required to view details")
            showBluetoothPermissionSnackbar = false // reset after showing
        }
    }

    if (showSettings) {
        SettingsScreen(
            onBack = { showSettings = false },
            snackbarHostState = snackbarHostState
        )
    } else if (showAddRobot) {
        AddRobot(
            onBack = { showAddRobot = false },
            snackbarHostState = snackbarHostState
        )
    } else if (showRobotDetails) {
        RobotDetails(
            onBack = { showRobotDetails = false },
            snackbarHostState = snackbarHostState
        )
    } else {
        RobotManagerScreen(
            onSettingsClick = { showSettings = true },
            onAddRobotClick = { showAddRobot = true },
            onRobotCardClick = {
                if (hasBluetoothPermission) {
                    val activity = context as? ComponentActivity
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    showRobotDetails = true
                } else {
                    showBluetoothPermissionSnackbar = true
                }
            },
            snackbarHostState = snackbarHostState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RobotManagerScreen(
    onSettingsClick: () -> Unit,
    onAddRobotClick: () -> Unit,
    onRobotCardClick: () -> Unit, // ✅ Just a regular lambda!
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nom, Ou Image") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRobotClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Robot")
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                RobotCard(onClick = onRobotCardClick)
            }
        },
        containerColor = Color.DarkGray
    )
}

@Composable
fun RobotCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Image", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Nom Robot", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Batterie", color = Color.White)
                Text("Force du signal", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    var hasBluetoothPermission by remember { mutableStateOf(false) }

    // Permission launcher for Compose
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasBluetoothPermission = isGranted
        Toast.makeText(
            context,
            if (isGranted) "Bluetooth permission granted" else "Bluetooth permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Check current permission state when entering screen
    LaunchedEffect(Unit) {
        hasBluetoothPermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Text("Settings Content")

                Button(onClick = {
                    // Check permission and ask if not granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        Toast.makeText(context, "Bluetooth permission already granted", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Check Bluetooth Permission")
                }

                Text(
                    text = if (hasBluetoothPermission) "Bluetooth permission: ✅ Granted" else "Bluetooth permission: ❌ Not granted",
                    color = if (hasBluetoothPermission) Color.Green else Color.Red
                )

                // You can add more settings here...
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRobot(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Robot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Text("Add Robot Content")
                // Add your Add Robot UI elements here, e.g., TextFields, Buttons
            }
        }
    )
}

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RobotDetails(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    var changeOrientation by remember { mutableStateOf(false) }
    var exit by remember { mutableStateOf(false) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val device = remember {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothAdapter?.bondedDevices?.firstOrNull {
                it.name == "YourGroveBTName" || it.address == "XX:XX:XX:XX:XX:XX"
            }
        } else {
            null
        }
    }
    val bluetoothClient = remember(device) { device?.let { MyBluetoothClient(it, context) } }


    LaunchedEffect(device) {
        isVisible = true
        changeOrientation = true

        bluetoothClient?.connectAndReceiveImage { byteArray ->
            val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            bitmap = bmp
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            isVisible = false
            bluetoothClient?.disconnect()
        }
    }

    LaunchedEffect(exit) {
        val activity = context as? ComponentActivity
        if (exit) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Robot Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        exit = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Camera Feed",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } ?: Text("Waiting for image...", style = MaterialTheme.typography.headlineMedium)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        MainScreen()
    }
}
