package com.example.projekt.ui.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.R
import com.example.projekt.ui.model.Pomiar

class PomiarAdapter(
    pomiary: List<Pomiar>,
    private val onEditClick: (Pomiar) -> Unit,
    private val onDeleteClick: (Pomiar) -> Unit,
    private val onItemClick: (Pomiar) -> Unit
) : RecyclerView.Adapter<PomiarAdapter.PomiarViewHolder>() {

    // MutableList zamiast List, by móc usuwać elementy
    private var pomiary: MutableList<Pomiar> = pomiary.toMutableList()

    inner class PomiarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataText: TextView = itemView.findViewById(R.id.textData)
        val cisnienieText: TextView = itemView.findViewById(R.id.textCisnienie)
        val pulsText: TextView = itemView.findViewById(R.id.textPuls)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PomiarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pomiar, parent, false)
        return PomiarViewHolder(view)
    }

    override fun onBindViewHolder(holder: PomiarViewHolder, position: Int) {
        val pomiar = pomiary[position]
        holder.dataText.text = "Data: ${pomiar.data}"
        holder.cisnienieText.text = "Ciśnienie: ${pomiar.cisnienieSkurczowe}/${pomiar.cisnienieRozkurczowe}"
        holder.pulsText.text = "Puls: ${pomiar.puls}"

        holder.itemView.setOnClickListener { onItemClick(pomiar) }
        holder.editButton.setOnClickListener { onEditClick(pomiar) }
        holder.deleteButton.setOnClickListener { onDeleteClick(pomiar) }
    }

    override fun getItemCount() = pomiary.size

    fun updateData(newData: List<Pomiar>) {
        pomiary = newData.toMutableList()
        notifyDataSetChanged()
    }

    // Nowa metoda do usuwania pojedynczego elementu po id
    fun removePomiar(pomiarToRemove: Pomiar) {
        val index = pomiary.indexOfFirst { it.id == pomiarToRemove.id }
        if (index != -1) {
            pomiary.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}


