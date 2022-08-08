package uz.gita.vocabulary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.words
import kotlinx.android.synthetic.main.activity_star.*
import uz.gita.vocabulary.adapters.WordsAdapter
import uz.gita.vocabulary.data.repository.WordsRepository
import java.util.*

class StarActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val repository = WordsRepository.getRepository()
    private lateinit var adapter: WordsAdapter
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star)
        tts = TextToSpeech(this, this)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = ContextCompat.getColor(this, R.color.status)

        adapter = WordsAdapter(repository.favourite(), "")
        words.adapter = adapter
        words.layoutManager = LinearLayoutManager(this)
        empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        adapter.setFavouriteClickListener {
            repository.update(it)
            adapter.cursor = repository.favourite()
            adapter.notifyDataSetChanged()
            empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
        adapter.setItemClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.apply {
                setTitle(it.word)
                setCancelable(false)
                setMessage(it.translate)
                setPositiveButton("OK") { dialog, _ ->
                    dialog.cancel()
                }
                setNegativeButton("Copy") { d, _ ->
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("text", it.translate)
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(this@StarActivity, "Text copied to clipboard", Toast.LENGTH_LONG)
                        .show()
                }
                setNeutralButton("Listen") { d, _ ->
                    tts!!.speak(it.word, TextToSpeech.QUEUE_FLUSH, null, "")
//                    val sendIntent: Intent = Intent().apply {
//                        action = Intent.ACTION_SEND
//                        putExtra(Intent.EXTRA_TEXT, it.translate)
//                        type = "text/plain"
//                    }

//                    val shareIntent = Intent.createChooser(sendIntent, null)
//                    startActivity(shareIntent)
                }
            }.create().show()
        }
        back.setOnClickListener {
            setResult(2)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(2)
    }

    override fun onInit(status: Int) {
        tts?.setPitch(1.1f); // saw from internet
        tts?.setSpeechRate(0.4f); // f denotes float, it actually type casts 0.5 to float
        if (status == TextToSpeech.SUCCESS) {
            var result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(
                    this,
                    "The lenguage specificated is not supported ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
//                binding.listening.isEnabled = true
            }
        }
    }
}