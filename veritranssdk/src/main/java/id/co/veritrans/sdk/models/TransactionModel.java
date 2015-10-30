package id.co.veritrans.sdk.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by shivam on 10/30/15.
 *
 * Global model class for payment transaction.
 *
 */
 class TransactionModel {

    /**
     * payment_type : bank_transfer
     * bank_transfer : {"bank":"permata"}
     * transaction_details : {"gross_amount":"100","order_id":"10938011"}
     */


    @SerializedName("payment_type")
    String paymentType;

    @SerializedName("transaction_details")
    TransactionDetails transactionDetails;

    @SerializedName("item_details")
    ArrayList<ItemDetails> itemDetails = new ArrayList<>();

    @SerializedName("billing_address")
    ArrayList<BillingAddress> billingAddresses = new ArrayList<>();

    @SerializedName("shipping_address")
    ArrayList<ShippingAddress> shippingAddresses = new ArrayList<>();

    @SerializedName("customer_details")
    CustomerDetails customerDetails = null;
}