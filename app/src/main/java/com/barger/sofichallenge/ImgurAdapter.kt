package com.barger.sofichallenge

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class ImgurAdapter : RecyclerView.Adapter<ImgurViewHolder>() {

    private var data = arrayListOf<ImgurViewModel>()

    fun addData(vms: List<ImgurViewModel>) {
        Log.d(this.javaClass.simpleName, "Adding ${vms.size} items")
        val start = data.size
        data.addAll(vms)
        notifyItemRangeInserted(start, vms.size)
    }

    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgurViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.imgur_item, parent, false)
        return ImgurViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImgurViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemId(position: Int): Long {
        return data[position].id.hashCode().toLong()
    }
}

class ImgurViewModel(val link: String,
                     val id: String)

class ImgurViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView = itemView.findViewById<ImageView>(R.id.image_view)
    private val textView = itemView.findViewById<TextView>(R.id.text_view)

    fun bindData(vm: ImgurViewModel) {
        textView.text = vm.id
        Picasso.get()
                .load(vm.link)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(imageView)
    }
}