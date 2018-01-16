package org.tasks.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tasks.R;

import java.util.ArrayList;
import java.util.List;

public class TypeTopArrayAdapter<T> extends ArrayAdapter<T> {

    private final List<String> hints;

    public TypeTopArrayAdapter(Context context, int resources, List<T> objects) {
        this(context, resources, objects, new ArrayList<>());
    }

    public TypeTopArrayAdapter(Context context, int resource, List<T> objects, List<String> hints) {
        super(context, resource, objects);
        this.hints = hints;
    }

    @Override
    public View getDropDownView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        View v;

        ViewGroup vg = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_dropdown_item, parent, false);
        ((TextView) vg.findViewById(R.id.text1)).setText(getItem(position).toString());
        if (position < hints.size()) {
            ((TextView) vg.findViewById(R.id.text2)).setText(hints.get(position));
        }
        v = vg;

        parent.setVerticalScrollBarEnabled(false);
        return v;
    }
}
