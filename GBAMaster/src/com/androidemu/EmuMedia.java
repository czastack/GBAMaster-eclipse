package com.androidemu;

import java.nio.Buffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.media.AudioTrack;
import android.view.SurfaceHolder;

class EmuMedia {
    private static Bitmap bitmap;
    private static Rect dirty = new Rect();
    private static boolean firstBlt;
    private static SurfaceHolder holder;
    private static Picture overlay;
    private static Rect region = new Rect();
    private static int surfaceHeight;
    private static int surfaceWidth;
    private static AudioTrack track;
    private static float volume = AudioTrack.getMaxVolume();

    static boolean audioCreate(int var0, int var1_1, int var2_2) {
        int var3_3 = var1_1 == 16 ? 2 : 3;
        int var4_4 = var2_2 == 2 ? 3 : 2;
        if ((((track != null) && (track.getSampleRate() == var0)) && (track.getAudioFormat() == var3_3)) && (track.getChannelCount() == var2_2)) {
            return true;
        }
        int var5_5 = (2 * (var2_2 * var0 * var1_1 / 8) / 60);
		int var6_6;
        if ((var6_6 = AudioTrack.getMinBufferSize(var0, var4_4, var3_3)) < var5_5) {
            var6_6 = var5_5;
        }
        try {
            EmuMedia.track = new AudioTrack(3, var0, var4_4, var3_3, var6_6, 1);
            if (EmuMedia.track.getState() == 0) {
                EmuMedia.track = null;
            }
            while (track == null) {
                return false;
            }
        }
        catch (Exception var7_7) {
            EmuMedia.track = null;
            return false;
        }
        track.setStereoVolume(volume, volume);
        return true;
    }

    static void audioDestroy() {
        if (track == null) return;
        track.stop();
        track = null;
    }

    static void audioPause() {
        if (track == null) return;
        track.pause();
    }

    static void audioPlay(byte[] arrby, int n) {
        if (track == null) return;
        track.write(arrby, 0, n);
    }

    static void audioSetVolume(int n) {
        float min = AudioTrack.getMinVolume();
        volume = min + ((AudioTrack.getMaxVolume() - min) * n) / 100;
        if (track == null) return;
        track.setStereoVolume(volume, volume);
    }

    static void audioStart() {
        if (track == null) return;
        track.play();
    }

    static void audioStop() {
        if (track == null) return;
        track.stop();
        track.flush();
    }

    static void bitBlt(Buffer buffer, boolean bl) {
        Canvas canvas;
        if (buffer != null) {
            bitmap.copyPixelsFromBuffer(buffer);
        }
        if (firstBlt) {
            firstBlt = false;
            canvas = holder.lockCanvas();
            canvas.drawColor(-16777216);
        } else {
            if (!(bl)) {
                dirty.set(region);
            } else {
                dirty.left = (surfaceWidth - region.right);
                dirty.right = (surfaceWidth - region.left);
                dirty.top = (surfaceHeight - region.bottom);
                dirty.bottom = (surfaceHeight - region.top);
            }
            canvas = holder.lockCanvas(dirty);
        }
        if (bl) {
            canvas.rotate(180, surfaceWidth / 2, surfaceHeight / 2);
        }
        canvas.drawBitmap(bitmap, region.left, region.top, null);
        if (overlay != null) {
            overlay.draw(canvas);
        }
        holder.unlockCanvasAndPost(canvas);
    }

    static void destroy() {
        overlay = null;
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        if (track == null) return;
        track.stop();
        track = null;
    }

    static void setOverlay(Picture picture) {
        overlay = picture;
    }

    static void setSurface(SurfaceHolder surfaceHolder) {
        holder = surfaceHolder;
    }

    static Bitmap setSurfaceRegion(int w, int h, int left, int top, int width, int height) {
        firstBlt = true;
        surfaceWidth = w;
        surfaceHeight = h;
        region.set(left, top, (left + width), (top + height));
        if (((bitmap != null) && (bitmap.getWidth() == width)) && (bitmap.getHeight() == height)) return bitmap;
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        return bitmap;
    }
}

