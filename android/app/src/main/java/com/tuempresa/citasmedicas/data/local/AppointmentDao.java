package com.tuempresa.citasmedicas.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO de citas. Devuelve LiveData para observabilidad automática en la UI
 * (lecturas locales, sin coste de Firebase).
 */
@Dao
public interface AppointmentDao {

    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    LiveData<List<AppointmentEntity>> observeAll();

    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    List<AppointmentEntity> getAllOnce();

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    AppointmentEntity getById(long id);

    @Insert
    long insert(AppointmentEntity appointment);

    /** Inserta un lote (usado al sincronizar desde Firestore). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AppointmentEntity> appointments);

    @Delete
    void delete(AppointmentEntity appointment);

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    void updateStatus(long id, String status);

    /** Borra sólo las filas sincronizadas con Firestore (conserva las locales). */
    @Query("DELETE FROM appointments WHERE remote_id IS NOT NULL")
    void clearRemote();

    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'Confirmed'")
    int countConfirmed();
}
