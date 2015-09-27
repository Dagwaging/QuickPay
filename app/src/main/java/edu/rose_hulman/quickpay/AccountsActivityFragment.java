package edu.rose_hulman.quickpay;

import android.app.Activity;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;

import me.figo.FigoSession;
import me.figo.models.Account;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccountsActivityFragment extends Fragment implements AdapterView.OnItemClickListener {

    private AccountAdapter adapter;
    private String token;

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        token = sharedPrefs.getString(MainActivity.EXTRA_TOKEN, null);

        final FigoSession session = new FigoSession(token, RequestQueueProvider.getInstance(getActivity()));
        session.getAccounts(new Response.Listener<List<Account>>() {
            @Override
            public void onResponse(List<Account> response) {
                adapter.setItems(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public AccountsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);

        adapter = new AccountAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Account account = adapter.getItem(position);

        Intent result = new Intent();
        result.putExtra(MainActivity.EXTRA_ACCOUNT, account.getAccountNumber());
        result.putExtra(MainActivity.EXTRA_BANK, account.getBankCode());

        getActivity().setResult(Activity.RESULT_OK, result);
        getActivity().finish();
    }
}
