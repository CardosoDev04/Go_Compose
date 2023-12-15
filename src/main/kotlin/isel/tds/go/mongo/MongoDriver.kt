package isel.tds.go.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import java.io.Closeable

class MongoDriver(nameDb: String? = null): Closeable {
    val db: MongoDatabase
    private val client: MongoClient
    init {
        val envConnection = System.getenv("MONGO_CONNECTION") ?: throw MongoClientException("Missing MONGO_CONNECTION environment variable")
        val dbName = requireNotNull(nameDb ?: ConnectionString(envConnection).database) { "Missing dbName" }
        client = MongoClient.create(envConnection)
        db = client.getDatabase(dbName)
    }
    override fun close() = client.close()
}


class Collection<T: Any>(val collection: MongoCollection<T>)

inline fun <reified T: Any> MongoDriver.getCollection(id: String): Collection<T> =
    Collection(db.getCollection(id, T::class.java))

inline fun <reified T: Any> MongoDriver.getAllCollectionNames(): List<Collection<T>> =
    db.listCollectionNames().toList().map { getCollection<T>(it) }

fun <T: Any> Collection<T>.getAllDocuments(): List<T> =
    collection.find().toList()

fun <T: Any, K> Collection<T>.getDocument(id: K): T? =
    collection.find(Filters.eq(id)).firstOrNull()

fun <T: Any> Collection<T>.insertDocument(document: T) =
    collection.insertOne(document).insertedId != null

fun <T: Any, K> Collection<T>.replaceDocument(id: K, document: T): Boolean =
    collection.replaceOne(Filters.eq(id), document).modifiedCount == 1L

fun <T: Any, K> Collection<T>.deleteDocument(id: K): Boolean =
    collection.deleteOne(Filters.eq("_id", id)).deletedCount == 1L

fun <T: Any> Collection<T>.deleteAllDocuments(): Boolean =
    collection.deleteMany(Filters.exists("_id")).wasAcknowledged()