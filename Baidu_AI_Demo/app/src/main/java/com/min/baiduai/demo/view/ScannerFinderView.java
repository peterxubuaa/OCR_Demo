package com.min.baiduai.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.min.baiduai.demo.R;
import com.min.baiduai.demo.utils.CommonTools;

import java.lang.ref.WeakReference;

import androidx.core.content.ContextCompat;

public final class ScannerFinderView extends RelativeLayout {

    private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private static final int MIN_FOCUS_BOX_WIDTH = 50;
    private static final int MIN_FOCUS_BOX_HEIGHT = 50;
    private static final int MIN_FOCUS_BOX_PAD = 10;
    private static int MIN_FOCUS_BOX_TOP = 200;
    private static int MAX_FOCUS_BOX_BOTTOM = 600;

    private static Point ScrRes;
    private int mTop;

    private Paint mPaint;
    private int mScannerAlpha;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
    private int mTextColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    private Rect mFrameRect; //绘制的Rect
//    private Rect mRect; //返回的Rect
    private Context mContext;
    private Handler mHandler;
    private int mOrientation;

    public ScannerFinderView(Context context) {
        this(context, null);
    }

    public ScannerFinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mHandler = new MyHandler(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMaskColor = ContextCompat.getColor(context, R.color.finder_mask);
        mFrameColor = ContextCompat.getColor(context, R.color.finder_frame);
        mLaserColor = ContextCompat.getColor(context, R.color.finder_laser);
        mTextColor = ContextCompat.getColor(context, R.color.white);

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;
        mScannerAlpha = 0;
        init();
        setOnTouchListener(getTouchListener());
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setTopBottomLimit(int topLimit, int bottomLimit) {
        MIN_FOCUS_BOX_TOP = topLimit;
        MAX_FOCUS_BOX_BOTTOM = ScrRes.y - bottomLimit;
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }
        // 需要调用下面的方法才会执行onDraw方法
        setWillNotDraw(false);

        if (mFrameRect == null) {
            ScrRes = CommonTools.getDisplaySize(mContext);

            int width = ScrRes.x - MIN_FOCUS_BOX_PAD * 2;
            int height = ScrRes.y / (mOrientation == 0? 3 : 6);

            width = width == 0
                    ? MIN_FOCUS_BOX_WIDTH
                    : width < MIN_FOCUS_BOX_WIDTH ? MIN_FOCUS_BOX_WIDTH : width;

            height = height == 0
                    ? MIN_FOCUS_BOX_HEIGHT
                    : height < MIN_FOCUS_BOX_HEIGHT ? MIN_FOCUS_BOX_HEIGHT : height;

            int left = (ScrRes.x - width) / 2;
            int top = MIN_FOCUS_BOX_TOP;
            mTop = top; //记录初始距离上方距离

            mFrameRect = new Rect(left, top, left + width, top + height);
//            mRect = mFrameRect;
        }
    }

    public Rect getRect() {
//        return new Rect(mFrameRect.left, mFrameRect.top - MIN_FOCUS_BOX_TOP, mFrameRect.right, mFrameRect.bottom);
        return mFrameRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        Rect frame = mFrameRect;
        if (frame == null) {
            return;
        }
        int width = getWidth();//canvas.getWidth();
        int height = getHeight();//canvas.getHeight();

        // 绘制焦点框外边的暗色背景
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        drawFocusRect(canvas, frame);
        drawAngle(canvas, frame);
        drawText(canvas, frame);
        drawLaser(canvas, frame);
    }

    /**
     * 画聚焦框，白色的
     *
     */
    private void drawFocusRect(Canvas canvas, Rect rect) {
        // 绘制焦点框（黑色）
        mPaint.setColor(mFrameColor);
        // 上
        canvas.drawRect(rect.left + mAngleLength, rect.top, rect.right - mAngleLength, rect.top + mFocusThick, mPaint);
        // 左
        canvas.drawRect(rect.left, rect.top + mAngleLength, rect.left + mFocusThick, rect.bottom - mAngleLength,
                mPaint);
        // 右
        canvas.drawRect(rect.right - mFocusThick, rect.top + mAngleLength, rect.right, rect.bottom - mAngleLength,
                mPaint);
        // 下
        canvas.drawRect(rect.left + mAngleLength, rect.bottom - mFocusThick, rect.right - mAngleLength, rect.bottom,
                mPaint);
    }

