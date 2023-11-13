package site.feiyuliuxing.camera2test

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import site.feiyuliuxing.camera2test.activities.FaceDetectionActivity
import site.feiyuliuxing.camera2test.activities.NoPreviewActivity
import site.feiyuliuxing.camera2test.activities.ScanQRCodeActivity
import site.feiyuliuxing.camera2test.activities.TextRecognizeActivity
import site.feiyuliuxing.camera2test.utils.CameraUtil

class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if(it.containsValue(false)) {

            } else {
                //获得所有权限
            }
        }
        //申请权限
        permissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ))

        CameraUtil.printCameraCharacteristics(this)

        setContent {
            HomeScreen {
                startActivity(Intent(this, it))
            }
        }
    }
}

class ActivityItem(val title: String, val clazz: Class<*>)

val activityItems = arrayOf(
    ActivityItem("扫描二维码", ScanQRCodeActivity::class.java),
    ActivityItem("人脸检测", FaceDetectionActivity::class.java),
    ActivityItem("文字识别", TextRecognizeActivity::class.java),
    ActivityItem("无预览", NoPreviewActivity::class.java),
)

@Composable
fun ActionItem(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = Color.White
        )
    }
}

@Composable
fun HomeScreen(onItemClick: (clazz: Class<*>) -> Unit) {
    MaterialTheme {
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar() {
                    ActionItem(icon = Icons.Filled.Menu) {
                        scope.launch {
                            scaffoldState.drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }
                    Text(text = "Camera2Test", modifier = Modifier.weight(1f), style = TextStyle(
                        color = Color.White, fontSize = 18.sp
                    )
                    )
                    ActionItem(icon = Icons.Filled.Settings) {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("设置")
                        }
                    }
                    ActionItem(icon = Icons.Filled.Share) {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("分享")
                        }
                    }
                }
            },
            drawerContent = {
                LazyColumn() {
                    items(arrayOf("Menu1", "Menu2")) {
                        Text(text = it, modifier = Modifier.clickable {
                            scope.launch {
                                scaffoldState.drawerState.close()
                                scaffoldState.snackbarHostState.showSnackbar(
                                    it,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        })
                        Divider()
                    }
                }
            }) {
            LazyColumn(Modifier.fillMaxSize().padding(it)) {
                items(activityItems) {
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                onItemClick(it.clazz)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = it.title,
                            style = TextStyle(fontSize = 18.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Filled.NavigateNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    }
}