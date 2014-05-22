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

import android.app.Activity;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ProductListAdapter extends ArrayAdapter<ProductListModel> {
	
	private ArrayList<ProductListModel> items;
	private Activity context;
	private LayoutInflater inflater;
	private db_handler handler;
	private FoodListAdapter fd_adapter;
	private long f_id;
	private int f_position;
	private ArrayList<String> ac_array;
	
	public ProductListAdapter(Activity context, ArrayList<ProductListModel> data, db_handler handler, 
							  	long f_id, int f_position, ArrayList<String> ac_array, FoodListAdapter fd_adapter) {
		super(context, R.layout.row_in_product, data);
		this.context = context;
		this.handler = handler;
		this.fd_adapter = fd_adapter;
		this.f_id = f_id;
		this.f_position = f_position;
		this.items = new ArrayList<ProductListModel>();
		this.ac_array = ac_array;
		
		this.items.addAll(data);
		inflater = context.getLayoutInflater();
	}
	
	class ProductListViewHolder {
		protected long id;
		protected long f_id;
		protected CheckBox checkbox;		
		protected TextView qty;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup) {

		View view = null;

		if(position > items.size()) {
			return null;
		}
		
		if(convertView == null) {
			view = inflater.inflate(R.layout.row_in_product, null);
			final ProductListViewHolder viewHolder = new ProductListViewHolder();

			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.product_checkbox);	
			viewHolder.qty = (TextView) view.findViewById(R.id.product_qty);
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
					ProductListModel product = (ProductListModel) viewHolder.checkbox.getTag();
					product.setChecked(buttonView.isChecked());
					
					SampleMenuActivity.changeProductListDelItem(thereIsChecked());
				}			
			});
			
			viewHolder.checkbox.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					
					ProductListModel m = items.get(position);
					
					DialogFragment productListAddDialog = ProductListAddDialog.newInstance(m, position, ac_array); 
					productListAddDialog.show(context.getFragmentManager(), "productListAddDialog");
					
					return false;
				}
				
			});
				
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(items.get(position));
		} else {
			view = convertView;
			((ProductListViewHolder) view.getTag()).checkbox.setTag(items.get(position));
		}
		
		view.setBackgroundColor(SampleMenuActivity.ROWS_COLORS[position % 2]);		// ракрашиваем строки четные/нечетные
		
		ProductListModel model = items.get(position);		
		ProductListViewHolder holder = (ProductListViewHolder) view.getTag();
		
		holder.id = model.getId();
		holder.f_id = model.getFId();
		holder.checkbox.setChecked(model.isChecked());
		holder.checkbox.setText(model.getName());
		holder.qty.setText(model.getQty());
		
		return view;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}	
	
	// Добавляет новый продукт
	public void addProductItem(Map<String, String> row) {
		
		long id = handler.insertProductListRow(this.f_id, row);
		
		for(String l : row.keySet()) {
			ProductListModel model = new ProductListModel(id, this.f_id, l, row.get(l), false);
			
			items.add(model);
			this.add(model);
		}
		
		if(items.size() == 1) {
			// первый продукт меняем статус блюда
			fd_adapter.changeEmptyStatus(this.f_id, this.f_position, 1);			
		}
	}
		
	// Редактирует существующий продукт
	public void updateProductItem(long id, long f_id, Map<String, String> row, int position) {
		
		items.remove(position);
		
		for(String l : row.keySet()) {
			items.add(position, new ProductListModel(id, f_id, l, row.get(l), false));
		}
			
		this.notifyDataSetChanged();
	
		handler.updateProductListRow(id, row);
	}
		
	// Удаляет выбранные продукты
	public void deleteProductListRow() {

		ArrayList<ProductListModel> pCopy = new ArrayList<ProductListModel>();
		pCopy.addAll(items);

		for(ProductListModel p : pCopy) {
			if(p.isChecked() == true) {
				items.remove(p);
				this.remove(p);
				
				handler.deleteProductListRow(p.getId());
			}
		}
		
		if(items.size() == 0) {
			// продуктов не осталось меняем статус блюда
			fd_adapter.changeEmptyStatus(this.f_id, this.f_position, 0);			
		}
	}
	
	public ArrayList<Map<String, String>> getCheckedProducts() {
		
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		
		for(ProductListModel p : items) {
			if(p.isChecked() == true) {
				Map<String, String> row = new HashMap<String,String>();
				
				row.put(p.getName(), p.getQty());				
				result.add(row);
			}
		}		
		
		return result;				
	}
	
	// Проверяет есть ли выбранные элементы
	public boolean thereIsChecked() {
		
		for(ProductListModel m : items) {
			if(m.isChecked() == true) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean thereIsEmpty() {
		return items.isEmpty();
	}
}
