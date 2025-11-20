package com.fahad.i210394.StoriesDB
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Data model for a story record
data class Story(
    val id: Int,
    val prfPic: String,
    val storyPic: String
)

class StoryDbHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stories.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_STORIES = "stories"
        private const val COL_ID = "id"
        private const val COL_PRF_PIC = "prfpic"
        private const val COL_STORY_PIC = "story_pic"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_STORIES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PRF_PIC TEXT,
                $COL_STORY_PIC TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STORIES")
        onCreate(db)
    }

    /**
     * Inserts a new story into the database.
     * @return the new row ID, or -1 on error
     */
    fun insertStory(prfPic: String, storyPic: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PRF_PIC, prfPic)
            put(COL_STORY_PIC, storyPic)
        }
        return db.insert(TABLE_STORIES, null, values).also { db.close() }
    }

    /**
     * Retrieves all stories, ordered by latest first.
     */
    fun getAllStories(): List<Story> {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_STORIES,
            arrayOf(COL_ID, COL_PRF_PIC, COL_STORY_PIC),
            null, null, null, null,
            "$COL_ID DESC"
        )

        val list = mutableListOf<Story>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COL_ID))
                val prf = getString(getColumnIndexOrThrow(COL_PRF_PIC))
                val story = getString(getColumnIndexOrThrow(COL_STORY_PIC))
                list += Story(id, prf, story)
            }
            close()
        }
        db.close()
        return list
    }

    /**
     * Retrieves a single story by its ID, or null if not found.
     */
    fun getStoryById(id: Int): Story? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STORIES,
            arrayOf(COL_ID, COL_PRF_PIC, COL_STORY_PIC),
            "$COL_ID = ?",
            arrayOf(id.toString()), null, null, null
        )
        val story: Story? = if (cursor.moveToFirst()) {
            Story(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                prfPic = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRF_PIC)),
                storyPic = cursor.getString(cursor.getColumnIndexOrThrow(COL_STORY_PIC))
            )
        } else {
            null
        }
        cursor.close()
        db.close()
        return story
    }

    fun clearAllStories() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_STORIES")
        db.close()
    }

}
