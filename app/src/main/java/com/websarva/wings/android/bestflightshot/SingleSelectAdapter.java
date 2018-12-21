package com.websarva.wings.android.bestflightshot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

public class SingleSelectAdapter extends SimpleAdapter {

    LayoutInflater mInflater;
    private int selectedIndex = -1; // 初期値は選択なし



    // コンストラクタ
    public SingleSelectAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] FROM, int[] TO){
        super(context, data, resource, FROM, TO);
    }

    // 指定した位置の選択状態を切り替えるメソッド
    public void toggleSelection(int position) {
        if (position == selectedIndex) {
            selectedIndex = -1;
        } else {
            selectedIndex = position;
        }
        notifyDataSetChanged(); // リストの更新を促す
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        // 選択された位置であればviewを選択状態にする
        view.setSelected(position == selectedIndex);
        return view;
    }

}
