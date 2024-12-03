package com.pkdex_together

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pkdex_together.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db = AppDatabase(this)

        // Botón de registro
        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                db.insertUser(email, password)
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de inicio de sesión
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val isValid = db.validateUser(email, password)
                if (isValid) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    navigateToMain(email)
                } else {
                    Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain(email: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USER_EMAIL", email)
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
