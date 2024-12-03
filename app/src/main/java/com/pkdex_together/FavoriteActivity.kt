package com.pkdex_together

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkdex_together.api.PokeApiClient
import com.pkdex_together.api.PokeApiService
import com.pkdex_together.api.PokemonResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesActivity : AppCompatActivity() {
    private val apiService = PokeApiClient.retrofit.create(PokeApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pkdex_fav)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", "") ?: ""


        val db = AppDatabase(this)
        // Obtener IDs de favoritos desde la base de datos
        val favoritePokemonIds = db.getFavorites(userEmail)

        if (favoritePokemonIds.isEmpty()) {
            Toast.makeText(this, "No tienes Pokémon favoritos.", Toast.LENGTH_SHORT).show()
        } else {
            loadFavoritePokemonDetails(favoritePokemonIds)
        }
    }

    private fun loadFavoritePokemonDetails(pokemonIds: List<Int>) {
        val favoritePokemons = mutableListOf<Pokemon>()
        val recyclerView: RecyclerView = findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pokemonIds.forEach { id ->
            apiService.getPokemonDetails(id).enqueue(object : Callback<PokemonResponse> {
                override fun onResponse(call: Call<PokemonResponse>, response: Response<PokemonResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { pokemon ->
                            favoritePokemons.add(
                                Pokemon(
                                    id = pokemon.id,
                                    name = pokemon.name.replaceFirstChar { it.uppercase() },
                                    imageUrl = pokemon.sprites.front_default
                                )
                            )
                            // Actualiza el adaptador solo cuando todos los datos estén listos
                            if (favoritePokemons.size == pokemonIds.size) {
                                recyclerView.adapter = FavoritesAdapter(favoritePokemons) { selectedPokemon ->
                                    Toast.makeText(
                                        this@FavoritesActivity,
                                        "Seleccionaste a ${selectedPokemon.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PokemonResponse>, t: Throwable) {
                    Toast.makeText(this@FavoritesActivity, "Error al cargar Pokémon con ID: $id", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
