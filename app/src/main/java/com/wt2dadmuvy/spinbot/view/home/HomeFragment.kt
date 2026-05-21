package com.wt2dadmuvy.spinbot.view.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
// Comentado temporalmente si da error por no usarse aún:
// androidx.navigation.fragment.findNavController
import com.wt2dadmuvy.spinbot.R
import com.wt2dadmuvy.spinbot.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    // Configuración de View Binding para acceder de forma segura a las vistas
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // MP1-24 Criterio 3: El estado del audio inicia por defecto en ENCENDIDO (true)
    private var isAudioOn = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos los listeners de tu Toolbar Personalizada
        setupToolbarListeners()
    }

    private fun setupToolbarListeners() {
        // Accedemos a los elementos usando el ID del include 'customToolbar'

        // MP1-23 Criterio 2: Ícono estrella manda a simulación de tienda (Nequi)
        binding.customToolbar.btnCalificar.setOnClickListener {
            val playStoreUrl = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            startActivity(intent)
        }

        // MP1-24 Criterio 3: Interruptor del audio de fondo (ON / OFF) con cambio visual
        binding.customToolbar.btnAudio.setOnClickListener {
            isAudioOn = !isAudioOn
            if (isAudioOn) {
                // Cambia al ícono de audio encendido
                binding.customToolbar.btnAudio.setImageResource(R.drawable.volume_up)
                Toast.makeText(requireContext(), "Audio: Encendido", Toast.LENGTH_SHORT).show()
                // TODO: Tus compañeros de la lógica de sonido usarán esto para REINICIAR el audio
            } else {
                // Cambia al ícono de audio apagado (con la X)
                binding.customToolbar.btnAudio.setImageResource(R.drawable.volume_off)
                Toast.makeText(requireContext(), "Audio: Pausado", Toast.LENGTH_SHORT).show()
                // TODO: Tus compañeros de la lógica de sonido usarán esto para PAUSAR el audio
            }

            // Mantiene el color naranja oficial (#FFFF5A00) en el nuevo ícono asignado
            val colorNaranja = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.orange)
            binding.customToolbar.btnAudio.imageTintList = android.content.res.ColorStateList.valueOf(colorNaranja)
        }

        // MP1-25 Criterio 4: Ícono instrucciones navega a HU 5.0
        binding.customToolbar.btnInstrucciones.setOnClickListener {
            // SE COMENTA PARA EVITAR ERRORES HASTA QUE SE CREA LA RUTA EN EL NAV_GRAPH
            // findNavController().navigate(R.id.action_homeFragment_to_instructionsFragment)
            Toast.makeText(requireContext(), "Navegación a Instrucciones (Pendiente por el equipo)", Toast.LENGTH_SHORT).show()
        }

        // MP1-26 Criterio 5: Ícono retos navega a HU 6.0
        binding.customToolbar.btnRetos.setOnClickListener {
            // SE COMENTA PARA EVITAR ERRORES HASTA QUE SE CREA LA RUTA EN EL NAV_GRAPH
            // findNavController().navigate(R.id.action_homeFragment_to_challengesFragment)
            Toast.makeText(requireContext(), "Navegación a Retos (Pendiente por el equipo)", Toast.LENGTH_SHORT).show()
        }

        // MP1-27 Criterio 6: Ícono compartir app lanza BottomSheet nativo con datos de Nequi
        binding.customToolbar.btnCompartir.setOnClickListener {
            val textoACompartir = "App pico botella\nSolo los valientes lo juegan !!\nhttps://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textoACompartir)
            }
            // Muestra el menú nativo de compartir (Criterio 1 de HU 10)
            startActivity(Intent.createChooser(shareIntent, "Compartir aplicación vía:"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitamos fugas de memoria (Memory Leaks)
    }
}