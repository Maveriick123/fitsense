package es.udc.psi_2025_26.FitSense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    // Lista che conterrà i modelli delle routine
    private List<RoutineModel> routines = new ArrayList<>();

    // Costruttore vuoto
    public RoutineAdapter() {
    }

    // Costruttore che accetta solo la lista (más limpio)
    public RoutineAdapter(List<RoutineModel> routines) {
        this.routines = routines;
    }

    // Metodo per aggiornare la lista quando arrivano dati da Firebase
    public void updateData(List<RoutineModel> newRoutines) {
        this.routines.clear();
        this.routines.addAll(newRoutines);
        notifyItemRangeChanged(0, newRoutines.size());
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        RoutineModel routine = routines.get(position);

        holder.tvRoutineName.setText(routine.getName());

        // Obtenemos el texto directamente desde el contexto de la vista
        String prefix = holder.itemView.getContext().getString(R.string.numero_repeticiones);
        holder.tvRoutineDifficulty.setText(prefix + routine.getDifficulty());
    }

    @Override
    public int getItemCount() {
        return routines != null ? routines.size() : 0;
    }

    // Classe ViewHolder per mappare gli elementi visivi
    public static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoutineName;
        TextView tvRoutineDifficulty;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoutineName = itemView.findViewById(R.id.tvRoutineName);
            tvRoutineDifficulty = itemView.findViewById(R.id.tvRoutineDifficulty);
        }
    }
}