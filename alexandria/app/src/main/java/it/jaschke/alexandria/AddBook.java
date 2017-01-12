package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;



import it.jaschke.alexandria.Utility.Utility;
import it.jaschke.alexandria.barcodescanner.ScannerActivity;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


//import android.app.Fragment;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {


    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private TextView tvMessage;
    private final String EAN_CONTENT="eanContent";
    public static final int PICK_BAR_CODE=1;
    private RelativeLayout mProgressBar;






    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public void onResume() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        //register localbroadcast
        IntentFilter intentFilter=new IntentFilter(getString(R.string.bookservice_filter));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, intentFilter);

        super.onResume();

    }

    @Override
    public void onPause() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        //unregister localbroadcast
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }
    //Broadcastreceiver to listen for messages from BookService
    private BroadcastReceiver mMessageReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressBar.setVisibility(View.GONE);
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);
        tvMessage=(TextView) rootView.findViewById(R.id.tvMessage);
        mProgressBar= (RelativeLayout) rootView.findViewById(R.id.rlprogressBar);
        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                tvMessage.setVisibility(View.INVISIBLE);
                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith(getString(R.string.ean_prefix))){
                    ean=getString(R.string.ean_prefix)+ean;
                }
                if(ean.length()<13){
                    clearFields();
                    return;
                }
                // if the books is already in database there is no need to
                //fetch the book
                if(isBookInDatabase(ean) && savedInstanceState==null){
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText(getString(R.string.book_already_in_db));

                } else {
                    //Once we have an ISBN, start a book intent
                    mProgressBar.setVisibility(View.VISIBLE);
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                    AddBook.this.restartLoader();
                }
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent barCodeIntent = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(barCodeIntent,PICK_BAR_CODE);

            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            if(!(ean.length()==0)) {
                ean.setHint(getString(R.string.empty_string));
            }
           // restartLoader();
        }

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("VILLANUEVA", String.valueOf(requestCode));
        if (requestCode == PICK_BAR_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                String scanResult = data.getStringExtra("result");
                if (scanResult != null) {
                    updateUi(scanResult);

                }

            }

        }
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith(getString(R.string.ean_prefix))){
            eanStr=getString(R.string.ean_prefix)+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        //prevent the app from crashing when there is no author
        if(authors!=null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        //check to avoid exception java.net.MalformedURLException caused by images with url equals
        // to empty string
        if(!imgUrl.equals(getString(R.string.empty_string))) {
            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
                rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
            }
        }
        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        data.close();

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.tvMessage).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    public void updateUi(String barcode){

        ean.setText(barcode);

    }

    /**
     * See if the book is already in the database
     */
    private boolean isBookInDatabase(String ean){
        boolean isInDatabase=false;
        Cursor bookEntry =getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        if(bookEntry.getCount()>0)
            isInDatabase=true;
        return isInDatabase;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getActivity().getString(R.string.pref_net_status_key))){
            updateNetworkUi();
        }
    }

    private void updateNetworkUi() {
        int message=R.string.no_network;
        @BookService.NetStatus int status= Utility.getNetworkStatus(getActivity());
        if (status==BookService.NET_STATUS_OK) {
            return;
        }
        switch (status) {

            case BookService.NET_STATUS_INVALID_JSON:
                message=R.string.network_status_invalid_json;
                break;
            case BookService.NET_STATUS_KO:
                message=R.string.no_network;
                break;
            case BookService.NET_STATUS_UNKNOWN:
                message=R.string.network_status_unknown;
                break;

        }
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
    }




}
