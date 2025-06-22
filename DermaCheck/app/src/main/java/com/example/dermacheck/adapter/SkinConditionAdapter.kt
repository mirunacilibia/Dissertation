package com.example.dermacheck.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.dermacheck.R
import com.example.dermacheck.data.models.SkinCondition
import com.example.dermacheck.ui.history.HistoryFragmentDirections
import com.example.dermacheck.ui.information.InformationFragmentDirections
import com.google.gson.Gson
import java.util.Locale

class SkinConditionAdapter : RecyclerView.Adapter<SkinConditionAdapter.ViewHolder>() {

    private var conditions: List<SkinCondition> = emptyList()

    fun submitList(list: List<SkinCondition>) {
        conditions = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.condition_name)
        val type: TextView = itemView.findViewById(R.id.condition_type)
        val description: TextView = itemView.findViewById(R.id.condition_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skin_condition, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = conditions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = conditions[position]

        // Set the text for each field
        holder.name.text = item.name
        holder.type.text = item.type.capitalize(Locale.ROOT)
        holder.description.text = if (item.description.length > 150) {
            item.description.take(150) + "..."
        } else {
            item.description
        }


        // Set the click listener to navigate to details fragment and pass data
        holder.itemView.setOnClickListener {
            val json = Gson().toJson(item)
            val action = InformationFragmentDirections.actionNavigationInformationToNavigationSkinCondition(json)
            it.findNavController().navigate(action)
        }
    }
}
