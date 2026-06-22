package com.wt2dadmuvy.spinbot.view.challenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wt2dadmuvy.spinbot.databinding.FragmentChallengesBinding
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.viewmodel.ChallengesViewModel
import com.wt2dadmuvy.spinbot.viewmodel.SharedAudioViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengesFragment : Fragment() {

    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!

    private val challengesViewModel: ChallengesViewModel by viewModels()
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()
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
        sharedAudioViewModel.requestPause(true)
        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(
            onEditClick   = { challenge -> showEditDialog(challenge) },
            onDeleteClick = { challenge -> showDeleteDialog(challenge) }
        )
        binding.rvChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengeAdapter
        }
    }

    private fun observeViewModel() {
        challengesViewModel.allChallenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
        }
    }

    private fun setupListeners() {
        binding.btnBackChallenges.setOnClickListener {
            sharedAudioViewModel.requestPause(false)
            findNavController().navigateUp()
        }

        binding.fabAddChallenge.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        AddChallengeDialogFragment().show(childFragmentManager, "AddChallengeDialog")
    }

    private fun showEditDialog(challenge: Challenge) {
        EditChallengeDialogFragment.newInstance(challenge)
            .show(childFragmentManager, "EditChallengeDialog")
    }

    private fun showDeleteDialog(challenge: Challenge) {
        DeleteChallengeDialogFragment.newInstance(challenge)
            .show(childFragmentManager, "DeleteChallengeDialog")
    }
}
