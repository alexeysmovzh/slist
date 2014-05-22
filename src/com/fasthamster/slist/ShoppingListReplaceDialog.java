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


public class ShoppingListReplaceDialog extends DialogFragment implements OnClickListener {
	
	private String scan;
	
	static interface Listener {
		void replaceShoppingListItems(String scan);
		void appendShoppingListItems(String scan);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		scan = getArguments().getString("scan");
	}
	
	public static ShoppingListReplaceDialog newInstance(String scan) {
		
		ShoppingListReplaceDialog dialog = new ShoppingListReplaceDialog();
		Bundle args = new Bundle();
		
		args.putString("scan", scan);
		dialog.setArguments(args);
		
		return dialog;		
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		
		getDialog().setTitle(R.string.shopping_list_replace_dialog_title);
		View v = inflater.inflate(R.layout.shopping_list_replace_dialog, null);
		
		v.findViewById(R.id.shopping_list_replace_dialog_text);
		v.findViewById(R.id.shopping_list_replace_dialog_btnappend).setOnClickListener(this);
		v.findViewById(R.id.shopping_list_repace_dialog_btnreplace).setOnClickListener(this);
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.shopping_list_replace_dialog_btnappend:

				((Listener)getActivity()).appendShoppingListItems(scan);	
				
				break;
				
			case R.id.shopping_list_repace_dialog_btnreplace:
				
				((Listener)getActivity()).replaceShoppingListItems(scan);
				
				break;
		}
		
		dismiss();
	}
}

