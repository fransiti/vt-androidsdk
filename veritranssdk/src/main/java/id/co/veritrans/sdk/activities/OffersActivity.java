package id.co.veritrans.sdk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IntegerRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.adapters.CardPagerAdapter;
import id.co.veritrans.sdk.callbacks.SavedCardCallback;
import id.co.veritrans.sdk.callbacks.TokenCallBack;
import id.co.veritrans.sdk.callbacks.TransactionCallback;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.core.SdkUtil;
import id.co.veritrans.sdk.core.StorageDataHandler;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.fragments.OffersListFragment;
import id.co.veritrans.sdk.fragments.PaymentTransactionStatusFragment;
import id.co.veritrans.sdk.models.BankDetail;
import id.co.veritrans.sdk.models.BankDetailArray;
import id.co.veritrans.sdk.models.BillingAddress;
import id.co.veritrans.sdk.models.CardPaymentDetails;
import id.co.veritrans.sdk.models.CardResponse;
import id.co.veritrans.sdk.models.CardTokenRequest;
import id.co.veritrans.sdk.models.CardTransfer;
import id.co.veritrans.sdk.models.CustomerDetails;
import id.co.veritrans.sdk.models.OffersListModel;
import id.co.veritrans.sdk.models.ShippingAddress;
import id.co.veritrans.sdk.models.TokenDetailsResponse;
import id.co.veritrans.sdk.models.TransactionDetails;
import id.co.veritrans.sdk.models.TransactionResponse;
import id.co.veritrans.sdk.models.UserAddress;
import id.co.veritrans.sdk.models.UserDetail;
import id.co.veritrans.sdk.utilities.Utils;
import id.co.veritrans.sdk.widgets.CirclePageIndicator;
import id.co.veritrans.sdk.widgets.MorphingButton;
import id.co.veritrans.sdk.widgets.TextViewFont;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Ankit on 12/7/15.
 */
public class OffersActivity extends AppCompatActivity implements TokenCallBack, TransactionCallback {

    public static final String OFFERS_FRAGMENT = "offersList";
    public static final String ADD_CARD_FRAGMENT = "addCard";
    public String currentFragment = "offersList";
    private Toolbar toolbar = null;
    private TextViewFont textViewTitleOffers = null;
    private VeritransSDK veritransSDK = null;
    private TextViewFont textViewTitleCardDetails = null;
    private TextViewFont textViewOfferName = null;

    public static final String OFFER_POSITION = "offer_position";
    public static final String OFFER_NAME = "offer_name";
    public static final String OFFER_TYPE = "offer_type";

    public static final String OFFER_TYPE_BINPROMO = "offer_type_binpromo";
    public static final String OFFER_TYPE_INSTALMENTS = "offer_type_instalments";

    private OffersListFragment offersListFragment = null;
    private int position = Constants.PAYMENT_METHOD_OFFERS;
    public ArrayList<OffersListModel> offersListModels = new ArrayList<>();

    public OffersListModel getSelectedOffer() {
        return selectedOffer;
    }

    public void setSelectedOffer(OffersListModel selectedOffer) {
        this.selectedOffer = selectedOffer;
    }

    private OffersListModel selectedOffer;

    private static final int PAYMENT_WEB_INTENT = 200;
    private static final int PAY_USING_CARD = 61;
    private int RESULT_CODE = RESULT_CANCELED;
    private String currentFragmentName;
    private FragmentManager fragmentManager;
    private float cardWidth;
    private UserDetail userDetail;
    private TokenDetailsResponse tokenDetailsResponse;
    private CardTokenRequest cardTokenRequest;
    private CardTransfer cardTransfer;
    private StorageDataHandler storageDataHandler;
    public ArrayList<CardTokenRequest> creditCards = new ArrayList<>();
    private RelativeLayout processingLayout;
    private ArrayList<BankDetail> bankDetails;
    private Subscription subscription;

