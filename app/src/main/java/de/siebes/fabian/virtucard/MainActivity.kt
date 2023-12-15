package de.siebes.fabian.virtucard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import de.siebes.fabian.virtucard.data.UserPreferencesRepository
import de.siebes.fabian.virtucard.ui.UserPrefsUiState
import de.siebes.fabian.virtucard.ui.UserPrefsViewModel
import de.siebes.fabian.virtucard.ui.UserPrefsViewModelFactory
import de.siebes.fabian.virtucard.ui.theme.VirtuCardTheme


private const val USER_PREFERENCES_NAME = "user_preferences"

public val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class MainActivity : ComponentActivity() {
    private lateinit var userPrefsViewModel: UserPrefsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPrefsViewModel = ViewModelProvider(
            this,
            UserPrefsViewModelFactory(
                UserPreferencesRepository(dataStore)
            )
        )[UserPrefsViewModel::class.java]

        setContent {
            VirtuCardTheme {
                MainContent(userPrefsViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(userPrefsViewModel: UserPrefsViewModel) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false,
            skipHiddenState = true,
        )
    )

    val userPrefsUiState = userPrefsViewModel.userPrefsUiStateLiveData.observeAsState(
        UserPrefsUiState("", "")
    )

    val peekHeight = 50.dp

    BottomSheetScaffold(
        sheetContent = {
            MyBottomSheet(
                { userPrefsViewModel.updateUserId(userId = it) },
                { userPrefsViewModel.updateUserPw(userPw = it) },
                userPrefsUiState
            )
        },
        containerColor = Color.Transparent,
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetPeekHeight = peekHeight,
    ) { _ -> // _ = innerPadding
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            MyWebView(userPrefsUiState, with(LocalDensity.current) { peekHeight.toPx() })
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MyWebView(userPrefsUiState: State<UserPrefsUiState>, bottomPad: Float) {
    var _urlToLoad by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(userPrefsUiState.value.userId, userPrefsUiState.value.userPw) {
        _urlToLoad = Utils.getProfileUrl(
            userPrefsUiState.value.userId,
            userPrefsUiState.value.userPw
        )
    }
    val urlToLoad = _urlToLoad

    val loading = remember { mutableStateOf(true) }

    var webViewHeight = 0

    Box {
        if (loading.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(alignment = Alignment.Center)
                    .padding(bottom = bottomPad.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

        }
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    // could be called multiple times
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // timeout to prevent flickering
                        postDelayed({
                            visibility = View.VISIBLE
                            loading.value = false
                        }, 300)
                    }
                }
                visibility = View.INVISIBLE
                settings.javaScriptEnabled = true
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun getBottomPad(webpageHeight: Double): Double {
                            return bottomPad / webViewHeight * webpageHeight
                        }
                    },
                    "VirtuCardApp"
                )
                settings.cacheMode =
                    WebSettings.LOAD_CACHE_ELSE_NETWORK // loading offline if possible

                val hasNetwork = Utils.isNetworkAvailable(context)
                if (urlToLoad == null) {
                    if (hasNetwork) {
                        loadUrl(Utils.BASE_URL)
                        loading.value = true
                    }
                } else {
                    loadUrl(urlToLoad)
                    loading.value = true
                }
            }
        }, update = {
            val hasNetwork = Utils.isNetworkAvailable(it.context)
            if (urlToLoad == null) {
                if (hasNetwork) {
                    it.loadUrl(Utils.BASE_URL)
                }
            } else {
                it.loadUrl(urlToLoad)
            }
        }, modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                webViewHeight = it.size.height
            })
    }
}


