package com.barger.sofichallenge

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
                     val id: String,
                     val title: String,
                     val description: String) {
    val caption: String
        get() {
            if (!TextUtils.isEmpty(title)) {
                return title
            } else if (!TextUtils.isEmpty(description)) {
                return description
            }
            return ""
        }
}

class ImgurViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView = itemView.findViewById<ImageView>(R.id.image_view)
    private val textView = itemView.findViewById<TextView>(R.id.text_view)

    fun bindData(vm: ImgurViewModel) {
        textView.text = vm.caption
        textView.visibility = if (!TextUtils.isEmpty(vm.caption)) View.VISIBLE else View.GONE
        Picasso.get()
                .load(vm.link)
                .into(imageView)
    }
}