    //for setResult
    private TransactionResponse transactionResponse = null;
    private String errorMessage = null;
    private CardPagerAdapter cardPagerAdapter;
    private CirclePageIndicator circlePageIndicator;
    private TextViewFont emptyCardsTextViewFont;
    private MorphingButton btnMorph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);
        storageDataHandler = new StorageDataHandler();
        veritransSDK = VeritransSDK.getVeritransSDK();
        fragmentManager = getSupportFragmentManager();
        btnMorph = (MorphingButton) findViewById(R.id.btnMorph1);
        morphToCircle(0);

        // get position of selected payment method
        Intent data = getIntent();
        if (data != null) {
            position = data.getIntExtra(Constants.POSITION, Constants
                    .PAYMENT_METHOD_OFFERS);
        } else {
            SdkUtil.showSnackbar(OffersActivity.this, Constants.ERROR_SOMETHING_WENT_WRONG);
            finish();
        }

        initializeView();
//        setUpHomeFragment();
        OffersListFragment offersListFragment = new OffersListFragment();
        replaceFragment(offersListFragment, true, false);
        calculateScreenWidth();
        getCreditCards();
        readBankDetails();
    }

    private void initializeView() {
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        textViewTitleOffers = (TextViewFont) findViewById(R.id.text_title);
        textViewTitleCardDetails = (TextViewFont) findViewById(R.id.text_title_card_details);
        textViewOfferName = (TextViewFont) findViewById(R.id.text_title_offer_name);
        //setup tool bar
        toolbar.setTitle(""); // disable default Text
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        processingLayout = (RelativeLayout) findViewById(R.id.processing_layout);

        textViewTitleCardDetails.setVisibility(View.GONE);
        textViewOfferName.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SdkUtil.hideKeyboard(this);
        if (fragmentManager.getBackStackEntryCount() == 1) {
            setResultAndFinish();
        } else {
            if (!TextUtils.isEmpty(currentFragmentName) && currentFragmentName.equalsIgnoreCase(PaymentTransactionStatusFragment.class
                    .getName())) {
                setResultAndFinish();
            } else {

                super.onBackPressed();
            }
        }
    }

    public void getToken(CardTokenRequest cardTokenRequest) {
        SdkUtil.showProgressDialog(this, getString(R.string.processing_payment), false);
        this.cardTokenRequest = cardTokenRequest;
        Logger.i("isSecure:" + this.cardTokenRequest.isSecure());
        veritransSDK.getToken(OffersActivity.this, this.cardTokenRequest, this);
    }

    public void payUsingCard() {

        SdkUtil.showProgressDialog(this, getString(R.string.processing_payment), false);
        processingLayout.setVisibility(View.VISIBLE);
        //getSupportActionBar().setTitle(getString(R.string.processing_payment));
        textViewTitleOffers.setText(getString(R.string.processing_payment));
        CustomerDetails customerDetails = new CustomerDetails(userDetail.getUserFullName(), "",
                userDetail.getEmail(), userDetail.getPhoneNumber());

        ArrayList<UserAddress> userAddresses = userDetail.getUserAddresses();
        TransactionDetails transactionDetails = new TransactionDetails("" + veritransSDK.
                getTransactionRequest().getAmount(),
                veritransSDK.getTransactionRequest().getOrderId());
        if (userAddresses != null && !userAddresses.isEmpty()) {
            ArrayList<BillingAddress> billingAddresses = new ArrayList<>();
            ArrayList<ShippingAddress> shippingAddresses = new ArrayList<>();

            for (int i = 0; i < userAddresses.size(); i++) {
                UserAddress userAddress = userAddresses.get(i);
                if (userAddress.getAddressType() == Constants.ADDRESS_TYPE_BILLING ||
                        userAddress.getAddressType() == Constants.ADDRESS_TYPE_BOTH) {
                    BillingAddress billingAddress;
                    billingAddress = new BillingAddress();
                    billingAddress.setCity(userAddress.getCity());
                    billingAddress.setFirstName(userDetail.getUserFullName());
                    billingAddress.setLastName("");
                    billingAddress.setPhone(userDetail.getPhoneNumber());
                    billingAddress.setCountryCode(userAddress.getCountry());
                    billingAddress.setPostalCode(userAddress.getZipcode());
                    billingAddresses.add(billingAddress);
                }
                if (userAddress.getAddressType() == Constants.ADDRESS_TYPE_SHIPPING ||
                        userAddress.getAddressType() == Constants.ADDRESS_TYPE_BOTH) {
                    ShippingAddress shippingAddress;
                    shippingAddress = new ShippingAddress();
                    shippingAddress.setCity(userAddress.getCity());
                    shippingAddress.setFirstName(userDetail.getUserFullName());
                    shippingAddress.setLastName("");
                    shippingAddress.setPhone(userDetail.getPhoneNumber());
                    shippingAddress.setCountryCode(userAddress.getCountry());
                    shippingAddress.setPostalCode(userAddress.getZipcode());
                    shippingAddresses.add(shippingAddress);
                }

            }

            CardPaymentDetails cardPaymentDetails = null;
            try {
                Logger.i("savetokenid:" + cardTokenRequest.getSavedTokenId());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            //for one click
            if (veritransSDK.getTransactionRequest().getCardClickType().equalsIgnoreCase
                    (Constants.CARD_CLICK_TYPE_ONE_CLICK) &&
                    !TextUtils.isEmpty(cardTokenRequest.getSavedTokenId())) {
                if (cardTokenRequest.isInstalment()) {
                    cardPaymentDetails = new CardPaymentDetails(cardTokenRequest.getBank(),
                            cardTokenRequest.getSavedTokenId(), cardTokenRequest.isSaved(),
                            cardTokenRequest.getFormattedInstalmentTerm(), cardTokenRequest.getBins());
                } else {
                    cardPaymentDetails = new CardPaymentDetails(cardTokenRequest.getBank(),
                            cardTokenRequest.getSavedTokenId(), cardTokenRequest.isSaved());
                    cardPaymentDetails.setBinsArray(cardTokenRequest.getBins());
                }

            } else if (tokenDetailsResponse != null) {

                if (cardTokenRequest.isInstalment()) {
                    Logger.i("tokenDetailsResponse.getTokenId():" + tokenDetailsResponse.getTokenId());
                    cardPaymentDetails = new CardPaymentDetails(cardTokenRequest.getBank(),
                            tokenDetailsResponse.getTokenId(), cardTokenRequest.isSaved(),
                            cardTokenRequest.getFormattedInstalmentTerm(), cardTokenRequest.getBins());
                } else {
                    Logger.i("tokenDetailsResponse.getTokenId():" + tokenDetailsResponse.getTokenId());
                    cardPaymentDetails = new CardPaymentDetails(cardTokenRequest.getBank(),
                            tokenDetailsResponse.getTokenId(), cardTokenRequest.isSaved());
                    cardPaymentDetails.setBinsArray(cardTokenRequest.getBins());
                }


            } else {
                SdkUtil.hideProgressDialog();
                processingLayout.setVisibility(View.GONE);
                return;
            }
            cardTransfer = new CardTransfer(cardPaymentDetails, transactionDetails,
                    null, billingAddresses, shippingAddresses, customerDetails);
            //currentApiCallNumber = PAY_USING_CARD;

        }

        veritransSDK.paymentUsingCard(this, this.cardTransfer, this);

    }

    public VeritransSDK getVeritransSDK() {
        return veritransSDK;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private void calculateScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        cardWidth = outMetrics.widthPixels;
        cardWidth = cardWidth - ((2 * getResources().getDimension(R.dimen.sixteen_dp)) / density);
    }

    public float getScreenWidth() {
        return cardWidth;
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, boolean clearBackStack) {
        if (fragment != null) {
            Logger.i("replace freagment");
            boolean fragmentPopped = false;
            String backStateName = fragment.getClass().getName();

            if (clearBackStack) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
            }

            if (!fragmentPopped) { //fragment not in back stack, create it.
                Logger.i("fragment not in back stack, create it");
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.offers_container, fragment, backStateName);
                if (addToBackStack) {
                    ft.addToBackStack(backStateName);
                }
                ft.commit();
                currentFragmentName = backStateName;
                //currentFragment = fragment;
            }
        }
    }

    //onSuccess for get token
    @Override
    public void onSuccess(TokenDetailsResponse tokenDetailsResponse) {
        if(tokenDetailsResponse!=null) {
            cardTokenRequest.setBank(tokenDetailsResponse.getBank());
        }
        this.tokenDetailsResponse = tokenDetailsResponse;
        Logger.i("token suc:" + tokenDetailsResponse.getTokenId() + ","
                + veritransSDK.getTransactionRequest().isSecureCard());
        if (veritransSDK.getTransactionRequest().isSecureCard()) {
            SdkUtil.hideProgressDialog();
            if (!TextUtils.isEmpty(tokenDetailsResponse.getRedirectUrl())) {
                Intent intentPaymentWeb = new Intent(this, PaymentWebActivity.class);
                intentPaymentWeb.putExtra(Constants.WEBURL, tokenDetailsResponse.getRedirectUrl());
                startActivityForResult(intentPaymentWeb, PAYMENT_WEB_INTENT);
            }
        } else {
            SdkUtil.showProgressDialog(this, getString(R.string.processing_payment), false);
            payUsingCard();
        }

    }

    @Override
    public void onFailure(String errorMessage, TokenDetailsResponse tokenDetailsResponse) {
        SdkUtil.hideProgressDialog();
        SdkUtil.showApiFailedMessage(this, errorMessage);
    }

    //onfailure for transaction api call
    @Override
    public void onFailure(String errorMessage, TransactionResponse transactionResponse) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                processingLayout.setVisibility(View.GONE);
            }
        }, 200);
        OffersActivity.this.transactionResponse = transactionResponse;
        OffersActivity.this.errorMessage = errorMessage;
        SdkUtil.hideProgressDialog();
        PaymentTransactionStatusFragment paymentTransactionStatusFragment =
                PaymentTransactionStatusFragment.newInstance(transactionResponse);
        replaceFragment(paymentTransactionStatusFragment, true, false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setTitle(getString(R.string.title_payment_failed));
        textViewTitleOffers.setText(getString(R.string.title_payment_status));

    }

    //onSuccess for transaction api call
    @Override
    public void onSuccess(TransactionResponse cardPaymentResponse) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                processingLayout.setVisibility(View.GONE);
            }
        }, 200);

        SdkUtil.hideProgressDialog();
        Logger.i("cardPaymentResponse:" + cardPaymentResponse.getStatusCode());

        if (cardPaymentResponse.getStatusCode().equalsIgnoreCase(Constants.SUCCESS_CODE_200) ||
                cardPaymentResponse.getStatusCode().equalsIgnoreCase(Constants.SUCCESS_CODE_201)) {

            transactionResponse = cardPaymentResponse;

            PaymentTransactionStatusFragment paymentTransactionStatusFragment =
                    PaymentTransactionStatusFragment.newInstance(cardPaymentResponse);
            replaceFragment(paymentTransactionStatusFragment, true, false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            //getSupportActionBar().setTitle(getString(R.string.title_payment_successful));
            textViewTitleOffers.setText(getString(R.string.title_payment_status));

            if (cardTokenRequest.isSaved()) {
                if (!creditCards.isEmpty()) {
                    int position = -1;
                    for (int i = 0; i < creditCards.size(); i++) {
                        CardTokenRequest card = creditCards.get(i);
                        if (card.getCardNumber().equalsIgnoreCase(cardTokenRequest.getCardNumber
                                ())) {
                            position = i;
                            break;
                        }
                    }
                    if (position >= 0) {
                        creditCards.remove(position);
                    }
                }
                cardTokenRequest.setCardCVV(0);
                cardTokenRequest.setClientKey("");
                cardTokenRequest.setGrossAmount(0);
                //to save card number in xxxxxxxxxxxx1118 format
                /*String formatedCardNumber = cardTokenRequest.getFormatedCardNumber();
                formatedCardNumber = formatedCardNumber.replace("-","");
                cardTokenRequest.setCardNumber(formatedCardNumber);*/
                //cardTokenRequest.setBank(Constants.BANK_NAME);
                if (bankDetails != null && !bankDetails.isEmpty()) {
                    String firstSix = cardTokenRequest.getCardNumber().substring(0, 6);
                    for (BankDetail bankDetail : bankDetails) {
                        if (bankDetail.getBin().equalsIgnoreCase(firstSix)) {
                            cardTokenRequest.setBank(bankDetail.getIssuing_bank());
                            cardTokenRequest.setCardType(bankDetail.getCard_association());
                            break;
                        }
                    }
                }

                if (cardTokenRequest.isSaved() && !TextUtils.isEmpty(cardPaymentResponse
                        .getSavedTokenId())) {
                    cardTokenRequest.setSavedTokenId(cardPaymentResponse.getSavedTokenId());
                }
                Logger.i("Card:" + cardTokenRequest.getString());
                creditCards.add(cardTokenRequest);
                saveCreditCards(cardTokenRequest);
            }
        }
    }

    public void saveCreditCards(CardTokenRequest creditCard) {
        /*try {
            storageDataHandler.writeObject(this, Constants.USERS_SAVED_CARD, creditCards);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        veritransSDK.saveCards(this, creditCard, new SavedCardCallback() {
            @Override
            public void onSuccess(CardResponse cardResponse) {
                Logger.i("card saved");
            }

            @Override
            public void onFailure(String errorMessage, CardResponse cardResponse) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i("reqCode:" + requestCode + ",res:" + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == PAYMENT_WEB_INTENT) {
                payUsingCard();
            }
        }
    }

    public ArrayList<CardTokenRequest> getCreditCards() {
        if (creditCards == null || creditCards.isEmpty()) {
            fetchCreditCards();
        }
        return creditCards;
    }

    public ArrayList<CardTokenRequest> getCreditCardList() {
        return creditCards;
    }

    public void fetchCreditCards() {
      //  SdkUtil.showProgressDialog(this, getString(R.string.fetching_cards), true);
        textViewTitleOffers.setText(getString(R.string.fetching_cards));
        //  processingLayout.setVisibility(View.VISIBLE);
        veritransSDK.getSavedCard(this, new SavedCardCallback() {
            @Override
            public void onSuccess(CardResponse cardResponse) {
                //  SdkUtil.hideProgressDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processingLayout.setVisibility(View.GONE);
                    }
                }, 200);

                Logger.i("cards api successful" + cardResponse);
                if (cardResponse != null && !cardResponse.getCreditCards().isEmpty()) {

                    creditCards.clear();
                    creditCards.addAll(cardResponse.getCreditCards());
                    if (cardPagerAdapter != null && circlePageIndicator != null) {
                        cardPagerAdapter.notifyDataSetChanged();
                        circlePageIndicator.notifyDataSetChanged();
                    }
                    //processingLayout.setVisibility(View.GONE);
                    if (emptyCardsTextViewFont != null) {
                        if (!creditCards.isEmpty()) {
                            emptyCardsTextViewFont.setVisibility(View.GONE);
                        } else {
                            emptyCardsTextViewFont.setVisibility(View.VISIBLE);
                        }

                    }
//                    OffersSavedCardFragment savedCardFragment = OffersSavedCardFragment
//                            .newInstance(OFFER_NAME);
//                    replaceFragment(savedCardFragment, true, false);

                } else {
//                    OffersAddCardDetailsFragment addCardDetailsFragment = OffersAddCardDetailsFragment
//                            .newInstance(OFFER_NAME);
//                    replaceFragment(addCardDetailsFragment, true, false);
                }

            }

            @Override
            public void onFailure(String errorMessage, CardResponse cardResponse) {
                SdkUtil.hideProgressDialog();
                Logger.i("card fetching failed :" + errorMessage);
                processingLayout.setVisibility(View.GONE);
            }
        });
    }

    public void oneClickPayment(CardTokenRequest cardDetail) {
        this.cardTokenRequest = cardDetail;
        payUsingCard();
    }

    public void twoClickPayment(CardTokenRequest cardDetail) {
        SdkUtil.showProgressDialog(this, getString(R.string.processing_payment), false);
        this.cardTokenRequest = cardDetail;
        this.cardTokenRequest.setTwoClick(true);
        this.cardTokenRequest.setClientKey(veritransSDK.getClientKey());
        veritransSDK.getToken(OffersActivity.this, this.cardTokenRequest, this);
    }

    public ArrayList<BankDetail> getBankDetails() {
        if (bankDetails != null && !bankDetails.isEmpty()) {
            return bankDetails;
        } else {
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void readBankDetails() {

        if (bankDetails == null || bankDetails.isEmpty()) {
            rx.Observable<List<BankDetail>> banksObservable = rx.Observable.create(
                    new rx.Observable.OnSubscribe<List<BankDetail>>() {
                        @Override
                        public void call(Subscriber<? super List<BankDetail>> sub) {
                            try {
                                userDetail = (UserDetail) storageDataHandler.readObject(OffersActivity.this,
                                        Constants.USER_DETAILS);
                                Logger.i("userDetail:" + userDetail.getUserFullName());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            ArrayList<BankDetail> bankDetails = new ArrayList<BankDetail>();
                            try {
                                bankDetails = (ArrayList<BankDetail>) storageDataHandler.readObject(
                                        OffersActivity.this, Constants.BANK_DETAILS);
                                Logger.i("bankDetails:" + bankDetails.size());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            if (bankDetails.isEmpty()) {
                                String json = null;
                                try {
                                    InputStream is = OffersActivity.this.getAssets().open("bank_details" +
                                            ".json");
                                    int size = is.available();
                                    byte[] buffer = new byte[size];
                                    is.read(buffer);
                                    is.close();
                                    json = new String(buffer, "UTF-8");
                                    Logger.i("json:" + json);
                                } catch (IOException ex) {
                                    ex.printStackTrace();

                                }

                                try {
                                    Gson gson = new Gson();
                                    bankDetails = gson.fromJson(json, BankDetailArray.class).getBankDetails();
                                    Logger.i("bankDetails:" + bankDetails.size());
                                    storageDataHandler.writeObject(OffersActivity.this, Constants
                                            .BANK_DETAILS, bankDetails);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

                            }
                            sub.onNext(bankDetails);
                            sub.onCompleted();
                        }
                    }
            );
            Subscriber<List<BankDetail>> subscriber = new Subscriber<List<BankDetail>>() {

                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    Logger.i("error:" + e.getMessage());
                }

                @Override
                public void onNext(List<BankDetail> bankDetails) {
                    OffersActivity.this.bankDetails = (ArrayList<BankDetail>) bankDetails;
                    Logger.i("bankdetail getter onnext" + bankDetails.size());
                }
            };
            subscription = banksObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);

        }
    }

    public void setResultAndFinish() {
        Intent data = new Intent();
        data.putExtra(Constants.TRANSACTION_RESPONSE, transactionResponse);
        data.putExtra(Constants.TRANSACTION_ERROR_MESSAGE, errorMessage);
        setResult(RESULT_CODE, data);
        finish();
    }

    public void setResultCode(int resultCode) {
        this.RESULT_CODE = resultCode;
    }

    public void setAdapterViews(CardPagerAdapter cardPagerAdapter, CirclePageIndicator circlePageIndicator, TextViewFont emptyCardsTextViewFont) {
        this.cardPagerAdapter = cardPagerAdapter;
        this.circlePageIndicator = circlePageIndicator;
        this.emptyCardsTextViewFont = emptyCardsTextViewFont;
    }

    public void morphingAnimation(){
        Logger.i("morphingAnimation");
        //Logger.i("64dp:"+ Utils.dpToPx(56));
        btnMorph.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MorphingButton.Params square = MorphingButton.Params.create()
                        .duration((int) Constants.CARD_ANIMATION_TIME)
                        .cornerRadius(dimen(R.dimen.mb_corner_radius_2))
                        .width((int) cardWidth)
                        .height(Utils.dpToPx(Constants.FAB_HEIGHT_DP))
                        .text(getString(R.string.pay_now))
                        .colorPressed(color(R.color.colorAccent))
                        .color(color(R.color.colorAccent));
                btnMorph.morph(square);
            }
        }, 50);



    }
    public MorphingButton.Params morphCicle(int time) {
        return MorphingButton.Params.create()
                .cornerRadius(Utils.dpToPx(Constants.FAB_HEIGHT_DP))
                .width(Utils.dpToPx(Constants.FAB_HEIGHT_DP))
                .height(Utils.dpToPx(Constants.FAB_HEIGHT_DP))
                .duration(time)
                .colorPressed(color(R.color.colorAccent))
                .color(color(R.color.colorAccent));


    }

    public int dimen(@DimenRes int resId) {
        return (int) getResources().getDimension(resId);
    }

    public int color(@ColorRes int resId) {
        return getResources().getColor(resId);
    }

    public int integer(@IntegerRes int resId) {
        return getResources().getInteger(resId);
    }

    public MorphingButton getBtnMorph() {
        return btnMorph;
    }
    
    public void morphToCircle(int time) {
        MorphingButton.Params circle = morphCicle(time);
        btnMorph.morph(circle);
    }
}

