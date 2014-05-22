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

import android.graphics.Paint;
import android.widget.TextView;

public class ShoppingListViewHolder {
	
	protected long id;
	protected TextView name;
	protected TextView qty;
	protected boolean selected;
	
	protected int position;
	protected ShoppingListModel model;
	private int color;
	private int style;
	
	public ShoppingListViewHolder(boolean selected, int position) {
//		position = 0;
		this.selected = selected;
		this.position = position;
		
		setStyle();
	}
	
	public int getColor() {
		return color;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getStyle() {
		return style;
	}
	
	public void setSelected(boolean selected) {
	
		model.setSelected(selected);
		this.selected = selected;		
		setStyle();
	}
	
	private void setStyle() {
		
		if(selected) {
			color = 0xFFffffb6;
			style = Paint.STRIKE_THRU_TEXT_FLAG;
		} else {
			color = SampleMenuActivity.ROWS_COLORS[this.position % 2];
			style = Paint.LINEAR_TEXT_FLAG;
		}
	}

}
