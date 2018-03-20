package com.testjni3;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

public class DefaultDataGenerator implements IDataGenerator {
	Context myContext;

	public InputStream getData(Context myContext) throws NullPointerException, IOException {
		AssetManager assetManager = myContext.getAssets();
		InputStream input;
		input = assetManager.open("myfile");
		return input;
	}

}
