package id.co.veritrans.sdk.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import id.co.veritrans.sdk.fragments.CardDetailFragment;
import id.co.veritrans.sdk.models.CardTokenRequest;

/**
 * Created by chetan on 27/10/15.
 */
public class CardPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<CardTokenRequest> cardDetails;
    private Fragment parentFragment;
    private long baseId = 0;
    public CardPagerAdapter(Fragment fragment, FragmentManager fm, ArrayList<CardTokenRequest> cardDetails) {
        super(fm);
        this.cardDetails = cardDetails;
        parentFragment = fragment;
    }

    @Override
    public Fragment getItem(int position) {
        return CardDetailFragment.newInstance(cardDetails.get(position),parentFragment);
    }

    @Override
    public float getPageWidth(int position) {
        return 1f;
    }

    @Override
    public int getCount() {
        if (cardDetails != null && cardDetails.size() > 0) {
            return cardDetails.size();
        } else {
            return 0;
        }
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     * @param n number of items which have been changed
     */
    public void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getCount() + n;
    }
}
