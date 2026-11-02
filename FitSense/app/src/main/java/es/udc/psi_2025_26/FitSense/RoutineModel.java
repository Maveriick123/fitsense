package es.udc.psi_2025_26.FitSense;

public class RoutineModel {
    private String name;
    private String difficulty;

    public RoutineModel() {
        // Costruttore vuoto richiesto da Firebase
    }

    public RoutineModel(String name, String difficulty) {
        this.name = name;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}