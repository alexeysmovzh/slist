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

import android.app.Activity;
import android.app.DialogFragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ShoppingListListener extends Activity implements OnTouchListener {
	
	private float paddingX = 0;
	private float initialX = 0;
	private float currentX = 0;
	
	private float paddingY = 0;
	private float initialY = 0;
	private float currentY = 0;

	private ShoppingListAdapter adapter;	
	private ShoppingListViewHolder viewHolder;
	private db_handler handler;
	private Activity context;
	private ArrayList<String> ac_array;

	public ShoppingListListener(Activity context, ShoppingListAdapter adapter, db_handler handler, ArrayList<String> ac_array) {
		this.adapter = adapter;
		this.handler = handler;
		this.context = context;
		this.ac_array = ac_array;
	}

	@Override
	public boolean onTouch(View v, MotionEvent motionEvent) {
	
		int event = motionEvent.getAction();
		
		if(event == MotionEvent.ACTION_DOWN) {
			paddingX = 0;
			initialX = motionEvent.getX();
			currentX = motionEvent.getX();
			
			paddingY = 0;
			initialY = motionEvent.getY();
			currentY = motionEvent.getY();

			viewHolder = ((ShoppingListViewHolder) v.getTag());  
			
			v.setBackgroundColor(0xff33b5e5);
		}
		
		if(event == MotionEvent.ACTION_MOVE) {
			currentX = motionEvent.getX();
			paddingX = currentX - initialX;
			
			currentY = motionEvent.getY();
			paddingY = currentY - initialY;

			v.setPadding((int)paddingX*5, 0, 0, 0);
			v.setBackgroundColor(viewHolder.getColor());
		}

		if(event == MotionEvent.ACTION_UP || event == MotionEvent.ACTION_CANCEL) {
			
			if(viewHolder != null) {
				
				if(paddingX > 10 && paddingY > -5 && paddingY < 5) {
					
					if(viewHolder.selected == true) {			// выполняется при восстановлении элемента
						
						viewHolder.setSelected(false);
						handler.setSelected(viewHolder.id, false);
						
					} else {									// выполняется при вычеркивании элемента
					
						viewHolder.setSelected(true);
						handler.setSelected(viewHolder.id, true);
					}
					
					adapter.changeItem(viewHolder.model);						
					viewHolder.name.setPaintFlags(viewHolder.getStyle());
					viewHolder.qty.setPaintFlags(viewHolder.getStyle());
					
					SampleMenuActivity.changeShoppingListMenuClearItem(adapter.thereIsSelected());
				}
				
				if(paddingX > -5 && paddingX < 5 && paddingY > -5 && paddingY < 5) {				// короткое нажатие
					
					DialogFragment shoppingListAddDialog = ShoppingListAddDialog.newInstance(viewHolder.model, viewHolder.getPosition(), ac_array);

					shoppingListAddDialog.show(context.getFragmentManager(), "shoppingListAddDialog");
				}

			paddingX = 0;
			initialX = 0;
			currentX = 0;
			
			paddingY = 0;
			initialY = 0;
			currentY = 0;
			
			v.setPadding(0, 0, 0, 0);
			}		
		}
		
		return true;
	}

}
