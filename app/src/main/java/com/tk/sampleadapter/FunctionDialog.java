package com.tk.sampleadapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/15
 *     desc   : xxxx描述
 * </pre>
 */
public class FunctionDialog extends Dialog {
    private Button btnInit;
    private Button btnClear;
    private Button btnInitRandom;
    private Button btnAdd;
    private Button btnRemove;
    private Button btnAddRandom;
    private Button btnRemoveRandom;
    private Button btnAddHeader;
    private Button btnRemoveHeader;
    private Button btnAddFooter;
    private Button btnRemoveFooter;
    private Button btnSwitch;
    private Button btnEmptySwitch;
    private Button btnErrorSwitch;

    private View.OnClickListener onClickListener;

    public FunctionDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_function);
        getWindow().getAttributes().width = getContext().getResources().getDisplayMetrics().widthPixels;
        btnInit = (Button) findViewById(R.id.btn_init);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnInitRandom = (Button) findViewById(R.id.btn_init_random);
        btnAdd = (Button) findViewById(R.id.btn_add);
        btnRemove = (Button) findViewById(R.id.btn_remove);
        btnAddRandom = (Button) findViewById(R.id.btn_add_random);
        btnRemoveRandom = (Button) findViewById(R.id.btn_remove_random);
        btnAddHeader = (Button) findViewById(R.id.btn_add_header);
        btnRemoveHeader = (Button) findViewById(R.id.btn_remove_header);
        btnAddFooter = (Button) findViewById(R.id.btn_add_footer);
        btnRemoveFooter = (Button) findViewById(R.id.btn_remove_footer);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        btnEmptySwitch = (Button) findViewById(R.id.btn_empty_switch);
        btnErrorSwitch = (Button) findViewById(R.id.btn_error_switch);
        btnInit.setOnClickListener(onClickListener);
        btnClear.setOnClickListener(onClickListener);
        btnInitRandom.setOnClickListener(onClickListener);
        btnAdd.setOnClickListener(onClickListener);
        btnRemove.setOnClickListener(onClickListener);
        btnAddRandom.setOnClickListener(onClickListener);
        btnRemoveRandom.setOnClickListener(onClickListener);
        btnAddHeader.setOnClickListener(onClickListener);
        btnRemoveHeader.setOnClickListener(onClickListener);
        btnAddFooter.setOnClickListener(onClickListener);
        btnRemoveFooter.setOnClickListener(onClickListener);
        btnSwitch.setOnClickListener(onClickListener);
        btnEmptySwitch.setOnClickListener(onClickListener);
        btnErrorSwitch.setOnClickListener(onClickListener);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
