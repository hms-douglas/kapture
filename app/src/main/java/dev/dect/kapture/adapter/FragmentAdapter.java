package dev.dect.kapture.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentStateAdapter {
    final private List<Fragment> FRAGMENT_LIST;

    public FragmentAdapter(FragmentActivity fa, List<Fragment> fl) {
        super(fa);
        this.FRAGMENT_LIST = fl;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FRAGMENT_LIST.get(position);
    }

    @Override
    public int getItemCount() {
        return FRAGMENT_LIST.size();
    }

}
