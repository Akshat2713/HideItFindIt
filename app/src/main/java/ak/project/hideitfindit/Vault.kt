package ak.project.hideitfindit

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.colorResource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Vault : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HideItFindItTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LogScreenPreview()
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen() {
    val context = LocalContext.current
    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val tertiaryColor = colorResource(id = R.color.purple_700)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(text = "Vault",
                    style = MaterialTheme.typography.headlineLarge,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,)
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = tertiaryColor))
        },
        containerColor = secondaryColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(listOf("Images", "Videos", "Documents" ,"Settings")) { title ->
                LogItem(title = title, backgroundColor = primaryColor) {
                    // Handle click events for each item here
                    when(title){
                        "Images"->  {val intent = Intent(context, Image::class.java)
                        context.startActivity(intent)}
                        "Videos"->  {val intent = Intent(context, Video::class.java)
                            context.startActivity(intent)}
                    }
                }
            }
        }
    }
}
@Composable
fun LogItem(title: String, backgroundColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick)
            .background(backgroundColor),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontSize = 24.sp)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LogScreenPreview() {
    HideItFindItTheme {
        LogScreen()
    }
}