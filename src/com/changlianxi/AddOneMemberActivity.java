package com.changlianxi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.changlianxi.applation.CLXApplication;
import com.changlianxi.data.CircleMember;
import com.changlianxi.data.Global;
import com.changlianxi.data.enums.RetError;
import com.changlianxi.inteface.ConfirmDialog;
import com.changlianxi.inteface.OnEditFocusChangeListener;
import com.changlianxi.task.BaseAsyncTask;
import com.changlianxi.task.IinviteCircleMemberTask;
import com.changlianxi.util.BroadCast;
import com.changlianxi.util.Constants;
import com.changlianxi.util.DialogUtil;
import com.changlianxi.util.EditWather;
import com.changlianxi.util.Utils;
import com.changlianxi.view.SearchEditText;
import com.umeng.analytics.MobclickAgent;

/**
 * 通过输入方式添加圈子成员界面
 * 
 * @author teeker_bin
 * 
 */
public class AddOneMemberActivity extends BaseActivity implements
        OnClickListener {
    private Button btnNext;
    private EditText editName;
    private SearchEditText editMobile;
    private int cid;
    private ImageView back;
    private Dialog pd;
    private String type;// add 添加成员 create 创建圈子
    private TextView titleTxt;
    private CircleMember member;
    private List<CircleMember> inviteMemberList = new ArrayList<CircleMember>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_one_member);
        CLXApplication.addInviteActivity(this);
        type = getIntent().getStringExtra("type");
        if (type.equals("add")) {
            cid = getIntent().getIntExtra("cid", 0);
        }
        initView();
        setListener();
    }

    /**
     * 设置页面统计
     * 
     */
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    /**
     * 初始化控件
     */
    private void initView() {
        btnNext = (Button) findViewById(R.id.next);
        editMobile = (SearchEditText) findViewById(R.id.editmobile);
        editMobile.addTextChangedListener(new EditWather(editMobile, this));
        editName = (EditText) findViewById(R.id.editname);
        back = (ImageView) findViewById(R.id.back);
        titleTxt = (TextView) findViewById(R.id.titleTxt);
        titleTxt.setText("输入联系人");
    }

    /**
     * 设置监听事件
     */
    @SuppressLint("NewApi")
    private void setListener() {
        btnNext.setOnClickListener(this);
        back.setOnClickListener(this);
        if (type.equals("add")) {
            btnNext.setText("完成");
        }
        editMobile.setOnFocusChangeListener(new OnEditFocusChangeListener(
                editMobile, this));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                editMobile.clearFocus();
                String name = editName.getText().toString();
                String mobile = editMobile.getText().toString()
                        .replace("-", "");
                if (name.length() == 0) {
                    Utils.showToast("姓名都没有，不能邀请哦", Toast.LENGTH_SHORT);
                    return;
                }
                if (mobile.length() == 0) {
                    Utils.showToast("手机号没有，不能邀请哦", Toast.LENGTH_SHORT);
                    return;
                }
                if (!Utils.isPhoneNum(mobile)) {
                    Utils.showToast("地球上貌似没有这种格式的手机号码:p", Toast.LENGTH_SHORT);
                    return;
                }
                CircleMember m = new CircleMember(cid);
                m.setName(name);
                m.setCellphone(mobile);
                inviteMemberList.add(m);
                if (type.equals("create")) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("contactsList",
                            (Serializable) (Serializable) inviteMemberList);
                    intent.putExtras(bundle);
                    intent.setClass(this, CreateCircleActivity.class);
                    startActivity(intent);
                    Utils.leftOutRightIn(this);
                    return;
                }
                InviteMember();
                break;
            case R.id.back:
                finish();
                Utils.rightOut(this);
                break;
            default:
                break;
        }
    }

    /**
     * 邀请 成员
     */
    private void InviteMember() {
        pd = DialogUtil.getWaitDialog(this, "请稍候");
        pd.show();
        member = new CircleMember(cid, 0, Global.getIntUid());
        IinviteCircleMemberTask task = new IinviteCircleMemberTask(
                inviteMemberList);
        task.setTaskCallBack(new BaseAsyncTask.PostCallBack<RetError>() {

            @Override
            public void taskFinish(RetError result) {
                if (pd != null) {
                    pd.dismiss();
                }
                if (result != RetError.NONE) {
                    return;
                }
                BroadCast.sendBroadCast(AddOneMemberActivity.this,
                        Constants.REFRESH_CIRCLE_USER_LIST);
                String str = "添加成功";
                for (int i = inviteMemberList.size() - 1; i >= 0; i--) {
                    if ("".equals(inviteMemberList.get(i).getInviteCode())) {
                        inviteMemberList.remove(i);
                        str = "联系人已存";
                        break;
                    }
                }
                continueAdd(str);
            }

            @Override
            public void readDBFinish() {

            }
        });
        task.executeWithCheckNet(member);
    }

    /**
     * 继续添加提示框
     */
    private void continueAdd(String str) {
        Dialog dialog = DialogUtil.confirmDialog(this, str + "\n是否继续添加？", "是",
                "否", new ConfirmDialog() {

                    @Override
                    public void onOKClick() {
                        editMobile.setText("");
                        editName.setText("");
                    }

                    @Override
                    public void onCancleClick() {
                        intentSmsPreviewActivity();
                    }
                });
        dialog.show();
    }

    /**
     * 跳转到短信预览界面
     */
    private void intentSmsPreviewActivity() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("contactsList", (Serializable) inviteMemberList);
        intent.putExtras(bundle);
        intent.setAction(Constants.CIRCLE_MEMBERS_INVITE_WARN);
        BroadCast.sendBroadCast(this, intent);
        CLXApplication.exitSmsInvite();
        Utils.rightOut(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd != null) {
            pd.dismiss();
        }

    }
}
