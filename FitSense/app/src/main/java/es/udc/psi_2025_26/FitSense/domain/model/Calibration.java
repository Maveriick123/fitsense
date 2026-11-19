package es.udc.psi_2025_26.FitSense.domain.model;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;

// Parámetros con los que se detecta un ejercicio y si son del usuario o los de fábrica
public class Calibration {

    private final ExerciseType exercise;
    private final DetectorParams params;
    private final boolean custom;

    public Calibration(ExerciseType exercise, DetectorParams params, boolean custom) {
        this.exercise = exercise;
        this.params = params;
        this.custom = custom;
    }

    // Calibración de fábrica de un ejercicio
    public static Calibration defaults(ExerciseType exercise) {
        return new Calibration(exercise, exercise.defaultParams, false);
    }

    public ExerciseType getExercise() {
        return exercise;
    }

    public DetectorParams getParams() {
        return params;
    }

    // Retorna true si el usuario guardó una calibración propia para este ejercicio
    public boolean isCustom() {
        return custom;
    }
}
