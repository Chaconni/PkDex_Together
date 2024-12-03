package com.pkdex_together

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class Pokemon(val id: Int, val name: String, val imageUrl: String)

class FavoritesAdapter(private val favorites: List<Pokemon>, private val onClick: (Pokemon) -> Unit) :
    RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    inner class FavoritesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonImage: ImageView = view.findViewById(R.id.pokemonImageFav)
        val pokemonName: TextView = view.findViewById(R.id.pokemonName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val pokemon = favorites[position]
        holder.pokemonName.text = pokemon.name
        Glide.with(holder.itemView.context)
            .load(pokemon.imageUrl)
            .into(holder.pokemonImage)

        holder.itemView.setOnClickListener { onClick(pokemon) }
    }

    override fun getItemCount(): Int = favorites.size
}
