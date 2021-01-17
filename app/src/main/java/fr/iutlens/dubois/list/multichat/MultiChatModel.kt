package fr.iutlens.dubois.list.multichat

import android.util.Log
import androidx.lifecycle.*
import fr.iutlens.dubois.list.database.AppDatabase
import fr.iutlens.dubois.list.util.SmackStore
import kotlinx.coroutines.launch
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.roster.RosterListener
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Localpart
import org.jxmpp.jid.parts.Resourcepart
import org.jxmpp.jid.util.JidUtil
import org.jxmpp.util.XmppStringUtils

class MultiChatModel() : ViewModel(), MessageListener {

    var _chat : MultiUserChat? = null
    private val room = "testmmi@conference.xabber.de"

    val chat : MultiUserChat? get(){
        if (_chat == null)
            _chat =  SmackStore.multiChatManager?.getMultiUserChat(JidCreate.entityBareFrom(room))
        return _chat
    }

    fun updateConnection() {
        if (chat?.isJoined ==  true) return
        chat?.apply {
            createOrJoin(Resourcepart.from(XmppStringUtils.parseLocalpart(SmackStore.jid)))?.makeInstant()
            this.
            addMessageListener(this@MultiChatModel)
        }
    }


    fun allMessages() : LiveData<List<fr.iutlens.dubois.list.database.Message>>? {
        return  AppDatabase.getDatabase()?.messageDao()?.getAllChat("$room%")?.asLiveData()
    }


    fun insert(message: Message) =  viewModelScope.launch {
        AppDatabase.getDatabase()?.messageDao()?.insertAll(
                fr.iutlens.dubois.list.database.Message.create(message)
        )
    }


    override fun processMessage(message: Message?) {
        if (message == null) return
        insert(message)
    }

    fun send(text: String) {
        chat?.sendMessage(text)
    }


}