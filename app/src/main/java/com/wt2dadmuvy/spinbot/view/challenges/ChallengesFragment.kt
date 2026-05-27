package com.wt2dadmuvy.spinbot.view.challenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wt2dadmuvy.spinbot.databinding.FragmentChallengesBinding
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.viewmodel.ChallengesViewModel
import com.wt2dadmuvy.spinbot.viewmodel.SharedAudioViewModel

/**
 * Pantalla principal de Retos - HU 6.0: Agregar y listar retos.
 *
 * Esta pantalla permite al jugador:
 * - Ver la lista de retos guardados en la base de datos local (Room/SQLite)
 * - Agregar un nuevo reto mediante el botón flotante (FAB) → lanza HU 7 (Jonatan)
 * - Editar un reto existente → lanza HU 8 (Alexandra)
 * - Eliminar un reto existente → lanza HU 9 (German)
 * - Regresar al home con la flecha atrás, restaurando el audio si estaba encendido
 */
class ChallengesFragment : Fragment() {

    // View Binding: acceso seguro a las vistas del layout sin findViewById
    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!

    // ViewModel propio: maneja la lista de retos y las operaciones de base de datos
    private val challengesViewModel: ChallengesViewModel by viewModels()

    // ViewModel compartido con HomeFragment y InstructionsFragment (HU 5):
    // permite saber si el audio estaba encendido y solicitar pausarlo/reanudarlo
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    // Adaptador del RecyclerView: recibe los callbacks de editar y eliminar
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // HU 6 Criterio 1: pausar el audio de fondo al entrar a esta pantalla
        pauseAudioOnEnter()

        // Configurar la lista de retos (RecyclerView + Adapter)
        setupRecyclerView()

        // Observar los cambios en la lista de retos desde la base de datos
        observeViewModel()

        // Configurar los listeners de los botones (flecha atrás y FAB)
        setupListeners()
    }

    override fun onDestroyView() {
        // Liberar el binding para evitar fugas de memoria (Memory Leaks)
        _binding = null
        super.onDestroyView()
    }

    // -------------------------------------------------------------------------
    // CONFIGURACIÓN DEL RECYCLERVIEW
    // -------------------------------------------------------------------------

    /**
     * Inicializa el RecyclerView con su LayoutManager y Adapter.
     * El Adapter recibe los callbacks para editar y eliminar cada reto.
     */
    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(
            // HU 6 Criterio 9: al dar click en editar, lanza el dialog de HU 8 (Alexandra)
            onEditClick = { challenge -> showEditDialog(challenge) },

            // HU 6 Criterio 10: al dar click en eliminar, lanza el dialog de HU 9 (German)
            onDeleteClick = { challenge -> showDeleteDialog(challenge) }
        )

        binding.rvChallenges.apply {
            // LinearLayoutManager muestra los ítems en lista vertical con scroll
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengeAdapter
        }
    }

    // -------------------------------------------------------------------------
    // OBSERVADORES DEL VIEWMODEL
    // -------------------------------------------------------------------------

    /**
     * Observa la lista de retos desde Room (base de datos local).
     * Cada vez que se agrega, edita o elimina un reto, esta función se llama
     * automáticamente y actualiza la lista en pantalla sin necesidad de recargar.
     *
     * HU 6 Criterio 6: los retos nuevos aparecen primero porque el DAO los ordena
     * por id DESC (más reciente primero).
     */
    private fun observeViewModel() {
        challengesViewModel.allChallenges.observe(viewLifecycleOwner) { challenges ->
            // submitList actualiza el RecyclerView de forma eficiente (solo cambia los ítems diferentes)
            challengeAdapter.submitList(challenges)
        }
    }

    // -------------------------------------------------------------------------
    // LISTENERS DE BOTONES
    // -------------------------------------------------------------------------

    /**
     * Configura los listeners del botón de regreso y del FAB (botón flotante).
     */
    private fun setupListeners() {

        // HU 6 Criterio 3: botón flecha atrás → regresa al home y restaura el audio
        binding.btnBackChallenges.setOnClickListener {
            restoreAudioOnExit()
            findNavController().navigateUp()
        }

        // HU 6 Criterio 8: FAB naranja → lanza el cuadro de diálogo de HU 7 (Jonatan)
        binding.fabAddChallenge.setOnClickListener {
            showAddDialog()
        }
    }

    // -------------------------------------------------------------------------
    // DIÁLOGOS (se conectan con los DialogFragments de los compañeros)
    // -------------------------------------------------------------------------

    /**
     * Lanza el cuadro de diálogo para AGREGAR un reto - HU 7 (Jonatan).
     */
    private fun showAddDialog() {
        AddChallengeDialogFragment().show(childFragmentManager, "AddChallengeDialog")
    }

    /**
     * Lanza el cuadro de diálogo para EDITAR un reto - HU 8 (Alexandra).
     *
     * TODO: Cuando Alexandra cree su EditChallengeDialogFragment, reemplazar el Toast.
     * Se debe pasar el reto seleccionado para que el dialog muestre su descripción actual.
     * Ejemplo:
     *   EditChallengeDialogFragment.newInstance(challenge).show(childFragmentManager, "EditDialog")
     *
     * @param challenge El reto que el jugador quiere editar.
     */
    private fun showEditDialog(challenge: Challenge) {
        // Placeholder temporal hasta que Alexandra entregue HU 8
        Toast.makeText(requireContext(), "Dialog Editar: ${challenge.description} - HU 8 (Alexandra)", Toast.LENGTH_SHORT).show()
    }

    /**
     * Lanza el cuadro de diálogo para ELIMINAR un reto - HU 9 (German).
     *
     * TODO: Cuando German cree su DeleteChallengeDialogFragment, reemplazar el Toast.
     * Se debe pasar el reto seleccionado para que el dialog muestre su descripción.
     * Ejemplo:
     *   DeleteChallengeDialogFragment.newInstance(challenge).show(childFragmentManager, "DeleteDialog")
     *
     * @param challenge El reto que el jugador quiere eliminar.
     */
    private fun showDeleteDialog(challenge: Challenge) {
        // Placeholder temporal hasta que German entregue HU 9
        Toast.makeText(requireContext(), "Dialog Eliminar: ${challenge.description} - HU 9 (German)", Toast.LENGTH_SHORT).show()
    }

    // -------------------------------------------------------------------------
    // MANEJO DEL AUDIO (HU 6 Criterio 1 y 3)
    // -------------------------------------------------------------------------

    /**
     * Pausa el audio de fondo al entrar a esta pantalla.
     * Solo pausa si el audio estaba encendido (no interrumpe si el usuario lo había apagado).
     *
     * NOTA PARA EL EQUIPO: Para que esto funcione, HomeFragment debe:
     * 1. Usar SharedAudioViewModel (activityViewModels) en lugar de la variable local isAudioOn.
     * 2. Observar sharedAudioViewModel.pauseRequested y pausar/reanudar el MediaPlayer según el valor.
     * 3. Al cambiar el botón de audio, llamar sharedAudioViewModel.setAudioOn(valor).
     */
    private fun pauseAudioOnEnter() {
        sharedAudioViewModel.requestPause(true)
    }

    /**
     * Restaura el audio de fondo al salir de esta pantalla hacia el home.
     * Solo reanuda si el audio estaba encendido antes de entrar (Criterio 3).
     */
    private fun restoreAudioOnExit() {
        sharedAudioViewModel.requestPause(false)
    }
}
