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
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.BarcodeFormat;


public class SampleMenuActivity extends Activity implements ShoppingListAddDialog.Listener, 
															ShoppingListClearDialog.Listener,
															ShoppingListReplaceDialog.Listener,
															FoodListAddDialog.Listener,
															FoodListDeleteDialog.Listener,
															ProductListAddDialog.Listener,
															ProductListDeleteDialog.Listener,
															GenerateShoppingListAsync.Listener {
	
	public int pageID;
	public db_handler DB_HANDLER = new db_handler(this);
	public ShoppingListAdapter SL_ADAPTER;
	public FoodListAdapter FD_ADAPTER;
	@SuppressWarnings("serial")
	public ArrayList<String> AC_ARRAY = new ArrayList<String>() {{ add(""); }};		// autocomplete array

	public SamplePagerAdapter PG_ADAPTER;
	public ViewPager V_PAGER;
	
	public final static int[] ROWS_COLORS = new int[] { 0xffffffff, 0xfff3f3f3 };
	
	private static MenuItem shoppingListMenuClearItem;
	private static MenuItem foodListMenuDelItem;
	private static MenuItem foodListMenuCreateItem;
	private static MenuItem productListMenuDelItem;
	
	private final static int PAGE_SHOPPING_LIST_ID = 0;
	private final static int PAGE_FOOD_LIST_ID = 1;
	private final static int PAGE_PRODUCTS_LIST_ID = 2;
	
	GenerateShoppingListAsync a_task;
	ProgressDialog progress;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		switch(pageID) {
			case PAGE_SHOPPING_LIST_ID:							
				menu.clear();
				getMenuInflater().inflate(R.menu.shopping_list_menu, menu);
				
				shoppingListMenuClearItem = (MenuItem) menu.findItem(R.id.shop_clear);
				shoppingListMenuClearItem.setEnabled(SL_ADAPTER.thereIsSelected());
				
				break;
				
			case PAGE_FOOD_LIST_ID:			
				menu.clear();
				getMenuInflater().inflate(R.menu.food_list_menu, menu);
				
				foodListMenuDelItem = (MenuItem) menu.findItem(R.id.food_del);
				foodListMenuCreateItem = (MenuItem) menu.findItem(R.id.food_create);
				
				foodListMenuDelItem.setEnabled(FD_ADAPTER.thereIsChecked());
				foodListMenuCreateItem.setEnabled(FD_ADAPTER.thereIsChecked());
				
				break;
				
			case PAGE_PRODUCTS_LIST_ID:
				menu.clear();
				getMenuInflater().inflate(R.menu.product_list_menu, menu);		
				
				productListMenuDelItem = (MenuItem) menu.findItem(R.id.product_delete);
				productListMenuDelItem.setEnabled(FD_ADAPTER.PL_ADAPTER.thereIsChecked());
				
				break;
		}
		
		return super.onPrepareOptionsMenu(menu);
		
	}
	
	@Override
	public void onBackPressed() {
		
		int currentItem = V_PAGER.getCurrentItem(); 
		
		if(currentItem == 2) {
			V_PAGER.setCurrentItem(1, true);
		} else {
			super.onBackPressed();
		}
	}

	// 
	public static void changeShoppingListMenuClearItem(boolean state) {
		shoppingListMenuClearItem.setEnabled(state);
	}
	
	// 
	public static void changeFoodListMenuDelItem(boolean state) {
		foodListMenuDelItem.setEnabled(state);
	}
	
	// 
	public static void changeFoodListMenuCreateItem(boolean state) {
		foodListMenuCreateItem.setEnabled(state);
	}
	
	//
	public static void changeProductListDelItem(boolean state) {
		productListMenuDelItem.setEnabled(state);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		
		case R.id.shop_clear:

			DialogFragment shoppingListClearDialog = new ShoppingListClearDialog();
			shoppingListClearDialog.show(getFragmentManager(), "shoppingListClearDialog");
			
			break;
			
		case R.id.shop_add:
			
			DialogFragment shoppingListAddDialog = ShoppingListAddDialog.newInstance(new ShoppingListModel(0, "", "", false), 0, AC_ARRAY);
			shoppingListAddDialog.show(getFragmentManager(), "shoppingListAddDialog");

			break;
			
		case R.id.shop_send:
				
			String message = SL_ADAPTER.createShoppingListMessage();
			
			// http://stackoverflow.com/questions/9730243/android-how-to-filter-specific-apps-for-action-send-intent
		    List<Intent> targetShareIntents=new ArrayList<Intent>();
		    Intent shareIntent=new Intent();
		    
		    shareIntent.setAction(Intent.ACTION_SEND);
		    shareIntent.setType("text/plain");
			    
	    	List<ResolveInfo> resInfos=getPackageManager().queryIntentActivities(shareIntent, 0);
		    // Добавляем системные интенты 
		    if(!resInfos.isEmpty()){
		   
		        for(ResolveInfo resInfo : resInfos){
		        	
		        	String packageName=resInfo.activityInfo.packageName;

		            if(packageName.contains("com.twitter.android") || 
		               packageName.contains("com.facebook.katana") || 
		               packageName.contains("com.google.android.gm") ||
		               packageName.contains("com.google.android.apps.plus") ||
		               packageName.contains("com.android.mms")) {
			            
		            	Intent intent=new Intent();
		                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
		                intent.setAction(Intent.ACTION_SEND);
		                intent.setType("text/plain");
		                intent.putExtra(Intent.EXTRA_TEXT, message);
			                
		                if(packageName.contains("com.android.mms") == false) {
		                	intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_action_subject));
		                }
			                
		                intent.setPackage(packageName);
		                targetShareIntents.add(intent);
		            }
		        }
			        
			    if(message.length() > 1) {			        
				    // QR code кодировщик интент		      
			        Intent qr_encode = new Intent(this, com.fasthamster.slist.QRcodeEncoder.class);
			        qr_encode.putExtra("MESSAGE", message);
			        qr_encode.setPackage("com.fasthamster.slist.QRcodeEncoder");
				    
				    targetShareIntents.add(qr_encode);
			    }

		        // QR code распознавание интент
			    Intent qr_read = new Intent("com.google.zxing.client.android.SCAN");
			    
			    qr_read.putExtra("SCAN_MODE", "SCAN_MODE");
			    qr_read.putExtra("SCAN_FORMATS", BarcodeFormat.QR_CODE);
			    qr_read.putExtra("RESULT_DISPLAY_DURATION_MS", 0L);

			    qr_read.putExtra("SCAN_WIDTH", 300);
			    qr_read.putExtra("SCAN_HEIGHT", 300);
                qr_read.setClass(this, com.google.zxing.client.android.CaptureActivity.class);
                qr_read.setPackage("com.google.zxing.client.android.CaptureActivity");
                
                targetShareIntents.add(qr_read);
                
			    
		        if(!targetShareIntents.isEmpty()){
		        	
		            Intent chooserIntent=Intent.createChooser(targetShareIntents.remove(0), getResources().getString(R.string.share_action_title));
		            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
		            startActivityForResult(chooserIntent, 0);
		            
		        }else{

					DialogFragment shoppingListSendErrorDialog = new ShoppingListSendErrorDialog();
					shoppingListSendErrorDialog.show(getFragmentManager(), "shoppingListSendErrorDialog");
		        }
		    }

			break;

		case R.id.food_del:
			
			DialogFragment foodListDeleteDialog = FoodListDeleteDialog.newInstance(FD_ADAPTER.getCheckedItems());
			foodListDeleteDialog.show(getFragmentManager(), "foodListDeleteDialog");
			
			break;

		case R.id.food_add:
						
			DialogFragment foodListAddDialog = FoodListAddDialog.newInstance(0, "", 0);
			foodListAddDialog.show(getFragmentManager(), "foodListAddDialog");
						
			break;

		case R.id.food_create:
			
			a_task = new GenerateShoppingListAsync(this);
			a_task.execute(FD_ADAPTER, DB_HANDLER);
			
			break;

		case R.id.product_add:
			
			DialogFragment productListAddDialog = ProductListAddDialog.newInstance(new ProductListModel(0, 0, "", "", false), 0, AC_ARRAY);
			productListAddDialog.show(getFragmentManager(), "productListAddDialog");

			break;
			
		case R.id.product_delete:

			DialogFragment productListDeleteDialog = ProductListDeleteDialog.newInstance(FD_ADAPTER.PL_ADAPTER.getCheckedProducts());
			productListDeleteDialog.show(getFragmentManager(), "productListDeleteDialog");
			
			break;			
		}
		
		return true;
	}
	
	@Override
	public void addItem(Map<String, String> row) {
		SL_ADAPTER.addItem(row);
	}

	@Override
	public void updateItem(int position, ShoppingListModel oldModel, Map<String, String> row) {
		SL_ADAPTER.updateItem(position, oldModel, row);
	}

	@Override
	public void clearShoppingListItems() {
		SL_ADAPTER.clearItems();		
		shoppingListMenuClearItem.setEnabled(SL_ADAPTER.thereIsSelected());		
	}

	@Override
	public void insertFoodListRow(String row) {
		FD_ADAPTER.insertFoodListRow(row);
	}

	@Override
	public void updateFoodListRow(long id, int position, String row) {
		FD_ADAPTER.updateFoodListRow(id, position, row);
	}

	@Override
	public void deleteFoodListRow(ArrayList<Map<Long, String>> rows) {
		FD_ADAPTER.deleteFoodListRow(rows);		
		if(FD_ADAPTER.thereIsEmpty() == true) {
			foodListMenuDelItem.setEnabled(false);
			foodListMenuCreateItem.setEnabled(false);
		}
	}
	
	@Override
	public void deleteProductListRow() {
		FD_ADAPTER.PL_ADAPTER.deleteProductListRow();
		if(FD_ADAPTER.PL_ADAPTER.thereIsEmpty() == true) {
			productListMenuDelItem.setEnabled(false);
		}
	}

	@Override
	public void addProductItem(Map<String, String> row) {
		FD_ADAPTER.PL_ADAPTER.addProductItem(row);
	}

	@Override
	public void updateProductItem(long id, long f_id, Map<String, String> row, int position) {
		FD_ADAPTER.PL_ADAPTER.updateProductItem(id, f_id, row, position);	
	}

	private void lockScreenOrientation() {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
	
	private void unlockScreenOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	@Override
	public void onTaskStarted() {
		
		lockScreenOrientation();
		progress = ProgressDialog.show(this, getResources().getText(R.string.async_title), 
											 getResources().getText(R.string.async_message));
		
	}

	@Override
	public void onTaskFinished(ArrayList<ShoppingListModel> list) {
		
		SL_ADAPTER.generateNewItemsList(list);
		
		if(progress != null) {
			progress.dismiss();
		}
		
		V_PAGER.setCurrentItem(0, true);
		
		unlockScreenOrientation();		
	}
	
	@Override
	public void replaceShoppingListItems(String scan) {
		
		SL_ADAPTER.newShoppingList(makeMapFromString(scan));
		
	}

	@Override
	public void appendShoppingListItems(String scan) {
		
		Map<String, String> list = makeMapFromString(scan);
		
		for(String l : list.keySet()) {
			Map<String, String> row = new HashMap<String, String>();
			row.put(l, list.get(l));
			
			if(SL_ADAPTER.isItemExists(row) == false) {					// если такой строки нет, вставляем
				SL_ADAPTER.addItem(row);
			}
		}		
	}
	
	// Возвращает наименьшее значение из ширины и высоты экрана
	public int getScreenWidth() {
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		if(metrics.heightPixels < metrics.widthPixels) {
			return metrics.heightPixels;
		} else {
			return metrics.widthPixels;
		} 
	}
	
	// Обработчик для интента QR сканера
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if(requestCode == 0) {
			if(resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				Map<String, String> list = makeMapFromString(contents);
			
				if(SL_ADAPTER.isShoppingListEmpty()) {					// лист пустой вставляем полученные элементы
					
					SL_ADAPTER.newShoppingList(list);
					
				} else {												// лист не пустой, спрашиваем заменить или дополнить
					DialogFragment shoppingListReplaceDialog = ShoppingListReplaceDialog.newInstance(contents);
					shoppingListReplaceDialog.show(getFragmentManager(), "shoppingListReplaceDialog");
				}
			} 
		}
	}	

	// Из строки scan делает Map для добавления списка продуктов
	// ключ - название продукта, значение - количество
	private Map<String, String> makeMapFromString(String s) {
		
		Map<String, String> list = new HashMap<String, String>();
		
		String[] row = s.split("\n");
		for(int i=0; i < row.length; i++) {
			String[] t = row[i].split("\t");
			
			if(t.length == 1) {
				list.put(t[0], "");
			} else {
				list.put(t[0], t[1]);
			}
		}
		
		return list;
	}
}
