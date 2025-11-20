package com.fahad.i210394.profiledb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.graphics.BitmapFactory

data class Profile(
    val id: Int,
    val username: String,
    val picture: String
)


class ProfileDBHelper(context: Context) : SQLiteOpenHelper(context, "ProfilesDB", null, 1) {

    companion object {
        private const val TABLE_NAME = "profiles"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY,
                username TEXT,
                picture TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
    fun getProfileById(id: Int): Profile? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM profiles WHERE id = ?", arrayOf(id.toString()))
        return if (cursor.moveToFirst()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val imageBlob = cursor.getString(cursor.getColumnIndexOrThrow("picture"))
            Profile(id, username, imageBlob)
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun insertProfile(profile: Profile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", profile.id)
            put("username", profile.username)
            put("picture", profile.picture)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getAllProfiles(): List<Profile> {
        val profileList = mutableListOf<Profile>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM profiles", null)

        if (cursor.moveToFirst()) {
            do {
                val profile = Profile(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    picture = cursor.getString(cursor.getColumnIndexOrThrow("picture"))
                )
                profileList.add(profile)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return profileList
    }

}
