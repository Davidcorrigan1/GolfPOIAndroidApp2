package org.wit.golfpoi.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.CardGolfpoiBinding
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber.i

// new interface will represent click events on the GolfPOI Card,
// and allow us to abstract the response to this event
interface GolfPOIListener {
    fun onGolfPOIClick(golfPOI: GolfPOIModel)

    fun onGolfPOIFavButtonClick(golfPOI: GolfPOIModel)
}


class GolfPOIAdapter constructor(private var golfPOIs: ArrayList<GolfPOIModel>,
                                 private var currentUser: GolfUserModel,
                                private val listener: GolfPOIListener) :
    RecyclerView.Adapter<GolfPOIAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardGolfpoiBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val golfPOI = golfPOIs[holder.adapterPosition]
        holder.bind(golfPOI, currentUser, listener)
    }

    fun removeAt(position: Int) {
        golfPOIs.removeAt(position)
        notifyItemRemoved(position)
    }


    override fun getItemCount(): Int = golfPOIs.size

    class MainHolder(private val binding : CardGolfpoiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(golfPOI: GolfPOIModel, currentUser: GolfUserModel, listener: GolfPOIListener) {

            // Show default image if none available
            if (golfPOI.image != null) {
                if (golfPOI.image.equals(Uri.EMPTY)) {
                    binding.imageIcon.setImageResource(R.drawable.golflogo)
                } else {
                    i("golfPOI Image: ${golfPOI.image}")
                    //Picasso.get().load(golfPOI.image).centerCrop().fit().into(binding.imageIcon)
                    Picasso.get().load(golfPOI.image).resize(200,200).into(binding.imageIcon)
                }
            }
            binding.golfPOITitle.text = golfPOI.courseTitle
            binding.golfPOIDesc.text = golfPOI.courseDescription
            binding.golfPOIProvince.text = golfPOI.courseProvince

            if (currentUser.favorites.contains(golfPOI.id)) {
                i("currentFavourites: ${currentUser.favorites}")
                i("CurrentCourse: ${golfPOI.id}")
                binding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                i("currentFavourites: ${currentUser.favorites}")
                i("CurrentCourse: ${golfPOI.id}")
                binding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }

            binding.golfPOIPar.text = "  Par: ${golfPOI.coursePar}"
            binding.root.setOnClickListener { listener.onGolfPOIClick(golfPOI) }
            binding.favoriteBtn.setOnClickListener { listener.onGolfPOIFavButtonClick(golfPOI) }
        }
    }
}