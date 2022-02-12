package ru.evgeniy.dpitunnelcli.database

import androidx.room.*

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles_table")
    suspend fun getAll(): List<Profile>

    @Query("SELECT * FROM profiles_table WHERE id = :id")
    suspend fun findByName(id: String): Profile?

    @Insert
    suspend fun insertProfile(profile: Profile)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(profile: Profile)

    @Query("UPDATE profiles_table SET title = :newTitle WHERE id = :id")
    suspend fun rename(id: String, newTitle: String)

    @Query("DELETE FROM profiles_table WHERE id = :id")
    suspend fun delete(id: String)

    suspend fun insertOrUpdate(profile: Profile) {
        val profileFromDB = findByName(profile.id)
        if (profileFromDB == null)
            insertProfile(profile)
        else
            update(profile)
    }
}