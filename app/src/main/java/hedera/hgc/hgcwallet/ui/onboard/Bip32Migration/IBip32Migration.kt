package hedera.hgc.hgcwallet.ui.onboard.Bip32Migration

import hedera.hgc.hgcwallet.crypto.HGCSeed
import hedera.hgc.hgcwallet.crypto.KeyPair
import hedera.hgc.hgcwallet.modals.HGCAccountID

interface IBip32Migration {
    fun getOldKey(): KeyPair
    fun getAccountID(): HGCAccountID
    fun bip32MigrationAborted()
    fun bip32MigrationRetry()
    fun bip32MigrationSuccessful(newSeed: HGCSeed, accountID: HGCAccountID)
}