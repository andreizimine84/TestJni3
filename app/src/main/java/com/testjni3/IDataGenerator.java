package com.testjni3;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public interface IDataGenerator {
	InputStream getData(Context context) throws NullPointerException, IOException;
}
