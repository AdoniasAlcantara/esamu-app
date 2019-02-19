package org.myopenproject.esamu.presentation.firstaid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.domain.FirstAidItem;

import java.util.List;

public class FirstAidAdapter extends PagerAdapter {
    private List<FirstAidItem> items;
    private Context context;

    public FirstAidAdapter(Context context, List<FirstAidItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater
                .from(context).inflate(R.layout.first_aid_item, container, false);

        ImageView image = view.findViewById(R.id.firstAidImage);
        TextView number = view.findViewById(R.id.firstAidNumber);
        TextView info = view.findViewById(R.id.firstAidInfo);

        FirstAidItem item = items.get(position);
        image.setImageBitmap(item.getImage());
        number.setText(Integer.toString(position + 1));
        info.setText(item.getInfo());

        container.addView(view);
        return view;
    }
}
