package es.udc.psi_2025_26.FitSense;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi_2025_26.FitSense.databinding.ActivityRoutinesBinding;

public class RoutinesActivity extends AppCompatActivity {

    private RoutineAdapter adapter;
    private List<RoutineModel> routineList;
    private AuthManager authManager;
    private DatabaseManager dbManager;

    private ActivityRoutinesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRoutinesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 1. Inizializzazione dei gestori backend con el contexto (this)
        authManager = new AuthManager(this);
        dbManager = new DatabaseManager(this);

        // Inizializzazione della RecyclerView
        routineList = new ArrayList<>();
        binding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(this));

        // 2. Uso del constructor limpio del Adapter (sin strings)
        adapter = new RoutineAdapter(routineList);
        binding.recyclerViewRoutines.setAdapter(adapter);

        // Avvio dell'ascolto Firebase
        collegaDatabase();
    }

    private void collegaDatabase() {
        // Otteniamo l'utente direttamente da Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";

        // Chiamata al metodo di Daniel
        dbManager.escucharRutina(currentUserId);

        // Popolamento automatico della RecyclerView
        DatabaseReference routinesRef = FirebaseDatabase.getInstance().getReference(DatabaseManager.NODE_ROUTINE);
        routinesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String ejercicio = snapshot.child(DatabaseManager.KEY_EXERCISE_CAMBER).getValue(String.class);
                Long reps = snapshot.child(DatabaseManager.KEY_REPS_CAMBER).getValue(Long.class);

                if (ejercicio != null && reps != null) {
                    // 3. Uso del string resource para el sufijo " reps"
                    String repsString = reps + getString(R.string.unidad_reps);
                    RoutineModel routine = new RoutineModel(ejercicio, repsString);

                    routineList.add(routine);
                    adapter.notifyItemInserted(routineList.size()-1);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoutinesActivity.this, getString(R.string.error_general) + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // Logout diretto con Firebase
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, R.string.log_out, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_new_routine) {
            // CONEXIÓN CON EL CÓDIGO DE PABLO
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.USER_ID, authManager.getCurrentUserId());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}