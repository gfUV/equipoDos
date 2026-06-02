package com.wt2dadmuvy.spinbot.view.challenges

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.wt2dadmuvy.spinbot.R
import com.wt2dadmuvy.spinbot.databinding.DialogRandomChallengeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Diálogo personalizado para HU 12 - Mostrar reto aleatorio.
 *
 * Esta primera implementación cubre los criterios 1 al 4:
 * 1. Fondo negro degradado, transparencia sutil, bordes redondeados y borde blanco.
 * 2. Círculo superior con borde blanco y fondo negro + imagen aleatoria de Pokémon.
 * 3. Texto blanco en negrita con el reto aleatorio obtenido desde Room.
 * 4. Botón naranja "Cerrar" ubicado en la parte inferior-central del diálogo.
 */
class RandomChallengeDialogFragment : DialogFragment() {

    private var _binding: DialogRandomChallengeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRandomChallengeBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // HU 12 - Criterio 6:
        // El diálogo NO debe cerrarse al tocar por fuera ni con el botón Atrás.
        // Solo debe desaparecer cuando el jugador presione el botón "Cerrar".
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val challengeText = requireArguments().getString(ARG_CHALLENGE_TEXT).orEmpty()
        val pokemonImageUrl = requireArguments().getString(ARG_POKEMON_IMAGE_URL)

        binding.tvRandomChallenge.text = challengeText
        loadPokemonImage(pokemonImageUrl)

        binding.btnCloseRandomChallenge.setOnClickListener {
            // HU 12 - Criterio 5:
            // Al cerrar el diálogo se notifica al Home para dejar el juego listo
            // para una nueva partida, sin alterar el flujo anterior del proyecto.
            parentFragmentManager.setFragmentResult(REQUEST_KEY_CLOSED, bundleOf())
            dismiss()
        }
    }

    private fun loadPokemonImage(imageUrl: String?) {
        binding.imgPokemon.setImageDrawable(null)
        if (imageUrl.isNullOrBlank()) {
            binding.pbPokemon.isVisible = false
            binding.imgPokemon.setImageResource(R.drawable.ic_bottle_home)
            return
        }

        binding.pbPokemon.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 10000
                        readTimeout = 10000
                    }
                    connection.inputStream.use { input -> BitmapFactory.decodeStream(input) }
                } catch (_: Exception) {
                    null
                }
            }

            binding.pbPokemon.isVisible = false
            if (bitmap != null) {
                binding.imgPokemon.setImageBitmap(bitmap)
            } else {
                binding.imgPokemon.setImageResource(R.drawable.ic_bottle_home)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY_CLOSED = "random_challenge_dialog_closed"
        private const val ARG_CHALLENGE_TEXT = "arg_challenge_text"
        private const val ARG_POKEMON_IMAGE_URL = "arg_pokemon_image_url"

        fun newInstance(challengeText: String, pokemonImageUrl: String?): RandomChallengeDialogFragment {
            return RandomChallengeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHALLENGE_TEXT, challengeText)
                    putString(ARG_POKEMON_IMAGE_URL, pokemonImageUrl)
                }
            }
        }
    }
}
