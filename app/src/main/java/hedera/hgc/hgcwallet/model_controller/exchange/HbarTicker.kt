package hedera.hgc.hgcwallet.model_controller.exchange

import androidx.collection.SimpleArrayMap
import hedera.hgc.hgcwallet.Config
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.UserSettings
import hedera.hgc.hgcwallet.data.exchange.Exchange
import hedera.hgc.hgcwallet.data.exchange.HbarPrice
import hedera.hgc.hgcwallet.model.exchange.*
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.slf4j.LoggerFactory

object HbarTicker {

    fun load() {
        for (exchange in Exchange.values()) {
            val subject = rawSubjects.get(exchange)!!
            val reader = Single.fromCallable {
                UserSettings.getRawTickerCacheData(exchange)
            }.subscribeOn(foregroundScheduler()) // FIXME: this should be done in the background
                    .observeOn(foregroundScheduler())
                    .subscribe({ result ->
                        if (result.data.isNotEmpty()) {
                            subject.onNext(result)
                        }
                        load2(subject)
                    }, { error ->
                        log?.debug("Unable to read ticker from user settings cache for " +
                                "${exchange.id}: ${error.message}")
                        load2(subject)
                    })
            workInProgress.add(reader)
            val output = subject.map { rawHbarPrice ->
                RawExchangeWebsite.process(rawHbarPrice)
            }.observeOn(foregroundScheduler())
            outputs.put(exchange, output)
        }
    }

    fun requestHbarPriceFromExchanges() {
        for (exchange in Exchange.values()) {
            val rawSubject = rawSubjects.get(exchange)!!
            val reader = Single.fromCallable {
                RawExchangeWebsite.rawHbarPriceFrom(exchange)
            }.subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        if (result.data.isNotEmpty()) {
                            rawSubject.onNext(result)
                        }
                    }, { error ->
                        log?.debug("Unable to read ticker from ${exchange.id}: ${error.message}")
                    })
            workInProgress.add(reader)
        }
    }

    fun forExchange(exchange: Exchange): Observable<HbarPrice> = outputs.get(exchange)!!

    private fun foregroundScheduler() = AndroidSchedulers.mainThread()
    private val log = if (Config.isLoggingEnabled) LoggerFactory.getLogger(this.javaClass) else null

    private val rawSubjects: SimpleArrayMap<Exchange, Subject<RawHbarPrice>> = {
        val map = SimpleArrayMap<Exchange, Subject<RawHbarPrice>>(Exchange.values().size)
        for (exchange in Exchange.values()) {
            map.put(exchange,
                    BehaviorSubject.createDefault(RawHbarPrice(exchange, "", 0L)))
        }
        map
    }()

    private var outputs = SimpleArrayMap<Exchange, Observable<HbarPrice>>(3)

    private val workInProgress = CompositeDisposable()

    private fun load2(subject: Subject<RawHbarPrice>) {
        val userSettingsWriter = subject.subscribe{ next ->
            // todo: proper observers chained appropriately, and/or move exchange rate out of
            //       singleton
            UserSettings.setRawTickerCacheData(next)
            Singleton.loadExchangeRate()
        }
        workInProgress.add(userSettingsWriter)
    }
}