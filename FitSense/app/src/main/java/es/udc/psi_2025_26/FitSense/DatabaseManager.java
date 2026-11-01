package es.udc.psi_2025_26.FitSense;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

// 1. Entrar en la consola de Firebase
// 2. Crear la Realtime BD
// 3. Agregar App - Android - Nombre del paquete de la app
// 4. Clonar google-service-json en app/
// 5. Sincronizar en el gradle el SDK de Firebase
// 6. Trabajar con el servicio de Firebase, en este caso Realtime Database

public class DatabaseManager {

    // Para acceder al nodo raíz
    private final DatabaseReference mDatabase;
    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_EXERCISE_CAMBER = "EXERCISE_CAMBER";
    public static final String KEY_REPS_CAMBER = "REPS_CAMBER";

    public static final String NODE_ROUTINE = "ROUTINES";


    public DatabaseManager() {
        // Inicializamos y apuntamos al nodo raíz de la base de datos
        // Antes es necesario obtener la refencia a la bd en la nube
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    /**
     * Método para guardar una rutina en la base de datos
     *
     */

    // usuarioId vendrá de Firebase-auth...
    public void registrarRutina(String usuarioId, String ejercicio, int repeticiones) {

        // Creamos el hashmap, una colección de tipo clave-valor
        Map<String, Object> rutinaInfo = new HashMap<>();
        rutinaInfo.put(KEY_USER_ID, usuarioId);
        rutinaInfo.put(KEY_EXERCISE_CAMBER, ejercicio);
        rutinaInfo.put(KEY_REPS_CAMBER, repeticiones);

        // Buscamos el nodo rutina (se creará si no existe en la nube)
        // .push() -> Genera una clave única aleatoria para que los datos no se sobrescriban
        // .setValue() -> Escribe el mapa en ese nodo nuevo
        mDatabase.child(NODE_ROUTINE).push().setValue(rutinaInfo);
    }


    /*
     * Método para informar sobre rutinas finalizadas de otros usuarios con sus resultados
     * Se muestran todas las existentes
     */
    public void escucharRutina(final String userId) {

        mDatabase.child(NODE_ROUTINE).addChildEventListener(new ChildEventListener() {

            // Cuando se sube una nueva rutina...
            @Override

            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {

                // Obtenemos el ID del usuario que subió esta rutina específica
                String usuarioIdRutina = snapshot.child(KEY_USER_ID).getValue(String.class);

                if(usuarioIdRutina != null /*&& !usuarioIdRutina.equals(userId)*/) {
                    String ejercicio = snapshot.child(KEY_EXERCISE_CAMBER).getValue(String.class);
                    Long reps = snapshot.child(KEY_REPS_CAMBER).getValue(Long.class);
                    Log.d(MainActivity.TAG,"¡Un usuario completó " + reps + " de " + ejercicio);
                }

            }


            @Override

            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {

                // Requerido: Se activa si se edita una rutina existente

            }


            @Override

            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                // Requerido: Se activa si se elimina una rutina

            }


            @Override

            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {

                // Requerido: Se activa si cambia el orden o prioridad

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                // Requerido: Se activa si hay un error de conexión o permisos

                Log.d(MainActivity.TAG, "Error al escuchar la base de datos: " + error.getMessage());

            }

        });

    }

}

