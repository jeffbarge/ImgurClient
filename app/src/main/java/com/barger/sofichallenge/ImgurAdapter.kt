package com.barger.sofichallenge

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso

class ImgurAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnImageClickedListener {
        fun onClicked(url: String, caption: String, imageView: ImageView)
    }

    var data = arrayListOf<ImgurViewModel>()
        private set

    var onImageClickedListener: OnImageClickedListener? = null

    fun addData(vms: List<ImgurViewModel>) {
        Log.d(this.javaClass.simpleName, "Adding ${vms.size} items")
        val start = data.size
        data.addAll(start, vms)
        //think I'm off by one somewhere; will come back to this to get the better
        //performance
        notifyDataSetChanged()
    }

    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == data.size) {
            return VIEW_TYPE_SPINNER
        }
        return VIEW_TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_IMAGE -> ImgurViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.imgur_item, parent, false))
            VIEW_TYPE_SPINNER -> SpinnerViewhHolder(LayoutInflater.from(parent.context).inflate(R.layout.spinner_item, parent, false))
            else -> throw RuntimeException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ImgurViewHolder) {
                val vm = data[position]
                holder.bindData(vm)
                holder.itemView.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.imageView.transitionName = "image_view"
                    }
                    onImageClickedListener?.onClicked(vm.link, vm.caption, holder.imageView)
                }
            }
    }

    companion object {
        val VIEW_TYPE_IMAGE = 1
        val VIEW_TYPE_SPINNER = 2
    }
}


class ImgurViewModel(val link: String,
                     val id: String,
                     val title: String,
                     val description: String) : Parcelable {
    private constructor(parcel: Parcel) : this(link = parcel.readString(),
            id = parcel.readString(),
            title = parcel.readString(),
            description = parcel.readString())

    val caption: String
        get() {
            if (!TextUtils.isEmpty(title)) {
                return title
            } else if (!TextUtils.isEmpty(description)) {
                return description
            }
            return ""
        }

    override fun writeToParcel(parcel: Parcel?, p1: Int) {
        parcel?.apply {
            writeString(link)
            writeString(id)
            writeString(title)
            writeString(description)
        }
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField val CREATOR = object:Parcelable.Creator<ImgurViewModel> {
            override fun createFromParcel(parcel: Parcel): ImgurViewModel = ImgurViewModel(parcel)

            override fun newArray(size: Int): Array<ImgurViewModel?> = arrayOfNulls(size)
        }
    }
}

class ImgurViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.image_view)
    private val textView = itemView.findViewById<TextView>(R.id.text_view)

    fun bindData(vm: ImgurViewModel) {
        textView.text = vm.caption
        textView.visibility = if (!TextUtils.isEmpty(vm.caption)) View.VISIBLE else View.GONE
        Picasso.get()
                .load(vm.link)
                .into(imageView)
    }
}

class SpinnerViewhHolder(itemView: View) : RecyclerView.ViewHolder(itemView)