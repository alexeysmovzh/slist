/* (C) Copyright 2014 Alexey Smovzh (http://fasthamster.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Alexey Smovzh (alexeysmovzh@gmail.com)
 */

package com.fasthamster.slist;

import java.util.ArrayList;
import java.util.Map;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProductListDeleteDialog extends DialogFragment implements OnClickListener {
	
	private static ArrayList<Map<String, String>> rows;

	static interface Listener {
		void deleteProductListRow();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		rows = (ArrayList<Map<String, String>>) getArguments().getSerializable("product");
	}
	
	public static ProductListDeleteDialog newInstance(ArrayList<Map<String, String>> product) {
	
		ProductListDeleteDialog dialog = new ProductListDeleteDialog();
		
		Bundle args = new Bundle();		
		args.putSerializable("product", product);
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
				
		getDialog().setTitle(R.string.product_list_delete_dialog_title);
		View v = inflater.inflate(R.layout.product_list_delete_dialog, null);
		
		v.findViewById(R.id.product_list_delete_dialog_text);
		TextView productList = (TextView) v.findViewById(R.id.product_list_delete_dialog_list);
		v.findViewById(R.id.product_list_delete_dialog_btnok).setOnClickListener(this);
		v.findViewById(R.id.product_list_delete_dialog_btncancel).setOnClickListener(this);
		
		if(!rows.isEmpty()) {

			String list = "";
			for(Map<String, String> r : rows) {
				for(String i : r.keySet()) {
					list += "- " + i + " 	" + r.get(i) + "\n";	
				}
			}
	
			productList.setText(list);
			productList.setTextColor(Color.GRAY);
		}
		
		return v;
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.product_list_delete_dialog_btnok:

				((Listener)getActivity()).deleteProductListRow();
				
				break;
				
			case R.id.product_list_delete_dialog_btncancel:				
				break;
		}
		
		dismiss();
	}
	
}