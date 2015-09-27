package edu.rose_hulman.quickpay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ch.uepaa.p2pkit.ConnectionCallbacks;
import ch.uepaa.p2pkit.ConnectionResult;
import ch.uepaa.p2pkit.ConnectionResultHandling;
import ch.uepaa.p2pkit.KitClient;
import ch.uepaa.p2pkit.discovery.InfoTooLongException;
import ch.uepaa.p2pkit.discovery.P2pListener;
import ch.uepaa.p2pkit.discovery.Peer;
import me.figo.FigoSession;
import me.figo.models.Account;
import me.figo.models.Payment;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements ConnectionCallbacks, P2pListener, AdapterView.OnItemClickListener {

    private static final int LOGIN_REQUEST = 3001;
    private static final int ACCOUNT_REQUEST = 3002;
    private String token;
    private UserAdapter adapter;
    private String accountNumber;
    private String bankCode;

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        token = sharedPrefs.getString(MainActivity.EXTRA_TOKEN, null);
        Date expires = new Date(sharedPrefs.getLong(MainActivity.EXTRA_EXPIRES, 0));
        accountNumber = sharedPrefs.getString(MainActivity.EXTRA_ACCOUNT, null);
        bankCode = sharedPrefs.getString(MainActivity.EXTRA_BANK, null);

        if (token == null || new Date().after(expires)) {
            Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
            startActivityForResult(loginIntent, LOGIN_REQUEST);

            return;
        }

        if (accountNumber == null || bankCode == null) {
            Intent accountIntent = new Intent(getActivity(), AccountsActivity.class);
            startActivityForResult(accountIntent, ACCOUNT_REQUEST);

            return;
        }

        final FigoSession session = new FigoSession(token, RequestQueueProvider.getInstance(getActivity()));
        session.getUser(new Response.Listener<me.figo.models.User>() {
            @Override
            public void onResponse(me.figo.models.User response) {
                try {
                    User me = new User();
                    me.accountNumber = accountNumber;
                    me.name = response.getName();
                    me.bankCode = bankCode;
                    KitClient.getInstance(getActivity()).getDiscoveryServices().setP2pDiscoveryInfo(me.write());
                } catch (InfoTooLongException e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        final int statusCode = KitClient.isP2PServicesAvailable(getActivity());

        if (statusCode == ConnectionResult.SUCCESS) {
            KitClient client = KitClient.getInstance(getActivity());
            client.registerConnectionCallbacks(this);

            if (!client.isConnected()) {
                client.connect(getString(R.string.p2pkit_app_key));
            }
        } else {
            ConnectionResultHandling.showAlertDialogForConnectionError(getActivity(), statusCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);

        adapter = new UserAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }

            token = data.getStringExtra(MainActivity.EXTRA_TOKEN);
            long expires = data.getLongExtra(MainActivity.EXTRA_EXPIRES, 0);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPrefs.edit()
                    .putLong(MainActivity.EXTRA_EXPIRES, expires)
                    .putString(MainActivity.EXTRA_TOKEN, token).apply();
        }
        else if(requestCode == ACCOUNT_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }

            accountNumber = data.getStringExtra(MainActivity.EXTRA_ACCOUNT);
            bankCode = data.getStringExtra(MainActivity.EXTRA_BANK);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPrefs.edit()
                    .putString(MainActivity.EXTRA_ACCOUNT, accountNumber)
                    .putString(MainActivity.EXTRA_BANK, bankCode).apply();
        }
    }

    @Override
    public void onConnected() {
        KitClient.getInstance(getActivity()).getDiscoveryServices().addListener(this);
    }

    @Override
    public void onConnectionSuspended() {
        KitClient.getInstance(getActivity()).getDiscoveryServices().removeListener(this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStateChanged(int i) {

    }

    @Override
    public void onPeerDiscovered(Peer peer) {
        if(peer.getDiscoveryInfo() == null) {
            return;
        }

        User user = new User(peer);
        adapter.put(user);
    }

    @Override
    public void onPeerLost(Peer peer) {
        User user = new User(peer);
        adapter.remove(user);
    }

    @Override
    public void onPeerUpdatedDiscoveryInfo(Peer peer) {
        User user = new User(peer);
        adapter.put(user);
        this.showPaymentDialog();
//        String token = PreferenceManager
//                .getDefaultSharedPreferences(getActivity())
//                .getString(MainActivity.EXTRA_TOKEN, null);
//        FigoSession session = new FigoSession(token, RequestQueueProvider.getInstance(getActivity()));
//        Payment payment = new Payment("Transfer", user., user.accountNumber, user.bankCode, "QuickPay Transaction", BigDecimal.TEN);
//        session.addPayment(payment, new Response.Listener<Payment>() {
//            @override
//            public void onResponse(Payment payment) {
//                session.sub
//            }
//        }, );
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User user = adapter.getItem(position);
    }

    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View theView = inflater.inflate(R.layout.dialog_payment, null);

        final NumberPicker unit_euro = (NumberPicker) theView.findViewById(R.id.euro_picker);
        final NumberPicker cent = (NumberPicker) theView.findViewById(R.id.cent_picker);

        builder.setView(theView)
                .setPositiveButton(R.string.accept_price_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Log.d("DBG", "Amount is: " + unit_euro.getValue() + "." + cent.getValue());
                        // Pass back amount input here
                    }
                })
                .setNegativeButton(R.string.reject_price_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Blank
                    }
                });

        unit_euro.setMinValue(0);
        unit_euro.setMaxValue(100);

        String cents[] = new String[100];
        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                cents[i] = "0" + i;
            } else {
                cents[i] = "" + i;
            }
        }
        cent.setDisplayedValues(cents);

        cent.setMinValue(0);
        cent.setMaxValue(99);
        cent.setValue(0);

        builder.show();
    }
}
