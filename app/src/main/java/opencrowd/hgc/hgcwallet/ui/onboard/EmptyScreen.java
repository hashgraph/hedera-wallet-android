package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;


public class EmptyScreen extends Screen<EmptyScreenView> {

    @NonNull
    @Override
    protected EmptyScreenView createView(@NonNull Context context) {
        return new EmptyScreenView(context);
    }
}


class EmptyScreenView extends BaseScreenView<EmptyScreen> {
    Button mTermAccept;
    public EmptyScreenView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.view_empty,this);

    }

}
