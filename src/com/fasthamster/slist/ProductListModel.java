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

public class ProductListModel {

	private long id;
	private long f_id;
	private String name;
	private String qty;
	private boolean checked;
	
	public ProductListModel(long id, long f_id, String name, String qty, boolean checked) {
		this.id = id;
		this.f_id = f_id;
		this.name = name;
		this.qty = qty;
		this.checked = checked;
	}
	
	public long getId() {
		return id;
	}
	
	public long getFId() {
		return f_id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getQty() {
		return qty;
	}
	
	public void setQty(String qty) {
		this.qty = qty;
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}		
}
