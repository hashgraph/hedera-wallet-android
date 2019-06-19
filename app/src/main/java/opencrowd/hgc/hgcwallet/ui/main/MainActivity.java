package opencrowd.hgc.hgcwallet.ui.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wealthfront.magellan.NavigationType;
import com.wealthfront.magellan.Navigator;
import com.wealthfront.magellan.Screen;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.grpc.okhttp.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.BaseTask;
import opencrowd.hgc.hgcwallet.common.TaskExecutor;
import opencrowd.hgc.hgcwallet.export_key.WebServer;
import opencrowd.hgc.hgcwallet.hapi.tasks.UpdateBalanceTaskAPI;
import opencrowd.hgc.hgcwallet.hapi.tasks.UpdateTransactionsTask;
import opencrowd.hgc.hgcwallet.app_intent.LinkAccountParams;
import opencrowd.hgc.hgcwallet.app_intent.LinkAccountRequestParams;
import opencrowd.hgc.hgcwallet.app_intent.TransferRequestParams;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.common.UserSettings;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.local_auth.AuthListener;
import opencrowd.hgc.hgcwallet.local_auth.AuthType;
import opencrowd.hgc.hgcwallet.ui.auth.fingerprint.FingerprintActivityHelper;
import opencrowd.hgc.hgcwallet.ui.main.account.AccountCreateScreen;
import opencrowd.hgc.hgcwallet.export_key.ExportKeyActivity;
import opencrowd.hgc.hgcwallet.crypto.KeyPair;
import opencrowd.hgc.hgcwallet.ui.BaseActivity;
import opencrowd.hgc.hgcwallet.local_auth.AuthManager;
import opencrowd.hgc.hgcwallet.ui.main.account.AccountListScreen;
import opencrowd.hgc.hgcwallet.ui.main.developertool.NodeScreen;
import opencrowd.hgc.hgcwallet.ui.main.home.AccountBalanceScreen;
import opencrowd.hgc.hgcwallet.ui.main.navigation_menu.AboutScreen;
import opencrowd.hgc.hgcwallet.ui.main.navigation_menu.BackupWalletScreen;
import opencrowd.hgc.hgcwallet.ui.main.request.RequestListScreen;
import opencrowd.hgc.hgcwallet.ui.scan.QRScanListener;
import opencrowd.hgc.hgcwallet.ui.main.settings.SettingsScreen;

public class MainActivity extends BaseActivity {

    TextView request, account, settings;
    ImageView mHomeImage, mSideMenuImage;
    TextView mActionBartitle;

