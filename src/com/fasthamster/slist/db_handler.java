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
import java.util.concurrent.atomic.AtomicInteger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class db_handler extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 2;
	private static final String DB_NAME = "shoppingDB";
	
	private AtomicInteger openCounter = new AtomicInteger();
	private SQLiteDatabase database;
	
	private static class Patch {
		public void apply(SQLiteDatabase db) {}
		public void revert(SQLiteDatabase db) {}
	}	

	private static final Patch[] PATCHES = new Patch[] {

		// Patch #1 фейк патч, сюда надо запихнуть создание базы из onCreate 
		new Patch() {
			public void apply(SQLiteDatabase db) {}
			public void revert(SQLiteDatabase db) {}
		},
		
		// Patch #2 добавление столбца наличия продуктов в таблицу food
		new Patch() {																
			public void apply(SQLiteDatabase db) {
				db.execSQL("ALTER TABLE food ADD COLUMN empty INTEGER DEFAULT 0;");
				db.execSQL("UPDATE food SET empty = 1 WHERE _id IN (SELECT DISTINCT f_id FROM products);");
			}
			public void revert(SQLiteDatabase db) {
				db.execSQL("ALTER TABLE food RENAME TO food_old;");
				db.execSQL("CREATE TABLE food (_id integer primary key autoincrement, name TEXT);");
				db.execSQL("INSERT INTO food (_id, name) SELECT _id, name FROM food_old;");
				db.execSQL("DROP TABLE food_old;");
			}
		}
		// Patch #3 ....
		//, new Patch() {.....}														
	};
	
	public db_handler (Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL("CREATE TABLE list (_id integer primary key autoincrement, name TEXT, qty TEXT, selected INTEGER);");
		db.execSQL("CREATE TABLE food (_id integer primary key autoincrement, name TEXT);");
		db.execSQL("CREATE TABLE products (_id integer primary key autoincrement, f_id INTEGER, name TEXT, qty TEXT);");
		
		for(int i = 0; i < PATCHES.length; i++) {
			PATCHES[i].apply(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
		
		for(int i = oldV; i < PATCHES.length; i++) {
			PATCHES[i].apply(db);
		}
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldV, int newV) {
		
		for(int i = oldV; i > newV; i++) {
			PATCHES[i].revert(db);
		}
	}
	
	public synchronized SQLiteDatabase openDatabase() {

		if(openCounter.incrementAndGet() == 1) {
			database = this.getWritableDatabase();
		}

		return database;
	}
	
	public synchronized void closeDatabase() {
		
		if(openCounter.decrementAndGet() == 0) {
			database.close();
		}
	}
	
	// ===========  SQL =========== //
	
	// Возвращает список продуктов для autocompletetextview
	public ArrayList<String> getProductNames() {
		
		ArrayList<String> result = new ArrayList<String>();

		SQLiteDatabase db = openDatabase();
		
		Cursor c = db.rawQuery("SELECT DISTINCT name FROM products", null);
			
		if(c != null) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
					
				result.add(c.getString(0));
				c.moveToNext();
			}
			c.close();
		}		

		closeDatabase();
		
		return result;
	}
	
	// очищает таблицу list, заносит значения нового списка, возвращает индексы записей
	public Map<String, Long> insertNewShoppingList(Map<String, String> list) {
		
		Map<String, Long> indexes = new HashMap<String, Long>();
		
		SQLiteDatabase db = openDatabase();			
		
		db.delete("list", null, null);

		ContentValues cv = new ContentValues();
		
		for(String name : list.keySet()) {
			cv.put("name", name);
			cv.put("qty", list.get(name));
			
			long idx = db.insert("list", null, cv);
			indexes.put(name, idx);
		}		

		closeDatabase();	
		
		return indexes;
	}
	
	// Возвращает список продуктов по id выбранных блюд для generateshoppingList
	public ArrayList<ProductGenerateModel> getSelectedProductList(ArrayList<Long> food_id) {
		
		ArrayList<ProductGenerateModel> result = new ArrayList<ProductGenerateModel>();

		SQLiteDatabase db = openDatabase();
		
		for(Long id : food_id) {
			Cursor c = db.rawQuery("SELECT food.name, products.name, products.qty " +
										"FROM food, products " +
										"WHERE food._id = products.f_id AND food._id = " + id, null);
			
			if(c != null) {
				c.moveToFirst();
				while(!c.isAfterLast()) {
						
					ProductGenerateModel m = new ProductGenerateModel(c.getString(0), c.getString(1), c.getString(2));
					result.add(m);
						
					c.moveToNext();
				}
				c.close();
			}
		}
		
		closeDatabase();		
		
		return result;
	}
	
	// Возвращает в массиве записи из таблицы списка покупок products по f_id (food_id)
	public ArrayList<ProductListModel> getProductListData(long f_id) {
		
		SQLiteDatabase db = openDatabase();
		Cursor c = db.rawQuery("SELECT rowid _id, f_id, name, qty FROM products WHERE f_id = " + f_id, null);

		ArrayList<ProductListModel> product_list = new ArrayList<ProductListModel>();
		
		if(c != null) {
			c.moveToFirst();
			while(!c.isAfterLast()) {

				ProductListModel m = new ProductListModel(c.getInt(0), c.getInt(1), 
												c.getString(2), c.getString(3), false);
				product_list.add(m);
					
				c.moveToNext();
			}
			c.close();
		}
				
		closeDatabase();
		
		return product_list;
	}
	
	// Редактирует продукт в рецепте блюда
	public void updateProductListRow(long id, Map<String, String> row) {
		
		SQLiteDatabase db = openDatabase();
		
		ContentValues cv = new ContentValues();
		
		for(String l : row.keySet()) {
			cv.put("name", l);
			cv.put("qty", row.get(l));
		}

		db.update("products", cv, "_id=" + id, null);
		
		closeDatabase();
	}
	
	// Удаляет продукты по id
	public void deleteProductListRow(long id) {		
		
		SQLiteDatabase db = openDatabase();			
		
		db.delete("products", "_id =" + id, null);
		
		closeDatabase();		
	}	
	
	// Добавляет продукт в рецепт блюда
	// f_id - id записи блюда из таблицы food, к которому относится продукт
	public long insertProductListRow(long f_id, Map<String, String> row) {
		
		SQLiteDatabase db = openDatabase();
		
		ContentValues cv = new ContentValues();
		
		for(String l : row.keySet()) {
			cv.put("f_id", f_id);
			cv.put("name", l);
			cv.put("qty", row.get(l));
		}
							
		long row_id = db.insert("products", null, cv);
				
		closeDatabase();
		
		return row_id;
	}
	
	// Переименовывает продукт в FoodList
	public void updateFoodListRow(Long id, String row) {
		
		SQLiteDatabase db = openDatabase();
		
		ContentValues cv = new ContentValues();
		
		cv.put("name", row);

		db.update("food", cv, "_id=" + id, null);
		
		closeDatabase();
	}
	
	// Обновляет статус наличия продуктов у блюда в базе
	public void updateEmptyStatus(Long id, int empty) {
		
		SQLiteDatabase db = openDatabase();
		
		ContentValues cv = new ContentValues();
		
		cv.put("empty", empty);

		db.update("food", cv, "_id=" + id, null);
		
		closeDatabase();
	}
	
	// Вставляет новый продукт в FoodList
	public long insertFoodListRow(String row) {
		
		SQLiteDatabase db = openDatabase();
				
		ContentValues cv = new ContentValues();
		cv.put("name", row);
		cv.put("empty", 0);
							
		long row_id = db.insert("food", null, cv);
				
		closeDatabase();
		
		return row_id;
	}
	
	// Удаляет выбранное блюдо и связанные с ним продукты
	public void deleteFoodListRow(Long id) {
		
		SQLiteDatabase db = openDatabase();			
		
		db.delete("food", "_id =" + id, null);
		
		Cursor value = db.rawQuery("SELECT rowid _id FROM products WHERE f_id = " + id, null);
		if(value != null) {
			db.delete("products", "f_id = " + id, null);
		}
		
		closeDatabase();
	}
	
	// Возвращает в массиве все записи из таблицы списка блюд food
		public ArrayList<FoodListModel> getFoodListData() {

			SQLiteDatabase db = openDatabase();
			Cursor c = db.rawQuery("SELECT rowid _id, name, empty FROM food ORDER BY name ASC", null);

			ArrayList<FoodListModel> food_list = new ArrayList<FoodListModel>();
			
			if(c != null) {
				c.moveToFirst();
				while(!c.isAfterLast()) {
					
					FoodListModel m = new FoodListModel(c.getInt(0), c.getString(1), false, c.getInt(2));
					food_list.add(m);
					
					c.moveToNext();
				}
				c.close();
			}
					
			closeDatabase();
			
			return food_list;			
		}
	
		// Возвращает в массиве все записи из таблицы списка покупок list
		public ArrayList<ShoppingListModel> getShoppingListData() {
			
			boolean selected;

			SQLiteDatabase db = openDatabase();
			Cursor c = db.rawQuery("SELECT rowid _id, name, qty, selected FROM list ORDER BY selected, name ASC", null);

			ArrayList<ShoppingListModel> shopping_list = new ArrayList<ShoppingListModel>();
			
			if(c != null) {
				c.moveToFirst();
				while(!c.isAfterLast()) {
					
					if(c.getInt(3) == 1) {
						selected = true;
					} else {
						selected = false;
					}
				
					ShoppingListModel m = new ShoppingListModel(c.getInt(0), c.getString(1), c.getString(2), selected);
					shopping_list.add(m);
						
					c.moveToNext();
				}
				c.close();
			}
				
			closeDatabase();
			
			return shopping_list;		
		}
		
		// Изменяет название продукта или количество
		public void updateShoppingListRow(long id, Map<String, String> row, boolean selected) {
			
			int boolean2int = selected ? 1 : 0;
			
			SQLiteDatabase db = openDatabase();
			
			ContentValues cv = new ContentValues();
			for(String l : row.keySet()) {
				cv.put("name", l);
				cv.put("qty", row.get(l));
				cv.put("selected", boolean2int);
			}

			db.update("list", cv, "_id=" + id, null);
			
			closeDatabase();
		}
		
		// Удаляет зачеркнутые продукты
		public void deleteSelectedShoppingListRows() {
			
			SQLiteDatabase db = openDatabase();			
			db.delete("list", "selected = 1", null);			
			closeDatabase();
		}
		
		public void setSelected(long id, boolean selected) {
			
			int boolean2int = selected ? 1 : 0;
						
			SQLiteDatabase db = openDatabase();
			
			ContentValues cv = new ContentValues();
			cv.put("selected", boolean2int);
			db.update("list", cv, "_id=" + id, null);
			
			closeDatabase();
		}

		// Вставляет новый продукт в ShoppingList
		public long insertShoppingListRow(Map<String, String> row) {
			
			SQLiteDatabase db = openDatabase();
			
			ContentValues cv = new ContentValues();
			for(String l : row.keySet()) {
				cv.put("name", l);
				cv.put("qty", row.get(l));
				cv.put("selected", 0);
			}
						
			long result = db.insert("list", null, cv);
			
			closeDatabase();
			
			return result;
		}
}