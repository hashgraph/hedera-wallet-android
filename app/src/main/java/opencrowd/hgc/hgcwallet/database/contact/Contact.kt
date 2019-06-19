package opencrowd.hgc.hgcwallet.database.contact

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity
data class Contact(@PrimaryKey var accountId: String, var name: String?, var isVerified: Boolean = false) {
}
