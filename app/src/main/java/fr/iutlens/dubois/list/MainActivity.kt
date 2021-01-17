package fr.iutlens.dubois.list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.google.android.gms.security.ProviderInstaller
import fr.iutlens.dubois.list.database.AppDatabase
import fr.iutlens.dubois.list.message.MessageModel
import fr.iutlens.dubois.list.util.Result
import fr.iutlens.dubois.list.util.SmackStore
import fr.iutlens.dubois.list.util.Status
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_roster.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jivesoftware.smack.android.AndroidSmackInitializer

class MainActivity : AppCompatActivity() {

    private val messageModel: MessageModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Décommentez la ligne suivante pour vider les informations de login au prochain lancement
//        getSharedPreferences("login", MODE_PRIVATE).edit().clear().apply()

        setContentView(R.layout.activity_main)

        Status.result.observe(this){
            textViewStatus.text=it.description
            textViewStatus.visibility =  if (it is Result.Success) View.GONE else View.VISIBLE
            progressBar.visibility = if (it is Result.Processing) View.VISIBLE else View.GONE

            if (it is Result.Success){
                messageModel.updateConnection()
                val fragment = MultiChatFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss()
            }
        }
/*
        messageModel.selection.observe(this){
            Log.d("Selection",it.jid.toString())
            val fragment = MessageFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                    .addToBackStack("OpenChat")
                .commitAllowingStateLoss()
        }
*/

        if(savedInstanceState == null) { // initial transaction should be wrapped like this
            val fragment : Fragment = if (SmackStore.neverLogged(this))  LoginFragment() else MultiChatFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()
        }
        init()
    }

    private fun init() {
        GlobalScope.launch {
            AppDatabase.getDatabase(this@MainActivity)
            ProviderInstaller.installIfNeeded(this@MainActivity)
            AndroidSmackInitializer.initialize(this@MainActivity);
            SmackStore.attemptDefaultLogin(this@MainActivity)
        }
    }
}