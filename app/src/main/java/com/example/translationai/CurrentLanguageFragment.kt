package com.example.translationai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.translationai.MainFragment.Companion.listOfLanguages
import com.example.translationai.databinding.FragmentCurrentLanguageBinding

class CurrentLanguageFragment : Fragment() {
    private var _binding: FragmentCurrentLanguageBinding? = null
    private val binding get() = _binding!!
    var currentLanguage = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCurrentLanguageBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Выберите язык"
            binding.toolbar.title = destination.label
        }
        arguments.let { bundle ->
            currentLanguage = bundle?.getString("CURRENT_LANGUAGE_MAIN").toString()
        }
        val adapter = CurrentLanguageAdapter(listOfLanguages, navController, currentLanguage)
        binding.rvCurrentLanguage.adapter = adapter

        return view
    }

}