package hedera.hgc.hgcwallet.database

import android.util.Log
import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.google.protobuf.InvalidProtocolBufferException
import com.hederahashgraph.api.proto.java.TransactionID
import org.spongycastle.util.encoders.Hex
import java.io.IOException
import java.util.*

class Converters {

    companion object {
        val gson: Gson = GsonBuilder().registerTypeAdapterFactory(ClassTypeAdapterFactory()).create()
    }

    @TypeConverter
    fun jsonToListOfStrings(value: String): List<String> {
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun listOfStringsToJson(value: List<String>): String {
        return gson.toJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun transactionIdToString(value: TransactionID?): String? {
        if (value == null) return null
        try {
            return Hex.toHexString(value.toByteArray()).toLowerCase()

        } catch (e: Exception) {
            return null
        }

    }

    @TypeConverter
    fun stringToTransactionID(value: String?): TransactionID? {
        if (value == null) return null
        try {
            return TransactionID.parseFrom(Hex.decode(value))
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @TypeConverter
    fun fromString(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {

        }.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }


    class ClassTypeAdapterFactory : TypeAdapterFactory {
        override fun <T> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? {
            return if (!Class::class.java.isAssignableFrom(typeToken.rawType)) {
                null
            } else ClassTypeAdapter() as TypeAdapter<T>
        }
    }

    class ClassTypeAdapter : TypeAdapter<Class<*>>() {
        companion object {
            private val TAG = "ClassTypeAdapter"
        }

        @Throws(IOException::class)
        override fun write(jsonWriter: JsonWriter, clazz: Class<*>?) {
            if (clazz == null) {
                jsonWriter.nullValue()
                return
            }
            jsonWriter.value(clazz.name)
        }

        @Throws(IOException::class)
        override fun read(jsonReader: JsonReader): Class<*>? {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull()
                return null
            }
            var clazz: Class<*>? = null
            try {
                clazz = Class.forName(jsonReader.nextString())
            } catch (exception: ClassNotFoundException) {
                Log.e(TAG, "read : ClassNotFoundException - ${exception.message
                        ?: ""}", exception.cause)
                throw IOException(exception)
            }

            return clazz
        }
    }
}