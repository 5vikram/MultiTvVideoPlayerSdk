//package com.multitv.ott.multitvvideoplayer.adapters
//
//import android.os.Trace
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.RadioButton
//import androidx.recyclerview.widget.RecyclerView
//import com.multitv.ott.multitvvideoplayer.R
//import com.multitv.ott.multitvvideoplayer.models.TrackResolution
//
//class ResolutionAdapter(val list: List<TrackResolution>, val callbacks: ResolutionAdapter.AdapterCallbacks) : RecyclerView.Adapter<ResolutionAdapter.ResolutionViewHolder>() {
//
//    interface AdapterCallbacks{
//        fun onItemSelected(trackResolution: TrackResolution)
//    }
//
//    inner class ResolutionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val radioBtn: RadioButton = itemView.findViewById(R.id.resolution_radio_btn)
//        init {
//            itemView.setOnClickListener {
//                callbacks.onItemSelected(list[adapterPosition])
//                notifyDataSetChanged()
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResolutionViewHolder {
//        return ResolutionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.track_selection_item_layout, parent, false))
//    }
//
//    override fun onBindViewHolder(holder: ResolutionViewHolder, position: Int) {
//        holder.radioBtn.text = list[position].height.toString()
//        for (i in 0..list.size){
//            if (i != position){
//                holder.radioBtn.isChecked = false
//            }
//        }
//    }
//
//    override fun getItemCount(): Int = list.size
//
//}