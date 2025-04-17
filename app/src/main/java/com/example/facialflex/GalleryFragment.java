package com.example.facialflex;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class GalleryFragment extends Fragment {

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Set up the ViewPager with the adapter
        pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
        viewPager.setAdapter(pagerAdapter);

        // Connect the TabLayout with the ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Facial Muscles");
                                break;
                            case 1:
                                tab.setText("Gallery");
                                break;
                            case 2:
                                tab.setText("Exercise History");
                                break;
                        }
                    }
                }).attach();

        // Check if there's an Intent with a "tabIndex" extra to navigate to the correct tab
        if (getArguments() != null) {
            int tabIndex = getArguments().getInt("tabIndex", 0); // Default to the first tab if not passed
            viewPager.setCurrentItem(tabIndex);
        }

        return view;
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PieChartFragment();
                case 1:
                    return new GalleryPageFragment();
                case 2:
                    return new ExercisesHistoryFragment();
                default:
                    return new PieChartFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Number of pages
        }
    }
}
