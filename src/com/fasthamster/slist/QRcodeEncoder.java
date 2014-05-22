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

import java.util.EnumMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRcodeEncoder extends Activity {
	
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
 
    private int dimension = Integer.MIN_VALUE;
    private String contents = null;
    
    private ProgressDialog progress;
    private ImageView qrView;
    private Bitmap code;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		contents = (String) getIntent().getStringExtra("MESSAGE");
		
		setContentView(R.layout.qr_code_encoder);		
		qrView = (ImageView) findViewById(R.id.qr_code_view);
		
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		@SuppressWarnings("deprecation")
		int height = display.getHeight();
		dimension = width < height ? width : height;
		
		if(savedInstanceState != null) {
			code = savedInstanceState.getParcelable("BITMAP");
			qrView.setImageBitmap(code);
		} else {
			new generateQRcodeAsync().execute();	
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putParcelable("BITMAP", code);	
				
	}
	
	public class generateQRcodeAsync extends AsyncTask<Void, Void, Bitmap> {
		
		@Override
		protected void onPreExecute() {
			lockScreenOrientation();
			
			progress = new ProgressDialog(QRcodeEncoder.this);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setMessage(getResources().getString(R.string.qr_generate_async_message));
			progress.show();

			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
		
			qrView.setImageBitmap(result);
			code = result;
			
			if(progress != null) {
				progress.dismiss();
			}
			
			unlockScreenOrientation();
			
			super.onPostExecute(result);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {

			Bitmap result;
			
			try {
				result = encodeAsBitmap();
				return result;
			} catch (WriterException e) {
				e.printStackTrace();
			} 
			return null;
		}		
	}
		 
   public Bitmap encodeAsBitmap() throws WriterException {
	 
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, BarcodeFormat.QR_CODE, dimension, dimension, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
 
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        
        return bitmap;
    }
 
    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) { return "UTF-8"; }
        }
        return null;
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
}



