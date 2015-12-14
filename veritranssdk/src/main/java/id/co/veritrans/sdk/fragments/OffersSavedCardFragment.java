package id.co.veritrans.sdk.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import id.co.veritrans.sdk.R;
import id.co.veritrans.sdk.activities.CreditDebitCardFlowActivity;
import id.co.veritrans.sdk.activities.OffersActivity;
import id.co.veritrans.sdk.adapters.CardPagerAdapter;
import id.co.veritrans.sdk.callbacks.DeleteCardCallback;
import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.core.SdkUtil;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.models.CardTokenRequest;
import id.co.veritrans.sdk.models.DeleteCardResponse;
import id.co.veritrans.sdk.models.OffersListModel;
import id.co.veritrans.sdk.widgets.CirclePageIndicator;
import id.co.veritrans.sdk.widgets.TextViewFont;

public class OffersSavedCardFragment extends Fragment {

    private TextViewFont textViewTitleOffers = null;
    private TextViewFont textViewTitleCardDetails = null;
    private TextViewFont textViewOfferName = null;
    private int offerPosition = 0;
    private String offerName = null;
    private String offerType = null;

    private ImageView imageViewPlus = null;
    private ImageView imageViewMinus = null;
    private TextViewFont textViewInstalment = null;
    private RelativeLayout layoutPayWithInstalment = null;

    private ViewPager savedCardPager;
    private CirclePageIndicator circlePageIndicator;
    private FloatingActionButton addCardBt;
    private VeritransSDK veritransSDK;
    private ArrayList<CardTokenRequest> creditCards;
    private CardPagerAdapter cardPagerAdapter;

    private TextViewFont emptyCardsTextViewFont;

    int currentPosition, totalPositions;

    private final String MONTH = "Month";


    public OffersSavedCardFragment() {

    }

