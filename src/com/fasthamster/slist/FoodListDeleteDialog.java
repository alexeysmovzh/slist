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

public class FoodListDeleteDialog extends DialogFragment implements OnClickListener {
	
	private static ArrayList<Map<Long, String>> rows;

	static interface Listener {
		void deleteFoodListRow(ArrayList<Map<Long, String>> rows);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		rows = (ArrayList<Map<Long, String>>) getArguments().getSerializable("food");
	}
	
	public static FoodListDeleteDialog newInstance(ArrayList<Map<Long, String>> food) {
	
		FoodListDeleteDialog dialog = new FoodListDeleteDialog();
		
		Bundle args = new Bundle();		
		args.putSerializable("food", food);
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
				
		getDialog().setTitle(R.string.food_list_delete_dialog_title);
		View v = inflater.inflate(R.layout.food_list_delete_dialog, null);
		
		v.findViewById(R.id.food_list_delete_dialog_text);
		TextView foodList = (TextView) v.findViewById(R.id.food_list_delete_dialog_list);
		v.findViewById(R.id.food_list_delete_dialog_btnok).setOnClickListener(this);
		v.findViewById(R.id.food_list_delete_dialog_btncancel).setOnClickListener(this);
		
		if(!rows.isEmpty()) {

			String list = "";
			for(Map<Long, String> r : rows) {
				for(Long i : r.keySet()) {
					list += "- " + r.get(i) + "\n";	
				}
			}
	
			foodList.setText(list);
			foodList.setTextColor(Color.GRAY);
		}
		
		return v;
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.food_list_delete_dialog_btnok:

				((Listener)getActivity()).deleteFoodListRow(rows);
				
				break;
				
			case R.id.food_list_delete_dialog_btncancel:				
				break;
		}
		
		dismiss();
	}
	
}