package id.co.veritrans.sdk.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.activities.CIMBClickPayActivity;
import id.co.veritrans.sdk.activities.CreditDebitCardFlowActivity;
import id.co.veritrans.sdk.activities.EpayBriActivity;
import id.co.veritrans.sdk.activities.MandiriECashActivity;
import id.co.veritrans.sdk.activities.NotificationActivity;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.models.TransactionResponse;
import id.co.veritrans.sdk.widgets.TextViewFont;

/**
 * Created by shivam on 10/27/15.
 */
public class PaymentTransactionStatusFragment extends Fragment {

    private static final String TRANSACTION_RESPONSE_PARAM = "transaction_response_param";
    private VeritransSDK veritrans;
    private TransactionResponse transactionResponse;
    private boolean isSuccessful;

    // views
    private Button actionBt = null;
    private ImageView paymentIv = null;
    private TextViewFont paymentStatusTv = null;
    private TextViewFont paymentMessageTv = null;
    private TextViewFont amountTextViewFont = null;
    private TextViewFont orderIdTextViewFont = null;
    private TextViewFont transactionTimeTextViewFont = null;
    private TextViewFont paymentTypeTextViewFont = null;
    private int count = 1;
    private TableLayout detailsTable;

    public PaymentTransactionStatusFragment() {
        // Required empty public constructor
    }

