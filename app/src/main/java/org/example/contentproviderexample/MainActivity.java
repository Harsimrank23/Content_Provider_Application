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

        // method to check if user has grant the permission or not
        // now there is a check self permission method in the framework but that only work for marshmallow and above now both the methods returns the same result but the context compat method first checks to see if it's running on android prior to api 23, if it is it just returns success because the new security model doesn't apply before android api 23
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission= " + hasReadContactPermission);
        // now if we run the app for first time we will get permissions_denied and we had to request the user for permissions:
        // ctrl+q read documentation of checkSelfPermission
//        if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "onCreate: permission granted");
////            READ_CONTACTS_GRANTED = true;
//            // if we have permissions then carry on
//        } else {
//            Log.d(TAG, "onCreate: requesting permission");
//            // but if permission is not granted we need to request it , now we can do that by calling the compat version of request
//            // there is a method builtin framework but it will work on api 23 and above
//            // first we provide is this next we provide an array containing the names of the permissions we're requesting now we only want a single permission but it still has to be provided in a string array because that's the type of the parameter.
//            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
//        }
        // or
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
                    // read documentation android contracts content provider -'https://developer.android.com/guide/topics/providers/contacts-provider'.
                    // we will use ContactsContract.Contacts table:Rows representing different people, based on aggregations of raw contact rows.

                    // now we will add contentResolver:
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                            projection,
                            null,
                            null,
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                    // so we get a content resolver from the activity using the get resolver method and then we use the content Resolver to query for the data we want.
                    // contentResolver.query returns a cursor so the parameters that were passing to the query method firstly uri so this somehow identifies the data source that we want to get data from now this could be database and a table for example but this code doesn't need to know where data is coming from at the moment ,2nd parameter is just a string array holding the names of the columns that we want to retrieve now we are just asking for the display_name_primary column here but we can put more field into the array if wanted. third parameter is null and that's a string containing a filter to determine which rows are returned so think of this a s where clause , null means we are asking to get all rows returned.4th parameter is for selection arguments so this is an array of values that will be used to replace placeholders in the selection string here we are passing null . th and the last parameter is the sort order this is just a string containing the names of the fields we want to data sorted by, it is just like order by clause in sql.
                    // we can use the cursor as we did in basic sql.
                    // to understand about contentResolver fully see video 192.
                    // it is a single instance of content resolver and is responsible for resolving or requests of the content by directing those requests to the appropriate content provider , when we call contentResolver.query the content resolver extracts the authority from the uri and uses that to decide which content provider it should ultimately send the query request to it then gets a cursor back from the content provider and returns the cursor to our calling code.

                    if (cursor != null) {
                        List<String> contacts = new ArrayList<String>();
                        // so if the cursor hasn't already moved to the first record then moved to next behave the same as moved to first so in other words it will position us to the first record no
                        while (cursor.moveToNext()) {
                            // so we are using array list to store the contact names in .
                            contacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
                            // we are using getColumnIndex here which will return column no with respect to the string passed and cursor will return string corresponding to that index.
                        }
                        // now we need to close the cursor and create n adapter for the listView
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

//    // override method onRequestPermissionResult:
//    // when android calls this method for us  it passes the response code when we request permissions that is what REQUEST_CODE_READ_CONTACTS for. by providing the unique response code when calling request permissions we can then tell which particular request we are actually getting the result so basically this method is called for all permission requests..
//    // the next two arguments are both the arrays the first contains the permissions we requested and the second contains a result for each permission , it is possible user grant some permissions and deny others if we request several at once so this allows to check which ones were accepted and which ones are denied.
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        Log.d(TAG, "onRequestPermissionsResult: starts");
//        switch (requestCode) {
//            case REQUEST_CODE_READ_CONTACTS: {
//                // if request is cancelled,the rest arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted yay! do the contact related tasks.
//                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
////                    READ_CONTACTS_GRANTED = true;
//                } else {
//                    //permission denied , boo! disable the functionality that depends on this permission
//                    Log.d(TAG, "onRequestPermissionsResult: permission refused");
//                }
//
////                fab.setEnabled(READ_CONTACTS_GRANTED); // not it will be enable if grant is there else it will not work in this way we can avoid crashing.
//            }
//        }
//        Log.d(TAG, "onRequestPermissionsResult: ends");
//    }

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

// now android includes quite a few content providers to allow apps to access data that's stored on an android device now that we're going to be using here will allow us to get access to users contact database again on an Android device now when we make a phone call or send a text message u can see all your contacts and search through them.
// so contacts app on android devices is registered a content provider that allows apps to access the phone's contacts provided that they request the necessary permissions.

// now lets start working:
// add list view in the content_main.xml and add constraints, in this particuar example we are going to use a single field the contacts name and its all going to be shown here in our listView.
// so next layout we need as a consequence of that is just a text widget to hold that name so make linearLayout with name contact_detail
// add single text view widget to this layout for the name.

// now its time to retrieve the contact details  so we're going to need a reference to the listView widget so lets starts writing code in MainActivity.
// then on fab.onClickListener add a string
// after completing above code we do need to change the permissions in android manifest file.
// <uses-permission android:name="android.permission.READ_CONTACTS"/> now with this app will run on any version of android before api 23
// for the api 23 and higher google changed the way that permissions work so we do not only change the permissions in manifest file but also need to write some code to request the permissions from the user when the app runs.
// read documentation - working with system permissions "https://developer.android.com/guide/topics/permissions/overview" video 194
// first step is declare permissions in manifest regardless of api version.

//The process of performing a permission request depends on the type of permission:
//        If the permission is an install-time permission, such as a normal permission or a signature permission, the permission is granted automatically at install time.
//        If the permission is a runtime permission, and if your app is installed on a device that runs Android 6.0 (API level 23) or higher, you must request the permission yourself.

// so now write code for requesting permissions at run time
// first declare required variables
// then write code for checking to see if the pp is already been granted the permission that it needs we do that by calling the check self permission method.


//    Run Time Permissions
// When we check the permission by calling the checkSelfPermission method, we'll automatically be granted the permission on devices running API 22 or earlier. So that bit's easy and there's nothing more we need to do.
// On API 23 and above, though, we have co request permission from the user. That's still quite straightforward - if the user grants the permission then we can proceed and everything's fine.
// If the user allows the permission then that's fine, but if they choose to deny it, we have to decide what we're going to do and how we're going to deal with the situation.
// In some apps, you could just disable the particular functionality that requires the permission In this app, though, that's not really an option. All the app does is access the contacts records and without the necessary permission to do that it's a bit of a pointless app.
// It gets slightly more complicated, because the next time the app is launched after the permission has been denied, the dialogue offers another choice
// When the user ticks that box, the only option provided is to deny the permission. That can be a bit confusing to us as developers, and the documentation doesn't make it very clear, but when they choose to allow the permission, then they won't be asked again. That's automatic, once you've allowed the permission then it remains allowed, unless you go into the settings to change it - and we'll have a look at that later.
// If the user ticks that box and denies the permission, they won't be asked to allow it again. So our app just won't get the permission and we have to cater for that in our code. Once again, some apps could just disable the particular functionality that needs the permission, but our contacts app becomes useless without it.

// now to find out whether the permission was granted or denied we have to implement a callback method that will be called once the user has made the decision so write code for that after the onCreate method.
// see documentation for handle the permissions request response.
// though app is very useless without permissions so before dealing with denied permission we should prevent the app from crashing when it doesn't have permission when we click on floating action button.so we can disable the fab when we do not have permission to access the button using variable and enable in onRequestPermissionResult.
// but this is not user friendly as app will not do anything now.
// so the challenge is to modify the OnCLickListener of the fab so that it's onClick method display a SnackBar if permission to view the contacts isn't granted.
// if the permission is granted , the contact details should be displayed , just as they were.
// so insert snackBar using if else condition in fab OnClickListener.

// ---------------SnackBars----------------------
// snackbars were introduced with material design and they are intended as alternative to Toast messages, snackbars are shown at the bottom.
// Both toast message and snackbar can be used to notify the user in a fairly way.
// snackBars are a lot more versatile than toast message , if we think toast message can be really easy to miss and there are two options how long toast message will stay on the screen when that specifies period expires toast message disappear now snack bars do actually do have another value we can use length_indefinite - This means that the Snackbar will be displayed from the time that is shown until either it is dismissed, or another Snackbar is shown.
// snackbars also contains a clickable link as well as displaying the message they can also behave like a button and allow us to provide some extra functionality

// so now just instead of showing the message we should rather provide the user the way to grant access.
// so we can add our action in setAction second argument , so we need to replace null to onClickListener to make it behave like button.
// so we got the ability to respond when a user clicks the action link what we can do is present the user with a dialogue to grant access that's pretty easy we just called the request permissions method again but what if they have permanently denied access so that case calling request permissions wouldn't pop up the dialogue again but what actually we can do in that case is we can take them into setting for our app so that they grant the access but unfortunately Google don't allow the way to go all the way into the permissions but we can get in as far as our apps setting and they just need to click in to actually set the permissions.
// so we want is if user denies the permission and click on fab again permission request dialog box is opened but if it ticks on don't ask again then app settings should open.
// we had already seen how to request permission using the request permission method and launching the app setting screen is done using intents and the start activity method.
// so main thing we had to check is that permission is denied permanently or not. for his we can use ActivityCompat.shouldShowRequestPermissionRationale method.
//Params:activity – The target activity,permission – A permission your app wants to request.
// Returns:Whether you should show permission rationale UI.and do the code
// now one problem is there when in app settings we give permissions and click back button couple of times now the problem is that is has the permission it needs but doesn't know it has and again if we click on fab it will show snackbar of showing grant permission but if we close the app and start again now changes will reflect but that's not the best user experience the problem is we didn't update the variable of grant_permission after setting has been updated in app settings.
// suggestion is to never use variable to store state because we need to keep it updated everytime which require more code so first of all get rid of this READ_CONTACTS_GRANTED field.
// so remove the field instead recheck the permission again and app will work fine.

// app is running fine but there are more tests for which app should run so it's a good idea to create a test script to make sure we cover all the possible situations
// test script should not be complicated but it should including starting condition and the expected outcome... to see test script eg video-201
