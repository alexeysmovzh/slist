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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShoppingListAdapter extends ArrayAdapter<ShoppingListModel> {
	
	private ArrayList<ShoppingListModel> items;
	private Activity context;
	private LayoutInflater inflator;
	private db_handler handler;
	private ArrayList<String> ac_array;
	private ListView sl_list;
	

	public ShoppingListAdapter(Activity context, ArrayList<ShoppingListModel> data, db_handler handler, 
							   ArrayList<String> ac_array, ListView sl_list) {
		
		super (context, R.layout.row_in_list, data);
		this.context = context;
		this.handler = handler;
		this.ac_array = ac_array;
		this.sl_list = sl_list;
		this.items = new ArrayList<ShoppingListModel>();
		
		this.items.addAll(data);
		inflator = context.getLayoutInflater();		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		
		View view = null;

		if(position > items.size()) {
			return null;
		}
		
		ShoppingListModel model = items.get(position);
		ShoppingListViewHolder holder = new ShoppingListViewHolder(model.isSelected(), position);
					
		view = inflator.inflate(R.layout.row_in_list, null);
			
		view.setTag(holder);

		holder.name = (TextView) view.findViewById(R.id.name);
		holder.name.setPaintFlags(holder.getStyle());
		holder.qty = (TextView) view.findViewById(R.id.qty);
		holder.qty.setPaintFlags(holder.getStyle());
					
		view.setBackgroundColor(holder.getColor());

		view.setOnTouchListener(new ShoppingListListener(context, this, handler, ac_array));
		
		holder.model = model;
		holder.position = position;
		holder.id = model.getId();
		holder.name.setText(model.getName());
		holder.qty.setText(model.getQty());
		
		return view;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}
	
	// проверят пустой список или нет
	public boolean isShoppingListEmpty() {
		if (items.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	// создает новый список
	public void newShoppingList(Map<String, String> list) {

		ArrayList<ShoppingListModel> result = new ArrayList<ShoppingListModel>();
		
		Map<String, Long> row_indexes = handler.insertNewShoppingList(list);
		
		for(String key : row_indexes.keySet()) {
			result.add(new ShoppingListModel(row_indexes.get(key), key, list.get(key), false));
		}
		
		generateNewItemsList(result);
	}

	// при генерации нового списка обновляет адаптер
	public void generateNewItemsList(ArrayList<ShoppingListModel> list) {
			
		if(items.isEmpty() == false) {
			items.clear();
			this.clear();
		}
		
		for(ShoppingListModel m : list) { 
			items.add(m);
			this.add(m);
		}
	}
	
	// Возвращает список незачеркнутых продуктов для отправки
	public String createShoppingListMessage() {
		
		String result = "";
		
		for(ShoppingListModel m : items) {
			if(m.isSelected() == false) {
				result += m.getName();
				if(m.getQty() != "") {
					result += "\t" + m.getQty() + "\n";
				} else {
					result += "\n";
				}
			}
		}
		return result;
	}
	
	// Добавляет новый продукт
	public void addItem(Map<String, String> row) {
		
		long id = handler.insertShoppingListRow(row); 
		int index = 0;

		for(String l : row.keySet()) {
			index = lastIndexOfSelected(items);
			
			ShoppingListModel model = new ShoppingListModel(id, l, row.get(l), false);
			items.add(index, model);
			this.insert(model, index);
		}	
		
		sl_list.setSelection(index);
	}

	// проверяет имя и кол-во, на совпадение row в items
	public boolean isItemExists(Map<String, String> row) {
		
		for(ShoppingListModel m : items) {
			
			String result = row.get(m.getName()); 
			
			if(result != null) {								// такое название продукта есть, проверим кол-во 

				if(result.equals(m.getQty()) == true) {			// количество совпадает 
					return true;								// возвращаем, что продукт уже в списке есть
				}				
			} 
		}
		
		return false;
	}
	
	// Изменяет продукт (название, кол-во)
	public void updateItem(int position, ShoppingListModel oldModel, Map<String, String> row) {
				
		for(String l : row.keySet()) {

			ShoppingListModel model = new ShoppingListModel(oldModel.getId(), l, row.get(l), oldModel.isSelected());

			items.remove(position);
			items.add(position, model);
		}
				
		this.notifyDataSetChanged();
		
		handler.updateShoppingListRow(oldModel.getId(), row, oldModel.isSelected());		
	}
	
	// Изменяет вид строки (зачеркнутая/незачеркнутая), перемещяет строку с списке
	// на границу между зачеркнутыми и незачеркнутыми
	public void changeItem(ShoppingListModel model) {
		
		items.remove(items.indexOf(model));
		items.add(lastIndexOfSelected(items), model);
		
		this.notifyDataSetChanged();
	}

	// Возвращает границу между зачеркнутыми и незачеркнутыми
	public int lastIndexOfSelected(ArrayList<ShoppingListModel> array) {
		
		int i = 0;
		
		for(ShoppingListModel m : array) {
			if(m.isSelected() == true) {
				return i;
			}
			i++;
		}
		return i;
	}
	
	// Удаляет все зачернутые продукты
	public void clearItems() {
		
		ArrayList<ShoppingListModel> iCopy = new ArrayList<ShoppingListModel>();
		iCopy.addAll(items);
		
		for(ShoppingListModel m : iCopy) {
			if(m.isSelected() == true) {
				items.remove(m);
				this.remove(m);
			}
		}
		
		handler.deleteSelectedShoppingListRows();
	}
	
	// Проверяет есть ли зачеркнутые элементы
	public boolean thereIsSelected() {
		
		for(ShoppingListModel m : items) {
			if(m.isSelected() == true) {
				return true;
			}
		}
		
		return false;
	}
}


















