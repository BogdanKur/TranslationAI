package com.example.translationai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView

class CurrentLanguageAdapter(val listOfLanguages: List<String>, val navController: NavController, val currentLanguage: String): RecyclerView.Adapter<CurrentLanguageAdapter.CurrentLanguageViewHolder>() {
    class CurrentLanguageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.btnChangeCurrentLanguage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentLanguageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_current_language, parent, false)
        return CurrentLanguageViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = listOfLanguages.size

    override fun onBindViewHolder(holder: CurrentLanguageViewHolder, position: Int) {
        holder.button.text = listOfLanguages[position]
        if(listOfLanguages[position] != currentLanguage) holder.button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
        else {
            holder.button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_language_is_select,0)
        }
        holder.button.setOnClickListener {
            val bundle = Bundle().apply {
                putString("CURRENT_LANGUAGE", holder.button.text.toString())
            }
            navController.navigate(R.id.action_currentLanguageFragment_to_mainFragment, bundle)
        }
    }
}