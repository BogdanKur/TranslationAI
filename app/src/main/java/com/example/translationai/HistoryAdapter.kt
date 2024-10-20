package com.example.translationai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(val listOfResults: List<ResultOfTranslate>, val navController: NavController): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    class HistoryViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.btnSelectResultOfSearch)
        val tvCurrent: TextView = view.findViewById(R.id.tvCurrentLanguageText)
        val tvTarget: TextView = view.findViewById(R.id.tvTargetLanguageText)
        val tvAbbrCode: TextView = view.findViewById(R.id.tvTargetAbbrCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_result_of_search, parent, false)
        return HistoryViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = listOfResults.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.tvCurrent.text = listOfResults[position].currentText
        holder.tvTarget.text = listOfResults[position].translateText
        holder.tvAbbrCode.text = listOfResults[position].targetAbbrCode
        holder.button.setOnClickListener {
            val bundle = Bundle().apply {
                putString("CURRENT", listOfResults[position].currentText)
                putString("TRANSLATED", listOfResults[position].translateText)
                putString("ABBRCODE", listOfResults[position].targetAbbrCode)
            }
            navController.navigate(R.id.action_historyFragment_to_readTranslateFragment, bundle)
        }
    }
}