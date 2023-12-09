package de.siebes.fabian.virtucard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.siebes.fabian.virtucard.ui.theme.VirtuCardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VirtuCardTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false,
            skipHiddenState = true,
        )
    )

    BottomSheetScaffold(
        sheetContent = { MyBottomSheet() },
        containerColor = Color.Transparent,
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetPeekHeight = 50.dp,
    ) { innerPadding ->

        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            MyWebView()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MyWebView() {
    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl("http://fabsie.tk")
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBottomSheet(
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(15.dp, bottom = 50.dp)
    ) {
        // Sheet content
        // QR-Code, Nearby, ...
        Button(onClick = { }) {
            Text(text = "Hello World!")
        }
        Divider(Modifier.padding(vertical = 15.dp), thickness = 1.dp)
        Button(onClick = {}) {
            Text(text = "Hello World!")
        }
        Text(
            text = "Created by Fabian Siebes",
            Modifier
                .padding(25.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    val sheetState = rememberModalBottomSheetState()

    VirtuCardTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            // BottomSheetContent()
            MyBottomSheet()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppViewPreview() {
    VirtuCardTheme {
        MainContent()
    }
}