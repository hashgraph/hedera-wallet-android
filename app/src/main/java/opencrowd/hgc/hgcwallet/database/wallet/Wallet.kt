package opencrowd.hgc.hgcwallet.database.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import opencrowd.hgc.hgcwallet.modals.HGCKeyType

@Entity
class Wallet(
        @PrimaryKey(autoGenerate = true)
        var walletId: Long = 0,
        var totalAccounts: Long = 0,
        var keyType: String = ""
) {
    constructor() : this(0, 0, "")


    fun getHGCKeyType(): HGCKeyType {
        return Wallet.keyTypeFrom(keyType)
    }

    companion object {

        fun createWallet(type: HGCKeyType): Wallet {
            val type = when (type) {
                HGCKeyType.ED25519 -> "ED25519"
                HGCKeyType.RSA3072 -> "RSA3072"
                HGCKeyType.ECDSA384 -> "ECDSA384"
            }
            return Wallet(0, 0, type)
        }

        fun keyTypeFrom(keyType: String): HGCKeyType {
            when (keyType) {
                "ED25519" -> return HGCKeyType.ED25519

                "RSA3072" -> return HGCKeyType.RSA3072

                "ECDSA384" -> return HGCKeyType.ECDSA384
            }
            return HGCKeyType.ECDSA384
        }
    }
}
