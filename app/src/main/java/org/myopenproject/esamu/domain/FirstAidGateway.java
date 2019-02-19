package org.myopenproject.esamu.domain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.myopenproject.esamu.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FirstAidGateway {
    private static final String TAG = "FIRST_AID_GATEWAY";

    private static int[][][] groups = {
            {
                    {R.raw.fa_0_0, R.string.fa_0_0},
                    {R.raw.fa_0_1, R.string.fa_0_1},
                    {R.raw.fa_0_2, R.string.fa_0_2}
            }
    };

    private FirstAidGateway() {}

    public static List<FirstAidItem> findByGroupId(Context context, int id) {
        if (id < 0 || id >= groups.length)
            return null;

        List<FirstAidItem> items = new ArrayList<>();

        for (int[] res : groups[id]) {
            try (InputStream is = context.getResources().openRawResource(res[0])) {
                Bitmap image = BitmapFactory.decodeStream(is);
                String info = context.getString(res[1]);
                items.add(new FirstAidItem(image, info));
            } catch (IOException e) {
                Log.w(TAG, e.getMessage());
            }
        }

        return items;
    }
}
