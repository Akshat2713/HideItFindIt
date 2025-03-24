package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.VideoFolder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ak.project.hideitfindit.ui.theme.HideItFindItTheme
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Video : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HideItFindItTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable

fun VideoUI(viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val folders= viewModel.videoFolders.value

    var showDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newFolderTitle by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFolder by remember { mutableStateOf<VideoFolder?>(null) }
    var selectedOption by remember { mutableStateOf("Rename") } // Default selection

    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val tertiaryColor = colorResource(id = R.color.purple_700)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Videos",
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
            FloatingActionButton(
                onClick = { showDialog = true }, // Show the dialog when clicked
                containerColor = tertiaryColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Video Folder",
                    tint = Color.White,
                    modifier = Modifier.focusable(false)
                )
            }
        },
        containerColor = secondaryColor
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(folders) { folder ->
                VideoCard(
                    videoText = folder.title,
                    modifier = Modifier
                        .fillMaxWidth(),
                        onLongPress = { selectedFolder = folder }
                        )

            }
        }
    }

    // Dialog for adding a new video folder
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Video Folder") },
            text = {
                Column {
                    TextField(
                        value = newFolderTitle,
                        onValueChange = { newFolderTitle = it },
                        placeholder = { Text("Video folder title") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderTitle.text.isNotBlank()) {
                        viewModel.addVideoFolder(newFolderTitle.text)// Add the new video folder
                        newFolderTitle = TextFieldValue("") // Reset the input field
                        showDialog = false // Close the dialog
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newFolderTitle = TextFieldValue("") // Reset the input field
                    showDialog = false // Close the dialog
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for Rename/Delete options
    if (selectedFolder != null) {
        AlertDialog(
            onDismissRequest = { selectedFolder = null },
            title = {
                Text(
                    text = "Video Folder Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text("Choose an action:")
                    listOf("Unhide", "Rename", "Delete").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when (selectedOption) {
                        "Rename" -> {
                            showRenameDialog = true
                            newFolderTitle = TextFieldValue(selectedFolder?.title ?:"")
                        }
                        "Delete" -> showDeleteConfirmDialog = true
                        "Unhide" -> Toast.makeText(context, "Unhiding", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Ok") }
            },
            dismissButton = {
                TextButton(onClick = { selectedFolder = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog for renaming video folder
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Video Folder") },
            text = {
                TextField(value = newFolderTitle, onValueChange = { newFolderTitle = it })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderTitle.text.isNotBlank() && selectedFolder != null) {
                        viewModel.updateVideoFolder(selectedFolder!!.copy(title = newFolderTitle.text))
                        selectedFolder = null // Clear selectedFolder after renaming
                        newFolderTitle = TextFieldValue("") // Clear newFolderTitle after renaming
                        showRenameDialog = false
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog for deleting video folder
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this video folder?") },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedFolder != null){
                        viewModel.deleteVideoFolder(selectedFolder!!)
                        showDeleteConfirmDialog = false
                        selectedFolder = null}
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoCard(videoText: String, modifier: Modifier, onLongPress: () ->Unit) {
    val primaryColor = colorResource(id = R.color.primary)
    Card(
        modifier = modifier
            .height(150.dp)
            .combinedClickable(
                onClick = {}, // Normal click can be used for another action
                onLongClick = onLongPress // Show rename/delete dialog
            ),
        colors = CardDefaults.cardColors(containerColor = primaryColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = videoText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VideoPreview() {
    HideItFindItTheme {
        VideoUI()
    }
}