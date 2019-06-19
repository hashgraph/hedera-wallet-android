package opencrowd.hgc.hgcwallet.ui.main.navigation_menu;



import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.Config;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;
import opencrowd.hgc.hgcwallet.ui.onboard.TermsScreen;

public class AboutScreen extends Screen<AboutScreenView>{
    @Override
    protected AboutScreenView createView(Context context) {
        return new AboutScreenView(context);
    }
}

class AboutScreenView extends BaseScreenView<AboutScreen> {

    private TextView mTextViewVersion,mTextVeiwBuild;
    private Button mBtnTerm,mBtnPolicy;

    public AboutScreenView(Context context) {
        super(context);
        inflate(context, R.layout.view_about_layout, this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("ABOUT");

        mTextViewVersion = (TextView)findViewById(R.id.text_version);
        mTextVeiwBuild = (TextView)findViewById(R.id.text_build);
        mBtnTerm = (Button)findViewById(R.id.btn_term);
        mBtnPolicy = (Button)findViewById(R.id.btn_privacy_policy);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                sendLogcatMail();
                return false;
            }
        });

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            if(versionName!=null) {
                mTextViewVersion.setText("Version "+versionName);
            }
            mTextVeiwBuild.setText("Build "+String.valueOf(versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mBtnTerm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new TermsScreen(false, Singleton.INSTANCE.contentFromFile(Config.termsFile), "Terms & Conditions"));
            }
        });
        mBtnPolicy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreen().getNavigator().goTo(new TermsScreen(false, Singleton.INSTANCE.contentFromFile(Config.privacyFile), "Privacy Policy"));
            }
        });

    }

    public void sendLogcatMail(){

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // Set type to "email"
        emailIntent.setType("message/rfc822");
        String to[] = {""};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent .putExtra(Intent.EXTRA_TEXT, Singleton.INSTANCE.getApiLogs().toString());
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "android wallet logs");
        getScreen().getActivity().startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

//    public static StringBuilder readLogs() {
//        StringBuilder logBuilder = new StringBuilder();
//        try {
//            Process process = Runtime.getRuntime().exec("logcat -d -v long");
//            BufferedReader bufferedReader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                logBuilder.append(line + "\n");
//            }
//        } catch (IOException e) {
//        }
//        return logBuilder;
//    }

}
