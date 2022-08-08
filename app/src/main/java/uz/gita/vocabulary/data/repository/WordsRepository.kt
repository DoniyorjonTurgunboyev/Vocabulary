package uz.gita.vocabulary.data.repository

import uz.gita.vocabulary.data.storage.MyDatabase
import uz.gita.vocabulary.data.model.Word
import uz.gita.vocabulary.data.storage.SharedPref

class WordsRepository private constructor() {
    private val database = MyDatabase.getAppDatabase()
    private val storage = SharedPref.getPref()

    companion object {
        private lateinit var instance: WordsRepository

        fun getRepository(): WordsRepository {
            if (!::instance.isInitialized) {
                instance = WordsRepository()
            }
            return instance
        }
    }

    init {
        if (storage.isFirst) {
            storage.isFirst = false
        }
    }

    fun word(query: String) = database.getAllDictionaryData(query)
    fun update(word: Word) = database.update(word)
    fun favourite() = database.favouriteWords()
}