package com.example.book_quotes_hub.db

// Adjust package name as per your project
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Quote::class, BookItem::class], version = 2, exportSchema = false) // Version incremented to 2
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_quotes_hub_db" // It's good practice to append "_db" or similar
                )
                    // Add the migration here
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Define your migration here
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // This migration handles the case where you added a unique index on the 'id' column
        // in the 'quotes' table.
        // It's crucial that if you're adding a UNIQUE constraint,
        // the existing data in the database *must* already be unique for that column.
        // If there are duplicates, this SQL will fail.

        // Option A: If you're confident 'id' values were always unique or you cleaned old data:
        // Add the unique index. Room might automatically create indices from the @Entity definition
        // when upgrading the schema, but explicitly adding it here ensures it.
        // If the index already exists, IF NOT EXISTS prevents errors.
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_quotes_id ON quotes (id)")

        // Option B (More robust if duplicates might exist in old data):
        // This would involve creating a new table with the unique constraint,
        // copying data, dropping the old table, and renaming the new one.
        // This is much more complex and depends on how you want to handle duplicates
        // (e.g., keep the first, keep the last, merge).
        // Example for a complex migration with potential duplicates on `id`:
        /*
        // 1. Create a temporary new table with the desired schema and unique constraint
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS quotes_new (
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                id INTEGER NOT NULL UNIQUE, -- New unique constraint
                category TEXT NOT NULL,
                quote TEXT NOT NULL,
                author TEXT NOT NULL,
                description TEXT NOT NULL
                -- Add any other columns you have in your Quote entity
            )
        """)

        // 2. Copy unique data from the old 'quotes' table to the new 'quotes_new' table.
        // This example selects the minimum localId for each distinct 'id', effectively keeping
        // one record if duplicates exist based on 'id'. Adjust GROUP BY and SELECT as needed
        // if you have more columns or a different strategy for duplicates.
        database.execSQL("""
            INSERT INTO quotes_new (localId, id, category, quote, author, description)
            SELECT MIN(localId), id, category, quote, author, description
            FROM quotes
            GROUP BY id
        """)

        // 3. Drop the old 'quotes' table
        database.execSQL("DROP TABLE quotes")

        // 4. Rename the new table to 'quotes'
        database.execSQL("ALTER TABLE quotes_new RENAME TO quotes")

        // 5. Recreate any other indices that were on the original 'quotes' table (besides 'id' which is now unique)
        // For example, if you had an index on 'category':
        // database.execSQL("CREATE INDEX IF NOT EXISTS index_quotes_category ON quotes (category)")
        */

        // Choose ONLY ONE of the above options based on your exact needs and data integrity.
        // For simply adding the `Index` annotation for `id` as `unique = true`, Option A is often sufficient
        // if your existing data already implicitly conforms to the unique constraint.
        // If you are unsure, or if your app has been released and might have non-unique `id`s,
        // Option B (the `CREATE TABLE`, `COPY`, `DROP`, `RENAME` approach) is safer.
    }
}