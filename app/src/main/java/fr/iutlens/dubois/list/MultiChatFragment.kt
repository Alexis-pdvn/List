package fr.iutlens.dubois.list

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import fr.iutlens.dubois.list.database.Message
import fr.iutlens.dubois.list.message.MessageAdapter
import fr.iutlens.dubois.list.multichat.MultiChatAdapter
import fr.iutlens.dubois.list.multichat.MultiChatModel
import fr.iutlens.dubois.list.util.Result
import fr.iutlens.dubois.list.util.SmackStore
import fr.iutlens.dubois.list.util.Status
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.fragment_roster.*


/**
 * A simple [Fragment] subclass.
 */
class MultiChatFragment : Fragment(), TextView.OnEditorActionListener {
    private var messageList: LiveData<List<Message>>? = null
    private val chatModel: MultiChatModel by activityViewModels()
    private lateinit var adapter: MultiChatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // The callback can be enabled or disabled here or in the lambda
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_multi_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())

        adapter = MultiChatAdapter(null, null)
        recyclerViewChat.adapter = adapter


        Status.result.observe(viewLifecycleOwner){
            updateChatManager()
        }
        updateChatManager()

        editTextMessage.setOnEditorActionListener(this)

        // Gestion de l'ouverture du clavier virtuel : on se positionne en bas de la liste
        view.viewTreeObserver.addOnGlobalLayoutListener {
            if (recyclerViewChat == null) return@addOnGlobalLayoutListener
            val pos : Int? = (recyclerViewChat.adapter as MultiChatAdapter).currentList.lastIndex
            if (pos != null && pos != -1){ recyclerViewChat.smoothScrollToPosition(pos) }
        }
    }

    private fun updateChatManager() {
        if (Status.result.value is Result.Success) {
            chatModel.updateConnection()
            messageList = chatModel.allMessages()
            messageList?.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)
                if (list.lastIndex != -1)
                    recyclerViewChat.smoothScrollToPosition(list.lastIndex)
            }
        }
    }


    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if(actionId == EditorInfo.IME_ACTION_SEND) {
            chatModel.send(editTextMessage.text.toString())
            editTextMessage.text.clear()
            return true
        }
        return false
    }

}