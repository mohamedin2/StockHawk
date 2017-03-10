package com.udacity.stockhawk.sync;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by themo on 3/10/2017.
 */
public class SymbolAutoCompleteAdapter extends BaseAdapter implements Filterable {
    public final String LOG_TAG = SymbolAutoCompleteAdapter.class.getSimpleName();
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<Stock> resultList = new ArrayList<>();

    public SymbolAutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Stock getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(/*android.R.layout.two_line_list_item*/R.layout.simple_dropdown_item_2line, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).getSymbol());
        ((TextView) convertView.findViewById(R.id.text2)).setText(getItem(position).getName());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Stock> stocks = findStocks(mContext, constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = stocks;
                    filterResults.count = stocks.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Stock>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }


    private List<Stock> getSymbolsDataFromJson(String symbolsJsonStr)
            throws JSONException {
        List<Stock> stocks = new ArrayList<>();
        try {
            JSONObject symbolsJson = new JSONObject(symbolsJsonStr);

            // TODO do we have an error?
//            if ( symbolsJson.has(OWM_MESSAGE_CODE) ) {
//                int errorCode = symbolsJson.getInt(OWM_MESSAGE_CODE);
//
//                switch (errorCode) {
//                    case HttpURLConnection.HTTP_OK:
//                        break;
//                    case HttpURLConnection.HTTP_NOT_FOUND:
//                        setLocationStatus(getContext(), LOCATION_STATUS_INVALID);
//                        return;
//                    default:
//                        setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
//                        return;
//                }
//            }

            final String YF_RS = "ResultSet";
            final String YF_SYM_ARR = "Result";
            final String YF_SYMBOL = "symbol";
            final String YF_NAME = "name";
            final String YF_TYPE = "type";
            final String EQUITY_TYPE = "S";

            JSONArray symbolsArray = symbolsJson.getJSONObject(YF_RS).getJSONArray(YF_SYM_ARR);

            for(int i = 0; i < symbolsArray.length(); i++) {
                String symbol, name, type;
                JSONObject symbolObj = symbolsArray.getJSONObject(i);

                symbol = symbolObj.getString(YF_SYMBOL);
                name = symbolObj.getString(YF_NAME);
                type = symbolObj.getString(YF_TYPE);
                if (EQUITY_TYPE.equals(type)) {
                    stocks.add(new Stock(symbol, name));
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
//            setLocationStatus(getContext(), LOCATION_STATUS_SERVER_INVALID);
        }
        return stocks;
    }

    /**
     * Returns a search result for the given book title.
     */
    private List<Stock> findStocks(Context context, String companyName) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String symbolsJsonStr = null;
        try {
            final String YF_BASE_URL =
                    "http://d.yimg.com/autoc.finance.yahoo.com/autoc?region=1&lang=en&callback=YAHOO.Finance.SymbolSuggest.ssCallback&";
            final String QUERY_PARAM = "query";

            Uri.Builder uriBuilder = Uri.parse(YF_BASE_URL).buildUpon();

            Uri builtUri = uriBuilder.appendQueryParameter(QUERY_PARAM, companyName)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return new ArrayList<>();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
//                setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
                return new ArrayList<>();
            }
            symbolsJsonStr = buffer.substring("YAHOO.Finance.SymbolSuggest.ssCallback(".length(), buffer.length() - 1);
            return  getSymbolsDataFromJson(symbolsJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
//            setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
//            setLocationStatus(getContext(), LOCATION_STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return new ArrayList<>();
    }
}

