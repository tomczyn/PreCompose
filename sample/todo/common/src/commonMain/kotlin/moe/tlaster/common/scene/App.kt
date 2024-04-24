package moe.tlaster.common.scene

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetApp() {
    PreComposeApp {
        MaterialTheme {
            Column(modifier = Modifier.fillMaxSize()) {
                var showBottomSheet by remember { mutableStateOf(-1) }
                val navigator = rememberNavigator("main")
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()
                NavHost(
                    navigator = navigator,
                    initialRoute = "/home",
                ) {
                    scene(route = "/home") {
                        Column(
                            Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = { showBottomSheet = 0 }) {
                                Text("Show NavHost bottom sheet")
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { showBottomSheet = 1 }) {
                                Text("Show Non NavHost bottom sheet")
                            }
                        }
                    }
                }
                if (showBottomSheet != -1) {
                    BottomSheetView(
                        showNavHost = showBottomSheet == 0,
                        sheetState = sheetState,
                        hideBottomSheet = { showBottomSheet = -1 })
                }
                LaunchedEffect(showBottomSheet) {
                    if (showBottomSheet != -1) {
                        val type = if (showBottomSheet == 0) "NavHost" else "Non NavHost"
                        println("Showing $type")
                        scope.launch { sheetState.show() }
                            .invokeOnCompletion {
                                println("$type shown")
                            }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BottomSheetView(
    showNavHost: Boolean,
    sheetState: SheetState,
    hideBottomSheet: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = hideBottomSheet,
        content = {
            val onHideBottomSheet: () -> Unit = remember {
                {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) hideBottomSheet()
                        }
                }
            }
            if (showNavHost) {
                BottomSheetContent(onHideBottomSheet)
            } else {
                NoNavHostBottomSheetContent(onHideBottomSheet)
            }
        }
    )
}

@Composable
private fun BottomSheetContent(
    onHideBottomSheet: () -> Unit,
) {
    val navigator = rememberNavigator()
    NavHost(
        modifier = Modifier.animateContentSize(),
        navigator = navigator,
        initialRoute = "/sheet1",
    ) {
        scene(route = "/sheet1") {
            Content(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                title = "NavHost 1",
                onClick = { navigator.navigate("/sheet2") }
            )
        }
        scene(route = "/sheet2") {
            Content(
                modifier = Modifier.fillMaxWidth().height(350.dp),
                title = "NavHost 2",
                onClick = { onHideBottomSheet() }
            )
        }
    }
    DisposableEffect(Unit) {
        onDispose { navigator.popBackStack() }
    }
}

@Composable
private fun NoNavHostBottomSheetContent(
    onHideBottomSheet: () -> Unit,
) {
    Column(
        modifier = Modifier.animateContentSize(),
    ) {
        var sheet2 by remember { mutableStateOf(false) }
        if (!sheet2) {
            Content(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                title = "Non NavHost 1",
                onClick = { sheet2 = true }
            )
        } else {
            Content(
                modifier = Modifier.fillMaxWidth().height(350.dp),
                title = "Non NavHost 2",
                onClick = { onHideBottomSheet(); sheet2 = false }
            )
        }
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    title: String,
    onClick: () -> Unit,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var field by remember { mutableStateOf("") }
        Text(title)
        TextField(
            value = field,
            onValueChange = { field = it })
        Button(onClick = { onClick() }) {
            Text("Next")
        }
    }
}
