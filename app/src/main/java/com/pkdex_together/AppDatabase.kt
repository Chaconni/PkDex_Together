package com.pkdex_together

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pokedex.db"
        private const val DATABASE_VERSION = 2

        // Tablas
        const val TABLE_USERS = "Usuarios"
        const val TABLE_FAVORITES = "Favoritos"

        // Columnas Usuarios
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"

        // Columnas Favoritos
        const val COL_ID = "id_favoritos"
        const val COL_USER_EMAIL = "user_email"
        const val COL_POKEMON_ID = "id_pokemon"

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_EMAIL TEXT PRIMARY KEY,
                $COL_PASSWORD TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_FAVORITES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_EMAIL TEXT NOT NULL,
                $COL_POKEMON_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_USER_EMAIL) REFERENCES $TABLE_USERS($COL_EMAIL)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun insertUser(email: String, password: String) {
        val db = writableDatabase
        db.execSQL("INSERT INTO $TABLE_USERS ($COL_EMAIL, $COL_PASSWORD) VALUES (?, ?)", arrayOf(email, password))
    }

    fun insertFavorite(userEmail: String, pokemonId: Int) {
        val db = writableDatabase
        db.execSQL("INSERT INTO $TABLE_FAVORITES ($COL_USER_EMAIL, $COL_POKEMON_ID) VALUES (?, ?)", arrayOf(userEmail, pokemonId))
    }

    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL = ? AND $COL_PASSWORD = ?", arrayOf(email, password))
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }

    fun getFavorites(userEmail: String): List<Int> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_POKEMON_ID FROM $TABLE_FAVORITES WHERE $COL_USER_EMAIL = ?", arrayOf(userEmail))

        val favorites = mutableListOf<Int>()
        while (cursor.moveToNext()) {
            favorites.add(cursor.getInt(cursor.getColumnIndexOrThrow(COL_POKEMON_ID)))
        }
        cursor.close()
        return favorites
    }

    fun isFavorite(userEmail: String, pokemonId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(" SELECT 1 FROM $TABLE_FAVORITES WHERE $COL_USER_EMAIL = ? AND $COL_POKEMON_ID = ? ", arrayOf(userEmail, pokemonId.toString() ) )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun removeFavorite(userEmail: String, pokemonId: Int) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_FAVORITES WHERE $COL_USER_EMAIL = ? AND $COL_POKEMON_ID = ?", arrayOf(userEmail, pokemonId.toString() ) )
    }


}
