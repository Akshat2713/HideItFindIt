package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.NoteDataClass
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
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class NoteScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val noteId = intent.getIntExtra("noteId",-1)

        setContent {
            HideItFindItTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteScreenUI(noteId= noteId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreenUI(viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),noteId: Int){
    val noteState = viewModel.getNoteById(noteId).collectAsState()
    val note= noteState.value
    val context = LocalContext.current
    var noteTitle by remember { mutableStateOf(TextFieldValue(note?.title?: "")) }
    var noteContent by remember { mutableStateOf(TextFieldValue(note?.content?: "")) }

    LaunchedEffect(note) {
        if (note == null) {
//            Toast.makeText(context, "Note not found", Toast.LENGTH_SHORT).show()
        }
        else {
            noteTitle = TextFieldValue(note.title)
            noteContent = TextFieldValue(note.content)
        }
    }

    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val tertiaryColor = colorResource(id = R.color.purple_700)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(
                text = noteTitle.text,
                style = MaterialTheme.typography.headlineLarge,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )},colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = tertiaryColor),
                actions = {
                    IconButton(onClick = {
                        // Save the note
                        if (note != null) {
                            viewModel.updateNote(note.copy(title = noteTitle.text, content = noteContent.text))
                            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
                            val intent= Intent(context,MainActivity::class.java)
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note", tint = Color.White)
                    }
                }
            )

    })
    {padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ){
            TextField(value = noteContent,
                onValueChange = {noteContent=it},
                placeholder = { Text("Write your note here...")},
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                textStyle = TextStyle(fontSize = 20.sp),
                maxLines = Int.MAX_VALUE

                )
        }

    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HideItFindItTheme {
        NoteScreenUI(noteId = 1)
    }
}