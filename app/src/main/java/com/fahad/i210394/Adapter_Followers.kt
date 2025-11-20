package com.fahad.i210394

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter_Followers(val context: Context, val list: MutableList<Model_Followers>) : RecyclerView.Adapter<Adapter_Followers.MyViewHolder>() {
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name: TextView =itemView.findViewById(R.id.name) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_followers,parent,false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = list[position].name
    }
}