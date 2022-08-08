package uz.gita.vocabulary.data.storage

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import uz.gita.vocabulary.app.App
import uz.gita.vocabulary.data.model.Word

class MyDatabase private constructor(context: Context) : DBHelper(context, "Dictionary.db", 1) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: MyDatabase

        fun getAppDatabase(): MyDatabase {
            if (!Companion::instance.isInitialized) {
                instance = MyDatabase(App.instance)
            }
            return instance
        }
    }

    fun getAllDictionaryData(query: String): Cursor {
        val query = "SELECT * FROM words WHERE words.word LIKE '%$query%'"
        return instance.database().rawQuery(query, null)
    }

    fun favouriteWords(): Cursor {
        return instance.database().rawQuery("SELECT * FROM words WHERE words.favourite = 1", null)
    }

    fun update(data: Word) {
        val cv = ContentValues()
        if (data.favourite == 0) cv.put("favourite", 1)
        else cv.put("favourite", 0)
        instance.database().update("words", cv, "words.id = ${data.id}", null)
    }
}