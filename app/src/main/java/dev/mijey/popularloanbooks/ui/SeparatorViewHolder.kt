package dev.mijey.popularloanbooks.ui

import androidx.recyclerview.widget.RecyclerView
import dev.mijey.popularloanbooks.databinding.SeparatorViewItemBinding

class SeparatorViewHolder(
    private val binding: SeparatorViewItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(roundedRank: Int) {
        binding.roundedRank = roundedRank
    }
}
