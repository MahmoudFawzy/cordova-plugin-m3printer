package com.m3printer;

import com.nbbse.mobiprint3.*;
import org.apache.cordova.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Bitmap.CompressFormat;
import android.util.Xml.Encoding;
import jdk.nashorn.internal.parser.*;
import android.util.Base64;
import java.util.ArrayList;
import java.util.List;
import android.content.*;
import android.R;

public class M3Printer extends CordovaPlugin {
	public static com.nbbse.mobiprint3.Printer print;
	public Context context;
	CordovaInterface mycordova;
	CordovaWebView mywebView;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		print = Printer.getInstance();
		context = this.cordova.getActivity().getApplicationContext();
		mycordova = cordova;
		mywebView = webView;
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("printTest")) {
			String txt = args.getString(0);

			InputStream is = context.getResources().openRawResource(getAppResource("logo", "drawable"));
			Bitmap bit = BitmapFactory.decodeStream(is);
			// BufferedInputStream br = new BufferedInputStream(is);

			print.printText(String.valueOf(bit.getWidth()));
			print.printText("----------------------------", 1, true);
			print.printBitmap(bit);
			print.printText("--------------------", 2, true);
			print.printBitmap(is);

			print.printEndLine();
			callbackContext.success("1");
			return true;
		} else if (action.equals("printText")) {
			String txt = args.getString(0);

			// InputStream is2 = context.getResources().openRawResource(R.raw.img);
			// InputStream is3 = context.getResources().openRawResource(R.raw.test);
			// InputStream is = context.getResources().openRawResource(R.raw.bitmap24);
			// print.printBitmap(is);

			JSONObject json = new JSONObject(txt);
			JSONArray jReciept = json.getJSONArray("Fields");

			for (int i = 0; i < jReciept.length(); i++) {
				JSONObject jO = jReciept.getJSONObject(i);

				print.printText(jO.getString("FieldName"), 1, true);
				print.printText(jO.getString("Value"), 1, false);
			}

			print.printText("تكلفة الخدمة", 1, true);
			print.printText(json.getString("Totalprice"), 1, false);

			print.printText("رسوم التحصيل", 1, true);
			print.printText(json.getString("Fees"), 1, false);

			int tot = json.getInt("Totalprice") + json.getInt("Fees");
			print.printText("الإجمالي", 1, true);
			print.printText(String.valueOf(tot), 1, false);

			String sDate = json.getString("AddedTime");

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Date convertedDate = new Date();
			try {
				convertedDate = dateFormat.parse(sDate);
			} catch (ParseException ex) {
				// Do something
			}
			SimpleDateFormat dateFormat_date = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat dateFormat_time = new SimpleDateFormat("hh:mm aa");

			print.printText("تاريخ التحصيل", 1, true);
			print.printText(dateFormat_date.format(convertedDate), 1, false);

			print.printText("وقت التحصيل", 1, true);
			print.printText(dateFormat_time.format(convertedDate), 1, false);

			print.printText("رقم الفرع", 1, true);
			print.printText(json.getString("AgentCode"), 1, false);

			print.printText("رقم الفاتورة", 1, true);
			print.printText(json.getString("InvoiceId"), 1, false);

			int s = json.getInt("Status");
			String s_str = "غير محدد";
			if (s == 0) {
				s_str = "التنفيذ";
			} else if (s == 2) {
				s_str = "مسترجع";
			} else if (s == 1 || s == 3 || s == 4) {
				s_str = "مسدد";
			}
			print.printText("حالة الفاتورة", 1, true);
			print.printText(s_str, 1, false);

			print.printText("--------------------------------");
			print.printText(json.getString("Footer"), 1, true);

			print.printEndLine();
			callbackContext.success("1");
			return true;
		} else if (action.equals("printJson")) {
			String txt = args.getString(0);

			// InputStream is2 = context.getResources().openRawResource(R.raw.img);
			// InputStream is3 = context.getResources().openRawResource(R.raw.test);
			// InputStream is = context.getResources().openRawResource(R.raw.bitmap24);
			// print.printBitmap(is);

			JSONObject json = new JSONObject(txt);
			JSONArray jReciept = json.getJSONArray("data");

			for (int i = 0; i < jReciept.length(); i++) {
				JSONObject jO = jReciept.getJSONObject(i);

				print.printText(jO.getString("label"), 1, true);
				print.printText(jO.getString("value"), 1, false);
			}

			print.printEndLine();
			callbackContext.success("1");
			return true;
		} else if (action.equals("printPath")) {
			String txt = args.getString(0);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(txt, options);

			print.printBitmap(txt);
			print.printEndLine();
			callbackContext.success(bitmap.getWidth());
			return true;
		} else if (action.equals("printBase64")) {
			String txt = args.getString(0);

			final byte[] decodedBytes = Base64.decode(txt, Base64.DEFAULT);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

			int mWidth = bitmap.getWidth();
			int mHeight = bitmap.getHeight();

			bitmap = resizeImage(bitmap, 48 * 8, mHeight);

			// ByteArrayOutputStream stream = new ByteArrayOutputStream();
			// bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			// byte[] byteArray = stream.toByteArray();
			// bitmap.recycle();

			// byte[] bt = decodeBitmap(bitmap);
			// convertARGBToGrayscale(bt, mWidth, mHeight);
			print.printBitmap(bitmap);
			print.printEndLine();
			callbackContext.success("1");
			return true;
		} else {
			return false;
		}
	}

	private int getAppResource(String name, String type) {
		return mycordova.getActivity().getResources().getIdentifier(name, type,
				mycordova.getActivity().getPackageName());
	}

	private static void convertARGBToGrayscale(int[] argb, int width, int height) {
		int pixels = width * height;

		for (int i = 0; i < pixels; ++i) {
			int r = argb[i] >> 16 & 255;
			int g = argb[i] >> 8 & 255;
			int b = argb[i] & 255;
			int color = r * 19 + g * 38 + b * 7 >> 6 & 255;
			argb[i] = color;
		}

	}

	private static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		Bitmap BitmapOrg = bitmap;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();

		if (width > w) {
			float scaleWidth = ((float) w) / width;
			float scaleHeight = ((float) h) / height + 24;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleWidth);
			Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
			return resizedBitmap;
		} else {
			Bitmap resizedBitmap = Bitmap.createBitmap(w, height + 24, Config.RGB_565);
			Canvas canvas = new Canvas(resizedBitmap);
			Paint paint = new Paint();
			canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(bitmap, (w - width) / 2, 0, paint);
			return resizedBitmap;
		}
	}

	////////////////////
	public static byte[] decodeBitmap(Bitmap bmp) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		List<String> list = new ArrayList<String>(); // binaryString list
		StringBuffer sb;
		int bitLen = bmpWidth / 8;
		int zeroCount = bmpWidth % 8;
		String zeroStr = "";
		if (zeroCount > 0) {
			bitLen = bmpWidth / 8 + 1;
			for (int i = 0; i < (8 - zeroCount); i++) {
				zeroStr = zeroStr + "0";
			}
		}

		for (int i = 0; i < bmpHeight; i++) {
			sb = new StringBuffer();
			for (int j = 0; j < bmpWidth; j++) {
				int color = bmp.getPixel(j, i);

				int r = (color >> 16) & 0xff;
				int g = (color >> 8) & 0xff;
				int b = color & 0xff;
				// if color close to white，bit='0', else bit='1'
				if (r > 160 && g > 160 && b > 160) {
					sb.append("0");
				} else {
					sb.append("1");
				}
			}
			if (zeroCount > 0) {
				sb.append(zeroStr);
			}
			list.add(sb.toString());
		}

		List<String> bmpHexList = binaryListToHexStringList(list);
		String commandHexString = "1D763000";

		// construct xL and xH
		// there are 8 pixels per byte. In case of modulo: add 1 to compensate.
		bmpWidth = bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1);
		int xL = bmpWidth % 256;
		int xH = (bmpWidth - xL) / 256;

		String xLHex = Integer.toHexString(xL);
		String xHHex = Integer.toHexString(xH);
		if (xLHex.length() == 1) {
			xLHex = "0" + xLHex;
		}
		if (xHHex.length() == 1) {
			xHHex = "0" + xHHex;
		}
		String widthHexString = xLHex + xHHex;

		// construct yL and yH
		int yL = bmpHeight % 256;
		int yH = (bmpHeight - yL) / 256;

		String yLHex = Integer.toHexString(yL);
		String yHHex = Integer.toHexString(yH);
		if (yLHex.length() == 1) {
			yLHex = "0" + yLHex;
		}
		if (yHHex.length() == 1) {
			yHHex = "0" + yHHex;
		}
		String heightHexString = yLHex + yHHex;

		List<String> commandList = new ArrayList<String>();
		commandList.add(commandHexString + widthHexString + heightHexString);
		commandList.addAll(bmpHexList);

		return hexList2Byte(commandList);
	}

	public static List<String> binaryListToHexStringList(List<String> list) {
		List<String> hexList = new ArrayList<String>();
		for (String binaryStr : list) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < binaryStr.length(); i += 8) {
				String str = binaryStr.substring(i, i + 8);

				String hexString = myBinaryStrToHexString(str);
				sb.append(hexString);
			}
			hexList.add(sb.toString());
		}
		return hexList;

	}

	public static String myBinaryStrToHexString(String binaryStr) {
		String hex = "";
		String f4 = binaryStr.substring(0, 4);
		String b4 = binaryStr.substring(4, 8);
		for (int i = 0; i < binaryArray.length; i++) {
			if (f4.equals(binaryArray[i])) {
				hex += hexStr.substring(i, i + 1);
			}
		}
		for (int i = 0; i < binaryArray.length; i++) {
			if (b4.equals(binaryArray[i])) {
				hex += hexStr.substring(i, i + 1);
			}
		}

		return hex;
	}

	private static String hexStr = "0123456789ABCDEF";

	private static String[] binaryArray = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000",
			"1001", "1010", "1011", "1100", "1101", "1110", "1111" };

	public static byte[] hexList2Byte(List<String> list) {
		List<byte[]> commandList = new ArrayList<byte[]>();

		for (String hexStr : list) {
			commandList.add(hexStringToBytes(hexStr));
		}
		byte[] bytes = sysCopy(commandList);
		return bytes;
	}

	// New implementation, change old
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static byte[] sysCopy(List<byte[]> srcArrays) {
		int len = 0;
		for (byte[] srcArray : srcArrays) {
			len += srcArray.length;
		}
		byte[] destArray = new byte[len];
		int destLen = 0;
		for (byte[] srcArray : srcArrays) {
			System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
		return destArray;
	}
}
