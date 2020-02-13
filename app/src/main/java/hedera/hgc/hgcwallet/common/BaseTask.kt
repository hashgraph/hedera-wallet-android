package hedera.hgc.hgcwallet.common

abstract class BaseTask {
    var error: String? = null
    var result: Any? = null
    abstract fun main()
}
