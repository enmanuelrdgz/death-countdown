package com.deathcoutndown.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new MyWallpaperEngine();
    }

    private class MyWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = this::draw;

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK); // ejemplo
                holder.unlockCanvasAndPost(canvas);
            }
            handler.postDelayed(drawRunner, 1000 / 30);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) handler.post(drawRunner);
            else handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(drawRunner);
        }
    }
}