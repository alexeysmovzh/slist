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
import java.util.HashMap;
import java.util.Map;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;


public class ProductListAddDialog extends DialogFragment implements OnClickListener, TextWatcher {

	private AutoCompleteTextView userInputName;
	private EditText userInputQty;
	private int position;
	private long id;
	private long f_id;
	private String name;
	private String qty;
	private ArrayList<String> ac_array;
	
	static interface Listener {
		void addProductItem(Map<String, String> row);
		void updateProductItem(long id, long f_id, Map<String, String> row, int position);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		position = getArguments().getInt("position");
		id = getArguments().getLong("id");
		f_id = getArguments().getLong("f_id");
		name = getArguments().getString("name");
		qty = getArguments().getString("qty");		
		ac_array = getArguments().getStringArrayList("ac_array");
	}
	
	public static ProductListAddDialog newInstance(ProductListModel model, int position, ArrayList<String> ac_array) {
		
		ProductListAddDialog dialog = new ProductListAddDialog();
		
		Bundle args = new Bundle();
		args.putInt("position", position);
		args.putLong("id", model.getId());
		args.putLong("f_id", model.getFId());
		args.putString("name", model.getName());
		args.putString("qty", model.getQty());		
		args.putStringArrayList("ac_array", ac_array);
		
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		
		getDialog().setTitle(R.string.shopping_list_add_dialog_title);
		View v = inflater.inflate(R.layout.shopping_list_add_dialog, null);
		
		userInputName = (AutoCompleteTextView) v.findViewById(R.id.shopping_list_add_dialog_name);
		userInputQty = (EditText) v.findViewById(R.id.shopping_list_add_dialog_qty);
		
		if(name.length() > 0) {
			userInputName.setText(name);
			userInputQty.setText(qty);			
		}

		if(ac_array.isEmpty() == false) {
			userInputName.addTextChangedListener(this);
			userInputName.setAdapter(new ArrayAdapter<String>(this.getActivity(), 
											android.R.layout.simple_dropdown_item_1line, ac_array));
		}
			
		v.findViewById(R.id.shopping_list_add_dialog_btnadd).setOnClickListener(this);
		v.findViewById(R.id.shopping_list_add_dialog_btncancel).setOnClickListener(this);
		
		userInputName.requestFocus();
		getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.shopping_list_add_dialog_btnadd:
			
				String inputName = userInputName.getText().toString().trim();
				String inputQty = userInputQty.getText().toString();
				
				if(!inputName.isEmpty()) {
					Map<String, String> row = new HashMap<String, String>();
					row.put(inputName, inputQty);
				
					if(name.length() > 0) {
						((Listener)getActivity()).updateProductItem(id, f_id, row, position);					
					} else {
						((Listener)getActivity()).addProductItem(row);
					}
				}
					
				break;
				
			case R.id.shopping_list_add_dialog_btncancel:				
				break;
		}
		
		dismiss();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}

