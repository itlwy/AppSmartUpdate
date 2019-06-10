package com.lwy.smartupdate.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.lwy.smartupdate.R;

/**
 * @author lwy 2018/8/30
 * @version v1.0.0
 * @name UpdateDialog
 * @description
 */
public class UpdateDialog extends Dialog implements View.OnClickListener {
    private TextView mUpdateInfoTV;
    private NumberProgressBar mProgressBar;
    private TextView mTitleTV;
    private Button mOKBtn;
    private TextView mIgnoreTV;
    private ImageView mCloseIV;
    private CallBack mCallBack;
    private LinearLayout mCloseLLT;
    private boolean isUpdating;

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public UpdateDialog(@NonNull Context context) {
        super(context);
        init();
    }


    public UpdateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected UpdateDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.lib_update_dialog, null);
        mProgressBar = (NumberProgressBar) view.findViewById(R.id.progressbar);
        mTitleTV = (TextView) view.findViewById(R.id.title_tv);
        mUpdateInfoTV = (TextView) view.findViewById(R.id.update_info_tv);
        mOKBtn = (Button) view.findViewById(R.id.ok_btn);
        mIgnoreTV = (TextView) view.findViewById(R.id.ignore_tv);
        mCloseIV = (ImageView) view.findViewById(R.id.close_iv);
        mCloseLLT = (LinearLayout) view.findViewById(R.id.close_llt);
        mIgnoreTV.setOnClickListener(this);
        mCloseIV.setOnClickListener(this);
        mOKBtn.setOnClickListener(this);
        setContentView(view);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;

        mIgnoreTV.setOnClickListener(this);

//        WindowManager m = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = m.getDefaultDisplay(); // 获取屏幕宽、高用
//        params.width = (int) (display.getWidth() * 0.7); // 宽度设置为屏幕的0.7
        getWindow().setAttributes(params);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(isCancel);
//        dialog.show();
    }

    public static UpdateDialog createDialog(Activity activity) {
        return new UpdateDialog(activity, R.style.UpdateAppDialog);
    }

    public void setUpdateTitle(CharSequence title) {
        mTitleTV.setText(title);
    }

    public void showCloseLLT(boolean isShow) {
        mCloseLLT.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void showIgnoreTextView(boolean isShow) {
        mIgnoreTV.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void showProgressBar(boolean isShow) {
        mProgressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setMax(int max) {
        mProgressBar.setMax(max);
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public void setText(String tip) {
        mUpdateInfoTV.setText(tip);
    }

    public void setOKBtnEnable(boolean enable) {
        mOKBtn.setEnabled(enable);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ignore_tv) {
            dismiss();
            if (mCallBack != null)
                mCallBack.onIgnored(v);
        } else if (i == R.id.ok_btn) {

            if (mCallBack != null)
                mCallBack.onOK(v);
        } else if (i == R.id.close_iv) {
            dismiss();
            if (mCallBack != null)
                mCallBack.onClosed(v);
        }
    }

    public interface CallBack {
        void onIgnored(View view);

        void onOK(View view);

        void onClosed(View view);

    }
}
