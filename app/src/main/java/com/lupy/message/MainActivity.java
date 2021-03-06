package com.lupy.message;

import android.app.Activity;
import android.os.Bundle;

import com.cylan.annotation.MessageCylan;
import com.cylan.message.MsgLoaderOfApp;

import java.util.HashMap;

/**
 * @author Lupy
 * @Description
 */
@MessageCylan(messageId = 699)
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HashMap<Integer, Class> container = MsgLoaderOfApp.get().getContainer();
    }
}
