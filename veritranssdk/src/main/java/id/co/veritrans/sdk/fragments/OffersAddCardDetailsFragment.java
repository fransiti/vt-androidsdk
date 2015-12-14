package id.co.veritrans.sdk.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.activities.CreditDebitCardFlowActivity;
import id.co.veritrans.sdk.activities.OffersActivity;
import id.co.veritrans.sdk.adapters.OffersAdapter;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.core.SdkUtil;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.models.BankDetail;
import id.co.veritrans.sdk.models.CardTokenRequest;
import id.co.veritrans.sdk.models.OffersListModel;
import id.co.veritrans.sdk.models.TransactionResponse;
import id.co.veritrans.sdk.models.UserDetail;
import id.co.veritrans.sdk.widgets.TextViewFont;
import id.co.veritrans.sdk.widgets.VeritransDialog;

/**
 * Created by Ankit on 12/8/15.
 */
public class OffersAddCardDetailsFragment extends Fragment {
    private TextViewFont textViewTitleOffers = null;
    private TextViewFont textViewTitleCardDetails = null;
    private TextViewFont textViewOfferName = null;

    private TextViewFont textViewOfferApplied = null;
    private TextViewFont textViewOfferNotApplied = null;
    private LinearLayout layoutOfferStatus = null;

    private String offerName = null;
    private String offerType = null;
    private ImageView imageViewPlus = null;
    private ImageView imageViewMinus = null;
    private TextViewFont textViewInstalment = null;
    private RelativeLayout layoutPayWithInstalment = null;


    private String lastExpDate = "";
    private EditText etCardNo;
    private EditText etCvv;
    private EditText etExpiryDate;
    private CheckBox cbStoreCard;
    private ImageView questionImg;
    private Button payNowBtn;
    private String cardNumber;
    private String cvv;
    private String expiryDate;
    private String[] expDateArray;
    private int expMonth;
    private int expYear;
    private VeritransSDK veritransSDK;
    private UserDetail userDetail;
    private ArrayList<BankDetail> bankDetails;
    private String cardType = "";

