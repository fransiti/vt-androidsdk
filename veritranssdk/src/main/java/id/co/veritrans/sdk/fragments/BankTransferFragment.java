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
import id.co.veritrans.sdk.widgets.TextViewFont;

/**
 * Created by shivam on 10/27/15.
 */
public class BankTransferFragment extends Fragment {

    private TextViewFont mTextViewSeeInstruction = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bank_transfer, container, false);


        mTextViewSeeInstruction = (TextViewFont)
                view.findViewById(R.id.text_see_instruction);

        mTextViewSeeInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getActivity() != null) {
                    getActivity().startActivity(new Intent(getActivity(),
                            BankTransferInstructionActivity.class));
                }
            }
        });


        return view;
    }


}