    public static PaymentTransactionStatusFragment newInstance(TransactionResponse
                                                                       transactionResponse) {
        Logger.i("payment status get instance called");
        PaymentTransactionStatusFragment fragment = new PaymentTransactionStatusFragment();
        Bundle args = new Bundle();
        args.putSerializable(TRANSACTION_RESPONSE_PARAM, transactionResponse);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionResponse = (TransactionResponse) getArguments().getSerializable
                    (TRANSACTION_RESPONSE_PARAM);
        }
        veritrans = VeritransSDK.getVeritransSDK();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_transaction_status, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeViews(view);
        if (getActivity().getClass().getName().equalsIgnoreCase(NotificationActivity.class.getName())) {
            bindDataForNotificationData();
        } else {
            bindDataToView();
        }
    }

    private void initializeViews(View view) {
        amountTextViewFont = (TextViewFont) view.findViewById(R.id.text_amount);
        orderIdTextViewFont = (TextViewFont) view.findViewById(R.id.text_order_id);
        transactionTimeTextViewFont = (TextViewFont) view.findViewById(R.id.text_transaction_time);
        paymentTypeTextViewFont = (TextViewFont) view.findViewById(R.id.text_payment_type);
        actionBt = (Button) view.findViewById(R.id.btn_action);
        paymentIv = (ImageView) view.findViewById(R.id.image_payment);
        paymentStatusTv = (TextViewFont) view.findViewById(R.id.text_payment_status);
        paymentMessageTv = (TextViewFont) view.findViewById(R.id.text_payment_message);
        detailsTable = (TableLayout) view.findViewById(R.id.transaction_info_layout);
    }

    private void bindDataForNotificationData() {
        actionBt.setVisibility(View.GONE);
        setPaymentType();
        setPaymentStatusValues();

    }

    private void setPaymentStatusValues() {
        if (transactionResponse.getStatusCode().equalsIgnoreCase(Constants.SUCCESS_CODE_200) ||
                transactionResponse.getTransactionStatus().equalsIgnoreCase(Constants.SUCCESS) ||
                transactionResponse.getTransactionStatus().equalsIgnoreCase(Constants.SETTLEMENT)) {
            paymentIv.setImageResource(R.drawable.ic_successful);
            paymentStatusTv.setText(getString(R.string.payment_successful));
            paymentMessageTv.setVisibility(View.GONE);
        } else if (transactionResponse.getStatusCode().equalsIgnoreCase(Constants.SUCCESS_CODE_201) ||
                transactionResponse.getTransactionStatus().equalsIgnoreCase(Constants.PENDING)) {
            /*if(transactionResponse.getFraudStatus().equalsIgnoreCase(Constants.CHALLENGE)){
                paymentIv.setImageResource(R.drawable.ic_successful);
                paymentStatusTv.setText(getString(R.string.payment_successful));
                paymentMessageTv.setVisibility(View.GONE);
            } else {*/
            if(transactionResponse.getFraudStatus().equalsIgnoreCase(Constants.CHALLENGE)){
                paymentIv.setImageResource(R.drawable.ic_successful);
                paymentStatusTv.setText(getString(R.string.payment_successful));
                paymentMessageTv.setVisibility(View.GONE);
            } else {
                paymentIv.setImageResource(R.drawable.ic_pending);
                paymentStatusTv.setText(getString(R.string.payment_pending));
            }
            //}
        } else {
            setUiForFailure();
        }
        try {
            transactionTimeTextViewFont.setText(transactionResponse.getTransactionTime());
            amountTextViewFont.setText(transactionResponse.getGrossAmount());
            orderIdTextViewFont.setText(transactionResponse.getOrderId());
        } catch (NullPointerException e) {
            e.printStackTrace();

        }
        if (transactionResponse != null && TextUtils.isEmpty(transactionResponse.getTransactionTime()) &&
                TextUtils.isEmpty(transactionResponse.getGrossAmount()) && TextUtils.isEmpty(transactionResponse.getOrderId())) {
            detailsTable.setVisibility(View.GONE);
        }
    }

    private void bindDataToView() {

        if (transactionResponse != null) {
            try {
                Logger.i("transactionstatus:" + transactionResponse.getString());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            setPaymentType();
            setPaymentStatusValues();

        } else {
            setUiForFailure();
        }

        actionBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().getClass().getName().equalsIgnoreCase(CreditDebitCardFlowActivity.class.getName())) {
                    ((CreditDebitCardFlowActivity) getActivity())
                            .setResultCode(getActivity().RESULT_OK);
                    ((CreditDebitCardFlowActivity) getActivity()).setResultAndFinish();
                } else if (getActivity().getClass().getName().equalsIgnoreCase(EpayBriActivity.class.getName())) {
                    ((EpayBriActivity) getActivity())
                            .setResultCode(getActivity().RESULT_OK);
                    ((EpayBriActivity) getActivity()).setResultAndFinish();
                } else if (getActivity().getClass().getName().equalsIgnoreCase(CIMBClickPayActivity
                        .class.getName())) {
                    ((CIMBClickPayActivity) getActivity())
                            .setResultCode(getActivity().RESULT_OK);
                    ((CIMBClickPayActivity) getActivity()).setResultAndFinish();
                } else if (getActivity().getClass().getName().equalsIgnoreCase
                        (MandiriECashActivity
                                .class.getName())) {
                    ((MandiriECashActivity) getActivity())
                            .setResultCode(getActivity().RESULT_OK);
                    ((MandiriECashActivity) getActivity()).setResultAndFinish();
                }

            }
        });
    }

    private void setUiForFailure() {
        isSuccessful = false;
        actionBt.setText(getString(R.string.retry));
        paymentIv.setImageResource(R.drawable.ic_failure);
        paymentStatusTv.setText(getString(R.string.payment_unsuccessful));

        if (transactionResponse == null) {
            paymentMessageTv.setVisibility(View.VISIBLE);
            paymentMessageTv.setText(getString(R.string.api_fail_message));
            detailsTable.setVisibility(View.GONE);
            return;
        }
        try {
            Logger.i("fail_message" + transactionResponse.getStatusMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (transactionResponse.getTransactionStatus().equalsIgnoreCase(Constants.DENY)) {
            paymentMessageTv.setVisibility(View.VISIBLE);
            paymentMessageTv.setText(getString(R.string.payment_deny));
        } else {
            if (!TextUtils.isEmpty(transactionResponse.getStatusMessage())) {
                paymentMessageTv.setVisibility(View.VISIBLE);
                paymentMessageTv.setText(transactionResponse.getStatusMessage());
            } else {
                paymentMessageTv.setVisibility(View.GONE);
            }
        }
    }
    private void setPaymentType() {
        try {
            Logger.i("PaymentType:" + transactionResponse.getPaymentType());
        } catch (NullPointerException e) {

        }
        if (transactionResponse == null) {
            return;
        }
        if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_EPAY_BRI)) {
            paymentTypeTextViewFont.setText(getString(R.string.epay_bri));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_MANDIRI_BILL_PAYMENT)) {
            paymentTypeTextViewFont.setText(getString(R.string.mandiri_bill_payment));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_CIMB_CLICKS)) {
            paymentTypeTextViewFont.setText(getString(R.string.cimb_clicks));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_MANDIRI_ECASH)) {
            paymentTypeTextViewFont.setText(getString(R.string.mandiri_e_cash));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_MANDIRI_CLICKPAY)) {
            paymentTypeTextViewFont.setText(getString(R.string.mandiri_click_pay));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_INDOMARET)) {
            paymentTypeTextViewFont.setText(getString(R.string.indomaret));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_CREDIT_DEBIT)) {
            paymentTypeTextViewFont.setText(getString(R.string.credit_card));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_MANDIRI_CLICKPAY)) {
            paymentTypeTextViewFont.setText(getString(R.string.mandiri_click_pay));
        } else if (transactionResponse.getPaymentType().equalsIgnoreCase(Constants.PAYMENT_INDOSAT_DOMPETKU)) {
            paymentTypeTextViewFont.setText(getString(R.string.indosat_dompetku));
        }

    }

}