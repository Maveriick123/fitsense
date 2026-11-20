package es.udc.psi_2025_26.FitSense.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.udc.psi_2025_26.FitSense.data.dto.RoutineDto;
import es.udc.psi_2025_26.FitSense.data.mapper.RoutineDtoMapper;
import es.udc.psi_2025_26.FitSense.domain.exception.DataUnavailableException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;

// Implementación del puerto RoutineRepository con Firebase Realtime Database
// El historial se escucha en tiempo real con un ChildEventListener: Firebase manda primero
// las rutinas que ya hay y después cada una nueva, en el hilo principal
// JSON -> DTO -> entidad, y los errores de Firebase -> DataUnavailableException
public class RoutineRepositoryImp implements RoutineRepository {

    private final DatabaseReference mRoutinesRef;
    private ChildEventListener mFirebaseListener;

    public RoutineRepositoryImp(FirebaseDatabase database) {
        mRoutinesRef = database.getReference(RoutineDto.NODE_ROUTINES);
    }

    @Override
    public void saveRoutine(Routine routine) {
        // No se espera a que se escriba, sin red Firebase lo encola y lo sincroniza después
        mRoutinesRef.push().setValue(RoutineDtoMapper.toMap(RoutineDtoMapper.toDto(routine)));
    }

    @Override
    public void setListener(Listener listener) {
        if (mFirebaseListener != null) {
            mRoutinesRef.removeEventListener(mFirebaseListener);
            mFirebaseListener = null;
        }
        if (listener == null) return;

        mFirebaseListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Routine routine = RoutineDtoMapper.toEntity(toDto(snapshot));
                if (routine != null) {
                    listener.onRoutineAdded(routine);
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
                listener.onError(new DataUnavailableException(error.getMessage(), error.toException()));
            }
        };
        mRoutinesRef.addChildEventListener(mFirebaseListener);
    }

    private static RoutineDto toDto(DataSnapshot node) {
        return new RoutineDto(
                node.child(RoutineDto.KEY_USER_ID).getValue(String.class),
                node.child(RoutineDto.KEY_EXERCISE).getValue(String.class),
                node.child(RoutineDto.KEY_REPS).getValue(Long.class));
    }
}
