package com.teenvan.buyhatke;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 12;
    // Declaration of member variables
    private Button sendSmsButton;
    private GoogleApiClient mApiClient;
    private DriveId id;
    SmsInboxFragment fragment;
    HashMap<String,String> mMessages = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setTitle("BuyHatke");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        sendSmsButton = (Button)findViewById(R.id.sendSmsButton);
        sendSmsButton.setVisibility(View.VISIBLE);
        sendSmsButton.setEnabled(true);

        // Fragment Setup
        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        fragment = new SmsInboxFragment();
        transaction.replace(R.id.fragmentContainer, fragment);

        final SendSmsFragment sendSmsFragment = new SendSmsFragment();

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction t = manager.beginTransaction();
                t.replace(R.id.fragmentContainer,sendSmsFragment );
                t.addToBackStack(null);
                t.commit();


            }
        });
        transaction.commit();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView

        MenuItem it = menu.findItem(R.id.upload);
        it.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mApiClient.connect();
                return true;
            }
        });
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Query query = new Query.Builder()
                .addFilter(Filters.and(Filters.eq(
                        SearchableField.TITLE, "SMSMessages"),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(mApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        if (!result.getStatus().isSuccess()) {
                            //showMessage("Cannot create folder in the root.");
                        } else {
                            boolean isFound = false;
                            for(Metadata m : result.getMetadataBuffer()) {
                                if (m.getTitle().equals("SMSMessages")) {
                                    //showMessage("Folder exists");
                                    id = m.getDriveId();
                                    isFound = true;
                                    break;
                                }
                            }
                            if(!isFound) {
                                //showMessage("Folder not found; creating it.");
                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle("SMSMessages")
                                        .build();
                                Drive.DriveApi.getRootFolder(mApiClient)
                                        .createFolder(mApiClient, changeSet)
                                        .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                            @Override
                                            public void onResult(DriveFolder.DriveFolderResult result) {
                                                if (!result.getStatus().isSuccess()) {
                                                   // showMessage("Error while trying to create the folder");
                                                    Toast.makeText(MainActivity.this,
                                                            "Error while trying to create the folder",
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                 //   showMessage("Created a folder");
                                                    Toast.makeText(MainActivity.this,
                                                            "Created Folder!",
                                                            Toast.LENGTH_LONG).show();
                                                    id = result.getDriveFolder().getDriveId();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(MainActivity.this, "Found Folder",
                                        Toast.LENGTH_SHORT).show();
                            }

                            Drive.DriveApi.newDriveContents(mApiClient)
                                    .setResultCallback(driveContentsCallback);

                        }
                    }
                });


    }


    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                       // showMessage("Error while trying to create new file contents");
                        return;
                    }
                    DriveFolder folder = id.asDriveFolder();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("SMS")
                            .setMimeType("text/plain")
                            .setStarred(true).build();

                    DriveContents c = result.getDriveContents();

                    folder.createFile(mApiClient, changeSet, c)
                            .setResultCallback(fileCallback);
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback =
            new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {

                    mMessages= fragment.getMessages();

                    if (!result.getStatus().isSuccess()) {
                       // showMessage("Error while trying to create the file");
                        return;
                    }
                   // showMessage("Created a file: " + result.getDriveFile().getDriveId());
                   Toast.makeText(MainActivity.this,"File Created/Updated",Toast.LENGTH_SHORT)
                           .show();

                    DriveFile file = result.getDriveFile();
                    file.open(mApiClient, DriveFile.MODE_WRITE_ONLY, null)
                            .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult result) {
                            if (!result.getStatus().isSuccess()) {
                                // Handle error
                                return;
                            }
                            DriveContents driveContents = result.getDriveContents();
                            try {
                                // Append to the file.
                                OutputStream outputStream = driveContents.getOutputStream();

                                for(int i=0;i<mMessages.size();i++){
                                    String sender = (new ArrayList<>(mMessages.keySet())).get(i);
                                    String body = (new ArrayList<>(mMessages.values())).get(i);
                                    outputStream.write(sender.getBytes());
                                    outputStream.write("\n".getBytes());
                                    outputStream.write(body.getBytes());
                                    outputStream.write("\n".getBytes());
                                }

                            } catch (IOException e) {
                                Log.d("Main",e.getMessage());
                            }

                            driveContents.commit(mApiClient, null).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status result) {
                                    // Handle the response status
                                    if(result.isSuccess()){
                                        Toast.makeText(MainActivity.this, "Wrote to file", Toast.LENGTH_LONG).show();
                                    }else{
                                        Log.d("Main", result.getStatusMessage()+" Message")  ;
                                    }
                                }
                            });
                        }
                    });
                }
            };



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mApiClient.connect();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendSmsButton.setVisibility(View.VISIBLE);
        sendSmsButton.setEnabled(true);
        getSupportActionBar().setTitle("BuyHatke");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendSmsButton.setVisibility(View.VISIBLE);
        sendSmsButton.setEnabled(true);
        getSupportActionBar().setTitle("BuyHatke");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    public void setButtonEnabled(boolean f){
        sendSmsButton.setEnabled(f);
    }
}
