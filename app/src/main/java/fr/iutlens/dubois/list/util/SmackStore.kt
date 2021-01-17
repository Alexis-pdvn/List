package fr.iutlens.dubois.list.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import fr.iutlens.dubois.list.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.offline.OfflineMessageManager

object SmackStore {
    private var connection : XMPPTCPConnection? = null

    private var _roster : Roster? = null

    val roster : Roster? get() {
        if (_roster == null) _roster =Roster.getInstanceFor(connection)
        return _roster
    }


    private var _chatManager : ChatManager? = null
    val chatManager : ChatManager? get() {
        if (_chatManager == null) _chatManager = ChatManager.getInstanceFor(connection)
        Log.d("ChatManager", _chatManager.toString())
        return _chatManager
    }

    val offlineMessageManager : OfflineMessageManager? get(){
        connection?.let { return OfflineMessageManager.getInstanceFor(connection) }
        return null
    }

    private var _multiChatManager : MultiUserChatManager? = null
    val multiChatManager : MultiUserChatManager? get() {
        if (_multiChatManager == null) _multiChatManager = MultiUserChatManager.getInstanceFor(connection)
        Log.d("MultiChatManager", _chatManager.toString())
        return _multiChatManager
    }


    private var _jid : String? = null

    val jid : String? get(){return  _jid
    }

    suspend fun attemptLogin(
        userName: String,
        domain: String,
        password: String,
        context: Context
    ) : Boolean {
        if (connection?.isAuthenticated== true) return true
        _roster = null
        _chatManager = null
        _jid = null
        Status.update(Result.Processing("Connexion au serveur $domain"))
        try {
            Log.d("AttemptLogin","$userName@$domain ($password)")
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all)

            XMPPTCPConnection.setUseStreamManagementDefault(true)

            val configBuilder = XMPPTCPConnectionConfiguration.builder()
            configBuilder.setUsernameAndPassword(userName, password)
            configBuilder.setPort(5222)
            configBuilder.setSendPresence(true)
            configBuilder.setResource(context.getString(R.string.app_name))

            withContext(Dispatchers.IO){
                configBuilder.setXmppDomain(domain)
                connection = XMPPTCPConnection(configBuilder.build()).also {
                    it.connect()
                }
            }
            if (connection?.isConnected == true) {
                Log.v("SmackStore", "-> attemptLogin -> connected")
            } else {
                Status.update(Result.Error("Echec de la connection à $domain"))
                return false
            }


            Status.update(Result.Processing("Identification de $userName en cours sur $domain"))

            withContext(Dispatchers.IO){  connection?.login()}
            if (connection?.isAuthenticated == true) {
                Log.v("SmackStore", "-> attemptLogin ->  authenticated")
            } else {
                Status.update(Result.Error("Echec de l'identification"))
                return false
            }



        } catch (exception:Exception){
            Status.update(Result.Error("Erreur"))
            return false
        }
        _jid = "$userName@$domain"
        Status.update(Result.Success("Connexion au compte $jid réussie"))
        store(userName,domain,password,context)

        return true
    }

    private fun store(userName: String, domain: String, password: String, context: Context) {
        context.getSharedPreferences( "login", MODE_PRIVATE).edit()
                .putString("userName",userName)
                .putString("domain",domain)
                .putString("password",password).apply()
    }

    fun attemptDefaultLogin(context :Context) {
        if (connection?.isAuthenticated== true) return
        val pref = context.getSharedPreferences("login", MODE_PRIVATE)
        val userName = pref.getString("userName",null) ?: return
        val domain = pref.getString("domain",null) ?: return
        val password = pref.getString("password",null) ?: return
        runBlocking {
            withContext(Dispatchers.Main) {
                attemptLogin(userName, domain, password, context)
            }
        }
    }

    fun neverLogged(context: Context): Boolean {
        val pref = context.getSharedPreferences("login", MODE_PRIVATE)
        return pref.getString("userName",null) == null
    }



}