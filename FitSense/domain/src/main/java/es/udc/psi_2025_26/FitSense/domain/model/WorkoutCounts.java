package es.udc.psi_2025_26.FitSense.domain.model;

// Contadores de la rutina en curso, uno por ejercicio. No se puede modificar
public final class WorkoutCounts {

    private final int[] counts;

    // counts tiene el contador de cada ejercicio en la posición de su ordinal()
    public WorkoutCounts(int[] counts) {
        this.counts = new int[ExerciseType.values().length];
        if (counts != null) {
            System.arraycopy(counts, 0, this.counts, 0, Math.min(counts.length, this.counts.length));
        }
    }

    public static WorkoutCounts empty() {
        return new WorkoutCounts(null);
    }

    public int get(ExerciseType type) {
        return counts[type.ordinal()];
    }

    public boolean isEmpty() {
        for (int c : counts) {
            if (c > 0) return false;
        }
        return true;
    }
}