    /**
     * 画四个角
     *
     */
    private void drawAngle(Canvas canvas, Rect rect) {
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(OPAQUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mAngleThick);
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        // 左上角
        canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, mPaint);
        canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, mPaint);
        // 右上角
        canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, mPaint);
        canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, mPaint);
        // 左下角
        canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, mPaint);
        canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, mPaint);
        // 右下角
        canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, mPaint);
        canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, mPaint);
    }

    private void drawText(Canvas canvas, Rect rect) {
        int margin = 40;
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(getResources().getDimension(R.dimen.text_size_13sp));
        String text = getResources().getString(R.string.auto_scan_notification);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = rect.bottom + margin + offY;
        float left = (ScrRes.x - mPaint.getTextSize() * text.length()) / 2;
        canvas.drawText(text, left, newY, mPaint);
    }

    private void drawLaser(Canvas canvas, Rect rect) {
        // 绘制焦点框内固定的一条扫描线
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
        mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = rect.height() / 2 + rect.top;
        canvas.drawRect(rect.left + 2, middle - 1, rect.right - 1, middle + 2, mPaint);

        mHandler.sendEmptyMessageDelayed(1, ANIMATION_DELAY);
    }

    static class MyHandler extends Handler {
        WeakReference<ScannerFinderView> mTheView;

        MyHandler(ScannerFinderView view) {
            mTheView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTheView.get().invalidate();
        }
    }

    private OnTouchListener getTouchListener() {
        return new OnTouchListener() {
            int lastX = -1;
            int lastY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                performClick();//???
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = -1;
                        lastY = -1;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int currentX = (int) event.getX();
                        int currentY = (int) event.getY();
                        try {
                            Rect rect = mFrameRect;
                            final int BUFFER = 60;
                            if (lastX >= 0) {

                                boolean currentXLeft = currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER;
                                boolean currentXRight = currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER;
                                boolean lastXLeft = lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER;
                                boolean lastXRight = lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER;

                                boolean currentYTop = currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER;
                                boolean currentYBottom = currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER;
                                boolean lastYTop = lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER;
                                boolean lastYBottom = lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER;

                                boolean XLeft = currentXLeft || lastXLeft;
                                boolean XRight = currentXRight || lastXRight;
                                boolean YTop = currentYTop || lastYTop;
                                boolean YBottom = currentYBottom || lastYBottom;

                                boolean YTopBottom = (currentY <= rect.bottom && currentY >= rect.top)
                                        || (lastY <= rect.bottom && lastY >= rect.top);

                                boolean XLeftRight = (currentX <= rect.right && currentX >= rect.left)
                                        || (lastX <= rect.right && lastX >= rect.left);

                                    //右上角
                                if (XLeft && YTop) {
                                    updateBoxRect(2 * (lastX - currentX), (lastY - currentY), true);
                                    //左上角
                                } else if (XRight && YTop) {
                                    updateBoxRect(2 * (currentX - lastX), (lastY - currentY), true);
                                    //右下角
                                } else if (XLeft && YBottom) {
                                    updateBoxRect(2 * (lastX - currentX), (currentY - lastY), false);
                                    //左下角
                                } else if (XRight && YBottom) {
                                    updateBoxRect(2 * (currentX - lastX), (currentY - lastY), false);
                                    //左侧
                                } else if (XLeft && YTopBottom) {
                                    updateBoxRect(2 * (lastX - currentX), 0, false);
                                    //右侧
                                } else if (XRight && YTopBottom) {
                                    updateBoxRect(2 * (currentX - lastX), 0, false);
                                    //上方
                                } else if (YTop && XLeftRight) {
                                    updateBoxRect(0, (lastY - currentY), true);
                                    //下方
                                } else if (YBottom && XLeftRight) {
                                    updateBoxRect(0, (currentY - lastY), false);
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        v.invalidate();
                        lastX = currentX;
                        lastY = currentY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        //移除之前的刷新
                        mHandler.removeMessages(1);
                        //松手时对外更新
//                        mRect = mFrameRect;
                        lastX = -1;
                        lastY = -1;
                        return true;
                    default:

                }
                return false;
            }
        };
    }

    private void updateBoxRect(int dW, int dH, boolean isUpward) {

        int newWidth = (mFrameRect.width() + dW > ScrRes.x || mFrameRect.width() + dW < MIN_FOCUS_BOX_WIDTH)
                ? 0 : mFrameRect.width() + dW;

        //限制扫描框最大高度不超过屏幕宽度
        int newHeight = (mFrameRect.height() + dH > ScrRes.y || mFrameRect.height() + dH < MIN_FOCUS_BOX_HEIGHT)
                ? 0 : mFrameRect.height() + dH;

        if (newWidth < MIN_FOCUS_BOX_WIDTH || newHeight < MIN_FOCUS_BOX_HEIGHT){
            return;
        }

        int leftOffset = (ScrRes.x - newWidth) / 2;

        if (isUpward){
            mTop -= dH;
        }

        int topOffset = mTop;

        if (topOffset < MIN_FOCUS_BOX_TOP){
            mTop = MIN_FOCUS_BOX_TOP;
            return;
        }

//        if (topOffset + newHeight > MIN_FOCUS_BOX_TOP + ScrRes.x){
        if (topOffset + newHeight > MAX_FOCUS_BOX_BOTTOM){
            return;
        }

        mFrameRect = new Rect(leftOffset, topOffset, leftOffset + newWidth, topOffset + newHeight);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(1);
    }
}
