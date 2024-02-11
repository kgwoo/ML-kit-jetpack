package com.example.ml_kit_jetpack.pose_detection.view

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ml_kit_jetpack.R
import com.example.ml_kit_jetpack.cameraX.CameraPreview
import com.example.ml_kit_jetpack.pose_detection.graphic.GraphicOverlay
import com.example.ml_kit_jetpack.pose_detection.graphic.PoseGraphic
import com.example.ml_kit_jetpack.pose_detection.view_model.PoseDetectionCameraViewModel
import com.example.ml_kit_jetpack.pose_detection.view_model.PoseDetectionCameraViewModelImpl
import com.google.mlkit.vision.pose.Pose


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoseDetectionCamera() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val graphicOverlay = remember { GraphicOverlay() }
    val cameraSelector: MutableState<Int> = remember {
        mutableStateOf(CameraSelector.LENS_FACING_FRONT)
    }
    val poseResult = remember { mutableStateOf<Pose?>(null) }
    val bitmapImage = remember { mutableStateOf<Bitmap?>(null) }

    val vm: PoseDetectionCameraViewModel = remember {
        PoseDetectionCameraViewModelImpl(
            graphicOverlay = graphicOverlay,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector.value,
            onResults = { bitmap, pose ->
                bitmapImage.value?.recycle()
                bitmapImage.value = bitmap
                poseResult.value = pose


            }
        )
    }


    Scaffold { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .onGloballyPositioned { screen ->
                graphicOverlay.updateGraphicOverlay(
                    width = screen.size.width.toFloat(),
                    height = screen.size.height.toFloat()
                )

                vm.startPoseDetection(
                    context = context,
                    previewView = previewView
                )

            }
        ) {
            CameraPreview(
                previewView = previewView,
                modifier = Modifier.fillMaxSize()
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (bitmapImage.value != null && poseResult.value != null) {
//                    graphicOverlay.add(CameraImageGraphic(graphicOverlay, bitmapImage.value!!))
                    graphicOverlay.add(PoseGraphic(graphicOverlay, poseResult.value!!))
                    graphicOverlay.onDraw(this)
                    graphicOverlay.clear()
                }
            }
            Row {
                IconButton(
                    onClick = {
                        cameraSelector.value =
                            if (cameraSelector.value == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        vm.onTabSwitchCamera(cameraSelector.value, context, previewView)
                    },
                    modifier = Modifier
                        .offset(16.dp, 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera_switch),
                        contentDescription = "Camera Flip",
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
            }
        }
    }

}