@Composable
fun MyBottomSheet(
    onUpdateUserId: (String) -> Unit,
    onUpdateUserPw: (String) -> Unit,
    userPrefsUiState: State<UserPrefsUiState>
) {
    var openChangeUserIdDialog by remember { mutableStateOf(false) }
    var openChangeUserPwDialog by remember { mutableStateOf(false) }

    val userId = userPrefsUiState.value.userId
    val userPw = userPrefsUiState.value.userPw

    val context = LocalContext.current
    val shareUrlViaNearby = {
        val sendNearbyIntent = Intent().apply {
            action = "com.google.android.gms.SHARE_NEARBY"
            putExtra(Intent.EXTRA_TEXT, Utils.getProfileUrl(userId))
            type = "text/plain"
            setPackage("com.google.android.gms")
        }
        context.startActivity(sendNearbyIntent)
    }
    val copyToClipboard = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("VirtuCard profile url", Utils.getProfileUrl(userId))
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    val shareUrlViaIntent = {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, Utils.getProfileUrl(userId))
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }


    val vSpaceDp = 15.dp
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        // Nearby, Copy, Share
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            MyIconOutlineButton(
                text = "Nearby",
                painter = painterResource(id = R.drawable.nearby_share),
                onClick = shareUrlViaNearby
            )
            MyIconOutlineButton(
                text = "Copy",
                painter = painterResource(id = R.drawable.baseline_content_copy_24),
                onClick = copyToClipboard
            )
            MyIconOutlineButton(
                text = "Share",
                painter = rememberVectorPainter(image = Icons.Filled.Share),
                onClick = shareUrlViaIntent
            )
        }

        Divider(Modifier.padding(vertical = vSpaceDp), thickness = 1.dp)

        if (userId.isNotEmpty()) {
            QRCode(userId)
        }
        Spacer(modifier = Modifier.height(vSpaceDp))
        Text(
            text = Utils.getProfileUrl(userId) ?: "Please enter a userid",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(Modifier.padding(vertical = vSpaceDp), thickness = 1.dp)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                openChangeUserIdDialog = true
            }) {
                Text(text = "Change userid")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = {
                openChangeUserPwDialog = true
            }) {
                Text(text = "Change password")
            }
        }

        Spacer(modifier = Modifier.height(vSpaceDp))
        Text(
            text = "Created by Fabian Siebes",
            Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(vSpaceDp))

        if (openChangeUserIdDialog) {
            EditDialog({ openChangeUserIdDialog = false }, onUpdateUserId, userId)
        }
        if (openChangeUserPwDialog) {
            EditDialog({ openChangeUserPwDialog = false }, onUpdateUserPw, userPw)
        }
    }
}

@Composable
fun MyIconOutlineButton(text: String, painter: Painter, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.padding(horizontal = 5.dp)) {
        Icon(
            painter = painter,
            contentDescription = "Icon for $text",
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}

@Composable
fun QRCode(userId: String) {
    val size = 512
    var bmpQRCode by remember {
        mutableStateOf(Bitmap.createBitmap(size, size, Bitmap.Config.RGBA_F16))
    }

    val frontCol = MaterialTheme.colorScheme.primary
    val backCol = MaterialTheme.colorScheme.background
    LaunchedEffect(userId, frontCol) {
        if (userId.isNotEmpty()) {
            // ~https://stackoverflow.com/a/64504871
            val hints = hashMapOf<EncodeHintType, Int>().also {
                it[EncodeHintType.MARGIN] = 1
            } // Make the QR code buffer border narrower
            val bits = QRCodeWriter().encode(
                Utils.getProfileUrl(userId),
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            bmpQRCode = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        it.setPixel(
                            x,
                            y,
                            if (bits[x, y]) frontCol.toArgb() else backCol.toArgb()
                        )
                    }
                }
            }
        }
    }

    Image(
        bmpQRCode.asImageBitmap(),
        contentDescription = "QR Code for url",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun EditDialog(closeDialog: () -> Unit, onUpdateUserId: (String) -> Unit, initValue: String) {
    var editValue by remember {
        mutableStateOf(initValue) // pass the initial value
    }

    LaunchedEffect(initValue) {
        editValue = initValue
    }

    AlertDialog(
        onDismissRequest = { closeDialog() },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdateUserId(editValue)
                    closeDialog()
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = closeDialog
            ) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(text = "Change url")
        },
        text = {
            Column {
                Text(text = "Enter your new url")
                TextField(value = editValue, onValueChange = { editValue = it })
            }
        }
    )
}
