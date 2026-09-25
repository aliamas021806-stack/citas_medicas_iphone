package com.tuempresa.citasmedicas.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos local Room (SQLite) para las citas médicas.
 */
@Database(entities = {AppointmentEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AppointmentDao appointmentDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "citas_medicas.db")
                            .fallbackToDestructiveMigration()
                            .addCallback(SEED_CALLBACK)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /** Inserta citas de ejemplo la primera vez que se crea la base de datos. */
    private static final RoomDatabase.Callback SEED_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                AppointmentDao dao = INSTANCE.appointmentDao();
                // Misma fuente de datos que la siembra de Firestore: evita
                // divergencias entre el modo local y el modo Firebase.
                java.util.List<com.tuempresa.citasmedicas.model.Appointment> seeds =
                        com.tuempresa.citasmedicas.data.mock.MockData.getSeedAppointments();
                for (com.tuempresa.citasmedicas.model.Appointment a : seeds) {
                    AppointmentEntity e =
                            com.tuempresa.citasmedicas.util.AppointmentMapper.toEntity(a);
                    e.id = 0;
                    dao.insert(e);
                }
            });
        }
    };
}
