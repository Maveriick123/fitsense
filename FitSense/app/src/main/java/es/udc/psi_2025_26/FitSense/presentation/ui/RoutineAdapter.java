package es.udc.psi_2025_26.FitSense.presentation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.databinding.ItemRoutineBinding;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.RoutineUiModel;

// Adaptador del historial de rutinas
public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private final List<RoutineUiModel> mRoutines = new ArrayList<>();

    // Añade una rutina al final de la lista
    public void addRoutine(RoutineUiModel routine) {
        mRoutines.add(routine);
        notifyItemInserted(mRoutines.size() - 1);
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoutineBinding binding = ItemRoutineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RoutineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        holder.bind(mRoutines.get(position));
    }

    @Override
    public int getItemCount() {
        return mRoutines.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {

        private final ItemRoutineBinding mBinding;

        RoutineViewHolder(@NonNull ItemRoutineBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        // ViewHolder con lo necesario para pintar una fila
        void bind(RoutineUiModel routine) {
            Context context = itemView.getContext();
            if (routine.exerciseNameRes != 0) {
                mBinding.tvRoutineName.setText(routine.exerciseNameRes);
            } else {
                mBinding.tvRoutineName.setText(routine.exerciseName);
            }
            mBinding.tvRoutineReps.setText(
                    context.getString(R.string.routine_repetitions, routine.repetitions));
        }
    }
}
