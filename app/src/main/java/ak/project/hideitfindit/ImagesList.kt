package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.ImageDataClass
import ak.project.hideitfindit.ui.theme.HideItFindItTheme
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*


class ImagesList : ComponentActivity() {
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val folderId = intent.getIntExtra("folderId", -1)
        val imageText= intent.getStringExtra("imageText")
        viewModel = ViewModelProvider(this).get(AppViewModel::class.java)


        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.clipData?.let { clipData ->
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until clipData.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                    showConfirmationDialog(uris,folderId) // Ask user for confirmation
                } ?: result.data?.data?.let { uri ->
                    showConfirmationDialog(listOf(uri),folderId) // Single image case
                }
            }
        }



        setContent {
            HideItFindItTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageListUI(folderId = folderId, onAddImagesClick = { openGallery() },imageText= imageText)
                }
            }
        }

    }

//    private fun showConfirmationDialog(uris: List<Uri>, folderId: Int) {
//        AlertDialog.Builder(this)
//            .setTitle("Confirm Selection")
//            .setMessage("Do you want to save ${uris.size} selected images?")
//            .setPositiveButton("Yes") { _, _ ->
//                uris.forEach { uri ->
//                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    viewModel.addImage(folderId, uri.toString(), this) // Pass context
//                }
//            }
//            .setNegativeButton("No", null)
//            .show()
//    }


    private fun openGallery() {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        pickImagesLauncher.launch(intent)
    }
    private fun showConfirmationDialog(uris: List<Uri>, folderId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Selection")
            .setMessage("Do you want to save ${uris.size} selected images?")
            .setPositiveButton("Yes") { _, _ ->
                uris.forEach { uri ->
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    viewModel.addImage(folderId, uri.toString(), this) // Pass context
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImageListUI(viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), folderId: Int,onAddImagesClick: () -> Unit,imageText: String?) {
    val context = LocalContext.current
//    val images by viewModel.getImagesById()
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var scale by remember { mutableStateOf(1f) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<ImageDataClass?>(null) }
    var showFolderOptionsDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Move") } // Default to Move



    LaunchedEffect(folderId) {
        viewModel.getImagesByFolderId(folderId)
    }
    val images = viewModel.images.value
    val secondaryColor = colorResource(id = R.color.secondary)
    val tertiaryColor = colorResource(id = R.color.purple_700)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = imageText!!,
                        style = MaterialTheme.typography.headlineLarge,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = tertiaryColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddImagesClick() }, containerColor = tertiaryColor) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Images", tint = Color.White)
            }
        },
        containerColor = secondaryColor
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(images.size) { index ->
                val image = images[index]
                AsyncImage(
                    model = image.filePath,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { selectedImageIndex = index },
                            onLongClick = {
                                selectedImage = image
                                showDialog = true
                            }
                        )
                )
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    selectedImage = null
                },
                title = { Text("Image Options") },
                text = {
                    Column {
                        listOf("Move", "Unhide").forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = (selectedOption == option),
                                    onClick = { selectedOption = option }
                                )
                                Text(text = option, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (selectedOption == "Unhide" && selectedImage != null) {
                            viewModel.deleteImage(selectedImage!!, context) // Call unhide logic
                            Toast.makeText(context, "Image Unhidden", Toast.LENGTH_SHORT).show()
                        } else if (selectedOption == "Move") {
                            Toast.makeText(context, "Move functionality is not implemented yet.", Toast.LENGTH_SHORT).show()
                        }
                        showDialog = false
                        selectedImage = null
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedImage = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }






        if (selectedImageIndex != null) {
            BackHandler {
                scale = 1f
                selectedImageIndex = null
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (scale == 1f) { // Allow swipe only if not zoomed
                                when {
                                    dragAmount < -100 && selectedImageIndex!! < images.size - 1 -> {selectedImageIndex = selectedImageIndex!! + 1
                                        Log.d("SwipeGesture", "DragAmount: $dragAmount, CurrentIndex: $selectedImageIndex")
                                    }
                                    dragAmount > 100 && selectedImageIndex!! > 0 -> {selectedImageIndex = selectedImageIndex!! - 1
                                        Log.d("SwipeGesture", "DragAmount: $dragAmount, CurrentIndex: $selectedImageIndex")
                                    }
                                }
                            }
                        }
                    }
            ) {
                AsyncImage(
                    model = images[selectedImageIndex!!].filePath,
                    contentDescription = "Full-Screen Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )

                IconButton(
                    onClick = { selectedImageIndex = null }, // Close full-screen mode
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Full-Screen", tint = Color.White)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    HideItFindItTheme {
//        ImageListUI(folderId = 0)
    }
}