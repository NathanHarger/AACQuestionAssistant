package com.nathan.harger.aacquestionassistant;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

@SuppressWarnings("CanBeFinal")
class CardPagerAdapter extends FragmentPagerAdapter {

    @SuppressWarnings("CanBeFinal")
    private List<Fragment> fragments;

    public CardPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
