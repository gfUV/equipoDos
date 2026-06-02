package com.wt2dadmuvy.spinbot.view.challenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wt2dadmuvy.spinbot.databinding.ItemChallengeBinding
import com.wt2dadmuvy.spinbot.model.Challenge

// Adaptador del RecyclerView para la lista de retos — usa DiffUtil para actualizaciones eficientes
class ChallengeAdapter(
    private val onEditClick: (Challenge) -> Unit,
    private val onDeleteClick: (Challenge) -> Unit
) : ListAdapter<Challenge, ChallengeAdapter.ChallengeViewHolder>(DiffCallback) {

    inner class ChallengeViewHolder(
        private val binding: ItemChallengeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(challenge: Challenge) {
            binding.tvChallengeDescription.text = challenge.description

            // Criterio 9: botón editar → el Fragment lanza el diálogo de edición (HU 8)
            binding.btnEditChallenge.setOnClickListener { onEditClick(challenge) }

            // Criterio 10: botón eliminar → el Fragment lanza el diálogo de eliminación (HU 9)
            binding.btnDeleteChallenge.setOnClickListener { onDeleteClick(challenge) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Challenge>() {
        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge) =
            oldItem == newItem
    }
}
