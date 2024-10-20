package com.example.translationai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.translationai.MainFragment.Companion.listOfLanguages
import com.example.translationai.databinding.FragmentTargetLanguageBinding

class TargetLanguageFragment : Fragment() {
    private var _binding: FragmentTargetLanguageBinding? = null
    private val binding get() = _binding!!

    companion object {
        var targetLanguage: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTargetLanguageBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Выберите язык"
            binding.toolbar.title = destination.label
        }
        arguments.let { bundle ->
            targetLanguage = bundle?.getString("CURRENT_LANGUAGE_MAIN_TO_TARGET").toString()
        }

        val adapter = TargetLanguageAdapter(listOfLanguages, navController, targetLanguage!!)
        binding.rvTargetLanguage.adapter = adapter

        return  view
    }

}