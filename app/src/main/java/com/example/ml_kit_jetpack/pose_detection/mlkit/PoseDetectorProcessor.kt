package com.example.ml_kit_jetpack.pose_detection.mlkit

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.pose.Pose

interface PoseDetectorProcessor {
    fun processImageProxy(image: ImageProxy?, onPoseDetected:(Pose?)->Unit)
    fun stop()
}