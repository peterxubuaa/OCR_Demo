/*
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.min.baiduai.demo.decode;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.min.baiduai.demo.R;
import com.min.baiduai.demo.camera.CameraManager;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler {
    private static final String TAG = CaptureActivityHandler.class.getName();

    private State mState;

    public CaptureActivityHandler(boolean flashEnable) {
        mState = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        restartPreviewAndDecode(flashEnable);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.auto_focus:
                // Log.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (mState == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                }
                break;
            case R.id.decode_succeeded:
                Log.e(TAG, "Got decode succeeded message");
                mState = State.SUCCESS;
                break;
            case R.id.decode_failed:
                // We're decoding as fast as possible, so when one decode fails, start another.
                mState = State.PREVIEW;
                CameraManager.get().requestPreviewFrame();
                break;
        }
    }

    public void quitSynchronously() {
        mState = State.DONE;
        CameraManager.get().stopPreview();

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void restartPreviewAndDecode(boolean flashEnable) {
        if (mState != State.PREVIEW) {
            CameraManager.get().startPreview();
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame();
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            if (flashEnable) CameraManager.get().setFlashLight(flashEnable);

        }
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public void onPause() {
        mState = State.DONE;
        CameraManager.get().stopPreview();
    }
}