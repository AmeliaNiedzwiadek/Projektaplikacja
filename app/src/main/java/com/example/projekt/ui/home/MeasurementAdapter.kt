package com.example.projekt.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.R
import com.example.projekt.ui.model.Pomiar

class MeasurementAdapter(
    val items: MutableList<Pomiar>,
    private val onItemClick: (Pomiar) -> Unit,
    private val onEditClick: (Pomiar, Int) -> Unit,
    private val onDeleteClick: (Pomiar, Int) -> Unit
) : RecyclerView.Adapter<MeasurementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dataText: TextView = view.findViewById(R.id.textData)
        val cisnienieText: TextView = view.findViewById(R.id.textCisnienie)
        val pulsText: TextView = view.findViewById(R.id.textPuls)
        val buttonEdit: Button = view.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_measurement, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pomiar = items[position]
        holder.dataText.text = "Data: ${pomiar.data}"
        holder.cisnienieText.text = "Ciśnienie: ${pomiar.cisnienieSkurczowe}/${pomiar.cisnienieRozkurczowe}"
        holder.pulsText.text = "Puls: ${pomiar.puls}"

        holder.itemView.setOnClickListener { onItemClick(pomiar) }
        holder.buttonEdit.setOnClickListener { onEditClick(pomiar, position) }
        holder.buttonDelete.setOnClickListener { onDeleteClick(pomiar, position) }
    }

    fun removeAt(position: Int) {
        if (position in 0 until items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateData(newData: List<Pomiar>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }
}
