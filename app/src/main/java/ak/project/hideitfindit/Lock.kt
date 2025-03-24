package ak.project.hideitfindit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ak.project.hideitfindit.ui.theme.HideItFindItTheme
import android.content.Intent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext


class Lock : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HideItFindItTheme {
                LockScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(context: Context) {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val preferences = EncryptedSharedPreferences.create(
        context,
        "SecureAppPrefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val savedPassword = preferences.getString("password", null)
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(if (savedPassword.isNullOrEmpty()) "set" else "enter") }
    var tempPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    val secondaryColor = colorResource(id = R.color.secondary)
    val tertiaryColor = colorResource(id = R.color.purple_700)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryColor),
        color = secondaryColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.lock_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.weight(2f))

            Text(
                text = when (step) {
                    "set" -> "Set Password"
                    "confirm" -> "Confirm Password"
                    "enter" -> "Enter Password"
                    else -> ""
                },
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                visualTransformation = PasswordVisualTransformation(mask = '•'),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White,
                    textColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, color = Color.Red, fontSize = 16.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                CompactNumberPad(
                    onNumberClick = { number ->
                        if (password.length < 4) {
                            password += number
                        }
                    },
                    onDeleteClick = {
                        if (password.isNotEmpty()) {
                            password = password.dropLast(1)
                        }
                    },
                    onOkClick = {
                        when (step) {
                            "set" -> {
                                if (password.length == 4) {
                                    tempPassword = password
                                    password = ""
                                    step = "confirm"
                                    errorMessage = ""
                                } else {
                                    errorMessage = "Password must be 4 digits"
                                }
                            }
                            "confirm" -> {
                                if (password == tempPassword) {
                                    preferences.edit().putString("password", password).apply()
                                    Toast.makeText(context, "Password Set", Toast.LENGTH_SHORT).show()
                                    step = "enter"
                                    errorMessage = ""
                                } else {
                                    errorMessage = "Passwords do not match"
                                }
                            }
                            "enter" -> {
                                if (password == savedPassword) {
//                                    Toast.makeText(context, "Entered correct password", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, Vault::class.java)
                                    context.startActivity(intent)
                                } else {
                                    errorMessage = "Incorrect Password"
                                }
                            }
                        }
                        password = ""
                    }
                )
            }
        }
    }
}

@Composable
fun CompactNumberPad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit, onOkClick: () -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("OK", "0", "⌫")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "OK" -> onOkClick()
                                "⌫" -> onDeleteClick()
                                else -> onNumberClick(label)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(text = label, fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LockScreenPreview() {
    HideItFindItTheme {
        LockScreen(context = androidx.compose.ui.platform.LocalContext.current)
    }
}
