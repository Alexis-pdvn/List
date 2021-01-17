package fr.iutlens.dubois.list.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
        @PrimaryKey val id : String,
                    val from_JID : String,
                    val to_JID : String,
                    val text : String,
                    val timestamp: Long = System.currentTimeMillis()
){
    companion object{
        fun create(message : org.jivesoftware.smack.packet.Message) : Message {
            return  Message(
                    message.stanzaId,
                    message.from.toString(),
                    message.to.toString(),
                    message.body.toString()
            )
        }
    }
}
