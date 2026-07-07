package com.mohdhussain.hrcontacts.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val destination = if (AuthRepository.getInstance(requireContext()).isLoggedIn()) {
            R.id.contactListFragment
        } else {
            R.id.welcomeFragment
        }
        findNavController().navigate(
            destination,
            null,
            navOptions { popUpTo(R.id.splashFragment) { inclusive = true } }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
