package com.wt2dadmuvy.spinbot.view.challenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wt2dadmuvy.spinbot.databinding.ItemChallengeBinding
import com.wt2dadmuvy.spinbot.model.Challenge

/**
 * Adaptador del RecyclerView para la lista de retos — HU 6 Criterio 4 y 5.
 *
 * ¿Qué es un Adapter?
 * Un RecyclerView no sabe qué datos mostrar ni cómo mostrarlos.
 * El Adapter actúa como puente: toma la lista de objetos Challenge y los convierte
 * en vistas visibles (ítems) dentro del RecyclerView.
 *
 * ¿Por qué ListAdapter y no RecyclerView.Adapter?
 * ListAdapter usa DiffUtil internamente para comparar la lista anterior con la nueva.
 * Solo actualiza los ítems que realmente cambiaron (más eficiente, con animaciones suaves).
 * Si usáramos notifyDataSetChanged(), recargaría TODA la lista aunque solo cambió un ítem.
 *
 * ¿Qué son los callbacks onEditClick y onDeleteClick?
 * Son funciones que se pasan desde ChallengesFragment al crear el Adapter.
 * Permiten que el Fragment decida qué hacer cuando el usuario toca editar o eliminar,
 * sin que el Adapter necesite conocer los diálogos (HU 8 y HU 9).
 * Esto sigue el principio de separación de responsabilidades.
 *
 * @param onEditClick   Función que se ejecuta al tocar el botón editar de un ítem.
 * @param onDeleteClick Función que se ejecuta al tocar el botón eliminar de un ítem.
 */
class ChallengeAdapter(
    private val onEditClick: (Challenge) -> Unit,
    private val onDeleteClick: (Challenge) -> Unit
) : ListAdapter<Challenge, ChallengeAdapter.ChallengeViewHolder>(DiffCallback) {

    /**
     * ViewHolder: representa una sola fila (ítem) visible en el RecyclerView.
     *
     * ¿Qué es un ViewHolder?
     * RecyclerView reutiliza las vistas que salen de pantalla para mostrar nuevos ítems
     * (de ahí su nombre: "recicla" las vistas). El ViewHolder guarda referencias a las
     * vistas de un ítem para no tener que buscarlas cada vez (mejora el rendimiento).
     *
     * Usamos View Binding (ItemChallengeBinding) para acceder a las vistas
     * de forma segura sin necesidad de findViewById.
     */
    inner class ChallengeViewHolder(
        private val binding: ItemChallengeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Llena las vistas del ítem con los datos del reto correspondiente.
         * Se llama automáticamente cada vez que el RecyclerView necesita mostrar un ítem.
         *
         * @param challenge El objeto Challenge con los datos a mostrar en este ítem.
         */
        fun bind(challenge: Challenge) {
            // Mostrar la descripción del reto en el TextView del ítem
            binding.tvChallengeDescription.text = challenge.description

            // HU 6 Criterio 9: botón editar → notifica al Fragment para lanzar HU 8 (Alexandra)
            // La animación de touch (ripple naranja) ya está definida en el XML del ítem
            binding.btnEditChallenge.setOnClickListener {
                onEditClick(challenge)
            }

            // HU 6 Criterio 10: botón eliminar → notifica al Fragment para lanzar HU 9 (German)
            // La animación de touch (ripple naranja) ya está definida en el XML del ítem
            binding.btnDeleteChallenge.setOnClickListener {
                onDeleteClick(challenge)
            }
        }
    }

    /**
     * Crea un nuevo ViewHolder cuando el RecyclerView necesita una vista nueva.
     * Infla (convierte de XML a vista) el layout "item_challenge.xml".
     * Solo se llama cuando no hay ViewHolders reciclados disponibles.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChallengeViewHolder(binding)
    }

    /**
     * Conecta un ViewHolder existente con los datos de la posición indicada.
     * Se llama cada vez que un ítem entra en pantalla (sea nuevo o reciclado).
     *
     * @param holder   El ViewHolder a actualizar.
     * @param position La posición en la lista (0 = primero = reto más reciente).
     */
    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffCallback: le indica a ListAdapter cómo comparar ítems viejos con nuevos.
     *
     * areItemsTheSame: ¿Son el mismo objeto? → compara por "id" (identidad única en BD).
     * areContentsTheSame: ¿Tienen el mismo contenido? → compara todos los campos.
     *
     * Si areItemsTheSame = true y areContentsTheSame = true → el ítem NO se actualiza.
     * Si areItemsTheSame = true y areContentsTheSame = false → solo ese ítem se actualiza.
     * Si areItemsTheSame = false → es un ítem nuevo, se inserta con animación.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Challenge>() {
        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge) =
            oldItem == newItem
    }
}
