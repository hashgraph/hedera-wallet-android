package hedera.hgc.hgcwallet.database.request

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class PayRequest(
        @PrimaryKey(autoGenerate = true) var requestId: Long,
        var accountId: String,
        var name: String?,
        var notes: String?,
        var amount: Long = 0,
        var importDate: Date
)