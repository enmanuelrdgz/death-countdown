package com.deathcoutndown.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;
import java.util.Calendar;

public class SettingsActivity extends Activity {
    private Button btnDatePicker;
    private NumberPicker npYears;
    private Button btnSave;
    private Button btnSetWallpaper;
    private Calendar birthDate = Calendar.getInstance();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE);

        btnDatePicker = findViewById(R.id.btnDatePicker);
        npYears = findViewById(R.id.npYears);
        btnSave = findViewById(R.id.btnSave);
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper);

        // Configurar NumberPicker (50-120 años)
        npYears.setMinValue(50);
        npYears.setMaxValue(120);
        npYears.setValue(80); // Valor por defecto

        // Cargar datos guardados (si existen)
        loadSavedData();

        btnDatePicker.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveSettings());
        btnSetWallpaper.setOnClickListener(v -> setLiveWallpaper());

        // Mensaje de bienvenida
        if (getIntent().getCategories() != null &&
                getIntent().getCategories().contains("android.intent.category.LAUNCHER")) {
            Toast.makeText(this, "Configura tu countdown de vida", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        // Configurar fecha máxima (hoy)
        Calendar maxDate = Calendar.getInstance();

        // Configurar fecha mínima (hace 120 años)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -120);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    birthDate.set(year, month, day);
                    btnDatePicker.setText(String.format("%02d/%02d/%d", day, month + 1, year));

                    // Calcular edad actual para sugerir expectativa de vida realista
                    Calendar now = Calendar.getInstance();
                    int currentAge = now.get(Calendar.YEAR) - year;
                    if (now.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                        currentAge--;
                    }

                    // Sugerir expectativa de vida basada en edad actual + promedio
                    int suggestedLifespan = Math.max(currentAge + 30, 80);
                    if (suggestedLifespan <= 120) {
                        npYears.setValue(suggestedLifespan);
                    }
                },
                birthDate.get(Calendar.YEAR),
                birthDate.get(Calendar.MONTH),
                birthDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void saveSettings() {
        // Validar que se haya seleccionado una fecha de nacimiento
        if (btnDatePicker.getText().toString().equals("Seleccionar fecha")) {
            Toast.makeText(this, "Por favor selecciona tu fecha de nacimiento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que la expectativa de vida sea mayor a la edad actual
        Calendar now = Calendar.getInstance();
        int currentAge = now.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            currentAge--;
        }

        if (npYears.getValue() <= currentAge) {
            Toast.makeText(this, "La expectativa de vida debe ser mayor a tu edad actual (" + currentAge + " años)", Toast.LENGTH_LONG).show();
            return;
        }

        // Guardar configuración usando SharedPrefsManager
        SharedPrefsManager.saveBirthDate(this, birthDate.getTimeInMillis());
        SharedPrefsManager.saveExpectedLifespan(this, npYears.getValue());

        // Calcular años restantes para mostrar en el mensaje
        int yearsLeft = npYears.getValue() - currentAge;

        Toast.makeText(this, "Configuración guardada. Te quedan aproximadamente " + yearsLeft + " años.", Toast.LENGTH_LONG).show();

        // Habilitar el botón de establecer wallpaper
        btnSetWallpaper.setEnabled(true);
    }

    private void setLiveWallpaper() {
        // Verificar que hay una configuración válida
        if (!SharedPrefsManager.hasValidConfiguration(this)) {
            Toast.makeText(this, "Primero debes configurar y guardar tu fecha de nacimiento", Toast.LENGTH_LONG).show();
            return;
        }

        // En Android, no podemos establecer un live wallpaper directamente por seguridad
        // La única forma es abrir el selector para que el usuario lo elija
        openWallpaperPicker();
    }

    private void openWallpaperPicker() {
        try {
            // Método 1: Abrir directamente el selector de live wallpaper con nuestro wallpaper preseleccionado
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            ComponentName componentName = new ComponentName(this, CountdownService.class);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName);
            startActivity(intent);
            Toast.makeText(this, "Presiona 'Establecer wallpaper' para confirmar", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Método 2: Abrir el selector general de live wallpapers
                Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                startActivity(intent);
                Toast.makeText(this, "Busca y selecciona 'Death Countdown' en la lista", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    // Método 3: Abrir configuración de wallpaper general
                    Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                    startActivity(intent);
                    Toast.makeText(this, "Ve a 'Live Wallpapers' y busca 'Death Countdown'", Toast.LENGTH_LONG).show();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    Toast.makeText(this, "Por favor ve manualmente a Configuración > Wallpaper > Live Wallpapers", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void loadSavedData() {
        long savedMillis = SharedPrefsManager.getBirthDateMillis(this);
        if (savedMillis != -1) {
            birthDate.setTimeInMillis(savedMillis);
            btnDatePicker.setText(String.format("%02d/%02d/%d",
                    birthDate.get(Calendar.DAY_OF_MONTH),
                    birthDate.get(Calendar.MONTH) + 1,
                    birthDate.get(Calendar.YEAR)
            ));
            // Si ya hay configuración válida, habilitar el botón de wallpaper
            btnSetWallpaper.setEnabled(true);
        } else {
            // Si no hay configuración, deshabilitar el botón hasta que se guarde
            btnSetWallpaper.setEnabled(false);
        }
        npYears.setValue(SharedPrefsManager.getExpectedLifespan(this));
    }
}