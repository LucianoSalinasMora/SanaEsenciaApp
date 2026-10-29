package com.sanaesencia.app.data.local

import com.sanaesencia.app.data.dao.AboutDao
import com.sanaesencia.app.data.dao.AppointmentDao
import com.sanaesencia.app.data.dao.ProductDao
import com.sanaesencia.app.data.dao.ReviewDao
import com.sanaesencia.app.data.dao.UserDao
import com.sanaesencia.app.data.model.AboutInfoEntity
import com.sanaesencia.app.data.model.AppointmentEntity
import com.sanaesencia.app.data.model.ProductEntity
import com.sanaesencia.app.data.model.ReviewEntity
import com.sanaesencia.app.data.model.UserEntity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// ==========================================
// Base de Datos Principal Room de Sanaesencia
// ==========================================
@Database(
    entities = [
        AppointmentEntity::class,
        UserEntity::class,
        ProductEntity::class,
        ReviewEntity::class,
        AboutInfoEntity::class,
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ------------------------------------------
    // Métodos Abstractos de Acceso a DAOs
    // ------------------------------------------
    abstract fun appointmentDao(): AppointmentDao
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun reviewDao(): ReviewDao
    abstract fun aboutDao(): AboutDao

    // ------------------------------------------
    // Patrón Singleton para la Instancia de la Base de Datos
    // ------------------------------------------
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Retorna la instancia única sincronizada de la base de datos
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sanaesencia_db",
                )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
