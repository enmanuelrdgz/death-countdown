package com.deathcoutndown.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import androidx.core.content.res.ResourcesCompat;

import java.util.Calendar;
import java.util.Locale;

import android.graphics.Typeface;

public class CountdownService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new CountdownEngine();
    }

    private class CountdownEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = this::draw;
        private boolean visible = false;
        private Paint textPaint, labelPaint;
        private Typeface boldTypeface, regularTypeface;

        public CountdownEngine() {
            // Configurar fuentes y colores
            boldTypeface = ResourcesCompat.getFont(CountdownService.this, R.font.specialeliteregular);
            regularTypeface = ResourcesCompat.getFont(CountdownService.this, R.font.specialeliteregular);

            // Paint para el texto grande (números)
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(120);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(boldTypeface);

            // Paint para las etiquetas (YEARS, MONTHS, DAYS, etc.)
            labelPaint = new Paint();
            labelPaint.setColor(Color.WHITE);
            labelPaint.setTextSize(40);
            labelPaint.setAntiAlias(true);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            labelPaint.setTypeface(regularTypeface);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    // Fondo negro
                    canvas.drawColor(Color.BLACK);

                    // Verificar si hay configuración válida
                    if (!SharedPrefsManager.hasValidConfiguration(CountdownService.this)) {
                        drawConfigurationMessage(canvas);
                        return;
                    }

                    // Obtener fecha de nacimiento y expectativa de vida
                    long birthDateMillis = SharedPrefsManager.getBirthDateMillis(CountdownService.this);
                    int expectedLifespanYears = SharedPrefsManager.getExpectedLifespan(CountdownService.this);

                    // Calcular fecha de muerte esperada
                    Calendar deathDate = Calendar.getInstance();
                    deathDate.setTimeInMillis(birthDateMillis);
                    deathDate.add(Calendar.YEAR, expectedLifespanYears);

                    // Obtener fecha actual
                    Calendar now = Calendar.getInstance();

                    // Calcular diferencia
                    long diffMillis = deathDate.getTimeInMillis() - now.getTimeInMillis();

                    if (diffMillis <= 0) {
                        // Si ya pasó el tiempo, mostrar "00:00:00..."
                        drawCountdownText(canvas, 0, 0, 0, 0, 0, 0, true);
                    } else {
                        // Convertir milisegundos a años, meses, días, horas, minutos, segundos
                        long seconds = diffMillis / 1000;
                        long minutes = seconds / 60;
                        seconds %= 60;
                        long hours = minutes / 60;
                        minutes %= 60;
                        long days = hours / 24;
                        hours %= 24;
                        long months = days / 30; // Aproximación
                        days %= 30;
                        long years = months / 12;
                        months %= 12;

                        drawCountdownText(canvas, years, months, days, hours, minutes, seconds, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Actualizar cada segundo
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, 1000);
            }
        }

        private void drawConfigurationMessage(Canvas canvas) {
            float centerX = canvas.getWidth() / 2f;
            float centerY = canvas.getHeight() / 2f;

            Paint messagePaint = new Paint();
            messagePaint.setColor(Color.WHITE);
            messagePaint.setTextSize(60);
            messagePaint.setAntiAlias(true);
            messagePaint.setTextAlign(Paint.Align.CENTER);
            messagePaint.setTypeface(regularTypeface);

            canvas.drawText("CONFIGURA TU", centerX, centerY - 50, messagePaint);
            canvas.drawText("COUNTDOWN", centerX, centerY + 50, messagePaint);
        }

        private void drawCountdownText(Canvas canvas, long years, long months, long days, long hours, long minutes, long seconds, boolean isExpired) {
            float centerX = canvas.getWidth() / 2f;
            float centerY = canvas.getHeight() / 2f;

            // Título
            String title = isExpired ? "TIME'S UP" : "TIME LEFT";
            canvas.drawText(title, centerX, centerY - 300, labelPaint);

            // Años, Meses, Días (grande)
            canvas.drawText(String.format(Locale.getDefault(), "%02d", years), centerX - 200, centerY - 150, textPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%02d", months), centerX, centerY - 150, textPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%02d", days), centerX + 200, centerY - 150, textPaint);

            // Etiquetas (YEARS, MONTHS, DAYS)
            canvas.drawText("YEARS", centerX - 200, centerY - 90, labelPaint);
            canvas.drawText("MONTHS", centerX, centerY - 90, labelPaint);
            canvas.drawText("DAYS", centerX + 200, centerY - 90, labelPaint);

            // Horas, Minutos, Segundos (formato 01:17:25)
            String timeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            canvas.drawText(timeText, centerX, centerY + 50, textPaint);

            // Etiquetas (HR, MIN, SEC)
            canvas.drawText("HR       MIN       SEC", centerX, centerY + 100, labelPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(drawRunner);
            super.onDestroy();
        }
    }
}