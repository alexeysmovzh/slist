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

import android.content.Context;
import android.os.AsyncTask;

public class GenerateShoppingListAsync extends AsyncTask<Object, Void, ArrayList<ShoppingListModel>> {

	Context context;
	FoodListAdapter fd_adapter;
	db_handler handler;
	
	private final Listener listener;
	
	public interface Listener {
		void onTaskStarted();
		void onTaskFinished(ArrayList<ShoppingListModel> list);
	}
	
	public GenerateShoppingListAsync(Listener listener) {
		this.listener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		
		listener.onTaskStarted();

	}
	
	@Override
	protected void onPostExecute(ArrayList<ShoppingListModel> result) {
	
		listener.onTaskFinished(result);
		
	}
	
	@Override
	protected ArrayList<ShoppingListModel> doInBackground(Object... params) {

		ArrayList<ShoppingListModel> result = new ArrayList<ShoppingListModel>();
		
		//		task.execute(FD_ADAPTER, DB_HANDLER);		
		fd_adapter = (FoodListAdapter) params[0];
		handler = (db_handler) params[1];
		
		Map<String, String> list = fd_adapter.generateShoppingList();
		Map<String, Long> row_indexes = handler.insertNewShoppingList(list);
		
		for(String key : row_indexes.keySet()) {
			result.add(new ShoppingListModel(row_indexes.get(key), key, list.get(key), false));
		}
		
		return result;
	}
}
