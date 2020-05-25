package com.example.as_cvr_otg;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;

import java.text.SimpleDateFormat;

import me.denghs.huashi.HsOtg;
import me.denghs.huashi.R;

public class Main2Activity extends Activity {

    private TextView tv_info;
    private TextView statu;
    private ImageView iv_photo;
    private Button conn;
    private Button m_close;
    private HsOtg api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setEnven();
    }

    private void initView() {
        setContentView(R.layout.activity_main2);
        tv_info = (TextView) findViewById(R.id.tv_info);
        statu = (TextView) findViewById(R.id.statu);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);

        conn = (Button) findViewById(R.id.conn);
        m_close = (Button) findViewById(R.id.close);

    }

    private void setEnven() {

        conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                api = new HsOtg(Main2Activity.this);
                api.init(new HsOtg.CallBack() {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式
                    @Override
                    public void connect_success(String samid) {
                        statu.setText("连接成功" + samid);
                    }

                    @Override
                    public void read_success(HSIDCardInfo ic, Bitmap portraitBmp, Bitmap panoramaBmp) {
                        iv_photo.setImageBitmap(panoramaBmp);

                        tv_info.setText("证件类型：身份证\n" + "姓名：" + ic.getPeopleName()
                                + "\n" + "性别：" + ic.getSex() + "\n" + "民族："
                                + ic.getPeople() + "\n" + "出生日期："
                                + df.format(ic.getBirthDay()) + "\n" + "地址："
                                + ic.getAddr() + "\n" + "身份号码：" + ic.getIDCard()
                                + "\n" + "签发机关：" + ic.getDepartment() + "\n"
                                + "有效期限：" + ic.getStrartDate() + "-"
                                + ic.getEndDate() + "\n");
                    }
                });
            }
        });
        m_close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (api != null) {
                    iv_photo.setImageBitmap(null);
                    tv_info.setText("");
                    statu.setText("状态");
                    api.close();
                }
            }
        });
    }
}
