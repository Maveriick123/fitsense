package es.udc.psi_2025_26.FitSense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RoutinesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RoutineAdapter adapter;
    private List<RoutineModel> routineList;
    private AuthManager authManager;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);

        // Inizializzazione dei gestori backend
        authManager = new AuthManager();
        dbManager = new DatabaseManager();

        // Inizializzazione della RecyclerView
        routineList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewRoutines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoutineAdapter(routineList);
        recyclerView.setAdapter(adapter);

        // Avvio dell'ascolto Firebase
        collegaDatabase();
    }

    private void collegaDatabase() {
        // Otteniamo l'utente direttamente da Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";

        // 1. Chiamata al metodo di Daniel
        dbManager.escucharRutina(currentUserId);

        // 2. Popolamento automatico della RecyclerView
        DatabaseReference routinesRef = FirebaseDatabase.getInstance().getReference(DatabaseManager.NODE_ROUTINE);
        routinesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String ejercicio = snapshot.child(DatabaseManager.KEY_EXERCISE_CAMBER).getValue(String.class);
                Long reps = snapshot.child(DatabaseManager.KEY_REPS_CAMBER).getValue(Long.class);

                if (ejercicio != null && reps != null) {
                    RoutineModel routine = new RoutineModel(ejercicio, reps + " reps");
                    routineList.add(routine);
                    adapter.notifyItemInserted(routineList.size() - 1);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoutinesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (id == R.id.action_login || id == R.id.action_register) {
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            // Logout diretto con Firebase
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_new_routine) {
            // CONEXIÓN CON EL CÓDIGO DE PABLO
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}