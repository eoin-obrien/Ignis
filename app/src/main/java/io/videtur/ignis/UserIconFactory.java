package io.videtur.ignis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

public class UserIconFactory {

    private final int[] mAvatarColors;
    private final int mFontSize;
    private Canvas mCanvas;
    private TextPaint mPaint;
    private Rect mBounds;


    public UserIconFactory(Context context) {
        mAvatarColors = context.getResources().getIntArray(R.array.avatar_colors);
        mFontSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_font_size);
        mCanvas = new Canvas();
        mPaint = new TextPaint();
        mPaint.setTypeface(Typeface.SANS_SERIF);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);
        mBounds = new Rect();
    }

    public Bitmap getDefaultAvatar(String name, String email, int width, int height) {
        int color = mAvatarColors[Math.abs(email.hashCode()) % mAvatarColors.length];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        String initial = name.substring(0, 1);

        mCanvas.setBitmap(bitmap);
        mCanvas.drawColor(color);
        mPaint.setTextSize(width * 2 / 3);
        mPaint.getTextBounds(initial, 0, 1, mBounds);
        mCanvas.drawText(initial, 0, 1, width / 2,
                height / 2 + (mBounds.bottom - mBounds.top) / 2, mPaint);

        return bitmap;
    }

}
