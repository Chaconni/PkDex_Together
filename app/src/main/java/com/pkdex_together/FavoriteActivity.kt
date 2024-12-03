package com.pkdex_together

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkdex_together.api.PokeApiClient
import com.pkdex_together.api.PokeApiService
import com.pkdex_together.api.PokemonResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesActivity : AppCompatActivity() {
    private val apiService = PokeApiClient.retrofit.create(PokeApiService::class.java)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pkdex_fav)

        // Inicializa RecyclerView y base de datos
        recyclerView = findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        db = AppDatabase(this)

        // Recuperar email del usuario desde SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", "") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para ver favoritos.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
        var remainingRequests = pokemonIds.size

        pokemonIds.forEach { id ->
            apiService.getPokemonDetails(id).enqueue(object : Callback<PokemonResponse> {
                override fun onResponse(call: Call<PokemonResponse>, response: Response<PokemonResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { pokemon ->
                            favoritePokemons.add(
                                Pokemon(
                                    id = pokemon.id,
                                    name = pokemon.name.replaceFirstChar { it.uppercase() },
                                    imageUrl = pokemon.sprites.front_default ?: ""
                                )
                            )
                        }
                    } else {
                        Toast.makeText(this@FavoritesActivity, "Error al cargar datos de ID: $id", Toast.LENGTH_SHORT).show()
                    }
                    remainingRequests--
                    if (remainingRequests == 0) setupRecyclerView(favoritePokemons)
                }

                override fun onFailure(call: Call<PokemonResponse>, t: Throwable) {
                    Toast.makeText(this@FavoritesActivity, "Error al conectar con la API para ID: $id", Toast.LENGTH_SHORT).show()
                    remainingRequests--
                    if (remainingRequests == 0) setupRecyclerView(favoritePokemons)
                }
            })
        }
    }

    private fun setupRecyclerView(pokemons: List<Pokemon>) {
        if (pokemons.isEmpty()) {
            Toast.makeText(this, "No se pudieron cargar los Pokémon favoritos.", Toast.LENGTH_SHORT).show()
            return
        }
        adapter = FavoritesAdapter(pokemons) { selectedPokemon ->
            navigateToPokeDetails(selectedPokemon.id)
        }
        recyclerView.adapter = adapter
    }

    private fun navigateToPokeDetails(pokemonId: Int) {
        val intent = Intent(this, PokeDetailsActivity::class.java)
        intent.putExtra("POKEMON_ID", pokemonId)
        startActivity(intent)
    }
}
