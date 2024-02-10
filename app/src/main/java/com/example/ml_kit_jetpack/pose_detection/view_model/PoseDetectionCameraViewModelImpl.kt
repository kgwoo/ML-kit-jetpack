package com.example.ml_kit_jetpack.pose_detection.view_model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ml_kit_jetpack.pose_detection.BitmapUtils
import com.example.ml_kit_jetpack.pose_detection.graphic.GraphicOverlay
import com.example.ml_kit_jetpack.pose_detection.mlkit.PoseDetectorProcessorImpl
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

class PoseDetectionCameraViewModelImpl(
    private val graphicOverlay: GraphicOverlay,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraSelector: Int,
    private val onResults: (bitmap: android.graphics.Bitmap, Pose) -> Unit
) : ViewModel(
), PoseDetectionCameraViewModel {
    private val poseDetectorProcessor by lazy {
        PoseDetectorProcessorImpl()
    }
    private var cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
    private var lensFacing: Int = cameraSelector
    private var imageAnalysis: ImageAnalysis? = null
    private var needUpdateGraphicOverlayImageSourceInfo: Boolean = true
    private var bitmap: android.graphics.Bitmap? = null

    override fun startPoseDetection(context: Context, previewView: PreviewView) {
        requestAllPermission(context)

        if (cameraProviderLiveData.value == null) {
            cameraProviderLiveData = getProcessCameraProvider(context)
        }
        cameraProviderLiveData.value?.unbindAll()

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        imageAnalysis = startImageAnalysis(context)


//        if (videoCapture == null) {
//            videoCapture = createVideoCaptureUseCase(context)
//        }

        cameraProviderLiveData.value?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
    }


    private fun requestAllPermission(context: Context) {
        if (!hasRequiredPermissions(context)) {
            Log.d("Permission1", "permission check")
            ActivityCompat.requestPermissions(
                context as Activity, CAMERAX_PERMISSION, 0
            )
        }
    }


    private fun hasRequiredPermissions(context: Context): Boolean {
        return CAMERAX_PERMISSION.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getProcessCameraProvider(context: Context): MutableLiveData<ProcessCameraProvider> {
        if (cameraProviderLiveData.value == null) {
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            viewModelScope.launch {
                try {
                    cameraProviderLiveData.value = cameraProviderFuture.get()
                    Log.d("CameraProvider", "hi")
                } catch (e: ExecutionException) {
                    // Handle any errors (including cancellation) here.
//                    Log.e(TAG, "Unhandled exception", e)
                } catch (e: InterruptedException) {
//                    Log.e(TAG, "InterruptedException", e)
                }
            }
        }
        Log.d("CameraProviderX", "${cameraProviderLiveData.value}")
        return cameraProviderLiveData
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun startImageAnalysis(context: Context): ImageAnalysis {
        val analysisUseCase = ImageAnalysis.Builder().build()
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    Log.d("CameraViewModel", "isImageFlipped: $isImageFlipped")
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                    } else {
                        graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    poseDetectorProcessor.processImageProxy(
                        image = imageProxy
                    ) { results ->
                        bitmap = BitmapUtils.getBitmap(imageProxy, graphicOverlay)
                        if (bitmap != null) {
                            if (results != null) {
                                onResults(bitmap!!, results)
                            }
                        }
                    }
                } catch (e: MlKitException) {
                    Log.e("Camera", "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
        return analysisUseCase
    }

    companion object {
        val CAMERAX_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}