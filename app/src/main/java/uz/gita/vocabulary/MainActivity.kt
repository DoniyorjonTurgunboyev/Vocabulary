package uz.gita.vocabulary

import android.app.Activity
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import uz.gita.vocabulary.adapters.WordsAdapter
import uz.gita.vocabulary.data.repository.WordsRepository
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val repository = WordsRepository.getRepository()
    private var query1 = ""
    private lateinit var adapter: WordsAdapter
    private lateinit var handler: Handler

    //    private lateinit var
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, this)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = ContextCompat.getColor(this, R.color.status)
        adapter = WordsAdapter(repository.word(query1), query1)
        words.adapter = adapter
        words.layoutManager = LinearLayoutManager(this)
//        speech.visibility = if (MediaUtil.getMicrophoneAvailable(this)) View.VISIBLE else View.GONE
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        if (am.mode == AudioManager.MODE_NORMAL) {
            speech.visibility = View.VISIBLE
        }
        adapter.setFavouriteClickListener {
            repository.update(it)
            adapter.cursor = repository.word(query1)
            adapter.notifyDataSetChanged()
        }
        speech.setOnClickListener {
            // Get the Intent action
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Adding an extra language, you can use any language from the Locale class.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Text that shows up on the Speech input prompt.
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
            try {
                // Start the intent for a result, and pass in our request code.
                startActivityForResult(sttIntent, REQUEST_CODE_STT)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()
                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
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
                    Toast.makeText(this@MainActivity, "Text copied to clipboard", Toast.LENGTH_LONG)
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
        stars.setOnClickListener {
            startActivityForResult(Intent(this, StarActivity::class.java), 1)
        }
        handler = Handler(Looper.getMainLooper())
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                handler.removeCallbacksAndMessages(null)
                query?.let {
                    query1 = it.trim().replace("'", "")
                    val cursor = repository.word(query1)
                    adapter.cursor = cursor
                    adapter.query = query1
                    adapter.notifyDataSetChanged()
                    empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                    search.setQuery(query1, false)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    newText?.let {
                        query1 = it.trim().replace("'", "")
                        adapter.cursor = repository.word(query1)
                        adapter.query = query1
                        adapter.notifyDataSetChanged()
                        empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                        search.setQuery(query1, false)
                    }
                }, 500)
                return true
            }
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            REQUEST_CODE_STT -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        // Recognized text is in the first position.
                        val recognizedText = result[0].replace("'", " a")
                        // Do what you want with the recognized text.
                        search.setQuery(recognizedText, true)
                    }
                }
            }
            2 -> {
                adapter.cursor = repository.word(query1)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
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

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_CODE_STT = 1
    }

}