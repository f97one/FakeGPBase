package net.formula97.fakegpbase;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.formula97.fakegpbase.fragments.MessageDialogs;


public class MainActivity extends Activity implements MessageDialogs.DialogsButtonSelectionCallback {

    private TextView tvBuilderName;
    private TextView tvFighterName;
    private TextView tvScale;
    private TextView tvClass;
    private TextView tvModelNo;
    private TextView tvGunplaName;

    protected NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBuilderName = (TextView) findViewById(R.id.tvBuilderName);
        tvFighterName = (TextView) findViewById(R.id.tvFighterName);
        tvScale = (TextView) findViewById(R.id.tvScale);
        tvClass = (TextView) findViewById(R.id.tvClass);
        tvModelNo = (TextView) findViewById(R.id.tvModelNo);
        tvGunplaName = (TextView) findViewById(R.id.tvGunplaName);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent setting = new Intent(this, SettingActivity.class);
                startActivity(setting);
                return true;
            case R.id.action_register_gunpla:
                Intent register = new Intent(this, GunplaRegisterActivity.class);
                startActivity(register);
                return true;
            case R.id.action_select_gunpla:
                // TODO ガンプラ選択のDialogFragmentを表示する処理を書く

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onButtonPressed(String messageBody, int which) {
        if (messageBody.equals(getString(R.string.wish_to_enable)) &&
                which == MessageDialogs.PRESSED_POSITIVE) {
            // 設定画面をstartActivityする
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            } else {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // NFCアダプターが有効でない場合は、設定画面を呼ぶか否かをDialogを表示する
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()) {
                MessageDialogs dialogs = MessageDialogs.getInstance(
                        getString(R.string.dialgo_info),
                        getString(R.string.wish_to_enable),
                        MessageDialogs.BUTTON_BOTH
                );
                dialogs.show(getFragmentManager(), MessageDialogs.FRAGMENT_TAG);
            } else {
                // フォアグラウンドでのNFCタグ読み込みを開始
                Intent i = new Intent(this, this.getClass());
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0 ,i, PendingIntent.FLAG_UPDATE_CURRENT);
                mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } else {
            MessageDialogs dialogs = MessageDialogs.getInstance(
                    getString(R.string.dialog_warn),
                    getString(R.string.no_nfc_present),
                    MessageDialogs.BUTTON_POSITIVE);
            dialogs.show(getFragmentManager(), MessageDialogs.FRAGMENT_TAG);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // NFCタグ読み込みを中止
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if ("android.nfc.action.NDEF_DISCOVERY".equals(intent.getAction())) {
            byte[] tagRawData = intent.getByteArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        }
    }
}
