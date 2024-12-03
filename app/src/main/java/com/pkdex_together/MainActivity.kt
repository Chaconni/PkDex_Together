package com.pkdex_together

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val verFavoritosButton: Button = findViewById(R.id.verFavoritos)
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", "") ?: ""




        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            val intent = Intent(this, PokeDetailsActivity::class.java)
            intent.putExtra("POKEMON_ID", 1) // ID inicial del Pokémon
            startActivity(intent)
        }


        val loginButton: Button = findViewById(R.id.login_Button)

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        if (userEmail.isEmpty()) {
            verFavoritosButton.isEnabled = false
            Toast.makeText(this, "Debes iniciar sesión para ver favoritos.", Toast.LENGTH_SHORT).show()
        } else {
            verFavoritosButton.isEnabled = true
        }

        verFavoritosButton.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail) // Pasa el email del usuario
            startActivity(intent)
        }
    }

}