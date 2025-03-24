package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.NoteDataClass
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ak.project.hideitfindit.ui.theme.HideItFindItTheme
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.foundation.combinedClickable
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideItFindItTheme {
                NotesAppUI()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesAppUI(viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val notes = viewModel.notes.value
    var showDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf(TextFieldValue("")) }
    var selectedNote by remember { mutableStateOf<NoteDataClass?>(null) } // Track selected note
    var selectedOption by remember { mutableStateOf("Rename") } // Default selection

    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val tertiaryColor = colorResource(id = R.color.purple_700)

    LaunchedEffect(Unit) {
        createHiddenFolder(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.headlineLarge,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
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
                    contentDescription = "Add Note",
                    tint = Color.White,
                    modifier = Modifier.focusable(false)
                        .combinedClickable(
                            onClick = {
                                showDialog = true
                            },
                            onLongClick = {
                                // Navigate to Lock activity
                                val intent = Intent(context, Lock::class.java)
                                context.startActivity(intent)
                            }
                        )
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
            items(notes) { note ->
                NoteCard(
                    noteText = note.title,
                    noteId= note.id,
                    context= context,
                    modifier = Modifier.fillMaxWidth(),
                    onLongPress = {
                        selectedNote = note
                    }
                )
            }
        }
    }

    // Dialog for adding a new note
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Note") },
            text = {
                Column {
                    TextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        placeholder = { Text("Note title") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNoteTitle.text.isNotBlank()) {
                        viewModel.addNote(newNoteTitle.text)
                        newNoteTitle = TextFieldValue("") // Clear newNoteTitle after adding
                        showDialog = false
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog for Rename/Delete options
    if (selectedNote != null) {
        AlertDialog(
            onDismissRequest = { selectedNote = null },
            title = {
                Text(
                    text = "Note Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text("Choose an action:")

                    // Rename Radio Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == "Rename",
                            onClick = { selectedOption = "Rename" }
                        )
                        Text(text = "Rename")
                    }

                    // Delete Radio Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == "Delete",
                            onClick = { selectedOption = "Delete" }
                        )
                        Text(text = "Delete")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedOption == "Rename") {
                        showRenameDialog = true
                        newNoteTitle = TextFieldValue(selectedNote?.title ?: "")
                    } else if (selectedOption == "Delete") {
                        showDeleteConfirmDialog = true
                    }

                }) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNote = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = MaterialTheme.shapes.medium
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Note") },
            text = {
                TextField(value = newNoteTitle, onValueChange = { newNoteTitle = it })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNoteTitle.text.isNotBlank() && selectedNote != null) {
                        viewModel.updateNote(selectedNote!!.copy(title = newNoteTitle.text))
                        selectedNote = null // Clear selectedNote after renaming
                        newNoteTitle = TextFieldValue("") // Clear newNoteTitle after renaming
                        showRenameDialog = false
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedNote != null) {
                        viewModel.deleteNote(selectedNote!!)
                        showDeleteConfirmDialog = false
                        selectedNote = null
                    }
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
fun NoteCard(noteText: String, noteId: Int,context: Context ,modifier: Modifier, onLongPress: () -> Unit) {
    val primaryColor = colorResource(id = R.color.primary)
    Card(
        modifier = modifier
            .height(150.dp)
            .combinedClickable(
                onClick = {val intent = Intent(context, NoteScreen::class.java).apply { putExtra("noteId",noteId) }
                    context.startActivity(intent)}, // Normal click can be used for another action
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
                text = noteText,
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
fun createHiddenFolder(context: Context) {
    val hiddenFolder = File(context.filesDir, "hidden_notes") // Internal storage folder
    if (!hiddenFolder.exists()) {
        val success = hiddenFolder.mkdirs() // Create the folder
        if (success) {
            // Create a .nomedia file to hide the folder from media scanners
            File(hiddenFolder, ".nomedia").createNewFile()
        }
    }
    val imagesFolder = File(hiddenFolder, "Images")
    if (!imagesFolder.exists()) {
        imagesFolder.mkdirs()
    }

    // Create Videos subfolder
    val videosFolder = File(hiddenFolder, "Videos")
    if (!videosFolder.exists()) {
        videosFolder.mkdirs()
    }
}



@Preview(showBackground = true)
@Composable
fun NotesAppUIPreview() {
    HideItFindItTheme {
        NotesAppUI()

    }
}
