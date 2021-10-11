package dev.mijey.popularloanbooks.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.popularloanbooks.databinding.BooksLoadStateFooterViewItemBinding

class BooksLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<BooksLoadStateAdapter.ReposLoadStateViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ReposLoadStateViewHolder {
        val binding = BooksLoadStateFooterViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReposLoadStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: ReposLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    inner class ReposLoadStateViewHolder(
        private val binding: BooksLoadStateFooterViewItemBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.also {
                it.setOnClickListener { retry.invoke() }
            }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.errorMsg.text = loadState.error.localizedMessage
            }
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.errorMsg.isVisible = loadState is LoadState.Error
        }
    }
}
