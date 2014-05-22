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

/*
 *  QR reader portrait ориентация камеры
 *  http://stackoverflow.com/questions/16252791/zxing-camera-in-portrait-mode-on-android
 *   
 */

package com.fasthamster.slist;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends SampleMenuActivity {

	LayoutInflater INFLATER;
	List<View> PAGES;
	public OnTouchListener shoppingListListener;
	private SharedPreferences PREF;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PREF = getSharedPreferences("pageID", 1);
		pageID = PREF.getInt("pageID", 1);
		
		setScreenMode(pageID);
		
		// Загружаем в отдельном потоке массив AC_ARRAY для предсказания ввода
		if(AC_ARRAY.size() < 2) {
			Thread load_ac_array = new Thread(new Runnable () {

				@Override
				public void run() {
					try {
						AC_ARRAY = DB_HANDLER.getProductNames();
					} catch (CursorIndexOutOfBoundsException e) {
						// не ошибка, экран повернули во время выполнения, onDestroy прибил поток по Thread.stop()
					}
				}				
			});
			
			load_ac_array.start();
		}
		
		// Slider staff
		INFLATER = LayoutInflater.from(this);
		PAGES = new ArrayList<View>();
		PG_ADAPTER = new SamplePagerAdapter(PAGES);
		V_PAGER = new ViewPager(this);
		
		PAGES.add(getShoppingListView());
		PAGES.add(getFoodListView());		

		V_PAGER.setAdapter(PG_ADAPTER);
		V_PAGER.setCurrentItem(pageID);
		
		V_PAGER.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) { }

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
			
			@Override
			public void onPageSelected(int position) {
				pageID = position;
				invalidateOptionsMenu();
				setScreenMode(pageID);
			}			
		});
		
		setContentView(V_PAGER);	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveProgramState();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		saveProgramState();
	}
		
	// Устанавливает режим выключения экрана в зависимости от ID страницы
	private void setScreenMode(int id) {
		if(id == 0) {												
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {													
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
	// Сохраняет ID открытой страницы
	private void saveProgramState() {
		
		PREF = getSharedPreferences("pageID", 1);
		SharedPreferences.Editor ed = PREF.edit();
		if(pageID == 2) {
			ed.putInt("pageID", 1);	
		} else {
			ed.putInt("pageID", pageID);
		}
		ed.commit();
	}
	
	// Формирует страницу со списком покупок
	private View getShoppingListView() {
		View shopping_list_page = INFLATER.inflate(R.layout.shopping_list, null);

		ListView SL = (ListView) shopping_list_page.findViewById(R.id.shopping_list_view);
		
		TextView emptyList = (TextView) shopping_list_page.findViewById(R.id.shopping_list_empty);
		SL.setEmptyView(emptyList);

		SL_ADAPTER = new ShoppingListAdapter(this, DB_HANDLER.getShoppingListData(), DB_HANDLER, AC_ARRAY, SL);
		
		SL.setAdapter(SL_ADAPTER);

		return shopping_list_page;
	}
	
	// Формирует страницу со списком блюд
	private View getFoodListView() {
		View food_list_page = INFLATER.inflate(R.layout.food_list, null);
		
		ListView FD = (ListView) food_list_page.findViewById(R.id.food_list);
		TextView emptyList = (TextView) food_list_page.findViewById(R.id.food_list_empty);
		FD.setEmptyView(emptyList);
		
		FD_ADAPTER = new FoodListAdapter(this, DB_HANDLER.getFoodListData(), DB_HANDLER, PG_ADAPTER, V_PAGER, AC_ARRAY);

		FD.setAdapter(FD_ADAPTER);
		
		return food_list_page;
	}
}
