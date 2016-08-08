package com.teenvan.buyhatke;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SearchResultsActivity extends AppCompatActivity {
    // Declaration of member variables
    private RecyclerView mSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mSearchResults = (RecyclerView)findViewById(R.id.searchResults);
        mSearchResults.setLayoutManager(new LinearLayoutManager(this));
        mSearchResults.setHasFixedSize(true);


        handleIntent(getIntent());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search
            Log.d("Query", query);
            query = ".*"+ query + ".*";
            HashMap<String, String> search = searchSmsInbox(query);
            Log.d("Search", search.size()+"");
            SmsAdapter adapter = new SmsAdapter(search, this);
            mSearchResults.setAdapter(adapter);
        }
    }


    private HashMap<String, String> searchSmsInbox(String regex){
        Log.d("Search String", regex);
        if(ContextCompat.checkSelfPermission(this, "android.permission.READ_SMS") ==
                PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = this.
                    getContentResolver().query(Uri.parse("content://sms/inbox"),
                    null, null, null, null);
            HashMap<String, String> mMessages = new HashMap<>();

            assert cursor != null;

            Pattern p = Pattern.compile(regex);

            int indexAddress = cursor.getColumnIndex("address");
            int indexBody = cursor.getColumnIndex("body");

            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    String sender = cursor.getString(indexAddress);
                    String body = cursor.getString(indexBody);


                    if(p.matcher(sender).matches() || p.matcher(body).matches()){
                        Log.d("Pattern", "true");
                        mMessages.put(sender,body);
                    }

                } while (cursor.moveToNext());
                cursor.close();
            } else {
                // empty box, no SMS
                cursor.close();
            }

            return mMessages;
        }else{
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
            HashMap<String, String> mMessages = new HashMap<>();
            return mMessages;
        }
    }
}
