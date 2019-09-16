/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.min.baiduai.demo.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.min.baiduai.demo.utils.CommonTools;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置相机的参数信息，获取最佳的预览界面
 * 
 */
public final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";
	// 屏幕分辨率
	private Point screenResolution;
	// 相机分辨率
	private Point cameraResolution;

	void initFromCameraParameters(Context ctx, Camera camera, int orientation) {
		// 需要判断摄像头是否支持缩放
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.isZoomSupported()) {
			// 设置成最大倍数的1/10，基本符合远近需求
			parameters.setZoom(parameters.getMaxZoom() / 10);
		}
		if (parameters.getMaxNumFocusAreas() > 0) {
			List focusAreas = new ArrayList();
			Rect focusRect = new Rect(-900, -900, 900, 900);
			focusAreas.add(new Camera.Area(focusRect, 1000));
			parameters.setFocusAreas(focusAreas);
		}
		
		screenResolution = CommonTools.getDisplaySize(ctx);
		Log.i(TAG, "Screen resolution: " + screenResolution);

		/* 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
		Point screenResolutionForCamera = new Point();
		if (90 == orientation || 270 == orientation) {
			screenResolutionForCamera.x = screenResolution.y;
			screenResolutionForCamera.y = screenResolution.x;
		} else {
			screenResolutionForCamera.x = screenResolution.x;
			screenResolutionForCamera.y = screenResolution.y;
		}

		cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolutionForCamera);
		Log.i(TAG, "Camera resolution x: " + cameraResolution.x);
		Log.i(TAG, "Camera resolution y: " + cameraResolution.y);
	}

	void setDesiredCameraParameters(Camera camera, int orientation) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

//		if (safeMode) {
//			Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
//		}

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y + ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}

		camera.setDisplayOrientation(orientation);
//		int orient = getDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);
		/* 设置相机预览为竖屏 */
//		camera.setDisplayOrientation(90);
		/* 设置相机预览为横屏 */
//		camera.setDisplayOrientation(0);
	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	private int getDisplayOrientation(int cameraId) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation + 360) % 360;
		}
//        Log.d(TAG,"getDisplayOrientation = " + result);
		return result;
	}

}