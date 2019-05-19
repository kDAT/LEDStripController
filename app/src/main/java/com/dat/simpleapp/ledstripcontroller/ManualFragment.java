package com.dat.simpleapp.ledstripcontroller;

/**
 * Based on https://stackoverflow.com/a/41656303/11245391
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ManualFragment extends Fragment {

    Mode mStaticMode;
    Mode mDynamicMode;
    StaticFragment mStaticFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static ManualFragment newInstance(Mode staticMode, Mode dynamicMode) {
        ManualFragment fragment = new ManualFragment();
        fragment.setModes(staticMode, dynamicMode);
        return fragment;
    }

    public void setModes(Mode staticMode, Mode dynamicMode) {
        mStaticMode = staticMode;
        mDynamicMode = dynamicMode;
        // Send the static on start
        mStaticMode.setBytes(mStaticMode.getBytes(), true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        mStaticFragment = StaticFragment.newInstance(mStaticMode);
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(mStaticFragment, getString(R.string.tab_title_static));
        adapter.addFragment(DynamicFragment.newInstance(mDynamicMode), getString(R.string.tab_title_dynamic));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                // Send when the Page changes
                if (i == 0) mStaticMode.setBytes(mStaticMode.getBytes(), true);
                else mDynamicMode.setBytes(mDynamicMode.getBytes(), true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void setStripSize(int stripSize) {
        mStaticFragment.setStripSize(stripSize);
    }
}
