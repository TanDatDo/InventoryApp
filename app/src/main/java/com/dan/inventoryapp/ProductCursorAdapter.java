package com.dan.inventoryapp;

/**
 * Created by Dat T Do on 7/20/2017.
 */


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.inventoryapp.data.ProductContract.ProductEntry;

import java.text.DecimalFormat;

import static android.R.attr.id;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text_view);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        Double productPrice = cursor.getDouble(priceColumnIndex);
        Integer quantity = cursor.getInt(quantityColumnIndex);
        byte[] imageByteArrayResource = cursor.getBlob(imageColumnIndex);

        /**
         * Update 4 Views in bindView with the attributes for the current product
         * */
        //update name TextView
        nameTextView.setText(productName);
        //update priceTextView
        // using Decimal format to show the price in form 0.00
        DecimalFormat formater = new DecimalFormat("#.##");
        priceTextView.setText(formater.format(productPrice));
        //update the quantityTextView
        quantityTextView.setText(quantity.toString());
        //update ImageView,
        // using ImageUtils.getImage to convert imageByteArrayResource to Bitmap type
        imageView.setImageBitmap(ImageUtils.getImage(imageByteArrayResource));


        /**
         * set the function to sale button
         * function= decrease the quantity when it is clicked
         * */
        Button saleButton = (Button) view.findViewById(R.id.button_view);
        int idColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry._ID);
        saleButton.setTag(cursor.getInt(idColumnIndex));
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = (int) v.getTag();
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                String[] projection = {
                        ProductEntry._ID,
                        ProductEntry.COLUMN_PRODUCT_NAME,
                        ProductEntry.COLUMN_PRODUCT_PRICE,
                        ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE,
                        ProductEntry.COLUMN_PRODUCT_QUANTITY,
                };

                Cursor cursor = v.getContext().getContentResolver().query(currentProductUri, projection, null, null, null);
                cursor.moveToFirst();

                int quantityColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);
                int quantity = cursor.getInt(quantityColumnIndex);

                if (quantity > 0) {
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);
                    v.getContext().getContentResolver().update(currentProductUri, values, null, null);

                    Toast.makeText(v.getContext(),
                            v.getContext().getString(R.string.sale_complete),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(),
                            v.getContext().getString(R.string.sale_deny),
                            Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            }
        });
    }
}
