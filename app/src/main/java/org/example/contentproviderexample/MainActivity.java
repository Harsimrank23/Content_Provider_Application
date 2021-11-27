package org.example.contentproviderexample;

import static android.Manifest.permission.READ_CONTACTS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.example.contentproviderexample.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView contactNames;

    // for requesting permissions:
    private static final int REQUEST_CODE_READ_CONTACTS = 1;
//    private static boolean READ_CONTACTS_GRANTED = false;
    FloatingActionButton fab = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactNames = (ListView) findViewById(R.id.contact_names);

      int hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission= " + hasReadContactPermission);

        if(hasReadContactPermission!=PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "onCreate: requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab onClick: starts");

//                if (READ_CONTACTS_GRANTED) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,READ_CONTACTS)==PackageManager.PERMISSION_GRANTED){
                    String[] projection = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

                    // now we will add contentResolver:
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                            projection,
                            null,
                            null,
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

                    if (cursor != null) {
                        List<String> contacts = new ArrayList<String>();
                       while (cursor.moveToNext()) {
                           contacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
                       }
                      cursor.close();
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.contact_detail, R.id.name, contacts);
                        contactNames.setAdapter(adapter);
                    }
                } else {
                    Snackbar.make(view, "This app can't display your Contact Records unless you...", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Grant Access", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "onClick: starts");
//                                    Toast.makeText(MainActivity.this,"Snackbar action clicked",Toast.LENGTH_SHORT).show();
                                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,READ_CONTACTS))
                                    {
                                        Log.d(TAG, "Snackbar onClick:calling permissions ");
                                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{READ_CONTACTS},REQUEST_CODE_READ_CONTACTS);
                                    }
                                    else{
                                        // if method returns false i.e user has permanently denied the permission then take the user to app setting...
                                        Log.d(TAG, "Snackbar onClick:launching settings ");
                                        Intent intent=new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        // uri consists of a scheme such as https or file in this case package and scheme is followed by ssp for scheme specific part in our case that's just the package name so we use the uri from parts method to build up the uris so that we ultimately need to pass as data to intent .
                                        Uri uri=Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                                        Log.d(TAG, "Snackbar onClick:Intent Uri is "+uri.toString());
                                        intent.setData(uri);
                                        MainActivity.this.startActivity(intent);
                                    }
                                    Log.d(TAG, "Snackbar onClick :ends ");
                                }
                            }).show();
                }
                Log.d(TAG, "fab  onClick: ends");
            }
        });
        Log.d(TAG, "onCreate: ends");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
