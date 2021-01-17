package fr.iutlens.dubois.list.multichat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.iutlens.dubois.list.R
import fr.iutlens.dubois.list.database.Message
import fr.iutlens.dubois.list.util.SmackStore
import org.jivesoftware.smack.util.StringUtils
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import org.jxmpp.util.XmppStringUtils

class MultiChatAdapter(private val onItemClickListener: ((Message) -> Unit)?,
                       private val onItemLongClickListener: ((Message) -> Boolean)?):
        ListAdapter<Message, MultiChatAdapter.ViewHolder>(ElementComparator()) {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.textView)
        val textViewPseudo: TextView = view.findViewById(R.id.textViewPseudo)

    }

    private val  FROM : Int  = 1
    private val  TO : Int  = 2


    override fun getItemViewType(position: Int): Int {

        return if (XmppStringUtils.parseResource(getItem(position).from_JID) ==
                XmppStringUtils.parseLocalpart(SmackStore.jid)) TO else FROM
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val layout = if (viewType == FROM) R.layout.chat_from_item else R.layout.chat_to_item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(layout, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val message =getItem(position)

        viewHolder.textViewMessage.text = message.text
        viewHolder.textViewPseudo.text = XmppStringUtils.parseResource(message.from_JID)

        // Set listeners on item (?.let content is executed only when listener is not null)
        onItemClickListener?.let {
            viewHolder.itemView.setOnClickListener { it(message) }
        }

        onItemLongClickListener?.let {
            viewHolder.itemView.setOnLongClickListener { it(message) }
        }
    }


    class ElementComparator : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem== newItem
        }
    }
}