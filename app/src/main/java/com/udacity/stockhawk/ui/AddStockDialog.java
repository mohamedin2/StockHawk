package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Stock;
import com.udacity.stockhawk.sync.SymbolAutoCompleteAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddStockDialog extends DialogFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    DelayAutoCompleteTextView stock;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    final int THRESHOLD = 2;

    private AlertDialog mDialog;
    private boolean isValidSymbol = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setThreshold(THRESHOLD);
        final SymbolAutoCompleteAdapter adapter = new SymbolAutoCompleteAdapter(getActivity());
        stock.setAdapter(adapter);
        stock.setLoadingIndicator(progressBar);
        stock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Stock stockObj = (Stock) adapterView.getItemAtPosition(position);
                stock.setText(stockObj.getSymbol());
                isValidSymbol = true;
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                String str = stock.getText().toString();
                for(int i = 0; i < adapter.getCount(); i++) {
                    Stock stock = adapter.getItem(i);
                    if(str.equalsIgnoreCase(stock.getSymbol())) { //symbol found
                        isValidSymbol = true;
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        return;
                    }
                }
                isValidSymbol = false;
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                isValidSymbol = false;
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (isValidSymbol)
                    addStock();
                return true;
            }
        });
        builder.setView(custom);

        builder.setMessage(getString(R.string.dialog_title));
        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        mDialog = builder.create();

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return mDialog;
    }

    private void addStock() {
        Activity parent = getActivity();
        if (parent instanceof MainActivity) {
            ((MainActivity) parent).addStock(stock.getText().toString());
        }
        dismissAllowingStateLoss();
    }


}
