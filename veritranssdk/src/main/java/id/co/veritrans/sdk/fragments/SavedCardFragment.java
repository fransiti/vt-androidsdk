package id.co.veritrans.sdk.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.activities.CreditDebitCardFlowActivity;
import id.co.veritrans.sdk.adapters.CardPagerAdapter;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.models.CardTokenRequest;
import id.co.veritrans.sdk.widgets.CirclePageIndicator;
import id.co.veritrans.sdk.widgets.TextViewFont;

public class SavedCardFragment extends Fragment {
    private ViewPager savedCardPager;
    private CirclePageIndicator circlePageIndicator;
    private FloatingActionButton addCardBt;
    private VeritransSDK veritransSDK;
    private ArrayList<CardTokenRequest> creditCards;
    private CardPagerAdapter cardPagerAdapter;

    private TextViewFont emptyCardsTextViewFont;

    public SavedCardFragment() {

    }

    public static SavedCardFragment newInstance() {
        SavedCardFragment fragment = new SavedCardFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        veritransSDK = VeritransSDK.getVeritransSDK();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_card, container, false);

        emptyCardsTextViewFont = (TextViewFont) view.findViewById(R.id.text_empty_saved_cards);


        bindViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((CreditDebitCardFlowActivity) getActivity()).getSupportActionBar().setTitle(getString(R
                    .string.saved_card));
            ((CreditDebitCardFlowActivity) getActivity()).getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void bindViews(View view) {
        savedCardPager = (ViewPager) view.findViewById(R.id.saved_card_pager);
        addCardBt = (FloatingActionButton) view.findViewById(R.id.btn_add_card);
        addCardBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCardDetailsFragment addCardDetailsFragment = AddCardDetailsFragment
                        .newInstance();
                ((CreditDebitCardFlowActivity) getActivity()).replaceFragment
                        (addCardDetailsFragment, true, false);
            }
        });
        float cardWidth = ((CreditDebitCardFlowActivity) getActivity()).getScreenWidth();
        float cardHeight = cardWidth * Constants.CARD_ASPECT_RATIO;
        Logger.i("card width:" + cardWidth + ",height:" + cardHeight);
        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, (int) cardHeight);
        savedCardPager.setLayoutParams(parms);
        circlePageIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        creditCards = ((CreditDebitCardFlowActivity) getActivity()).getCreditCards();

        if (creditCards != null) {
             cardPagerAdapter = new CardPagerAdapter(this,getChildFragmentManager(),
                    creditCards);
            savedCardPager.setAdapter(cardPagerAdapter);
            savedCardPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int
                        positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    /*SdkUtil.hideKeyboard(getActivity());*/
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            circlePageIndicator.setViewPager(savedCardPager);
            showHideNoCardMessage();
        }


    }

    private void showHideNoCardMessage() {
        if (creditCards.isEmpty()) {
            emptyCardsTextViewFont.setVisibility(View.VISIBLE);
            //savedCardPager.setVisibility(View.GONE);
        } else {
            emptyCardsTextViewFont.setVisibility(View.GONE);
            //savedCardPager.setVisibility(View.VISIBLE);
        }
    }

    public void deleteCreditCard(String cardNumber) {

        if(creditCards!=null && !creditCards.isEmpty()){
            int position  = -1;
            for(int i =0; i < creditCards.size();i++){
                if(creditCards.get(i).getCardNumber().equalsIgnoreCase(cardNumber)){
                    position = i;
                }
            }
            Logger.i("position to delete:"+position+",creditCard size:"+creditCards.size());
            if(position >= 0){
                creditCards.remove(position);
                Logger.i("creditCard size:" + creditCards.size());
            }
            cardPagerAdapter.notifyDataSetChanged();
            circlePageIndicator.notifyDataSetChanged();
            showHideNoCardMessage();
            ((CreditDebitCardFlowActivity)getActivity()).saveCreditCards();
        }
    }
}