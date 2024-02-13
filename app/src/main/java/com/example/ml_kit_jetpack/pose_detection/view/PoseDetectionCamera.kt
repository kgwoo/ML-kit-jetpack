package com.example.ml_kit_jetpack.pose_detection.view

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ml_kit_jetpack.R
import com.example.ml_kit_jetpack.cameraX.CameraPreview
import com.example.ml_kit_jetpack.pose_detection.graphic.GraphicOverlay
import com.example.ml_kit_jetpack.pose_detection.graphic.PoseGraphic
import com.example.ml_kit_jetpack.pose_detection.view_model.PoseDetectionCameraViewModel
import com.example.ml_kit_jetpack.pose_detection.view_model.PoseDetectionCameraViewModelImpl
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark


@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
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

    var rightPosition = remember { mutableStateOf<Float?>(0.0f) }
    var leftPosition = remember { mutableStateOf<Float?>(0.0f) }
    var count = remember { mutableStateOf<Int>(0) }
    var inOut = remember{  mutableStateOf<Boolean>(false) }
    val textMeasurer = rememberTextMeasurer()

    val vm: PoseDetectionCameraViewModel = remember {
        PoseDetectionCameraViewModelImpl(
            graphicOverlay = graphicOverlay,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector.value,
            onResults = { bitmap, pose ->
                val rightThumbPosition =
                    pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)?.position3D?.x
                val leftThumbPostion = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)?.position3D?.x

                rightPosition.value = rightThumbPosition
                leftPosition.value = leftThumbPostion

                bitmapImage.value?.recycle()
                bitmapImage.value = bitmap
                poseResult.value = pose

                if(inOut.value){
                    if(rightPosition.value != null && leftPosition.value != null ){
                        if(rightPosition.value!! > 180 && leftPosition.value!! < 290){
                            count.value += 1
                            inOut.value = false
                        }
                    }
                }else{
                    if(rightPosition.value != null && leftPosition.value != null ) {
                        if (rightPosition.value!! < 120 && leftPosition.value!! > 250) {
                            inOut.value = true
                        }
                    }
                }
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
            val textToDraw = "A"

            val style = TextStyle(
                fontSize = 150.sp,
                color = Color.Black,
                background = Color.Red.copy(alpha = 0.2f)
            )

            val textLayoutResult = remember(textToDraw) {
                textMeasurer.measure(textToDraw, style)
            }

            CameraPreview(
                previewView = previewView,
                modifier = Modifier.fillMaxSize()
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (bitmapImage.value != null && poseResult.value != null) {
//                    graphicOverlay.add(CameraImageGraphic(graphicOverlay, bitmapImage.value!!))
                    graphicOverlay.add(PoseGraphic(graphicOverlay, poseResult.value!!))
                    graphicOverlay.onDraw(this)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "오른쪽: ${rightPosition.value}",
                        style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.White,
//                            background = Color.Red.copy(alpha = 0.2f)
                        ),
                        topLeft = Offset(
                            x = center.x - textLayoutResult.size.width, y = 40f
                        ),
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "왼쪽: ${leftPosition.value}",
                        style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.White,
//                            background = Color.Red.copy(alpha = 0.2f)
                        ),
                        topLeft = Offset(
                            x = center.x - textLayoutResult.size.width, y = 120f
                        ),
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "횟수: ${count.value}",
                        style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.Red,
//                            background = Color.Red.copy(alpha = 0.2f)
                        ),
                        topLeft = Offset(
                            x = center.x - textLayoutResult.size.width, y = 240f
                        ),
                    )
                    graphicOverlay.clear()
                }else{
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "운동을 준비해주세요!",
                        style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.LightGray,
//                            background = Color.Red.copy(alpha = 0.2f)
                        ),
                        topLeft = Offset(
                            x = center.x - textLayoutResult.size.width, y = center.y - textLayoutResult.size.height
                        ),
                    )
                }
            }
            Column {
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
//                Row(
//                    modifier = Modifier
//                        .offset(16.dp, 16.dp)
//                        .size(width = 60.dp, height = 40.dp)
//                        .background(Color.Red)
//                ) {
////                    Text(text = "하하하 ${rightPosition.value}")
//                }
            }

        }
    }

}