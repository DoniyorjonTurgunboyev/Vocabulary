package uz.gita.vocabulary.adapters

import android.database.Cursor
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.word_item.view.*
import uz.gita.vocabulary.R
import uz.gita.vocabulary.app.App
import uz.gita.vocabulary.data.model.Word

class WordsAdapter(var cursor: Cursor, var query: String) :
    RecyclerView.Adapter<WordsAdapter.VH>() {
    private var clickItem: ((Word) -> Unit)? = null
    private var clickFavourite: ((Word) -> Unit)? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                clickItem?.invoke(wordByPosition(adapterPosition))
//                itemView.expandable_layout.toggle()
            }
            itemView.star.setOnClickListener {
                val word = wordByPosition(adapterPosition)
                it.star.setImageResource(if (word.favourite == 1) R.drawable.ic_star_out else R.drawable.ic_star)
                clickFavourite?.invoke(word)
            }
        }

        fun bind(word: Word) {
            val spannable = SpannableString(word.word)
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(App.instance, R.color.red))
            val startIndex = word.word.indexOf(query, 0, true)
            val endIndex = startIndex + query.length
            spannable.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            itemView.word.text = spannable
            itemView.translate.text = word.translate
            itemView.star.setImageResource(if (word.favourite == 1) R.drawable.ic_star else R.drawable.ic_star_out)
        }
    }

    fun wordByPosition(position: Int): Word {
        cursor.moveToPosition(position)
        return Word(
            cursor.getInt(cursor.getColumnIndex("id")),
            cursor.getString(cursor.getColumnIndex("word")),
            cursor.getString(cursor.getColumnIndex("definition")),
            cursor.getInt(cursor.getColumnIndex("favourite")),
        )
    }

    fun setItemClickListener(f: ((Word) -> Unit)) {
        clickItem = f
    }

    fun setFavouriteClickListener(f: ((Word) -> Unit)) {
        clickFavourite = f
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.word_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(wordByPosition(position))

    override fun getItemCount() = cursor.count
}