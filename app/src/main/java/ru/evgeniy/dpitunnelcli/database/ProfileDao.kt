package ru.evgeniy.dpitunnelcli.database

import androidx.room.*

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles_table")
    suspend fun getAll(): List<Profile>

    @Query("SELECT * FROM profiles_table WHERE id = :id")
    suspend fun findById(id: Int): Profile?

    @Insert
    suspend fun insertProfile(profile: Profile)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(profile: Profile)

    @Query("UPDATE profiles_table SET title = :newTitle WHERE id = :id")
    suspend fun rename(id: Int, newTitle: String)

    @Query("DELETE FROM profiles_table WHERE id = :id")
    suspend fun delete(id: Int)

    suspend fun insertOrUpdate(profile: Profile) {
        val profileFromDB = profile.id?.let { findById(it) }
        if (profileFromDB == null)
            insertProfile(profile)
        else
            update(profile)
    }
}