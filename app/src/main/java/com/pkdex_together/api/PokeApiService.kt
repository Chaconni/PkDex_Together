package com.pkdex_together.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

//Data class donde se almacena los datos de los Pokémons
data class PokemonResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<PokemonType>
)


data class Sprites(val front_default: String)//Data class de los sprites de los Pokémons
data class PokemonType(val type: TypeName) //Data class de la lista de los tipos de los Pokémons
data class TypeName(val name: String) //Data class de los tipos de los Pokémons


//Data class para buscar el idioma español en el titulo de los Pokémon
data class SpeciesResponse(
    val flavor_text_entries: List<FlavorTextEntry>,
    val genera: List<GeneraEntry>
)

data class GeneraEntry(
    val genus: String,
    val language: Language
)

data class FlavorTextEntry(
    val flavor_text: String,
    val language: Language
)

data class Language(val name: String)

interface PokeApiService {
    @GET("pokemon/{id}") //Para conseguir los datos buscando por id
    fun getPokemonDetails(@Path("id") id: Int): Call<PokemonResponse>

    @GET("pokemon/{name}") //Para conseguir los datos buscando por nombre
    fun getPokemonDetailsByName(@Path("name") name: String): Call<PokemonResponse>

    @GET("pokemon-species/{name}") //Para la descripción del pokemon
    fun getPokemonSpecies(@Path("name") name: String): Call<SpeciesResponse>
}