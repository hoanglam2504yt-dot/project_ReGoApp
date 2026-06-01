package com.example.rego.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rego.databinding.FragmentChatBinding
import com.example.rego.ui.adapters.ChatAdapter
import com.example.rego.ui.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    
    private lateinit var chatAdapter: ChatAdapter
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String get() = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Nhận receiverId (ID người nhắn cùng) từ arguments
        val receiverId = arguments?.getString("receiverId") ?: ""
        
        setupRecyclerView()
        setupListeners(receiverId)
        observeViewModel()

        if (receiverId.isNotEmpty() && currentUserId.isNotEmpty()) {
            // Khởi tạo phòng chat dựa trên ID của mình và người kia
            viewModel.setupChat(currentUserId, receiverId)
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(currentUserId)
        binding.recyclerViewMessages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupListeners(receiverId: String) {
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty() && currentUserId.isNotEmpty()) {
                viewModel.sendMessage(text, currentUserId, receiverId)
                binding.editTextMessage.text.clear()
            }
        }
    }

    private fun observeViewModel() {
        // Lắng nghe danh sách tin nhắn realtime
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    chatAdapter.submitList(messages) {
                        if (messages.isNotEmpty()) {
                            binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
                        }
                    }
                }
            }
        }

        // Lắng nghe thông tin người nhận để cập nhật UI Toolbar
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.receiverUser.collect { user ->
                    user?.let {
                        // Hiển thị tên lên Toolbar của Activity
                        (activity as? AppCompatActivity)?.supportActionBar?.title = it.name
                        // Cung cấp avatar cho adapter để hiển thị bên trái tin nhắn
                        chatAdapter.setReceiverAvatar(it.avatarUrl)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