    public OffersAddCardDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        veritransSDK = ((OffersActivity) getActivity()).getVeritransSDK();
        userDetail = ((OffersActivity) getActivity()).getUserDetail();
        bankDetails = ((OffersActivity) getActivity()).getBankDetails();
    }

    public static OffersAddCardDetailsFragment newInstance(String offerName, String offerType) {
        OffersAddCardDetailsFragment fragment = new OffersAddCardDetailsFragment();
        Bundle data = new Bundle();
        data.putString(OffersActivity.OFFER_NAME, offerName);
        data.putString(OffersActivity.OFFER_TYPE, offerType);
        fragment.setArguments(data);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offers_add_card, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //retrieve data from bundle.
        Bundle data = getArguments();
        offerName = data.getString(OffersActivity.OFFER_NAME);
        offerType = data.getString(OffersActivity.OFFER_TYPE);
        initialiseView(view);
    }

    private void initialiseView(View view) {

        if (view != null && getActivity() != null) {
            textViewTitleOffers = (TextViewFont) getActivity().findViewById(R.id.text_title);
            textViewTitleCardDetails = (TextViewFont) getActivity().findViewById(R.id
                    .text_title_card_details);
            textViewOfferName = (TextViewFont) getActivity().findViewById(R.id.text_title_offer_name);

            setToolbar();

            textViewOfferApplied = (TextViewFont) view.findViewById(R.id.text_offer_status_applied);
            textViewOfferNotApplied = (TextViewFont) view.findViewById(R.id
                    .text_offer_status_not_applied);
            layoutOfferStatus = (LinearLayout) view.findViewById(R.id.layout_offer_status);

            imageViewPlus = (ImageView) view.findViewById(R.id.img_plus);
            imageViewMinus = (ImageView) view.findViewById(R.id.img_minus);
            textViewInstalment = (TextViewFont) view.findViewById(R.id.text_instalment);
            layoutPayWithInstalment = (RelativeLayout) view.findViewById(R.id
                    .layout_pay_with_instalments);

            if (offerType.equalsIgnoreCase(OffersActivity.OFFER_TYPE_INSTALMENTS)) {
                hideOrShowPayWithInstalment(true);
            } else {
                hideOrShowPayWithInstalment(false);
            }

            hideOrShowOfferStatus(false, false);

            bindCreditCardView(view);
        }
    }

    private void setToolbar() {
        textViewTitleOffers.setVisibility(View.GONE);
        textViewTitleCardDetails.setVisibility(View.VISIBLE);
        textViewOfferName.setVisibility(View.VISIBLE);
        textViewOfferName.setText(offerName);

        textViewTitleCardDetails.setText(getResources().getString(R.string.card_details));
    }

    private void hideOrShowPayWithInstalment(boolean isShowLayout) {
        if (isShowLayout) {
            layoutPayWithInstalment.setVisibility(View.VISIBLE);
        } else {
            layoutPayWithInstalment.setVisibility(View.GONE);
        }
    }

    private void hideOrShowOfferStatus(boolean isShowLayout, boolean isOfferApplied) {
        if (isShowLayout) {
            layoutOfferStatus.setVisibility(View.VISIBLE);
            if (isOfferApplied) {
                textViewOfferApplied.setVisibility(View.VISIBLE);
                textViewOfferNotApplied.setVisibility(View.GONE);
            } else {
                textViewOfferApplied.setVisibility(View.GONE);
                textViewOfferNotApplied.setVisibility(View.VISIBLE);
            }
        } else {
            layoutOfferStatus.setVisibility(View.GONE);
        }
    }

    private void bindCreditCardView(View view) {
        etCardNo = (EditText) view.findViewById(R.id.et_card_no);
        etCvv = (EditText) view.findViewById(R.id.et_cvv);
        etExpiryDate = (EditText) view.findViewById(R.id.et_exp_date);
        cbStoreCard = (CheckBox) view.findViewById(R.id.cb_store_card);
        questionImg = (ImageView) view.findViewById(R.id.image_question);
        payNowBtn = (Button) view.findViewById(R.id.btn_pay_now);
        etCardNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Logger.i("onFocus change etCardNo");
                if (!hasFocus) {
                    Logger.i("onFocus change not etCardNo");
                    focusChange();
                } else {
                    Logger.i("onFocus change has focus etCardNo");
                }
            }
        });
        etCvv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Logger.i("onFocus change etCvv");
                if (!hasFocus) {
                    Logger.i("onFocus change not etCvv");
                    focusChange();
                } else {
                    Logger.i("onFocus change has focus etCvv");
                }
            }
        });
        etExpiryDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Logger.i("onFocus change etExpiryDate");
                if (!hasFocus) {
                    Logger.i("onFocus change not etExpiryDate");
                    focusChange();
                } else {
                    Logger.i("onFocus change has focus etExpiryDate");
                }
            }
        });
        cbStoreCard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                isValid(false);
            }
        });
        if (veritransSDK.isLogEnabled()) {
            /*etExpiryDate.setText("12/20");
            etCardNo.setText("4811 1111 1111 1114");
            // etCardHolderName.setText("Chetan");
            etCvv.setText("123");*/
        }
        payNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid(true)) {

                    CardTokenRequest cardTokenRequest = new CardTokenRequest(cardNumber, Integer
                            .parseInt(cvv),
                            expMonth, expYear,
                            veritransSDK.getClientKey());
                    cardTokenRequest.setIsSaved(cbStoreCard.isChecked());
                    cardTokenRequest.setSecure(veritransSDK.getTransactionRequest().isSecureCard());
                    cardTokenRequest.setGrossAmount(veritransSDK.getTransactionRequest()
                            .getAmount());
                    cardTokenRequest.setCardType(cardType);
                    if (bankDetails != null && !bankDetails.isEmpty()) {
                        String firstSix = cardNumber.substring(0, 6);
                        for (BankDetail bankDetail : bankDetails) {
                            if (bankDetail.getBin().equalsIgnoreCase(firstSix)) {
                                cardTokenRequest.setBank(bankDetail.getIssuing_bank());
                                cardTokenRequest.setCardType(bankDetail.getCard_association());
                                break;
                            }
                        }
                    }
                    //make payment
                    SdkUtil.showProgressDialog(getActivity(), false);
                    ((OffersActivity) getActivity()).getToken(cardTokenRequest);
                }
            }
        });
        questionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VeritransDialog veritransDialog = new VeritransDialog(getActivity(), getResources().getDrawable(R.drawable.cvv_dialog_image, null),
                        getString(R.string.message_cvv), getString(R.string.got_it), "");
                veritransDialog.show();
            }
        });

        etCardNo.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf
                            (space)).length <= 3) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }
                setCardType();
            }
        });

        etExpiryDate.addTextChangedListener(new TextWatcher() {
                                                @Override
                                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                }

                                                @Override
                                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                }

                                                @Override
                                                public void afterTextChanged(Editable s) {
                                                    String input = s.toString();
                                                    if (s.length() == 2 && !lastExpDate.endsWith("/")) {
                                                        int month = Integer.parseInt(input);
                                                        if (month <= 12) {
                                                            etExpiryDate.setText(etExpiryDate.getText().toString() + "/");
                                                            etExpiryDate.setSelection(etExpiryDate.getText().toString().length());
                                                        } else {
                                                            etExpiryDate.setText(Constants.MONTH_COUNT + "/");
                                                            etExpiryDate.setSelection(etExpiryDate.getText().toString().length());
                                                        }
                                                    } else if (s.length() == 2 && lastExpDate.endsWith("/")) {
                                                        int month = Integer.parseInt(input);
                                                        if (month <= 12) {
                                                            etExpiryDate.setText(etExpiryDate.getText().toString().substring(0, 1));
                                                            etExpiryDate.setSelection(etExpiryDate.getText().toString().length());
                                                        } else {
                                                            etExpiryDate.setText("");
                                                            etExpiryDate.setSelection(etExpiryDate.getText().toString().length());
                                                        }
                                                    } else if (s.length() == 1) {
                                                        int month = Integer.parseInt(input);
                                                        if (month > 1) {
                                                            etExpiryDate.setText("0" + etExpiryDate.getText().toString() + "/");
                                                            etExpiryDate.setSelection(etExpiryDate.getText().toString().length());
                                                        }
                                                    }
                                                    lastExpDate = etExpiryDate.getText().toString();
                                                }
                                            }

        );
    }

    private boolean isValid(boolean isPayButtonClick) {
        cardNumber = etCardNo.getText().toString().trim().replace(" ", "");
        expiryDate = etExpiryDate.getText().toString().trim();
        cvv = etCvv.getText().toString().trim();
        questionImg.setVisibility(View.VISIBLE);
        try {
            expDateArray = expiryDate.split("/");
            Logger.i("expDate:" + expDateArray[0], "" + expDateArray[1]);
        } catch (NullPointerException e) {
            Logger.i("expiry date empty");
        } catch (IndexOutOfBoundsException e) {
            Logger.i("expiry date issue");
        }
        if (TextUtils.isEmpty(cardNumber)) {
            if (isPayButtonClick) {
                SdkUtil.showSnackbar(getActivity(), getString(R.string.validation_message_card_number));
                // etCardNo.requestFocus();
            }
            if (!etCardNo.hasFocus()) {
                etCardNo.setError(getString(R.string.validation_message_card_number));
            }
            /*etExpiryDate.clearFocus();
            etCvv.clearFocus();*/
            return false;
        } else if (cardNumber.length() < 16 || !SdkUtil.isValidCardNumber(cardNumber)) {
            /*etCardNo.requestFocus();
            etExpiryDate.clearFocus();
            etCvv.clearFocus();*/
            if (!etCardNo.hasFocus()) {
                etCardNo.setError(getString(R.string.validation_message_invalid_card_no));
            }
            if (isPayButtonClick) {
                SdkUtil.showSnackbar(getActivity(), getString(R.string
                        .validation_message_invalid_card_no));
            }
            return false;
        } else if (TextUtils.isEmpty(expiryDate)) {
            /*etCardNo.clearFocus();
            etCvv.clearFocus();
            etExpiryDate.requestFocus();*/

            if (isPayButtonClick) {
                SdkUtil.showSnackbar(getActivity(), getString(R.string
                        .validation_message_empty_expiry_date));
            }
            if (!etExpiryDate.hasFocus()) {
                etExpiryDate.setError(getString(R.string.validation_message_empty_expiry_date));
            }
            return false;
        } else if (!expiryDate.contains("/")) {
            /*etCardNo.clearFocus();
            etCvv.clearFocus();
            etExpiryDate.requestFocus();*/
            if (isPayButtonClick) {
                SdkUtil.showSnackbar(getActivity(), getString(R.string
                        .validation_message_invalid_expiry_date));
            }
            if (!etExpiryDate.hasFocus()) {
                etExpiryDate.setError(getString(R.string.validation_message_invalid_expiry_date));
            }
            return false;
        } else if (expDateArray == null || expDateArray.length != 2) {
            /*etCardNo.clearFocus();
            etCvv.clearFocus();
            etExpiryDate.requestFocus();*/
            if (isPayButtonClick) {
                SdkUtil.showSnackbar(getActivity(), getString(R.string
                        .validation_message_invalid_expiry_date));
            }
            if (!etExpiryDate.hasFocus()) {
                etExpiryDate.setError(getString(R.string.validation_message_invalid_expiry_date));
            }
            return false;
        } else if (expDateArray != null && expDateArray.length == 2) {
            try {
                expMonth = Integer.parseInt(expDateArray[0]);
            } catch (NumberFormatException e) {
               /* etCardNo.clearFocus();
                etCvv.clearFocus();
                etExpiryDate.requestFocus();*/
                if (isPayButtonClick) {
                    SdkUtil.showSnackbar(getActivity(), getString(R.string
                            .validation_message_invalid_expiry_date));
                }
                if (!etExpiryDate.hasFocus()) {
                    etExpiryDate.setError(getString(R.string.validation_message_invalid_expiry_date));
                }
                return false;
            }
            try {
                expYear = Integer.parseInt(expDateArray[1]);
            } catch (NumberFormatException e) {
              /*  etCardNo.clearFocus();
                etCvv.clearFocus();
                etExpiryDate.requestFocus();*/
                if (isPayButtonClick) {
                    SdkUtil.showSnackbar(getActivity(), getString(R.string
                            .validation_message_invalid_expiry_date));
                }
                if (!etExpiryDate.hasFocus()) {
                    etExpiryDate.setError(getString(R.string.validation_message_invalid_expiry_date));
                }
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            SimpleDateFormat format = new SimpleDateFormat("yy");
            String year = format.format(date);

            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = Integer.parseInt(year);
            Logger.i("currentMonth:" + currentMonth + ",currentYear:" + currentYear);
            if (expYear < currentYear) {
              /*  etCardNo.clearFocus();
                etCvv.clearFocus();
                etExpiryDate.requestFocus();*/
                if (isPayButtonClick) {
                    SdkUtil.showSnackbar(getActivity(), getString(R.string
                            .validation_message_invalid_expiry_date));
                }
                if (!etExpiryDate.hasFocus()) {
                    etExpiryDate.setError(getString(R.string.validation_message_invalid_expiry_date));
                }
                return false;
            } else if (expYear == currentYear && currentMonth > expMonth) {
             /*   etCardNo.clearFocus();
                etCvv.clearFocus();
                etExpiryDate.requestFocus();*/
                if (isPayButtonClick) {
                    SdkUtil.showSnackbar(getActivity(), getString(R.string
                            .validation_message_invalid_expiry_date));
                }
                if (!etExpiryDate.hasFocus()) {
                    etExpiryDate.setError(getString(R.string.validation_message_invalid_expiry_date));
                }
                return false;
            } else if (TextUtils.isEmpty(cvv)) {
                /*etCardNo.clearFocus();
                etCvv.requestFocus();
                etExpiryDate.clearFocus();*/
                if (isPayButtonClick) {
                    SdkUtil.showSnackbar(getActivity(), getString(R.string.validation_message_cvv));
                }
                if (!etCvv.hasFocus()) {
                    etCvv.setError(getString(R.string.validation_message_cvv));
                }
                questionImg.setVisibility(View.GONE);
                return false;
            } else {
                if (cvv.length() < 3) {
                  /*  etCardNo.clearFocus();
                    etCvv.requestFocus();
                    etExpiryDate.clearFocus();*/
                    if (isPayButtonClick) {
                        SdkUtil.showSnackbar(getActivity(), getString(R.string
                                .validation_message_invalid_cvv));
                    }
                    if (!etCvv.hasFocus()) {
                        etCvv.setError(getString(R.string.validation_message_invalid_cvv));
                    }
                    questionImg.setVisibility(View.GONE);
                    return false;
                }
                questionImg.setVisibility(View.VISIBLE);
                /*etCvv.clearFocus();
                cbStoreCard.requestFocus();*/
            }

        }
        return true;
    }

    public void focusChange() {
        Logger.i("onFocus change has not focus");
        isValid(false);
    }

    private void setCardType() {
        String cardNo = etCardNo.getText().toString().trim();
        if (cardNo.isEmpty() || cardNo.length() < 2) {
            etCardNo.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            return;
        }
        if (cardNo.charAt(0) == '4') {
            Drawable visa = getResources().getDrawable(R.drawable.visa_non_transperent);
            etCardNo.setCompoundDrawablesWithIntrinsicBounds(null, null, visa, null);
            cardType = Constants.CARD_TYPE_VISA;
        } else if ((cardNo.charAt(0) == '5') && ((cardNo.charAt(1) == '1') || (cardNo.charAt(1) == '2')
                || (cardNo.charAt(1) == '3') || (cardNo.charAt(1) == '4') || (cardNo.charAt(1) == '5'))) {
            Drawable masterCard = getResources().getDrawable(R.drawable.mastercard_non_transperent);
            etCardNo.setCompoundDrawablesWithIntrinsicBounds(null, null, masterCard, null);
            cardType = Constants.CARD_TYPE_MASTERCARD;

        } else if ((cardNo.charAt(0) == '3') && ((cardNo.charAt(1) == '4') || (cardNo.charAt(1) == '7'))) {
            Drawable amex = getResources().getDrawable(R.drawable.amex_non_transperent);
            etCardNo.setCompoundDrawablesWithIntrinsicBounds(null, null, amex, null);
            cardType = "AMEX";

        } else {
            cardType = "";

        }
    }
}
