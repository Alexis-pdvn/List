package fr.iutlens.dubois.list.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM message WHERE :jid LIKE from_JID OR  :jid LIKE to_JID ORDER BY timestamp")
    fun getAll(jid : String): Flow<List<Message>>

    @Query("SELECT * FROM message ORDER BY timestamp")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE  from_JID LIKE :room ORDER BY timestamp")
    fun getAllChat(room : String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg elements: Message)

    @Delete
    suspend fun delete(element: Message)

}