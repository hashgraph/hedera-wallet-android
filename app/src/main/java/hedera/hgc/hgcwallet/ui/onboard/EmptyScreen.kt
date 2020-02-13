package hedera.hgc.hgcwallet.ui.onboard

import android.content.Context
import android.view.View
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import hedera.hgc.hgcwallet.R

class EmptyScreen : Screen<EmptyView>() {

    override fun createView(context: Context): EmptyView {
        return EmptyView(context)
    }
}

class EmptyView(context: Context) : BaseScreenView<EmptyScreen>(context) {
    init {
        View.inflate(context, R.layout.view_empty, this)
    }
}