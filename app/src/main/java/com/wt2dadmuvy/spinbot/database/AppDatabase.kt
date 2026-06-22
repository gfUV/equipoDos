package com.wt2dadmuvy.spinbot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.utils.Constants

/**
 * Base de datos principal de la aplicación usando Room (SQLite).
 *
 * ¿Qué es Room?
 * Room es una librería de Android que simplifica el uso de SQLite.
 * En lugar de escribir SQL manualmente, usamos anotaciones (@Entity, @Dao, @Database)
 * y Room genera todo el código necesario automáticamente al compilar.
 *
 * ¿Qué hace esta clase?
 * Es el punto de entrada a la base de datos. A través de ella se obtiene el DAO
 * (ChallengeDao) para poder hacer operaciones sobre la tabla de retos.
 *
 * @Database define:
 *   - entities: lista de tablas (Challenge → genera la tabla "Challenge" en SQLite)
 *   - version: versión del esquema de la BD. Si se modifica la estructura de una tabla,
 *              se debe aumentar este número y agregar una Migration.
 *   - exportSchema: false → no guarda el historial del esquema en un archivo JSON.
 */
@Database(entities = [Challenge::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Punto de acceso al DAO de retos.
     * A través de este método se obtienen todas las operaciones
     * disponibles sobre la tabla Challenge (insertar, listar, editar, eliminar).
     */
    abstract fun challengeDao(): ChallengeDao

    /**
     * Patrón Singleton: garantiza que solo exista UNA instancia de la base de datos
     * en toda la aplicación. Crear múltiples instancias de Room es costoso y puede
     * causar errores de concurrencia.
     *
     * ¿Cómo funciona?
     * - La primera vez que se llama getDatabase(), INSTANCE es null → crea la BD.
     * - Las siguientes veces, INSTANCE ya tiene valor → devuelve la misma instancia.
     * - @Volatile garantiza que todos los hilos vean el valor actualizado de INSTANCE.
     * - synchronized(this) evita que dos hilos creen la BD al mismo tiempo.
     */
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Callback que se ejecuta UNA SOLA VEZ cuando la base de datos se crea por primera vez.
         * Se usa para insertar los retos iniciales del juego.
         *
         * ¿Por qué execSQL y no el DAO?
         * En el momento en que onCreate se ejecuta, la instancia de Room aún no está
         * completamente lista, por lo que no podemos usar el DAO todavía.
         * execSQL nos permite ejecutar SQL directamente de forma segura en ese momento.
         *
         * HU 6 Criterio 6: los retos se insertan en orden ascendente (1, 2, 3)
         * pero el DAO los lista por id DESC, así que el Reto 3 aparecerá primero
         * en la pantalla (el más reciente arriba).
         */
        private val seedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Reto 1 — id más bajo → aparecerá de último en la lista (ORDER BY id DESC)
                db.execSQL(
                    "INSERT INTO Challenge (id, name, description) VALUES " +
                    "('1', 'Reto 1', 'Realiza un RecyclerView que liste una api de Pokemones')"
                )

                // Reto 2
                db.execSQL(
                    "INSERT INTO Challenge (id, name, description) VALUES " +
                    "('2', 'Reto 2', 'Crea un botón que al dar clic muestre una cuenta regresiva de 5 a 0')"
                )

                // Reto 3
                db.execSQL(
                    "INSERT INTO Challenge (id, name, description) VALUES " +
                    "('3', 'Reto 3', 'Crea un botón que al dar clic cambie de color y lance un mensaje emergente que diga \"Hola Mundo\"')"
                )

                // Retos adicionales para probar HU 6 Criterio 5 (scroll cuando la lista supera la pantalla)

                // Reto 4
                db.execSQL(
                    "INSERT INTO Challenge (id, name, description) VALUES " +
                    "('4', 'Reto 4', 'Implementa un Fragment con una barra de progreso que se llene automáticamente en 10 segundos')"
                )

                // Reto 5
                db.execSQL(
                    "INSERT INTO Challenge (id, name, description) VALUES " +
                    "('5', 'Reto 5', 'Crea una pantalla con un campo de texto y un botón que invierta el texto escrito al dar clic')"
                )

                // Reto 6 — id más alto → aparecerá primero en la lista (ORDER BY id DESC)
                db.execSQL(
                    "INSERT INTO Challenge (id, name, description) VALUES " +
                    "('6', 'Reto 6', 'Diseña un layout con 3 imágenes en forma de cuadrícula y al tocar cualquiera muestra su nombre en un Toast')"
                )
            }
        }

        /**
         * Obtiene la instancia única de la base de datos.
         * Si no existe, la crea con el nombre definido en Constants.NAME_BD ("app_data.db").
         * Al crearla por primera vez, el seedCallback inserta los 3 retos iniciales.
         *
         * @param context Contexto de la aplicación (se usa applicationContext para evitar
         *                fugas de memoria con Activity o Fragment contexts).
         * @return La instancia única de AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.NAME_BD  // Nombre del archivo: "app_data.db"
                )
                .addCallback(seedCallback)  // Inserta retos iniciales al crear la BD
                .fallbackToDestructiveMigration() // Evita crash si cambia el esquema (ej: id Int -> String)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
