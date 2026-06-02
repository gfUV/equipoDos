package com.wt2dadmuvy.spinbot.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repositorio remoto para obtener la imagen aleatoria de un Pokémon.
 *
 * HU 12 - Criterio 2:
 * La lista de pokémon se consume desde la API indicada en el enunciado del miniproyecto.
 * Se mantiene como Repository para respetar el enfoque MVVM visto en clase.
 */
class PokemonRepository {

    suspend fun getRandomPokemonImageUrl(): String? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(POKEDEX_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }

            connection.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                val pokemonArray = JSONObject(response).getJSONArray("pokemon")

                if (pokemonArray.length() == 0) return@withContext null

                val randomPokemon = pokemonArray.getJSONObject((0 until pokemonArray.length()).random())
                randomPokemon.optString("img", "")
                    .replace("http://", "https://")
                    .takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val POKEDEX_URL =
            "https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json"
    }
}
