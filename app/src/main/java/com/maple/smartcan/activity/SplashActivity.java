package com.maple.smartcan.activity;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.maple.smartcan.R;
import com.maple.smartcan.network.HttpHelper;
import com.maple.smartcan.network.ServerCode;
import com.maple.smartcan.network.VollySimpleRequest;
import com.maple.smartcan.view.AnimationButton.AnimationButton;
import com.skyfishjy.library.RippleBackground;
import com.wega.library.loadingDialog.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends PermissionActivity {

    RippleBackground rippleBackground;//波纹动画
    AnimationButton animationButton;//波纹动画
    private LoadingDialog loadingDialog;//加载框

    boolean permission = false;

    private String Id;
    private String password;

    private EditText et_account;
    private EditText et_password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        permission = checkPermission();
        init();
    }


    private void init() {
        rippleBackground = findViewById(R.id.splash_ripple);
        animationButton = findViewById(R.id.splash_login);
        et_account = findViewById(R.id.splash_account);
        et_password = findViewById(R.id.splash_password);

        animationButton.setVisibility(View.INVISIBLE);
        decorateLogin();//修饰登录动画按钮
        rippleBackground.startRippleAnimation();//开启波纹动画
        //添加输入监控按钮
        addinputwatcher();
        decorateLoading();
        //获取缓存的账号数据
        getStorage();
    }

    private void addinputwatcher() {
        et_account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String account = et_account.getText().toString();
                Id = account;
                if (account != null) {
                    if (!account.equals("")) {
                        if (password != null) {
                            if (!password.equals("")) {
                                animationButton.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                    }
                }
                animationButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password_ = et_password.getText().toString();
                password = password_;
                if (password_ != null) {
                    if (!password_.equals("")) {
                        if (Id != null) {
                            if (!Id.equals("")) {
                                animationButton.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                    }
                }
                animationButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void getStorage() {
        SharedPreferences sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE);
        String account = sharedPreferences.getString("Id", "");

        if (account != null) {
            if (!account.equals("")) {
                Id = account;
                et_account.setText(account);
                password = sharedPreferences.getString("password", "");
                et_password.setText(password);
                animationButton.setVisibility(View.VISIBLE);
            }
        }
    }

    //装饰加载条
    private void decorateLoading() {
        LoadingDialog.Builder builder = new LoadingDialog.Builder(this);
        builder.setLoading_text(getText(R.string.loginaccount))
                .setSuccess_text(getText(R.string.success))
                .setFail_text(getText(R.string.fail));
        loadingDialog = builder.create();
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(false);
    }


    private void decorateLogin() {
        animationButton.setText(getResources().getString(R.string.login));
        animationButton.setColorBase(getResources().getColor(R.color.viewback3)); //设置整体基色，不设置有默认值
        animationButton.setColorBack(Color.GRAY); //设置进度条的背景色，不设置有默认值
        animationButton.setMode(AnimationButton.Mode.Hand_Finish); //设置进度条模式，不设置有默认值Mode.Auto_Finish
        animationButton.setOnAnimationButtonClickListener(new AnimationButton.OnAnimationButtonClickListener() {
            @Override
            public void onClick() {
                //stopProgress方法 仅仅在button.setMode(AnimationButton.Mode.Hand_Finish);之后才有效。
                animationButton.stopProgress();
            }

            @Override
            public void finish() {
                login();
            }

            @Override
            public boolean check() {
                return checkPermission();
            }
        });
    }

    //检查权限
    private boolean checkPermission() {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result_package = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result_ = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int result_location = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result_location2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result_package == PackageManager.PERMISSION_DENIED || result == PackageManager.PERMISSION_DENIED || result_ == PackageManager.PERMISSION_DENIED || result_location == PackageManager.PERMISSION_DENIED || result_location2 == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermission(permissions, 0x0001);
            return false;
        } else {
            return true;
        }
    }


    //登陆
    private void login() {
        loadingDialog.loading();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.LOGIN_ACCOUNT);
        params.put("Id", Id);
        params.put("password", password);
        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_CONTROL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String result = jsonObject.getString("result");
                    if (result.equals("success")) {
                        //登陆成功
                        //添加sharepreference
                        SharedPreferences sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        editor.putString("Id", Id);
                        editor.putString("password", password);
                        editor.commit();
                        loadingDialog.loadSuccess();
                        toMain();
                    } else {
                        loadingDialog.loadFail();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.loadFail();
            }
        }, params);
    }


    //前往main
    private void toMain() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
        //设置切换动画
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
        finish();
    }

}