    public static OffersSavedCardFragment newInstance(int position, String offerName, String
            offerType) {
        OffersSavedCardFragment fragment = new OffersSavedCardFragment();
        Bundle data = new Bundle();
        data.putInt(OffersActivity.OFFER_POSITION, position);
        data.putString(OffersActivity.OFFER_NAME, offerName);
        data.putString(OffersActivity.OFFER_TYPE, offerType);
        fragment.setArguments(data);
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
        return inflater.inflate(R.layout.fragment_offers_saved_card, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle data = getArguments();
        offerPosition = data.getInt(OffersActivity.OFFER_POSITION);
        offerName = data.getString(OffersActivity.OFFER_NAME);
        offerType = data.getString(OffersActivity.OFFER_TYPE);
        bindViews(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void setToolbar() {
        textViewTitleOffers.setVisibility(View.GONE);
        textViewTitleCardDetails.setVisibility(View.VISIBLE);
        textViewOfferName.setVisibility(View.VISIBLE);
        textViewOfferName.setText(offerName);

        textViewTitleCardDetails.setText(getResources().getString(R.string.saved_card));
    }

    private void hideOrShowPayWithInstalment(boolean isShowLayout) {
        if (isShowLayout) {
            layoutPayWithInstalment.setVisibility(View.VISIBLE);
            showDuration();
        } else {
            layoutPayWithInstalment.setVisibility(View.GONE);
        }
    }

    private void showDuration() {

        ArrayList<OffersListModel> offersList = new ArrayList<>();

        if (((OffersActivity) getActivity()).offersListModels != null || !((OffersActivity) getActivity()).offersListModels.isEmpty()) {
            offersList.addAll(((OffersActivity) getActivity()).offersListModels);
            if (offersList.get(offerPosition).getDuration() != null || !offersList.get(offerPosition).getDuration().isEmpty()) {
                currentPosition = 0;
                totalPositions = offersList.get(offerPosition).getDuration().size() - 1;
                textViewInstalment.setText(offersList.get(offerPosition).getDuration().get(0)
                        .toString() + " " + MONTH);
                disableEnableMinusPlus();
            }
        }
    }

    private void disableEnableMinusPlus() {

        Logger.i("Positions: " + currentPosition + "----" + totalPositions);

        if (currentPosition == 0 && totalPositions == 0) {
            imageViewMinus.setEnabled(false);
            imageViewPlus.setEnabled(false);
        } else if (currentPosition > 0 && currentPosition < totalPositions) {
            imageViewMinus.setEnabled(true);
            imageViewPlus.setEnabled(true);
        } else if (currentPosition > 0 && currentPosition == totalPositions) {
            imageViewMinus.setEnabled(true);
            imageViewPlus.setEnabled(false);
        } else if (currentPosition == 0 && currentPosition < totalPositions) {
            imageViewMinus.setEnabled(false);
            imageViewPlus.setEnabled(true);
        }
    }

    private void onMinusClicked() {
        if (currentPosition > 0 && currentPosition <= totalPositions) {
            currentPosition = currentPosition - 1;
            textViewInstalment.setText(((OffersActivity) getActivity()).offersListModels.get
                    (offerPosition).getDuration().get(currentPosition)
                    .toString() + " " + MONTH);
        }
        disableEnableMinusPlus();
    }

    private void onPlusClicked() {
        if (currentPosition >= 0 && currentPosition < totalPositions) {
            currentPosition = currentPosition + 1;
            textViewInstalment.setText(((OffersActivity) getActivity()).offersListModels.get
                    (offerPosition).getDuration().get(currentPosition)
                    .toString() + " " + MONTH);
        }
        disableEnableMinusPlus();
    }

    private void bindViews(View view) {

        textViewTitleOffers = (TextViewFont) getActivity().findViewById(R.id.text_title);
        textViewTitleCardDetails = (TextViewFont) getActivity().findViewById(R.id
                .text_title_card_details);
        textViewOfferName = (TextViewFont) getActivity().findViewById(R.id.text_title_offer_name);

        setToolbar();

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

        imageViewMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMinusClicked();
            }
        });

        imageViewPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlusClicked();
            }
        });

        emptyCardsTextViewFont = (TextViewFont) view.findViewById(R.id.text_empty_saved_cards);
        savedCardPager = (ViewPager) view.findViewById(R.id.saved_card_pager);
        addCardBt = (FloatingActionButton) view.findViewById(R.id.btn_add_card);
        addCardBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OffersAddCardDetailsFragment offersAddCardDetailsFragment = OffersAddCardDetailsFragment
                        .newInstance(offerPosition, offerName, offerType);
                ((OffersActivity) getActivity()).replaceFragment
                        (offersAddCardDetailsFragment, true, false);
            }
        });
        float cardWidth = ((OffersActivity) getActivity()).getScreenWidth();
        float cardHeight = cardWidth * Constants.CARD_ASPECT_RATIO;
        Logger.i("card width:" + cardWidth + ",height:" + cardHeight);
        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, (int) cardHeight);
        savedCardPager.setLayoutParams(parms);
        circlePageIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        creditCards = ((OffersActivity) getActivity()).getCreditCardList();

        setViewPagerValues();
    }


    private void setViewPagerValues() {
        if (creditCards != null) {
            if (getActivity() != null) {
                cardPagerAdapter = new CardPagerAdapter(this, getChildFragmentManager(),
                        creditCards, getActivity());
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
                //to notify adapter when credit card details received
                ((OffersActivity) getActivity()).setAdapterViews(cardPagerAdapter, circlePageIndicator, emptyCardsTextViewFont);
                showHideNoCardMessage();
            }
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
        showHideNoCardMessage();
        deleteCards(cardNumber);

    }

    public void deleteCards(final String cardNumber) {
        CardTokenRequest creditCard = null;
        Logger.i("cardNumber:" + cardNumber);
        if (creditCards != null && !creditCards.isEmpty()) {

            for (int i = 0; i < creditCards.size(); i++) {
                if (creditCards.get(i).getCardNumber().equalsIgnoreCase(cardNumber)) {
                    creditCard = creditCards.get(i);
                }
            }
            try {
                Logger.i("position to delete:" + creditCard.getCardNumber() + ",creditCard size:" + creditCards.size());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (creditCard != null) {
            veritransSDK.deleteCard(getActivity(), creditCard, new DeleteCardCallback() {
                @Override
                public void onFailure(String errorMessage) {
                    SdkUtil.showSnackbar(getActivity(), errorMessage);
                }

                @Override
                public void onSuccess(DeleteCardResponse deleteResponse) {
                    if (deleteResponse == null || !deleteResponse.getMessage().equalsIgnoreCase(getString(R.string.success))) {
                        return;
                    }
                    int position = -1;
                    for (int i = 0; i < creditCards.size(); i++) {
                        if (creditCards.get(i).getCardNumber().equalsIgnoreCase(cardNumber)) {
                            position = i;
                        }
                    }
                    if (creditCards != null && !creditCards.isEmpty()) {
                        Logger.i("position to delete:" + position + "," + creditCards.size());
                        if (!creditCards.isEmpty()) {
                            for (int i = 0; i < creditCards.size(); i++) {
                                Logger.i("cards before:" + creditCards.get(i).getCardNumber());
                            }
                        }

                        creditCards.remove(position);


                        if (!creditCards.isEmpty()) {
                            for (int i = 0; i < creditCards.size(); i++) {

                                Logger.i("cards after:" + creditCards.get(i).getCardNumber());
                            }
                        }

                        //notifydataset change not worked properly for viewpager so setting it again
                        Logger.i("setting view pager value");
                        // setViewPagerValues(creditCardsNew);
                        /*if(creditCards.size()>1) {
                            try {
                                savedCardPager.setCurrentItem(position);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                savedCardPager.setCurrentItem(creditCards.size() - 1);
                            }
                        }*/
                        if (cardPagerAdapter != null && circlePageIndicator != null) {
                            Logger.i("notifying data");
                            cardPagerAdapter.notifyChangeInPosition(1);
                            cardPagerAdapter.notifyDataSetChanged();
                            circlePageIndicator.notifyDataSetChanged();
                            if (creditCards.isEmpty()) {
                                emptyCardsTextViewFont.setVisibility(View.VISIBLE);
                            } else {
                                emptyCardsTextViewFont.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }
    }
}