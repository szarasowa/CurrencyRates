package ovh.szarasowa.currencyrates;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Em on 2017-10-05.
 */

public class GraphAdapter extends ArrayAdapter<GraphItem> {

    public GraphAdapter(Activity activity, ArrayList<GraphItem> itemList) {
        super(activity, 0, itemList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_layout, parent, false);
        }

        GraphItem currentTransaction = getItem(position);
        assert currentTransaction != null;

        TextView type = (TextView) listItemView.findViewById(R.id.item_type_textView);
        type.setText(currentTransaction.getType());

        TextView price = (TextView) listItemView.findViewById(R.id.item_price_textView);
        price.setText(currentTransaction.getPrice());

        TextView time = (TextView) listItemView.findViewById(R.id.item_time_textView);
        time.setText(currentTransaction.getTime());

        return listItemView;
    }
}
