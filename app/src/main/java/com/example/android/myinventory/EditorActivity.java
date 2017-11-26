package com.example.android.myinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.myinventory.Data.InventoryContract;
import com.example.android.myinventory.Data.InventoryDbHelper;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    //EditText field to enter the name of the product
    private EditText mProductNameEditText;
    //EditText filed to enter the quantity of the product
    private EditText mProductQuantityEditText;
    //EditText filed to enter the price of the product
    private EditText mProductPriceEditText;
    //Create a URI for one pet
    private Uri mCurrentProductUri;
    //Identifies a particular Loader being used in the component
    private static final int EXISTING_PRODUCT_LOADER = 0;
    //Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mProductHasChanged = false;

    /**
     * Creating an onTouchListener that listens to all the changes the user has made
     * If the user performs any changes, it will turn mProductHasChanged to true
     */

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //examine the intent that was used to launch the activity
        //to figure out if I'm creating a new product or edit an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //Check to see if the currentProductUri contains any data
        //If the intent DOES NOT contain a content URI, then I know that I am creating a new product
        if(mCurrentProductUri == null){
            //if it doesn't contain, it will know that it will need to create a new product
            //and add the title "Add a product"
            setTitle(R.string.editor_activity_title_new_product);
            //Invalidate the option menu, so the "Delete" menu option can be hidden.
            //It doesn't make sense to delete a pet that hasn't been created yet
            invalidateOptionsMenu();
        }else {
            //If it contains it will update the information and it will change the
            //title to "Edit product"
            setTitle(R.string.editor_activity_title_edit_product);
            //Initialize a loader to read the product data from the database
            //and display the current value in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all the relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.product_name);
        mProductQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mProductPriceEditText = (EditText) findViewById(R.id.product_price);

        //Check and see if any of the fields had been changed
        //I use this for the mTouchListener
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductQuantityEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);

        //Create an onClickListener for the minus button, to decrement the quantity
        Button minusButton = (Button) findViewById(R.id.minus_button);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the inserted quantity
                String productQuantity = mProductQuantityEditText.getText().toString().trim();
                //set it as an integer so we can remove from it
                int minus = Integer.parseInt(productQuantity);
                if(minus <= 0){
                    Toast.makeText(view.getContext(), R.string.cant_go_below_zero, Toast.LENGTH_SHORT).show();
                }else {

                    //Remove from it
                    minus--;
                    //display the EditText with the new quantity
                    mProductQuantityEditText.setText(String.valueOf(minus));
                    //set mProductHasChanged to true so the Alert dialog will be displayed if the user
                    //clicks on the back button or the top back button
                    mProductHasChanged = true;
                }
            }
        });

        //Create an onClickListener for the plus button, to increment the quantity
        Button plusButton = (Button) findViewById(R.id.plus_button);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the inserted quantity
                String productQuantity = mProductQuantityEditText.getText().toString().trim();
                //set it as an integer so we can add to it
                int plus = Integer.parseInt(productQuantity);
                //Add to it
                plus++;
                //display the EditText with the new quantity
                mProductQuantityEditText.setText(String.valueOf(plus));
                //set mProductHasChanged to true so the Alert dialog will be displayed if the user
                //clicks on the back button or the top back button
                mProductHasChanged = true;
            }
        });

        //Create an onClickListener for the order button, that will create an intent to the e-mail
        Button orderButton = (Button) findViewById(R.id.order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener cancelButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //user clicks "Discard" button, close the current activity
                                dialogInterface.cancel();
                            }
                        };
                showOrderDialog(cancelButtonClickListener);
            }
        });
    }

    //Gets user input and saves the new product into the Inventory database
    public void saveProduct() {
        //get the inserted text
        String productName = mProductNameEditText.getText().toString().trim();
        String productQuantity = mProductQuantityEditText.getText().toString().trim();
        String productPrice = mProductPriceEditText.getText().toString().trim();

        //Check and see if all fields in the editor are blank
        //If so, I'll return it so it won't bother with the rest of the saveProduct method
        if(mCurrentProductUri == null &&
                TextUtils.isEmpty(productName) && TextUtils.isEmpty(productPrice) &&
                TextUtils.isEmpty(productQuantity)){
            return;
        }

        //If the quantity and the price are not provided by the user, do not try to parse
        //the strings into Integers. Use 0 by default
        int quantity = 0;
        if(!TextUtils.isEmpty(productQuantity)){
            //Convert the String into an integer
            quantity = Integer.parseInt(productQuantity);
        }

        int price = 0;
        if(!TextUtils.isEmpty(productPrice)){
            //Convert the String into an integer
            price = Integer.parseInt(productPrice);
        }

        //Create a ContentValues object where column names are the keys,
        //and inventory attributes from the editor are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        //Check to see if the URI is null or not to know if you are updating an existing product
        //or create a new one
        //If the URI != null it means that Im updating an existing pet
        if (mCurrentProductUri == null){
            //Insert a new product into the provider, returning the content URI for the new pet
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            //Add a toast message to confirm that the product was added successfully
            if(newUri == null){
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_activity_insert_fail), Toast.LENGTH_SHORT).show();
            }else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_activity_insert_success), Toast.LENGTH_SHORT).show();
            }
        }else {
            //Otherwise this is an EXISTING product and I will need to update the data
            //Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            //Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0){
                //if no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.editor_activity_update_fail), Toast.LENGTH_SHORT).show();
            }else{
                //Otherwise, the update was successful and we can display the toast
                Toast.makeText(this, getString(R.string.editor_activity_update_success), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu option from the res/menu/editor_menu.xml file.
        //This adds menu items to the top of the bar
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    //This option is called after invalidateOptionMenu() in the onCreate, so that the menu can
    //be updated (some menu items can be hidden or visible)


    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //If this is a new pet, hide the "Delete" menu item
        if(mCurrentProductUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //user click on the menu option in the app bar overflow menu
        switch (item.getItemId()) {
            //respond to the Save menu option
            case R.id.action_save:
                saveProduct();
                //exit the activity
                finish();
                return true;
                //respond to the Delete menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            //respond to a click on the "Up" arrow button in the app bar by displaying the
            //showUnsavedChangesDialog
            case android.R.id.home:
            //If the product hasn't changed, continue with navigating up to the parent activity
                if(!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
            //Otherwise if there are unsaved changes, setup a dialog to warn the user.
            //Create a clickListener to handle the user confirming that changes should be discarded
            DialogInterface.OnClickListener discardedButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //User clicked on the "Discard" button, navigate to the parent activity
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };
            //show dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardedButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define projector for a single row in the db
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY
        };
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1){
            return;
        }
        //Proceed with moving to the first row of the cursor and reading data from it
        if(cursor.moveToFirst()){
            //find the columns of pet attributes that I'm interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

            //Extract the values from the cursor for the given index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            //set the values to the Views
            mProductNameEditText.setText(name);
            mProductPriceEditText.setText(Integer.toString(price));
            mProductQuantityEditText.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
        DialogInterface.OnClickListener discardButtonClickListener) {
        //Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //User click the "Keep editing" button, so dismiss the dialog
                //and continue editing the pet
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });
        //Create an show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Show a dialog for when the user presses on the Order button. The dialog will ask the user
     * how many products he would want to order
     */
    private void showOrderDialog(DialogInterface.OnClickListener cancelButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.how_many_products);
        builder.setNegativeButton(R.string.cancel, cancelButtonClickListener);
        builder.setPositiveButton(R.string.order, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
            }
        });
        //Display an EditText where the user can
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    //I hook up the showUnsavedChanges method to the back button
    //so it will appear when the user made some changes and touches the phone's back button

    @Override
    public void onBackPressed() {
        //If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        //Otherwise if there are unsaved changes, setup  a dialog to warn the user.
        //Create a click listener to handle the user confirming that the changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //user clicks "Discard" button, close the current activity
                        finish();
                    }
                };
        //show dialog that there are unsaved changes using the discardButtonClickListener
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        //Only perform the delete if this is an existing product
        if(mCurrentProductUri != null){
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(
                    mCurrentProductUri,
                    null,
                    null
            );
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0){
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.editor_delete_product_failed, Toast.LENGTH_SHORT).show();
            }else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.editor_delete_product_successful, Toast.LENGTH_SHORT).show();
            }
        }
        //close the activity
        finish();
    }
}
