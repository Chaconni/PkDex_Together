package com.pkdex_together


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.pkdex_together.api.PokeApiClient
import com.pkdex_together.api.PokeApiService
import com.pkdex_together.api.PokemonResponse
import com.pkdex_together.api.SpeciesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokeDetailsActivity : AppCompatActivity() {

    private var pokemonId = 1               //Empezar en el pokemon id 1
    private val maxPokemonId = 1010         //Id del ultimo pokemon
    private val apiService = PokeApiClient.retrofit.create(PokeApiService::class.java)   //api


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pkdex_layout)

        //Configuración de variables
        val infoTitle: TextView = findViewById(R.id.infoTitle)
        val searchField: EditText = findViewById(R.id.searchPokemon)
        val searchButton: Button = findViewById(R.id.searchButton)
        val prevButton: Button = findViewById(R.id.prevButton)
        val nextButton: Button = findViewById(R.id.nextButton)




        //Botón para mostrar o quitar el buscar Pokémon y el botón buscar
        infoTitle.setOnClickListener {
            val visibility = if (searchField.visibility == View.GONE) View.VISIBLE else View.GONE
            searchField.visibility = visibility
            searchButton.visibility = visibility
        }

        searchButton.visibility = View.GONE

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", "") ?: ""

        val addToFavoritesButton: Button = findViewById(R.id.favoriteButton)

        addToFavoritesButton.setOnClickListener {
            val db = AppDatabase(this)
            if (userEmail.isNotEmpty()) {
                if (db.isFavorite(userEmail, pokemonId)) {
                    db.removeFavorite(userEmail, pokemonId)
                    Toast.makeText(this, "Pokémon eliminado de favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    db.insertFavorite(userEmail, pokemonId)
                    Toast.makeText(this, "Pokémon añadido a favoritos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Debes iniciar sesión para gestionar favoritos.", Toast.LENGTH_SHORT).show()
            }
        }


        //Botón de buscar Pokémon
        searchButton.setOnClickListener {
            val input = searchField.text.toString().trim()
            if (input.isNotEmpty()) {
                try {
                    //Si es un número, buscar por ID
                    pokemonId = input.toInt()
                } catch (e: NumberFormatException) {
                    //Si no es un número, buscar por nombre
                    fetchPokemonByName(input)
                    return@setOnClickListener
                }
                if (pokemonId in 1..maxPokemonId) {
                    fetchPokemonDetails(pokemonId)
                } else {
                    showError("ID fuera de rango")
                }
            } else {
                showError("Introduce un nombre o ID")
            }
        }


        //Botón Anterior
        prevButton.setOnClickListener {
            if (pokemonId > 1) {
                pokemonId--
                fetchPokemonDetails(pokemonId)
            }
        }

        //Botón Siguiente
        nextButton.setOnClickListener {
            if (pokemonId < maxPokemonId) {
                pokemonId++
                fetchPokemonDetails(pokemonId)
            }
        }

        pokemonId = intent.getIntExtra("POKEMON_ID", 1)

        fetchPokemonDetails(pokemonId)
    }

    //Método que llama a la API para conseguir los datos de los pokemons
    private fun fetchPokemonDetails(id: Int) {
        val imageView: ImageView = findViewById(R.id.pokemonImage)
        val nameView: TextView = findViewById(R.id.pokemonName)
        val titleView: TextView = findViewById(R.id.pokemonTitle)
        val typeView: TextView = findViewById(R.id.pokemonType)
        val heightView: TextView = findViewById(R.id.pokemonHeight)
        val weightView: TextView = findViewById(R.id.pokemonWeight)
        val descriptionView: TextView = findViewById(R.id.pokemonDescription)

        apiService.getPokemonDetails(id).enqueue(object : Callback<PokemonResponse> {
            override fun onResponse(
                call: Call<PokemonResponse>,
                response: Response<PokemonResponse>
            ) {
                if (response.isSuccessful) {
                    val pokemon = response.body()
                    pokemon?.let {

                        //Llamar al nombre del Pokémon
                        nameView.text = it.name.replaceFirstChar { char -> char.uppercase() }

                        //Llamar al tipo de Pokémon; Tipo1/Tipo2  (en la api solo se encuentrán en ingles)
                        val types = it.types.joinToString("/") { typeEntry ->
                            typeEntry.type.name.replaceFirstChar { char -> char.uppercase() }
                        }
                        typeView.text = "$types"  //Tipos de los Pokémon

                        heightView.text = "${it.height / 10.0} m"  //Altura de los Pokémons, se divide entre 10 para que esté en metros

                        weightView.text = "${it.weight / 10.0} kg"  //Peso de los Pokémons, se divide entre 10 para que esté en kilogramos

                        Glide.with(this@PokeDetailsActivity).load(it.sprites.front_default)
                            .into(imageView)

                        fetchPokemonDescription(it.name, descriptionView, titleView)
                    }
                }
            }
                //Mensaje de error si no encuentra el Pokemón
            override fun onFailure(call: Call<PokemonResponse>, t: Throwable) {
                descriptionView.text = "Error al cargar datos del Pokémon."
            }
        })
    }
    //Llamar a la api para conseguir la descripción y el título del Pokémon
    private fun fetchPokemonDescription(
        name: String,
        titleView: TextView,
        descriptionView: TextView
    ) {
        apiService.getPokemonSpecies(name).enqueue(object : Callback<SpeciesResponse> {
            override fun onResponse(
                call: Call<SpeciesResponse>,
                response: Response<SpeciesResponse>
            ) {
                if (response.isSuccessful) {
                    val species = response.body()
                    species?.let {
                        //variable donde se guarda la descripciçon del Pokémon
                        val description = it.flavor_text_entries.find { entry ->
                            entry.language.name == "es"
                        }?.flavor_text?.replace("\n", " ")?.replace("\u000c", " ")
                            ?: "No disponible"

                        //variable titulo en la cual se guarda el titulo donde el idioma sea español
                        val title = it.genera.find { generaEntry ->
                            generaEntry.language.name == "es"
                        }?.genus ?: "Título no disponible"


                        descriptionView.text = description
                        titleView.text = "$title"
                    }
                }
            }

            //En caso de error o que en la api no se encuentre aún la descripción del Pokémon, se cambiará por el siguiente mensaje
            override fun onFailure(call: Call<SpeciesResponse>, t: Throwable) {
                descriptionView.text = "Error al cargar la descripción."
            }
        })
    }

    //Método para buscar pokemon por el nombre
    private fun fetchPokemonByName(name: String) {

        val lowercaseName = name.lowercase() //Transforma el nombre añadido por el usuario a minusculas para poder buscarlo por la api

        apiService.getPokemonDetailsByName(lowercaseName).enqueue(object : Callback<PokemonResponse> {
            override fun onResponse(call: Call<PokemonResponse>, response: Response<PokemonResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { pokemon ->
                        pokemonId = pokemon.id
                        fetchPokemonDetails(pokemonId) //Una vez comprobado que el nombre esté, llama al método para obtener la información del pokemon y le manda como parametro la id del pokemon seleccionado
                    }
                } else {
                    showError("Pokémon no encontrado")
                }
            }

            override fun onFailure(call: Call<PokemonResponse>, t: Throwable) {
                showError("Error al buscar Pokémon por nombre")
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
