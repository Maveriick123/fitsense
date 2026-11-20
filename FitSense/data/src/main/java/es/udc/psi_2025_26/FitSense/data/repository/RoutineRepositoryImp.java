package es.udc.psi_2025_26.FitSense.data.repository;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import es.udc.psi_2025_26.FitSense.data.dto.RoutineDto;
import es.udc.psi_2025_26.FitSense.data.mapper.RoutineDtoMapper;
import es.udc.psi_2025_26.FitSense.domain.exception.DataUnavailableException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;

// Implementación del puerto RoutineRepository con Firebase Realtime Database
// JSON -> DTO -> entidad, y los errores de Firebase -> DataUnavailableException
public class RoutineRepositoryImp implements RoutineRepository {

    private static final long TIMEOUT_S = 30;

    private final DatabaseReference mRoutinesRef;

    public RoutineRepositoryImp(FirebaseDatabase database) {
        mRoutinesRef = database.getReference(RoutineDto.NODE_ROUTINES);
    }

    @Override
    public void saveRoutine(Routine routine) {
        // No se espera a que se escriba, sin red Firebase lo encola y lo sincroniza después
        mRoutinesRef.push().setValue(RoutineDtoMapper.toMap(RoutineDtoMapper.toDto(routine)));
    }

    @Override
    public List<Routine> getRoutines(String userId) throws DataUnavailableException {
        DataSnapshot snapshot;
        try {
            snapshot = Tasks.await(
                    mRoutinesRef.orderByChild(RoutineDto.KEY_USER_ID).equalTo(userId).get(),
                    TIMEOUT_S, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new DataUnavailableException(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DataUnavailableException(e.getMessage(), e);
        }

        List<Routine> routines = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Routine routine = RoutineDtoMapper.toEntity(toDto(child));
            if (routine != null) {
                routines.add(routine);
            }
        }
        return routines;
    }

    private static RoutineDto toDto(DataSnapshot node) {
        return new RoutineDto(
                node.child(RoutineDto.KEY_USER_ID).getValue(String.class),
                node.child(RoutineDto.KEY_EXERCISE).getValue(String.class),
                node.child(RoutineDto.KEY_REPS).getValue(Long.class));
    }
}
