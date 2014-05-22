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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class FoodListAdapter extends ArrayAdapter<FoodListModel> {
	
	public ProductListAdapter PL_ADAPTER;
	
	private ArrayList<FoodListModel> items;
	private Activity context;
	private LayoutInflater inflater;
	private db_handler handler;
	private SamplePagerAdapter adapter;
	private ViewPager pager;	
	private ArrayList<String> ac_array;
	
	public FoodListAdapter(Activity context, ArrayList<FoodListModel> data, db_handler handler, 
							SamplePagerAdapter adapter, ViewPager pager, ArrayList<String> ac_array) {
		super(context, R.layout.row_in_food, data);
		this.context = context;
		this.handler = handler;
		this.adapter = adapter;
		this.pager = pager;
		this.ac_array = ac_array;
		this.items = new ArrayList<FoodListModel>();
		
		this.items.addAll(data);
		inflater = context.getLayoutInflater();
	}
	
	class FoodListViewHolder {
		protected long id;
		protected CheckBox checkbox;
		protected ImageButton button;
		protected TextView indicator;
		protected int empty;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup) {

		View view = null;

		if(position > items.size()) {
			return null;
		}
		
		if(convertView == null) {
			view = inflater.inflate(R.layout.row_in_food, null);
			final FoodListViewHolder viewHolder = new FoodListViewHolder();

			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.food_checkbox);			
			viewHolder.button = (ImageButton) view.findViewById(R.id.food_list_arrow_btn);	
			viewHolder.indicator = (TextView) view.findViewById(R.id.food_list_empty_indicator);
			
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
					FoodListModel model = (FoodListModel) viewHolder.checkbox.getTag();
					model.setChecked(buttonView.isChecked());
					
					if(thereIsChecked() == true) {
						SampleMenuActivity.changeFoodListMenuDelItem(true);
						SampleMenuActivity.changeFoodListMenuCreateItem(true);
					} else {
						SampleMenuActivity.changeFoodListMenuDelItem(false);
						SampleMenuActivity.changeFoodListMenuCreateItem(false);
					}
				}			
			});
			
			viewHolder.checkbox.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					
					FoodListModel m = items.get(position);
					
					DialogFragment foodListAddDialog = FoodListAddDialog.newInstance(m.getId(), m.getName(), position); 
					foodListAddDialog.show(context.getFragmentManager(), "foodListAddDialog");
					
					return false;
				}
				
			});
			
			viewHolder.button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					addProductView2Pager(viewHolder.id, position, adapter, pager);
					
				}
				
			});
		
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(items.get(position));		
		} else {
			view = convertView;
			((FoodListViewHolder) view.getTag()).checkbox.setTag(items.get(position));
		}
				
		view.setBackgroundColor(SampleMenuActivity.ROWS_COLORS[position % 2]);		// ракрашиваем строки четные/нечетные
		
		FoodListViewHolder holder = (FoodListViewHolder) view.getTag();
		
		// Помечаем блюда у которых нет продуктов
		if(items.get(position).isEmpty() == 0) {
			holder.indicator.setBackgroundColor(0xffff4444);
		} else {
			holder.indicator.setBackgroundColor(Color.TRANSPARENT);
		}

		holder.id = items.get(position).getId();
		holder.checkbox.setChecked(items.get(position).isChecked());
		holder.checkbox.setText(items.get(position).getName());
		holder.empty = items.get(position).isEmpty();
				
		return view;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}
	
	// Добавляет View со страницей с продуктами во ViewPager
	private void addProductView2Pager(long id, int position, SamplePagerAdapter adapter, ViewPager pager) {
		
		if(adapter.getCount() > 2) {
			adapter.pages.remove(2);
			
			pager.setAdapter(null);
			pager.setAdapter(adapter);
		}
		
		View pv = getProductListView(id, position);
		
		adapter.pages.add(pv);
		adapter.notifyDataSetChanged();

		pager.setCurrentItem(2, true);
	}
	
	// Создает View со страницей с продуктами, для формирования передается f_id (food_id)
	private View getProductListView(long f_id, int position) {
		
		View product_list_page = inflater.inflate(R.layout.product_list, null);
		
		ListView PL = (ListView) product_list_page.findViewById(R.id.product_list_view);
		TextView emptyList = (TextView) product_list_page.findViewById(R.id.product_list_empty);
		PL.setEmptyView(emptyList);

		PL_ADAPTER = new ProductListAdapter(context, handler.getProductListData(f_id), handler, f_id, position, ac_array, this);
		PL.setAdapter(PL_ADAPTER);
		
		return product_list_page;
	}
	
	// создает список покупок
	public Map<String, String> generateShoppingList() {
		
		ArrayList<ProductGenerateModel> products = handler.getSelectedProductList(getCheckedItemsId());
		ArrayList<ProductGenerateModel> pCopy = new ArrayList<ProductGenerateModel>();
		ArrayList<String> notEmptyQty = getNotEmptyQty(products);
		Map<String, String> result = new HashMap<String, String>();
	
		pCopy.addAll(products);
		
		for(ProductGenerateModel m : products) {
			
			pCopy.remove(m);
			
			if(checkProductRepeate(m.getProduct(), pCopy) == true) {			// есть повтор
				
				pCopy.add(m);

				if(checkIndex(m.getProduct(), notEmptyQty) == true) {			// есть среди повторов продукт с непустым кол-вом
						result.put(m.getProduct() + " (" + m.getFood() + ")", m.getQty());
				} else {													
								
					if(checkProductInResult(m.getProduct(), result) == false) {	// продукт с пустым кол-вом заносим в result один раз 
						result.put(m.getProduct(), m.getQty());
					}
				}				
			} else {
				result.put(m.getProduct(), m.getQty());
			}			
		}
		
		return result;
	}
	
	// проверяет есть ли продукт в списке
	private boolean checkProductInResult(String product, Map<String, String> list) {
		
		for(String s : list.keySet()) {
			if(s.trim().equalsIgnoreCase(product.trim()) == true) {
				return true;
			}
		}
		return false;
	}
	
	// Проверяет наличие продукта в массиве
	private boolean checkIndex(String product, ArrayList<String> list) {
		
		for (String s : list) {
			if(s.trim().equalsIgnoreCase(product.trim()) == true) {
				return true;
			}
		}
		return false;
	}
	
	// Возвращает названия продуктов с не пустым количеством
	private ArrayList<String> getNotEmptyQty(ArrayList<ProductGenerateModel> list) {
		
		ArrayList<String> result = new ArrayList<String>();
		
		for(ProductGenerateModel p : list) {
			if(p.getQty().isEmpty() == false) {
				result.add(p.getProduct());
			}
		}
		
		return result;
	}
	
	// Проверяет повтор в списке
	private boolean checkProductRepeate(String product, ArrayList<ProductGenerateModel> list) {
		
		for(ProductGenerateModel p : list) {
			if(p.getProduct().trim().equalsIgnoreCase(product.trim())) {
				return true;
			}
		}
		
		return false;
	}
	
	// При добавлении продуктов в блюдо меняет его статус (красный индикатор в listview)
	public void changeEmptyStatus(Long id, int position, int empty) {

		FoodListModel m = items.get(position);

		items.remove(m);
		items.add(position, new FoodListModel(m.getId(), m.getName(), m.isChecked(), empty));
	
		this.notifyDataSetChanged();
		
		handler.updateEmptyStatus(id, empty);
	}
	
	// Добавляет новое блюдо
	public void insertFoodListRow(String row) {

		long id = handler.insertFoodListRow(row);
		
		FoodListModel model = new FoodListModel(id, row, false, 0);
		
		items.add(model);
		this.add(model);

		addProductView2Pager(id, items.size() - 1, adapter, pager);
	}
	
	// Переименовывает существующее блюдо
	public void updateFoodListRow(Long id, int position, String row) {
		
		FoodListModel m = items.get(position);

		items.remove(position);
		items.add(position, new FoodListModel(m.getId(), row, m.isChecked(), m.isEmpty()));
		
		this.notifyDataSetChanged();
	
		handler.updateFoodListRow(id, row);
	}
	
	// Удаляет выбранное блюдо
	public void deleteFoodListRow(ArrayList<Map<Long, String>> rows) {

		ArrayList<FoodListModel> iCopy = new ArrayList<FoodListModel>();
		iCopy.addAll(items);

		for(Map<Long, String> map : rows) {
			for(Long i : map.keySet()) {
				for(FoodListModel m : iCopy) {
					if(m.getId() == i) {
						items.remove(m);
						this.remove(m);						
						handler.deleteFoodListRow(i);
					}
				}
			}
		}
	}
	
	// Возвращает все выбранные блюда
	@SuppressLint("UseSparseArrays")
	public ArrayList<Map<Long, String>> getCheckedItems() {
		
		ArrayList<Map<Long, String>> result = new ArrayList<Map<Long, String>>();
		
		for(FoodListModel m : items) {
			if(m.isChecked() == true) {
				
				Map<Long, String> map = new HashMap<Long, String>();
			
				map.put(m.getId(), m.getName());
				result.add(map);
			}
		}
		
		return result;
	}
	
	// Возвращает id всех выбранных блюд
	public ArrayList<Long> getCheckedItemsId() {
		
		ArrayList<Long> result = new ArrayList<Long>();
		
		for(FoodListModel m : items) {
			if(m.isChecked() == true) {		
				result.add(m.getId());
			}
		}
		
		return result;
	}
	
	// Проверяет есть ли выбранные элементы
	public boolean thereIsChecked() {
		
		for(FoodListModel m : items) {
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