package com.example.translationai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.translationai.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        val dao = ResultDatabase.getInstance(requireContext()).dao
        val viewModelFactory = HistoryViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(HistoryViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        viewModel.listOfResults.observe(viewLifecycleOwner) { list->
            val adapter = HistoryAdapter(list, navController)
            binding.rvHistorySearchesList.adapter = adapter
        }

        return view
    }


}