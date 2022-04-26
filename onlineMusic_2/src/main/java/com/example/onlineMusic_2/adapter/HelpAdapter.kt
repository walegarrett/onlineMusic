package com.example.onlineMusic_2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.Help


class HelpAdapter(val context: Context, val helpList:List<Help>) : RecyclerView.Adapter<HelpAdapter.ViewHolder>() {
    private var expandedPosition = 1
    private var viewHolder:ViewHolder? = null

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val imageview: ImageView= itemView.findViewById(R.id.imageview)
        var rlParent: RelativeLayout= itemView.findViewById(R.id.rl_parent)
        var rlChild: RelativeLayout= itemView.findViewById(R.id.rl_child)
        var textviewparent: TextView= itemView.findViewById(R.id.textViewparent)
        var textviewchild: TextView= itemView.findViewById(R.id.textViewchild)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.help_item, parent,false)
        val viewHolder=ViewHolder(view)
        return viewHolder
    }
    override fun getItemCount(): Int= helpList.size

    override fun onBindViewHolder(holder: ViewHolder, j: Int) {
        val position = holder.adapterPosition
        holder.textviewparent.text = "${position+1}. " + helpList[j].question
        holder.imageview.setImageResource(R.color.tengluoPurple50)
        holder.textviewchild.text = helpList[j].answer
        val isExpanded = j == expandedPosition
        holder.rlChild.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.rlParent.setOnClickListener {
            if (viewHolder != null) {
                viewHolder!!.rlChild.visibility = View.GONE
                notifyItemChanged(expandedPosition)
            }
            expandedPosition = if (isExpanded) -1 else holder.getAdapterPosition()
            viewHolder = if (isExpanded) null else holder
            notifyItemChanged(holder.adapterPosition)
        }
    }

}