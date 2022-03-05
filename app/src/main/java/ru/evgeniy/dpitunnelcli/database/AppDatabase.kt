package ru.evgeniy.dpitunnelcli.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Profile::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                prepopulateDb(getInstance(context))
                            }
                        })
                        .fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance
            }
        }

        private fun prepopulateDb(db: AppDatabase) = CoroutineScope(Dispatchers.Default).launch {
            db.profileDao().insertProfile(
                Profile(
                    id = null,
                    name = "default",
                    title = "Auto TTL universal",
                    bufferSize = null,
                    splitPosition = 3,
                    splitAtSni = true,
                    autoTtl = true,
                    fakePacketsTtl = null,
                    windowSize = 1,
                    windowScaleFactor = 6,
                    inBuiltDNS = true,
                    inBuiltDNSIP = "8.8.8.8",
                    inBuiltDNSPort = null,
                    doh = true,
                    dohServer = "https://dns.google/dns-query",
                    desyncAttacks = true,
                    desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_FAKE,
                    desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER_FAKE
            )
            )
        }
    }
}