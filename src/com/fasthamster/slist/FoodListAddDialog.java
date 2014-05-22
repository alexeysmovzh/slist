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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

public class FoodListAddDialog extends DialogFragment implements OnClickListener {

	private EditText userInput;
	private long id;	
	private int position;
	private String name;

	static interface Listener {
		void insertFoodListRow(String row);
		void updateFoodListRow(long id, int position, String row);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		id = getArguments().getLong("id");
		position = getArguments().getInt("position");
		name = getArguments().getString("name");
	}
	
	public static FoodListAddDialog newInstance(long id, String name, int position) {
		
		FoodListAddDialog dialog = new FoodListAddDialog();
		
		Bundle args = new Bundle();
		args.putLong("id", id);
		args.putString("name", name);
		args.putInt("position", position);
		
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.food_list_add_dialog, null);
		
		userInput = (EditText) v.findViewById(R.id.food_list_add_dialog_text);
		
		if(name.length() > 0) {
			getDialog().setTitle(R.string.food_list_add_dialog_title_rename);
			
			userInput.setText(name);
			
		} else {
			getDialog().setTitle(R.string.food_list_add_dialog_title);
		}

		v.findViewById(R.id.food_list_add_dialog_btnsave).setOnClickListener(this);
		v.findViewById(R.id.food_list_add_dialog_btncancel).setOnClickListener(this);
		
		userInput.requestFocus();
		getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.food_list_add_dialog_btnsave:
			
				String row = userInput.getText().toString();
				
				if(!row.isEmpty()) {				
					if(name.length() > 0) {
						((Listener)getActivity()).updateFoodListRow(id, position, row);
					} else {
						((Listener)getActivity()).insertFoodListRow(row);
					}
				}
				
				break;
				
			case R.id.food_list_add_dialog_btncancel:				
				break;
		}
		
		dismiss();
	}
}