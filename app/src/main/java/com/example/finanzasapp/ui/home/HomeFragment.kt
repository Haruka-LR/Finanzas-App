package com.example.finanzasapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import com.example.finanzasapp.data.repository.CategoryRepository
import com.example.finanzasapp.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            ExpenseRepository(),
            CategoryRepository()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.example.finanzasapp.R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // Aquí se actualiza la UI con state.totalMonth, state.recentExpenses, etc.
                // Ejemplo:
                // textViewTotal.text = state.totalMonth.toString()
            }
        }
    }
}