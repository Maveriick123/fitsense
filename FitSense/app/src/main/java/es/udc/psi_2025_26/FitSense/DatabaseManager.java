package es.udc.psi_2025_26.FitSense;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

public class DatabaseManager {

    // Para acceder al nodo raíz
    private final DatabaseReference mDatabase;
    private final Context context;

    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_EXERCISE_CAMBER = "EXERCISE_CAMBER";
    public static final String KEY_REPS_CAMBER = "REPS_CAMBER";
    public static final String NODE_ROUTINE = "ROUTINES";

    public DatabaseManager(Context context) {
        // Guardamos el contexto global
        this.context = context.getApplicationContext();

        // Inicializamos y apuntamos al nodo raíz de la base de datos
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    public void registrarRutina(String usuarioId, ExerciseType ejercicio, int repeticiones) {
        Map<String, Object> rutinaInfo = new HashMap<>();
        rutinaInfo.put(KEY_USER_ID, usuarioId);
        rutinaInfo.put(KEY_EXERCISE_CAMBER, nombreEnBd(ejercicio));
        rutinaInfo.put(KEY_REPS_CAMBER, repeticiones);

        mDatabase.child(NODE_ROUTINE).push().setValue(rutinaInfo);
    }

    // Nombre con el que se guarda cada ejercicio, no se traduce para no romper los datos ya guardados
    private static String nombreEnBd(ExerciseType ejercicio) {
        switch (ejercicio) {
            case FLEXIONES: return "Flexiones";
            case PASOS: return "Pasos";
            case DOMINADAS: return "Dominadas";
            case SALTOS:
            default: return "Saltos";
        }
    }

    public void escucharRutina(final String userId) {
        mDatabase.child(NODE_ROUTINE).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String usuarioIdRutina = snapshot.child(KEY_USER_ID).getValue(String.class);

                if(usuarioIdRutina != null) {
                    String ejercicio = snapshot.child(KEY_EXERCISE_CAMBER).getValue(String.class);
                    Long reps = snapshot.child(KEY_REPS_CAMBER).getValue(Long.class);

                    // Uso de getString con argumentos posicionales
                    String logMensaje = context.getString(R.string.log_rutina_completada, reps, ejercicio);
                    Log.d(MainActivity.TAG, logMensaje);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Uso de getString con argumentos posicionales
                String errorMensaje = context.getString(R.string.log_error_bd, error.getMessage());
                Log.d(MainActivity.TAG, errorMensaje);
            }
        });
    }
}