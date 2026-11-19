package es.udc.psi_2025_26.FitSense.presentation.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

// Clase para comprobar los permisos, la usan WorkoutActivity y CalibrationActivity
// así las dos pantallas siguen la misma regla antes de arrancar el servicio de sensores
// El servicio de tipo "health" necesita ACTIVITY_RECOGNITION en Android 14+
// y las notificaciones necesitan POST_NOTIFICATIONS en Android 13+
public final class PermissionHelper {

    public static final String[] REQUIRED_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? new String[]{Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACTIVITY_RECOGNITION}
            : new String[0];

    private PermissionHelper() {}

    public static boolean hasServicePermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
