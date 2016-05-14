package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

public class LogoutAct extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());        // NEED THIS FOR LOGOUT
        setContentView(R.layout.activity_logout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FB SDK", "LogoutAct: Going to Next Act");

                fbLogout();
                clearSharedPreferences();

                Snackbar.make(view, "Logging out...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                goToInitialAct();

            }
        });
    }

    private void clearSharedPreferences() {
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", "0");
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
    }

    private void fbLogout() {
        Log.d("FB SDK", "Logout Act: Accesstoken should be something: " + AccessToken.getCurrentAccessToken());
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
            Log.d("FB SDK", "Logout Act: Accesstoken should be null: " + AccessToken.getCurrentAccessToken());
            Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();

        }
    }

    private void goToInitialAct(){
        startActivity(new Intent(this, InitialAct.class));
    }


}