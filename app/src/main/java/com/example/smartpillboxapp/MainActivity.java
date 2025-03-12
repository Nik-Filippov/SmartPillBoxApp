package com.example.smartpillboxapp;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private FrameLayout viewPagerContainer;
    private FrameLayout fragmentContainer;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        viewPagerContainer = findViewById(R.id.viewPagerContainer);

        // Setup the ViewPager with an adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach the TabLayout with the ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Home");
                    break;
                case 1:
                    tab.setText("Calendar");
                    break;
                case 2:
                    tab.setText("Settings");
                    break;
            }
        }).attach();
    }

    // Method to navigate to EditContainer1Fragment and swap ViewPager
    public void navigateToEditFragment() {
        // First, remove the ViewPager2 from the layout
        viewPagerContainer.setVisibility(View.GONE);  // Hide ViewPager container

        // Replace the FrameLayout with EditContainer1Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.viewPagerContainer, new EditContainerInfoDialog()) // Add EditContainer1Fragment dynamically
                .commit();
    }

    public void navigateToHomeFragment() {
        // First, remove the EditContainer1Fragment if it's visible
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.viewPagerContainer, new HomeFragment())
                .addToBackStack(null) // Optional: If you want to allow back navigation
                .commit();

        // Show the ViewPager container again
        viewPagerContainer.setVisibility(View.VISIBLE);  // Make sure the ViewPager container is visible
    }
}
