package fr.iutlens.dubois.list

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jxmpp.jid.EntityBareJid
import java.util.prefs.Preferences

object SmackStore {
    private var connection : XMPPTCPConnection? = null


    suspend fun attemptLogin(
        userName: String,
        domain: String,
        password: String,
        context: Context
    ) : Boolean {
        Status.update(Result.Processing("Connexion au serveur $domain"))
        try {
            Log.d("AttemptLogin","$userName@$domain ($password)")
            if (connection?.isAuthenticated== true) return true

            XMPPTCPConnection.setUseStreamManagementDefault(true)

            val configBuilder = XMPPTCPConnectionConfiguration.builder()
            configBuilder.setUsernameAndPassword(userName, password)
            configBuilder.setPort(5222)
            withContext(Dispatchers.IO){configBuilder.setXmppDomain(domain)}


            withContext(Dispatchers.IO){
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
        Status.update(Result.Success("Connexion au compte $userName@$domain réussie"))
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