    @Override
    protected Navigator createNavigator() {
        return Navigator.withRoot(new AccountBalanceScreen()).build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//Line Should be reviewed
        setContentView(R.layout.activity_home);
        mHomeImage = findViewById(R.id.image_home);
        mSideMenuImage = findViewById(R.id.image_side_menu);
        request = findViewById(R.id.request);
        account = findViewById(R.id.account);
        settings = findViewById(R.id.settings);
        mActionBartitle = findViewById(R.id.actionbar_title);
        mHomeImage.setVisibility(View.VISIBLE);
        mSideMenuImage.setVisibility(View.VISIBLE);
        mActionBartitle.setText("");

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ImageView syncButton = findViewById(R.id.image_sync);
        syncButton.setVisibility(View.VISIBLE);

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synchronizeData(true, true);
            }
        });

        mHomeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unCheckAllBottomTab();
                switchToScreen(new AccountBalanceScreen());
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSelectedtab(request, R.drawable.ic_requests_on);
                switchToScreen(new RequestListScreen());
            }
        });
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSelectedtab(account, R.drawable.ic_accounts_on);
                switchToScreen(new AccountListScreen(null));
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSelectedtab(settings, R.drawable.ic_settings_on);
                switchToScreen(new SettingsScreen());
            }
        });

        mSideMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
            }
        });

        NavigationView navigationView2 = findViewById(R.id.nav_view2);
        MenuItem enablePin = navigationView2.getMenu().findItem(R.id.enable_pin);
        if (enablePin != null) {
            if (isPinAuthEnabled())
                enablePin.setTitle(R.string.menuItem_change_pin);
            else
                enablePin.setTitle(R.string.menuItem_enable_pin);
        }

        MenuItem account0 = navigationView2.getMenu().findItem(R.id.default_account);
        if (account0 != null)
            account0.setTitle(DBHelper.getAllAccounts().get(0).getName());


        navigationView2.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                int id = menuItem.getItemId();
                String text = "";
                switch (id) {

                    case R.id.default_account:
                        getNavigator().goTo(new AccountCreateScreen(DBHelper.getAllAccounts().get(0), "ACCOUNT DETAILS", true));
                        break;

                    case R.id.requests:
                        getNavigator().goTo(new RequestListScreen());
                        break;
                    case R.id.backup_phrases:
                        getNavigator().goTo(new BackupWalletScreen());
                        break;
                    case R.id.export_key:
                        if (Singleton.INSTANCE.checkInternetConnType(MainActivity.this, ConnectivityManager.TYPE_WIFI)) {
                            String ip = WebServer.Companion.getWifiIP();
                            if (ip != null) {
                                Intent intent = new Intent(MainActivity.this, ExportKeyActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }else
                                Singleton.INSTANCE.showToast(MainActivity.this, getResources().getString(R.string.export_key_not_wifi_msg));
                        } else
                            Singleton.INSTANCE.showToast(MainActivity.this, getResources().getString(R.string.export_key_not_wifi_msg));

                        break;
                    case R.id.enable_face_fingerprint:
                        enableFingerprintAuth();
                        break;
                    case R.id.enable_pin:
                        enablePinAuth();
                        break;
                    case R.id.nodes:
                        getNavigator().goTo(new NodeScreen());
                        break;
                    case R.id.synchronize_data:
                        synchronizeData(true, true);
                        break;
                    case R.id.profile:
                        getNavigator().goTo(new SettingsScreen());
                        break;
                    case R.id.app_info:
                        getNavigator().goTo(new AboutScreen());
                        break;


                }

                drawer.closeDrawer(GravityCompat.END);
                return true;
            }

        });
    }

    public void clearCache() {
        for (Account account : DBHelper.getAllAccounts()) {
            account.setBalance(0);
            DBHelper.saveAccount(account);
        }
        App.instance.database.txnRecordDao().deleteAll();
        unCheckAllBottomTab();
        switchToScreen(new AccountBalanceScreen());
        Singleton.INSTANCE.showToast(MainActivity.this, "Cache Cleared");
    }

    private void enableFingerprintAuth() {
        if (AuthManager.INSTANCE.getAuthType() != AuthType.FINGER) {
            FingerprintActivityHelper helper = new FingerprintActivityHelper();
            helper.setup(this, true, null);
            if (helper.errorMsg == null) {
                setupAuth(AuthType.FINGER);
            } else Singleton.INSTANCE.showToast(this, helper.errorMsg);
        } else {
            Singleton.INSTANCE.showToast(this, getResources().getString(R.string.biometric_already_enabled_msg));
        }
    }

    private void enablePinAuth() {
        //currently on enable pin and change pin we have same UI
        setupAuth(AuthType.PIN);
    }

    private boolean isPinAuthEnabled() {
        return AuthManager.INSTANCE.getAuthType() == AuthType.PIN;
    }

    public void synchronizeData(final boolean showIndicator, final boolean fetchRecords) {
        if (UserSettings.instance.getBoolValue(UserSettings.KEY_ASKED_FOR_QUERY_COST_WARNING)) {
            synchronizeDataPrivate(showIndicator,fetchRecords);

        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.warning_query_cost)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            UserSettings.instance.setValue(UserSettings.KEY_ASKED_FOR_QUERY_COST_WARNING, true);
                            synchronizeDataPrivate(showIndicator,fetchRecords);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            UserSettings.instance.setValue(UserSettings.KEY_ASKED_FOR_QUERY_COST_WARNING, true);
                        }
                    }).create().show();
        }

    }

    private void synchronizeDataPrivate(final boolean showIndicator, final boolean fetchRecords) {
        if (showIndicator)
            showActivityProgress("Fetching balances");
        final TaskExecutor balanceTaskExecutor = new TaskExecutor();
        final TaskExecutor transactionTaskExecutor = new TaskExecutor();
        transactionTaskExecutor.setListner(new TaskExecutor.TaskListner() {
            @Override
            public void onResult(BaseTask task1) {
                hideActivityProgress();
                if (task1.error != null) {
                    Singleton.INSTANCE.showToast(MainActivity.this, (getString(R.string.failed_to_fetch_records) + "\n" + task1.error));
                }
                reloadHomeTabData();
            }
        });

        balanceTaskExecutor.setListner(new TaskExecutor.TaskListner() {
            @Override
            public void onResult(BaseTask task1) {
                hideActivityProgress();
                if (task1.error != null) {
                    Singleton.INSTANCE.showToast(MainActivity.this, (getString(R.string.failed_to_fetch_balances) + "\n" + task1.error));
                    reloadHomeTabData();
                } else {
                    if (fetchRecords) {
                        if (showIndicator)
                            showActivityProgress("Fetching records");
                        transactionTaskExecutor.execute(new UpdateTransactionsTask());
                    } else {
                        reloadHomeTabData();
                    }
                }

            }
        });

        balanceTaskExecutor.execute(new UpdateBalanceTaskAPI());
    }

    private void reloadHomeTabData() {
        switchToScreen(new AccountBalanceScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthManager.INSTANCE.hasAuth()) {
            requestAuth(0);
        } else {
            checkForPendingURLIntent();
        }
    }


    private void checkForPendingURLIntent() {
        String urlString = UserSettings.instance.getValue(UserSettings.KEY_INTENT_URL);
        if (urlString != null) {
            Uri uri = Uri.parse(urlString);
            if (uri != null) {
                handleURLIntent(uri);
                UserSettings.instance.resetValue(UserSettings.KEY_INTENT_URL);
            }
        }

        JSONObject branchParam = UserSettings.instance.getJSONValue(UserSettings.KEY_BRANCH_PARAMS);
        if (branchParam != null) {
            handleBranchParams(branchParam);
            UserSettings.instance.resetValue(UserSettings.KEY_BRANCH_PARAMS);
        }
    }

    private void handleURLIntent(@NonNull Uri uri) {
        /*TransferRequestParams params = TransferRequestParams.from(uri);
        if (params != null) {
            handleTrasferRequestParam(params);
        }*/
    }

    private void handleBranchParams(@NonNull JSONObject params) {
        LinkAccountParams linkAccountParams = LinkAccountParams.from(params);
        if (linkAccountParams != null) {
            handleLinkAccount(linkAccountParams);
        } else {
            LinkAccountRequestParams linkAccountRequestParams = LinkAccountRequestParams.from(params);
            if (linkAccountRequestParams != null)
                handleLinkAccountRequest(linkAccountRequestParams);
            else {
                TransferRequestParams transferRequestParams = TransferRequestParams.from(params);
                if (transferRequestParams != null) {
                    handleTrasferRequestParam(transferRequestParams);
                }
            }
        }
    }

    private void handleTrasferRequestParam(TransferRequestParams params) {
        DBHelper.createPayRequest(params.account, params.amount, params.name, params.note);
        DBHelper.createContact(params.account, params.name, false);
        checkSelectedtab(request, R.drawable.ic_requests_on);
        switchToScreen(new RequestListScreen());
    }

    private void handleLinkAccount(final LinkAccountParams params) {
        List<Account> accounts = DBHelper.getAllAccounts();
        for (Account account : accounts) {
            String pk = Singleton.INSTANCE.publicKeyString(account).trim().toLowerCase();
            if (pk.equals(params.address.stringRepresentation().toLowerCase())) {
                account.setAccountID(params.accountID);
                DBHelper.saveAccount(account);
                Singleton.INSTANCE.showDefaultAlert(MainActivity.this, "Account linked", "Your account " + account.getName() + " is successfully linked with your accountID", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (params.redirect != null) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, params.redirect);
                            startActivity(browserIntent);
                        }
                    }
                });

                break;
            }
        }
    }

    private void handleLinkAccountRequest(final LinkAccountRequestParams params) {
        ArrayList<String> list = new ArrayList<>();
        List<Account> accounts = DBHelper.getAllAccounts();
        final ArrayList<Account> unlinkedAccounts = new ArrayList<>();
        for (Account account : accounts) {
            if (account.accountID() == null) {
                unlinkedAccounts.add(account);
                String pk = account.getName() + " ( ..." + Singleton.INSTANCE.publicKeyStringShort(account) + " )";
                list.add(pk);
            }
        }
        if (!list.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select account to link");
            String[] items = list.toArray(new String[0]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    makeLinkAccountRequest(params, unlinkedAccounts.get(which));
                }
            });

            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void makeLinkAccountRequest(final LinkAccountRequestParams params, final Account account) {


        TaskExecutor taskExecutor = new TaskExecutor();
        taskExecutor.setListner(new TaskExecutor.TaskListner() {
            @Override
            public void onResult(BaseTask task) {
                if (task.error == null) {
                    Singleton.INSTANCE.showDefaultAlert(MainActivity.this, "Sent", "Your request for link account has been sent successfully", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (params.redirect != null) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, params.redirect);
                                startActivity(browserIntent);
                            }
                        }
                    });

                } else {
                    Singleton.INSTANCE.showDefaultAlert(MainActivity.this, "Error", "Failed to send link account request");
                }
            }
        });
        taskExecutor.execute(new BaseTask() {
            @Override
            public void main() {
                OkHttpClient client = new OkHttpClient();
                client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
                MediaType MEDIA_TYPE_MARKDOWN
                        = MediaType.parse("application/octet-stream");
                KeyPair keyPair = Singleton.INSTANCE.keyForAccount(account);
                InputStream inputStream = new ByteArrayInputStream(keyPair.getPublicKey());

                RequestBody requestBody = new RequestBodyUtil().create(MEDIA_TYPE_MARKDOWN, inputStream);
                Request request = new Request.Builder()
                        .url(params.callback.toString())
                        .post(requestBody).addHeader("Content-Type","application/octet-stream").addHeader("User-Agent","HGCApp/")
                        .build();


                Response response = null;
                boolean success = false;
                try {
                    response = client.newCall(request).execute();
                    success =  response.isSuccessful();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                error = success ? null : "";
            }
        });

    }

    public void switchToScreen(Screen screen) {
        if (!getNavigator().atRoot()) {
            getNavigator().goBackToRoot(NavigationType.NO_ANIM);
        }
        getNavigator().replaceNow(screen);
    }

    private void unCheckAllBottomTab() {
        Drawable requestImage = getResources().getDrawable(R.drawable.ic_requests, null);
        Drawable accountImage = getResources().getDrawable(R.drawable.ic_accounts, null);
        Drawable settingsImage = getResources().getDrawable(R.drawable.ic_settings, null);
        request.setCompoundDrawablesWithIntrinsicBounds(null, requestImage, null, null);
        account.setCompoundDrawablesWithIntrinsicBounds(null, accountImage, null, null);
        settings.setCompoundDrawablesWithIntrinsicBounds(null, settingsImage, null, null);
        request.setTextColor(getResources().getColor(R.color.gray, null));
        account.setTextColor(getResources().getColor(R.color.gray, null));
        settings.setTextColor(getResources().getColor(R.color.gray, null));
    }

    private void checkSelectedtab(TextView textView, int drawable) {
        unCheckAllBottomTab();
        Drawable requestImage = getResources().getDrawable(drawable, null);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, requestImage, null, null);
        textView.setTextColor(getResources().getColor(R.color.tint, null));
    }

    @Override
    public void onAuthSetupSuccess() {

    }

    @Override
    public void onAuthSuccess(int requestCode) {
        checkForPendingURLIntent();
        Screen currentScreen = getNavigator().currentScreen();
        if (currentScreen instanceof AuthListener) {
            AuthListener listener = (AuthListener) currentScreen;
            listener.onAuthSuccess(requestCode);
        }
    }

    @Override
    public void onAuthFailed(int requestCode, boolean isCancelled) {
        finish();
    }

    @Override
    public void onAuthSetupFailed(boolean isCancelled) {
        Screen currentScreen = getNavigator().currentScreen();
        if (currentScreen instanceof AuthListener) {
            AuthListener listener = (AuthListener) currentScreen;
            listener.onAuthSetupFailed(isCancelled);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            Screen currentScreen = getNavigator().currentScreen();
            if (currentScreen instanceof QRScanListener) {
                QRScanListener listener = (QRScanListener) currentScreen;
                if (result.getContents() == null) {
                    listener.onQRScanFinished(false, null);
                } else {
                    listener.onQRScanFinished(true, result.getContents());
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public class RequestBodyUtil {

        public  RequestBody create(final MediaType mediaType, final InputStream inputStream) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return mediaType;
                }

                @Override
                public long contentLength() {
                    try {
                        return inputStream.available();
                    } catch (IOException e) {
                        return 0;
                    }
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    Source source = null;
                    try {
                        source = Okio.source(inputStream);
                        sink.writeAll(source);
                    } finally {
                        Util.closeQuietly(source);
                    }
                }
            };
        }
    }
}
