package com.example.translationai

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.translationai.TargetLanguageFragment.Companion.targetLanguage
import com.example.translationai.databinding.FragmentMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLEncoder
import java.util.Locale
import android.Manifest


class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    var translated: String = ""
    lateinit var job: Job
    private var currentLanguage: String? = null
    private lateinit var startAnimation: Animation
    private lateinit var stopAnimation: Animation
    private val GALLERY_REQUEST_CODE = 1002
    private var selectedImageUri: Uri? = null
    private var imgUri: Intent? = null
    private val CAMERA_REQUEST_CODE = 1001
    private val AUDIO_REQUEST_CODE = 1002

    lateinit var viewModel: HistoryViewModel
    companion object{
        val listOfLanguages = listOf(
        "Африкаанс", "Албанский", "Амхарский", "Английский", "Армянский", "Арабский", "Баскский", "Белорусский",
        "Болгарский", "Боснийский", "Венгерский", "Вьетнамский", "Греческий", "Грузинский", "Датский", "Еврейский",
        "Индонезийский", "Ирландский", "Испанский", "Итальянский", "Казахский", "Каталонский", "Китайский (упрощенный)",
        "Китайский (традиционный)", "Корейский", "Курдский", "Латвийский", "Литовский", "Македонский", "Малайский",
        "Молдавский", "Немецкий", "Норвежский", "Персидский", "Польский", "Португальский", "Русский", "Сербский",
        "Словакский", "Словенский", "Таджикский", "Турецкий", "Узбекский", "Украинский", "Финский", "Французский",
        "Хорватский", "Чешский", "Шведский", "Шотландский (гэльский)", "Эстонский")
    }

    @SuppressLint("ResourceAsColor", "RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        checkPermissions()
        startAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.btn_animation_for_start)
        stopAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.btn_animation_for_stop)
        val navController = findNavController()
        val dao = ResultDatabase.getInstance(requireContext()).dao
        val viewModelFactory = HistoryViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HistoryViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Нельзя вернуться назад с этого экрана", Toast.LENGTH_SHORT).show()
        }
        if (savedInstanceState != null) {
            currentLanguage = savedInstanceState.getString("currentLanguage")
            currentLanguage = savedInstanceState.getString("targetLanguage")
        }
        arguments?.let { bundle ->
            currentLanguage = bundle.getString("CURRENT_LANGUAGE").toString()
        }
        if(currentLanguage != null) binding.btnCurrentLanguageText.text = currentLanguage
        else binding.btnCurrentLanguageText.text = "Английский"
        if(targetLanguage != null) binding.btnTargetLanguageText.text = targetLanguage
        else binding.btnTargetLanguageText.text = "Русский"


        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if(binding.etCurrentText.text.toString() != "" || translated != "") {
                    binding.tvTranslateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    binding.tvTranslateText.text = translated
                } else if(binding.etCurrentText.text.toString() == "" || translated == "") {
                    binding.tvTranslateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.startsTextColor))
                    binding.tvTranslateText.text = "Перевод"
                    binding.imageView.visibility = View.GONE
                }
                delay(200)
            }
        }

        binding.etCurrentText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                translateText(s.toString(), targetAbbr(binding.btnTargetLanguageText.text.toString()))
            }

        })

        binding.btnCurrentLanguageText.setOnClickListener {
            val bundle = Bundle().apply {
                putString("CURRENT_LANGUAGE_MAIN", binding.btnCurrentLanguageText.text.toString())
            }
            navController.navigate(R.id.action_mainFragment_to_currentLanguageFragment, bundle)
        }
        binding.btnTargetLanguageText.setOnClickListener {
            val bundle = Bundle().apply {
                putString("CURRENT_LANGUAGE_MAIN_TO_TARGET", binding.btnTargetLanguageText.text.toString())
            }
            navController.navigate(R.id.action_mainFragment_to_targetLanguageFragment, bundle)
        }

        binding.imgBtnVoiceTranslate.setOnClickListener {
            binding.imageView.visibility = View.GONE
            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer.startListening(recognizerIntent)
            binding.imgBtnVoiceTranslate.startAnimation(startAnimation)
            if(binding.etCurrentText.text.toString() == "")
                translated = ""
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                }
                override fun onBeginningOfSpeech() {
                }
                override fun onRmsChanged(rmsdB: Float) {
                }
                override fun onBufferReceived(buffer: ByteArray?) {
                }
                override fun onEndOfSpeech() {
                }
                override fun onError(error: Int) {
                }
                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.let {
                        val spokenText = it[0]
                        binding.etCurrentText.setText(spokenText)
                        binding.tvTranslateText.text = translated
                        binding.imgBtnVoiceTranslate.startAnimation(stopAnimation)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                }
                override fun onEvent(eventType: Int, params: Bundle?) {
                }
            })
        }

        binding.imgBtnPhotoTranslate.setOnClickListener {
            binding.imgBtnPhotoTranslate.startAnimation(startAnimation)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imgUri = intent
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        binding.imgBtnClearTranslate.setOnClickListener {
            binding.imageView.visibility = View.GONE
            binding.etCurrentText.setText("")
        }

        binding.imgBtnCopyTranslate.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Перевод скопирован", binding.tvTranslateText.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        binding.imgBtnChangeLanguages.setOnClickListener {
            val tmpLanguage = binding.btnCurrentLanguageText.text
            binding.btnCurrentLanguageText.text = binding.btnTargetLanguageText.text
            binding.btnTargetLanguageText.text = tmpLanguage
            binding.etCurrentText.setText(translated)
            if(binding.imageView.visibility == View.VISIBLE) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if(selectedImageUri != null) {
                        Glide.with(binding.imageView)
                            .asBitmap()
                            .load(selectedImageUri)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    val finalBitmap = drawTextOnBitmap(resource, translated)
                                    binding.imageView.setImageBitmap(finalBitmap)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                }
                            })
                    }
                },10000)
            }
        }

        binding.imgBtnHistoryOfTranslate.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_historyFragment)
        }

        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data?.data != null) {
            selectedImageUri = data.data!!
            binding.imgBtnPhotoTranslate.startAnimation(stopAnimation)
            selectedImageUri.let {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizeText(inputImage)
                Handler(Looper.getMainLooper()).postDelayed({
                    if(selectedImageUri != null) {
                        binding.imageView.visibility = View.VISIBLE
                        Glide.with(binding.imageView)
                            .asBitmap()
                            .load(selectedImageUri)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    val finalBitmap = drawTextOnBitmap(resource, translated)
                                    binding.imageView.setImageBitmap(finalBitmap)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                }
                            })
                    }
                },2000)
            }
        }
    }

    private fun recognizeText(image: InputImage) {
        val options = TextRecognizerOptions.Builder()
            .build()
        val recognizer = TextRecognition.getClient(options)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                binding.etCurrentText.setText(recognizedText)
            }
            .addOnFailureListener { e ->
            }
    }

    private fun drawTextOnBitmap(bitmap: Bitmap, text: String): Bitmap {
        val editedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(editedBitmap)
        val paint = Paint()

        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, editedBitmap.width.toFloat(), editedBitmap.height.toFloat(), paint)

        val textPaint = TextPaint()
        textPaint.color = Color.BLACK
        var textSize = 26f
        textPaint.textSize = textSize

        val maxWidth = 950
        var staticLayout = StaticLayout(text, textPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)

        while (staticLayout.height > editedBitmap.height - 20) {
            textSize -= 1f
            textPaint.textSize = textSize
            staticLayout = StaticLayout(text, textPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
        }

        canvas.save()
        canvas.translate(0f, 0f)
        staticLayout.draw(canvas)
        canvas.restore()

        return editedBitmap
    }




    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString("currentLanguage", currentLanguage)
        savedInstanceState.putString("targetLanguage", targetLanguage)
        super.onSaveInstanceState(savedInstanceState)
    }

    fun translateText(text: String, targetLanguage: String) {
        val client = OkHttpClient()

        val urlEncodedText = URLEncoder.encode(text, "UTF-8")
        val requestUrl = "https://translate.google.com/m?hl=$targetLanguage&q=$urlEncodedText"
        val request = Request.Builder()
            .url(requestUrl)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @SuppressLint("ResourceAsColor")
            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
                    val responseData = response.body?.string()
                    translated = extractTranslation(responseData)

                    if(binding.etCurrentText.text.toString() != "") {
                        val result = ResultOfTranslate()
                        result.translateText = translated
                        result.currentText = binding.etCurrentText.text.toString()
                        result.targetAbbrCode = binding.btnTargetLanguageText.text.toString()
                        viewModel.insertNew(result)
                    }
                }
            }

        })
    }

    fun extractTranslation(html: String?): String {
        val doc = Jsoup.parse(html)
        val translatedText = doc.select("div.result-container").text()
        return translatedText
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun targetAbbr(text: String): String {
        when(text) {
            "Африкаанс" -> return "af"
            "Албанский" -> return "sq"
            "Арабский" -> return "ar"
            "Армянский" -> return "hy"
            "Английский" -> return "en"
            "Астонский" -> return "et"
            "Баскский" -> return "eu"
            "Белорусский" -> return "be"
            "Бенгальский" -> return "bn"
            "Болгарский" -> return "bg"
            "Боснийский" -> return "bs"
            "Венгерский" -> return "hu"
            "Вьетнамский" -> return "vi"
            "Галисийский" -> return "gl"
            "Греческий" -> return "el"
            "Грузинский" -> return "ka"
            "Датский" -> return "da"
            "Иврит" -> return "iw"
            "Индонезийский" -> return "id"
            "Ирландский" -> return "ga"
            "Испанский" -> return "es"
            "Итальянский" -> return "it"
            "Китайский (упрощенный)" -> return "zh-CN"
            "Китайский (традиционный)" -> return "zh-TW"
            "Корейский" -> return "ko"
            "Латышский" -> return "lv"
            "Литовский" -> return "lt"
            "Македонский" -> return "mk"
            "Малайский" -> return "ms"
            "Немецкий" -> return "de"
            "Норвежский" -> return "no"
            "Персидский" -> return "fa"
            "Польский" -> return "pl"
            "Португальский" -> return "pt"
            "Румынский" -> return "ro"
            "Русский" -> return "ru"
            "Сербский" -> return "sr"
            "Словацкий" -> return "sk"
            "Словенский" -> return "sl"
            "Тайский" -> return "th"
            "Турецкий" -> return "tr"
            "Угорский" -> return "hu"
            "Украинский" -> return "uk"
            "Финский" -> return "fi"
            "Французский" -> return "fr"
            "Хинди" -> return "hi"
            "Хорватский" -> return "hr"
            "Чешский" -> return "cs"
            "Шведский" -> return "sv"
            "Шотландский" -> return "gd"
            "Эстонский" -> return "et"
        }

        return "ru"
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)

        val permissionsNeeded = mutableListOf<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsNeeded.toTypedArray(), 0)
        } else {
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            } else {
                Toast.makeText(requireContext(), "Разрешения на камеру и микрофон необходимы для работы приложения.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}