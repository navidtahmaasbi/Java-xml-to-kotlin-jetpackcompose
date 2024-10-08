package com.azarpark.watchman.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.azarpark.watchman.R;
import com.azarpark.watchman.web_service.responses.DebtObject;

import java.util.ArrayList;
import java.util.List;

public class DebtObjectAdapter extends BaseAdapter {

    private Context context;
    private List<DebtObject> debtObjects;
    private boolean[] checkedItems;
    private OnSelectionsChanged onSelectionsChangedListener;


    public DebtObjectAdapter(Context context, List<DebtObject> debtObjects) {
        this.context = context;
        this.debtObjects = debtObjects;
        this.checkedItems = new boolean[debtObjects.size()];
    }

    public void setOnSelectionsChangedListener(OnSelectionsChanged onSelectionsChangedListener) {
        this.onSelectionsChangedListener = onSelectionsChangedListener;
    }

    @Override
    public int getCount() {
        return debtObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return debtObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_change_plate_object, parent, false);
        }

        CheckBox checkBox = convertView.findViewById(R.id.checkbox);
        TextView priceTextView = convertView.findViewById(R.id.price_tv);

        DebtObject debtObject = debtObjects.get(position);

        checkBox.setText(debtObject.getName());
        priceTextView.setText(String.valueOf(debtObject.getValue()) + " تومان");

        // Set the checked state
        checkBox.setChecked(checkedItems[position]);

        // Set a listener for checkbox state changes
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedItems[position] = isChecked;
            if(onSelectionsChangedListener != null) onSelectionsChangedListener.onChanged();
        });

        if (debtObject.key.equals("balance")) {
            checkBox.setEnabled(false);
        }

        return convertView;
    }

    // Function to check all items
    public void checkAll() {
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = true;
        }
        notifyDataSetChanged();
    }

    // Function to clear all items
    public void clearAll() {
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = false;
        }
        notifyDataSetChanged();
    }

    // Function to get selected items
    public List<DebtObject> getSelectedItems() {
        List<DebtObject> selectedItems = new ArrayList<>();
        for (int i = 0; i < checkedItems.length; i++) {
            if (checkedItems[i]) {
                selectedItems.add(debtObjects.get(i));
            }
        }
        return selectedItems;
    }

    public interface OnSelectionsChanged{
        void onChanged();
    }
}
