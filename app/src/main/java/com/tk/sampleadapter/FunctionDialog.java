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

/**
 * <pre>
 *     author : TK
 *     time   : 2017/07/15
 *     desc   : xxxx描述
 * </pre>
 */
public class FunctionDialog extends Dialog {

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

        findViewById(R.id.btn_init).setOnClickListener(onClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(onClickListener);
        findViewById(R.id.btn_init_diff).setOnClickListener(onClickListener);
        findViewById(R.id.btn_add_head).setOnClickListener(onClickListener);
        findViewById(R.id.btn_add_foot).setOnClickListener(onClickListener);
        findViewById(R.id.btn_add_random).setOnClickListener(onClickListener);
        findViewById(R.id.btn_add_random_list).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_head).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_foot).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_random).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_if).setOnClickListener(onClickListener);
        findViewById(R.id.btn_add_header).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_header).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_header_all).setOnClickListener(onClickListener);
        findViewById(R.id.btn_switch_header).setOnClickListener(onClickListener);
        findViewById(R.id.btn_add_footer).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_footer).setOnClickListener(onClickListener);
        findViewById(R.id.btn_remove_footer_all).setOnClickListener(onClickListener);
        findViewById(R.id.btn_switch_footer).setOnClickListener(onClickListener);
        findViewById(R.id.btn_empty_switch).setOnClickListener(onClickListener);
        findViewById(R.id.btn_error_switch).setOnClickListener(onClickListener);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
