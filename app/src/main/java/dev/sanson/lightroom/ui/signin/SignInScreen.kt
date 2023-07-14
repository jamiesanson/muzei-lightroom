package dev.sanson.lightroom.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sanson.lightroom.android.LocalNewIntent

@Composable
fun SignIn() {
    val viewModel: SignInViewModel = hiltViewModel()
    val context = LocalContext.current
    val nextIntent = LocalNewIntent.current

    LaunchedEffect(true) {
        nextIntent.next.collect { viewModel.onCompleteSignIn(it) }
    }

    SignInScreen(
        onSignIn = { viewModel.signIn(context) }
    )
}

@Composable
private fun SignInScreen(onSignIn: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Button(
            onClick = onSignIn,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            Text("Sign in with Adobe")
        }
    }
}
