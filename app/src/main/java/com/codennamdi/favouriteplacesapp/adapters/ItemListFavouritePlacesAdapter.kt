package com.codennamdi.favouriteplacesapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codennamdi.favouriteplacesapp.activities.AddNewFavouritePlace
import com.codennamdi.favouriteplacesapp.activities.MainActivity
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceDao
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceEntity
import com.codennamdi.favouriteplacesapp.databinding.ItemListFavouritePlacesBinding

open class ItemListFavouritePlacesAdapter(
    private val item: ArrayList<FavouritePlaceEntity>,
    private val context: Context
) : RecyclerView.Adapter<ItemListFavouritePlacesAdapter.ViewHolder>() {
    private lateinit var onClickListener: OnClickListener

    class ViewHolder(binding: ItemListFavouritePlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val circularImageView = binding.circleImageViewItemImage
        val textViewTitle = binding.textViewItemTitle
        val textViewDescription = binding.textViewItemDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListFavouritePlacesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun notifyEditItem(activity: Activity, position: Int) {
        val intent = Intent(context, AddNewFavouritePlace::class.java)
        intent.putExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS, item[position])
        activity.startActivity(intent)
        notifyItemChanged(position)
    }

    suspend fun notifyDeletedItem(position: Int, favouritePlaceDao: FavouritePlaceDao) {
        val favouritePlaceItem = item[position]
        favouritePlaceDao.delete(favouritePlaceItem)
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = item[position]

        holder.circularImageView.setImageURI(Uri.parse(item.image))
        holder.textViewTitle.text = item.title
        holder.textViewDescription.text = item.description

        //Before you bind the viewHolders together
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener.onClick(position, item)
            }
        }

    }

    override fun getItemCount(): Int {
        return item.size
    }

    //First step you create an interface
    interface OnClickListener {
        fun onClick(position: Int, items: FavouritePlaceEntity)
    }

    //Secondly you would have to create an a function called setOnClickListener
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}