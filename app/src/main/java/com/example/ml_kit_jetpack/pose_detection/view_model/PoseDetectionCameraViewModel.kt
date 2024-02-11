package com.example.ml_kit_jetpack.pose_detection.view_model

import android.content.Context
import androidx.camera.view.PreviewView

interface PoseDetectionCameraViewModel {
    fun startPoseDetection(context:Context, previewView: PreviewView)
    fun onTabSwitchCamera(lensFacing: Int,context: Context, previewView: PreviewView)
}