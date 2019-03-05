package com.nathan.harger.aacquestionassistant;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import static android.content.res.AssetManager.ACCESS_STREAMING;

public class CardFragmentActivity extends androidx.fragment.app.FragmentActivity {
    private List<Fragment> fragments;
    private ImageDatabaseHelper idh;
    private boolean locked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main);

        BottomAppBar bab = findViewById(R.id.bottomAppBar);

        bab.replaceMenu(R.menu.contextual_menu);

        fragments = new LinkedList<>();

        //initialise the pager
        this.initialisePaging();
        idh = ImageDatabaseHelper.getInstance(this.getApplicationContext());

        if (idh.getSize() == 0) {
            setupDB();
        }

    }

    public void setBottomappbarCallbackInterface(final BottomappbarCallbackInterface listener) {
        final ActionMenuItemView newItemCreate = findViewById(R.id.new_item_create);
        final ActionMenuItemView itemEdit = findViewById(R.id.item_edit);
        final ActionMenuItemView itemDelete = findViewById(R.id.item_delete);
        final ActionMenuItemView newcard = findViewById(R.id.add_card);
        final BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locked = !locked;
                bottomAppBar.setNavigationIcon(locked ? R.drawable.locked : R.drawable.unlocked);
                Objects.requireNonNull(bottomAppBar.getNavigationIcon()).setAlpha(locked ? 158 : 255);

                if (newItemCreate != null)
                    newItemCreate.setAlpha(locked ? .5f : 1f);
                
                if (itemEdit != null)
                    itemEdit.setAlpha(locked ? .5f : 1f);

                if (itemDelete != null)
                    itemDelete.setAlpha(locked ? .5f : 1f);

                if (newcard != null)
                    newcard.setAlpha(locked ? .5f : 1f);
                listener.toggleUiLockClick();
            }
        });

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                listener.menuClick(item.getItemId());
                return false;
            }
        });

    }

    public void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);
        //getSupportFragmentManager().putFragment(outstate,"myfragment",fragments.get(0));
    }

    public void onRestoreInstanceState(Bundle instate) {
        super.onSaveInstanceState(instate);
        fragments.set(0, getSupportFragmentManager().getFragment(instate, "myfragment"));
    }

    private void initialisePaging() {
        fragments.add(Fragment.instantiate(this, CardTablePageFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, YesNoPageFragment.class.getName()));
        PagerAdapter pagerAdapter = new CardPagerAdapter(super.getSupportFragmentManager(), fragments);
        ViewPager pager = super.findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager, true);
        pager.setAdapter(pagerAdapter);
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("Question");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("Yes No");
    }

    public void pressYesNo(View v) {
        ConstraintLayout cv = ((ConstraintLayout) v.getParent());
        CardView other;
        int otherColor;

        if (v.getId() == R.id.cv) {
            other = cv.findViewById(R.id.cv1);
            otherColor = R.color.red;
        } else {
            other = cv.findViewById(R.id.cv);
            otherColor = R.color.green;

        }


        Context c = this.getApplicationContext();
        Resources r = c.getResources();
        Resources.Theme theme = c.getTheme();

        Drawable d = VectorDrawableCompat.create(r, R.drawable.outline, theme);

        v.setBackground(d);
        other.setBackgroundColor(otherColor);
        TextView tv = v.findViewWithTag("name");
        String s = tv.getText().toString();
        TextToSpeechManager.speak(s);
    }


    private void setupDB() {
        String line;
        String split = ",";
        BufferedReader b = null;
        try {
            // read in core vocab
            InputStream s = this.getApplicationContext().getAssets().open("symbol-info.csv", ACCESS_STREAMING);
            b = new BufferedReader(new InputStreamReader(s));

            // read header
            b.readLine();
            while ((line = b.readLine()) != null) {
                idh.addImage(new Card(line.split(split)));
            }

            // read in custom vocab
            FileOperations.readNewVocab(this.getApplicationContext(), idh);
        } catch (FileNotFoundException e) {
            Log.e("CSV parsing: ", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    Log.e("CSV parsing: ", e.getMessage());
                }
            }
        }
    }
}


