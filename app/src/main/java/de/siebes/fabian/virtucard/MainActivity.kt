package de.siebes.fabian.virtucard

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
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
            initialValue = SheetValue.Expanded,
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


@Composable
fun MyBottomSheet(
) {
    var shareUrl by remember { mutableStateOf("") }

    val vSpaceDp = 15.dp

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        // Nearby, ...
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            OutlinedButton(onClick = { }, modifier = Modifier.padding(horizontal = 5.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.nearby_share),
                    contentDescription = "Nearby Icon",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = "Nearby")
            }
            OutlinedButton(onClick = { }, modifier = Modifier.padding(horizontal = 5.dp)) {
                // use drawable inside of icon
                Icon(
                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
                    contentDescription = "Copy Icon",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = "Copy")
            }
            OutlinedButton(onClick = { }, modifier = Modifier.padding(horizontal = 5.dp)) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "Share Icon",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = "Share")
            }
        }

        Divider(Modifier.padding(vertical = vSpaceDp), thickness = 1.dp)

        // QR-Code, ...
        // ~https://stackoverflow.com/a/64504871
        val  size = 512
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode("test", BarcodeFormat.QR_CODE, size, size, hints)
        val bmpQRCode = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) MaterialTheme.colorScheme.primary.toArgb() else MaterialTheme.colorScheme.background.toArgb())
                }
            }
        }
        Image(bmpQRCode.asImageBitmap(), contentDescription = "QR Code for url", modifier = Modifier.fillMaxWidth())
        Button(onClick = {}) {
            Text(text = "Hello World!")
        }

        Spacer(modifier = Modifier.height(vSpaceDp))
        TextField(
            value = shareUrl,
            onValueChange = { shareUrl = it },
            label = { Text("Share URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(vSpaceDp))
        Text(
            text = "Created by Fabian Siebes",
            Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(vSpaceDp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
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