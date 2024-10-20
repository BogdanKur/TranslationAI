package com.example.translationai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.translationai.databinding.FragmentReadTranslateBinding

class ReadTranslateFragment : Fragment() {
    private var _binding: FragmentReadTranslateBinding? = null
    private val binding get() = _binding!!
    lateinit var currentText: String
    lateinit var translateText: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadTranslateBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        arguments?.let { bundle->
            currentText = bundle.getString("CURRENT").toString()
            translateText = bundle.getString("TRANSLATED").toString()
            binding.btnOrigin.text = currentText
            binding.btnTranslate.text = translateText
        }
        binding.btnOrigin.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Оригинал скопирован", currentText)
            clipboard.setPrimaryClip(clip)
        }

        binding.btnTranslate.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Перевод скопирован", translateText)
            clipboard.setPrimaryClip(clip)
        }

        binding.imgBtnExit.setOnClickListener {
            navController.navigate(R.id.action_readTranslateFragment_to_mainFragment)
        }
        return view
    }

}