package id.co.veritrans.sdk.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.callbacks.PermataBankTransferStatus;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.core.SdkUtil;
import id.co.veritrans.sdk.core.StorageDataHandler;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.fragments.BankTransactionStatusFragment;
import id.co.veritrans.sdk.fragments.BankTransferFragment;
import id.co.veritrans.sdk.fragments.BankTransferPaymentFragment;
import id.co.veritrans.sdk.models.BankTransfer;
import id.co.veritrans.sdk.models.BillingAddress;
import id.co.veritrans.sdk.models.CustomerDetails;
import id.co.veritrans.sdk.models.PermataBankTransfer;
import id.co.veritrans.sdk.models.PermataBankTransferResponse;
import id.co.veritrans.sdk.models.ShippingAddress;
import id.co.veritrans.sdk.models.TransactionDetails;
import id.co.veritrans.sdk.models.UserAddress;
import id.co.veritrans.sdk.models.UserDetail;
import id.co.veritrans.sdk.widgets.TextViewFont;

/**
 * Created by shivam on 10/26/15.
 */
public class BankTransferActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String HOME_FRAGMENT = "home";
    public static final String PAYMENT_FRAGMENT = "payment";
    public static final String STATUS_FRAGMENT = "transaction_status";
    public String currentFragment = "home";

    private TextViewFont mTextViewOrderId = null;
    private TextViewFont mTextViewAmount = null;
    private Button mButtonConfirmPayment = null;

    private VeritransSDK mVeritransSDK = null;
    private Toolbar mToolbar = null;

    private ArrayList<BillingAddress> mBillingAddressArrayList = new ArrayList<>();
    private ArrayList<ShippingAddress> mShippingAddressArrayList = new ArrayList<>();
    private CustomerDetails mCustomerDetails = null;
    private BankTransferFragment bankTransferFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);

        mVeritransSDK = VeritransSDK.getVeritransSDK();

        initializeView();
        bindDataToView();

        setUpHomeFragment();
        getUserDetails();

    }

    private void setUpHomeFragment() {
        // setup home fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        bankTransferFragment = new BankTransferFragment();

        fragmentTransaction.add(R.id.bank_transfer_container,
                bankTransferFragment, HOME_FRAGMENT);
        fragmentTransaction.commit();

        currentFragment = HOME_FRAGMENT;
    }

    @Override
    public void onBackPressed() {

        //enable this code if wants to travers backward in fragment back stack

        /*FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 1) {
            fragmentManager.popBackStack();
        } else {
            finish();
        }*/

        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return false;
    }


    private void initializeView() {

        mTextViewOrderId = (TextViewFont) findViewById(R.id.text_order_id);
        mTextViewAmount = (TextViewFont) findViewById(R.id.text_amount);
        mButtonConfirmPayment = (Button) findViewById(R.id.btn_confirm_payment);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        //setup tool bar
        mToolbar.setTitle(""); // disable default Text
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void bindDataToView() {

        if (mVeritransSDK != null) {
            mTextViewAmount.setText(Constants.CURRENCY_PREFIX + " " + mVeritransSDK.getAmount());
            mTextViewOrderId.setText(" " + mVeritransSDK.getOrderId());
            mButtonConfirmPayment.setTypeface(mVeritransSDK.getTypefaceOpenSansSemiBold());
            mButtonConfirmPayment.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_confirm_payment) {

            if (currentFragment.equalsIgnoreCase(HOME_FRAGMENT)) {
                  performTrsansaction();
            } else if (currentFragment.equalsIgnoreCase(PAYMENT_FRAGMENT)) {
                setUpTransactionStatusFragment();
            } else {
                onBackPressed();
            }

        }
    }


    private void setUpTransactionStatusFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // setup transaction status fragment
        fragmentTransaction.replace(R.id.bank_transfer_container,
                new BankTransactionStatusFragment(), STATUS_FRAGMENT);
        fragmentTransaction.addToBackStack(STATUS_FRAGMENT);
        fragmentTransaction.commit();


        currentFragment = STATUS_FRAGMENT;
        mButtonConfirmPayment.setText(R.string.done);
    }


    private void setUpTransactionFragment(final PermataBankTransferResponse
                                                  permataBankTransferResponse) {
        // setup transaction fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        BankTransferPaymentFragment bankTransferPaymentFragment =
                BankTransferPaymentFragment.newInstance(permataBankTransferResponse);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.bank_transfer_container, bankTransferPaymentFragment
                , PAYMENT_FRAGMENT);
        fragmentTransaction.addToBackStack(PAYMENT_FRAGMENT);
        fragmentTransaction.commit();

        currentFragment = PAYMENT_FRAGMENT;
        mButtonConfirmPayment.setText(R.string.complete_payment_at_atm);
    }


    private void getUserDetails() {

        UserDetail userDetail = null;

        try {
            userDetail = (UserDetail) StorageDataHandler.readObject(getApplicationContext(),
                    Constants.USER_DETAILS);

            if (userDetail != null && !TextUtils.isEmpty(userDetail.getUserFullName())) {

                ArrayList<UserAddress> userAddresses = userDetail.getUserAddresses();
                if (userAddresses != null && !userAddresses.isEmpty()) {

                    Logger.i("Found " + userAddresses.size()+" user addresses.");

                    mCustomerDetails = new CustomerDetails();
                    mCustomerDetails.setPhone(userDetail.getPhoneNumber());
                    mCustomerDetails.setFirst_name(userDetail.getUserFullName());
                    mCustomerDetails.setLast_name(" ");
                    mCustomerDetails.setEmail("");

                    //add email which is entered in editText
                    //mCustomerDetails.setEmail(userDetail.getEmail());


                    extractUserAddress(userDetail, userAddresses);
                }

            }else {
                SdkUtil.showSnackbar(BankTransferActivity.this, "User details not available.");
                finish();
            }
        } catch (ClassNotFoundException ex) {
            Logger.e("Error while fetching user details : " + ex.getMessage());
        } catch (IOException ex) {
            Logger.e("Error while fetching user details : " + ex.getMessage());
        }

    }



    private void extractUserAddress(UserDetail userDetail, ArrayList<UserAddress> userAddresses) {

        for (int i = 0; i < userAddresses.size(); i++) {

            UserAddress userAddress = userAddresses.get(i);

            if (userAddress.getAddressType() == Constants.ADDRESS_TYPE_BILLING) {
                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setCity(userAddress.getCity());
                billingAddress.setFirst_name(userDetail.getUserFullName());
                billingAddress.setLast_name("");
                billingAddress.setPhone(userDetail.getPhoneNumber());
                billingAddress.setCountry_code(userAddress.getCountry());
                billingAddress.setPostal_code(userAddress.getZipcode());
                mBillingAddressArrayList.add(billingAddress);
            } else {

                ShippingAddress shippingAddress = new ShippingAddress();
                shippingAddress.setCity(userAddress.getCity());
                shippingAddress.setFirst_name(userDetail.getUserFullName());
                shippingAddress.setLast_name("");
                shippingAddress.setPhone(userDetail.getPhoneNumber());
                shippingAddress.setCountry_code(userAddress.getCountry());
                shippingAddress.setPostal_code(userAddress.getZipcode());
                mShippingAddressArrayList.add(shippingAddress);

            }

        }
    }


    private void performTrsansaction() {

        final VeritransSDK veritransSDK = VeritransSDK.getVeritransSDK();

        if (veritransSDK != null) {

            // bank name
            BankTransfer bankTransfer = new BankTransfer();
            bankTransfer.setBank("permata");

            //transaction details
            TransactionDetails transactionDetails = new TransactionDetails();
            transactionDetails.setGrossAmount("" + mVeritransSDK.getAmount());
            transactionDetails.setOrderId(mVeritransSDK.getOrderId());


            // for sending instruction on email only if email-Id is entered.
            if( bankTransferFragment != null && !bankTransferFragment.isDetached()) {
                String emailId  = bankTransferFragment.getEmailId();
                if( !TextUtils.isEmpty(emailId) && SdkUtil.isEmailValid(emailId)) {
                    mCustomerDetails.setEmail(emailId.trim());
                }else if(!TextUtils.isEmpty(emailId) && emailId.trim().length() > 0){
                    SdkUtil.showSnackbar(BankTransferActivity.this, Constants.ERROR_INVALID_EMAIL_ID);
                    return;
                }
            }

            SdkUtil.showProgressDialog(BankTransferActivity.this, false);


            final PermataBankTransfer permataBankTransfer =
                    new PermataBankTransfer(bankTransfer, transactionDetails, null,
                            mBillingAddressArrayList, mShippingAddressArrayList,
                            mCustomerDetails);

            veritransSDK.paymentUsingPermataBank(BankTransferActivity.this,
                    permataBankTransfer, new PermataBankTransferStatus() {


                        @Override
                        public void onSuccess(PermataBankTransferResponse
                                                      permataBankTransferResponse) {

                            SdkUtil.hideProgressDialog();

                            if (permataBankTransferResponse != null) {
                                setUpTransactionFragment(permataBankTransferResponse);
                            } else {
                                onBackPressed();
                            }

                        }

                        @Override
                        public void onFailure(String errorMessage) {

                            try {

                                SdkUtil.hideProgressDialog();
                                SdkUtil.showSnackbar(BankTransferActivity.this, "" + errorMessage);

                            } catch (NullPointerException ex) {
                                Logger.e("transaction error is " + errorMessage);
                            }
                        }
                    });


        } else {
            Logger.e(Constants.ERROR_SDK_IS_NOT_INITIALIZED);
            finish();
        }
    }
}