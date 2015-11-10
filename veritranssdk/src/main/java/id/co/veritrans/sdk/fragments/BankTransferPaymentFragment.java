package id.co.veritrans.sdk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.activities.BankTransferInstructionActivity;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.models.TransactionResponse;
import id.co.veritrans.sdk.utilities.Utils;
import id.co.veritrans.sdk.widgets.TextViewFont;

/**
 * Created by shivam on 10/27/15.
 */
public class BankTransferPaymentFragment extends Fragment {

    public static final String VALID_UNTILL = "Valid Untill : ";
    private static TransactionResponse sPermataBankTransferResponse = null;

    private TextViewFont mTextViewVirtualAccountNumber = null;
    private TextViewFont mTextViewSeeInstruction = null;
    private TextViewFont mTextViewValidity = null;


    public static BankTransferPaymentFragment newInstance(TransactionResponse
                                                                  permataBankTransferResponse) {
        sPermataBankTransferResponse = permataBankTransferResponse;
        BankTransferPaymentFragment fragment = new BankTransferPaymentFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bank_transfer_payment, container, false);
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {

        mTextViewVirtualAccountNumber = (TextViewFont)
                view.findViewById(R.id.text_virtual_account_number);

        mTextViewSeeInstruction = (TextViewFont) view.findViewById(R.id.text_see_instruction);
        mTextViewValidity = (TextViewFont) view.findViewById(R.id.text_validaty);


        if (sPermataBankTransferResponse != null) {
            if (sPermataBankTransferResponse.getStatusCode().trim().equalsIgnoreCase(Constants
                    .SUCCESS_CODE_200) ||
                    sPermataBankTransferResponse.getStatusCode().trim().equalsIgnoreCase
                            (Constants.SUCCESS_CODE_201)
                    )
                mTextViewVirtualAccountNumber.setText(sPermataBankTransferResponse
                        .getPermataVANumber());

            mTextViewValidity.setText(VALID_UNTILL + Utils.getValidityTime
                    (sPermataBankTransferResponse.getTransactionTime()));

        } else {
            mTextViewVirtualAccountNumber.setText(sPermataBankTransferResponse.getStatusMessage());
        }

        mTextViewSeeInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstruction();
            }
        });
    }

    private void showInstruction() {
        Intent intent = new Intent(getActivity(),
                BankTransferInstructionActivity.class);
        intent.putExtra(Constants.POSITION, 2);
        getActivity().startActivity(intent);
    }

}