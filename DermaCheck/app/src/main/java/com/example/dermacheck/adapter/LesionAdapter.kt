package com.example.dermacheck.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermacheck.R
import com.example.dermacheck.data.models.Lesion
import com.google.gson.Gson
import com.example.dermacheck.ui.history.HistoryFragmentDirections

class LesionAdapter(private val context: Context) : RecyclerView.Adapter<LesionAdapter.LesionViewHolder>() {

    private var lesionList = listOf<Lesion>()

    fun submitList(list: List<Lesion>) {
        lesionList = list
        notifyDataSetChanged()
    }

    inner class LesionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diagnosis: TextView = itemView.findViewById(R.id.diagnosisText)
        val location: TextView = itemView.findViewById(R.id.locationText)
        val date: TextView = itemView.findViewById(R.id.dateText)
        val card: CardView = itemView.findViewById(R.id.lesionCard)
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LesionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesion_card, parent, false)
        return LesionViewHolder(view)
    }

    override fun getItemCount(): Int = lesionList.size

    override fun onBindViewHolder(holder: LesionViewHolder, position: Int) {
        val lesion = lesionList[position]
        holder.diagnosis.text = "Diagnosis: ${lesion.currentDiagnosis}"
        holder.location.text = "Location: ${lesion.bodyLocationValue}"
        holder.date.text = "Date: ${lesion.lastUpdated?.toDate()?.toString()?.substring(0, 16)}"

        val latestImage = lesion.history.lastOrNull()?.imageUrl
        Glide.with(holder.image.context)
            .load(latestImage)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.image)

        holder.card.setOnClickListener {
            val json = Gson().toJson(lesion)
            val action = HistoryFragmentDirections.actionHistoryFragmentToLesionDetailFragment(json)
            it.findNavController().navigate(action)
        }
    }
}
