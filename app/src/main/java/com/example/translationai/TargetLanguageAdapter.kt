package com.example.translationai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.translationai.TargetLanguageFragment.Companion.targetLanguage

class TargetLanguageAdapter(val listOfLanguages: List<String>, val navController: NavController, val currentLanguage: String): RecyclerView.Adapter<TargetLanguageAdapter.TargetLanguageViewHolder>() {
    class TargetLanguageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.btnChangeCurrentLanguage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetLanguageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_current_language, parent, false)
        return  TargetLanguageViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = listOfLanguages.size

    override fun onBindViewHolder(holder: TargetLanguageViewHolder, position: Int) {
        holder.button.text = listOfLanguages[position]
        if(listOfLanguages[position] != currentLanguage) holder.button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
        else {
            holder.button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_language_is_select,0)
        }
        holder.button.setOnClickListener {
            targetLanguage = listOfLanguages[position]
            navController.navigate(R.id.action_targetLanguageFragment_to_mainFragment)
        }
    }
}