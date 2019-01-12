package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CardFragmentActivity extends androidx.fragment.app.FragmentActivity {
    final int PAGE_COUNT = 2;
    private PagerAdapter pagerAdapter;
    private String tabTitles[] = new String[]{"Question", "Yes No"};
    private Context context;
    private List<Fragment> fragments;
    BottomappbarCallbackInterface bottomappbarCallbackInterface;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main);

        BottomAppBar bab = findViewById(R.id.bottomAppBar);
        bab.replaceMenu(R.menu.contextual_menu);

        fragments = new LinkedList<Fragment>();

        //initialsie the pager
        this.initialisePaging();
    }

    private boolean locked = false;


    public void setBottomappbarCallbackInterface(final BottomappbarCallbackInterface listener){
        bottomappbarCallbackInterface = listener;
        final FloatingActionButton fab = findViewById(R.id.add_card_fab);
        final ActionMenuItemView  newItemCreate = findViewById(R.id.new_item_create);
        final ActionMenuItemView  itemEdit = findViewById(R.id.item_edit);
        final ActionMenuItemView itemDelete = findViewById(R.id.item_delete);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addButtonClick();
            }
        });

        final BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locked = !locked;

                bottomAppBar.setNavigationIcon(locked ? R.drawable.ic_lock_outline_black_24dp : R.drawable.ic_lock_open_black_24dp);
                bottomAppBar.getNavigationIcon().setAlpha(locked ? 158:255);
                fab.setAlpha(locked? .5f:1f);
                newItemCreate.setAlpha(locked? .5f:1f);
                itemEdit.setAlpha(locked? .5f:1f);
                itemDelete.setAlpha(locked? .5f:1f);



                listener.toggleUiLockClick();


            }
        });

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
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
        this.pagerAdapter = new CardPagerAdapter(super.getSupportFragmentManager(), fragments);
        //
        ViewPager pager = super.findViewById(R.id.viewpager);
        //TabLayout tabLayout = findViewById(R.id.tabDots);
        //tabLayout.setupWithViewPager(pager, true);
        pager.setAdapter(this.pagerAdapter);


    }

    public void pressYesNo(View v) {
        TextView tv = v.findViewWithTag("name");
        String s = tv.getText().toString();
        TextToSpeechManager.speak(s);
    }


}


