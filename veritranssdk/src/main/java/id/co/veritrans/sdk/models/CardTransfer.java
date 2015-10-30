package id.co.veritrans.sdk.models;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by shivam on 10/26/15.
 */
public class CardTransfer extends TransactionModel{

    public static final String PAYMENT_TYPE = "credit_card";

    /**
     * {
     "payment_type":"credit_card",
     "credit_card":{
     "token_id":"481111-1114-7fd8c06e-a612-4f0b-a6d4-4fa2b8918c39",
     "bank": "bni",
     "save_token_id":"true"
     },
     "transaction_details":{
     "gross_amount":10000,
     "order_id":"10938033"
     }
     }
     */


    @SerializedName("credit_card")
    private CardPaymentDetails cardPaymentDetails;


    public CardTransfer(CardPaymentDetails cardPaymentDetails, TransactionDetails transactionDetails,
                        ArrayList<ItemDetails> itemDetails, ArrayList<BillingAddress>
                                billingAddresses, ArrayList<ShippingAddress>
                                shippingAddresses, CustomerDetails customerDetails) {

        this.paymentType = PAYMENT_TYPE;

        this.cardPaymentDetails = cardPaymentDetails;
        this.transactionDetails = transactionDetails;
        this.itemDetails = itemDetails;
        this.billingAddresses = billingAddresses;
        this.shippingAddresses = shippingAddresses;
        this.customerDetails = customerDetails;
    }


    public String getPayment_type() {
        return paymentType;
    }

    public CardPaymentDetails getCardPaymentDetails() {
        return cardPaymentDetails;
    }

    public TransactionDetails getTransactionDetails() {
        return transactionDetails;
    }

    public ArrayList<ItemDetails> getItemDetails() {
        return itemDetails;
    }

    public ArrayList<BillingAddress> getBillingAddresses() {
        return billingAddresses;
    }

    public ArrayList<ShippingAddress> getShippingAddresses() {
        return shippingAddresses;
    }

    public CustomerDetails getCustomerDetails() {
        return customerDetails;
    }

}