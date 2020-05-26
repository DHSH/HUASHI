package com.example.as_cvr_otg;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huashi.otg.sdk.GetImg;
import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import me.denghs.huashi.R;

public class MainActivity extends Activity {

    private TextView tv_info, statu;
    private ImageView iv_photo;
    private Button conn, read, autoread,m_close;

    boolean m_Con = false;

    boolean m_Auto = false;
    long startTime;
    HsOtgApi api = null;
    String filepath = "";
    SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式

    Handler h = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 99 || msg.what == 100) {
                statu.setText((String) msg.obj);
                m_Con = false;
            }
            // 第一次授权时候的判断是利用handler判断，授权过后就不用这个判断了
            if (msg.what == HandlerMsg.CONNECT_SUCCESS) {
                statu.setText("连接成功" + api.GetSAMID());
                m_Con = true;
                //sam.setText(api.GetSAMID());
            }
            if (msg.what == HandlerMsg.CONNECT_ERROR) {
                statu.setText("连接失败");
            }
            if (msg.what == HandlerMsg.READ_ERROR) {
                // cz();
                // statu.setText("卡认证失败");
                statu.setText("请放卡...");
            }
            if (msg.what == HandlerMsg.READ_SUCCESS) {

                HSIDCardInfo ic = (HSIDCardInfo) msg.obj;
                byte[] fp = new byte[1024];
                fp = ic.getFpDate();
                String m_FristPFInfo = "";
                String m_SecondPFInfo = "";

                if (fp[4] == (byte) 0x01) {
                    m_FristPFInfo = String.format(
                            "指纹  信息：第一枚指纹注册成功。指位：%s。指纹质量：%d \n",
                            GetFPcode(fp[5]), fp[6]);
                } else {
                    m_FristPFInfo = "身份证无指纹 \n";
                }
                if (fp[512 + 4] == (byte) 0x01) {
                    m_SecondPFInfo = String.format(
                            "指纹  信息：第二枚指纹注册成功。指位：%s。指纹质量：%d \n",
                            GetFPcode(fp[512 + 5]), fp[512 + 6]);
                } else {
                    m_SecondPFInfo = "身份证无指纹 \n";
                }
                if (ic.getcertType() == " ") {
                    tv_info.setText("证件类型：身份证\n" + "姓名：" + ic.getPeopleName()
                            + "\n" + "性别：" + ic.getSex() + "\n" + "民族："
                            + ic.getPeople() + "\n" + "出生日期："
                            + df.format(ic.getBirthDay()) + "\n" + "地址："
                            + ic.getAddr() + "\n" + "身份号码：" + ic.getIDCard()
                            + "\n" + "签发机关：" + ic.getDepartment() + "\n"
                            + "有效期限：" + ic.getStrartDate() + "-"
                            + ic.getEndDate() + "\n" + m_FristPFInfo + "\n"
                            + m_SecondPFInfo);
                } else {
                    if (ic.getcertType() == "J") {
                        tv_info.setText("证件类型：港澳台居住证（J）\n" + "姓名："
                                + ic.getPeopleName() + "\n" + "性别："
                                + ic.getSex() + "\n" + "签发次数："
                                + ic.getissuesNum() + "\n" + "通行证号码："
                                + ic.getPassCheckID() + "\n" + "出生日期："
                                + df.format(ic.getBirthDay()) + "\n" + "地址："
                                + ic.getAddr() + "\n" + "身份号码："
                                + ic.getIDCard() + "\n" + "签发机关："
                                + ic.getDepartment() + "\n" + "有效期限："
                                + ic.getStrartDate() + "-" + ic.getEndDate()
                                + "\n" + m_FristPFInfo + "\n" + m_SecondPFInfo);
                    } else {
                        if (ic.getcertType() == "I") {
                            tv_info.setText("证件类型：外国人永久居留证（I）\n" + "英文名称："
                                    + ic.getPeopleName() + "\n" + "中文名称："
                                    + ic.getstrChineseName() + "\n" + "性别："
                                    + ic.getSex() + "\n" + "永久居留证号："
                                    + ic.getIDCard() + "\n" + "国籍："
                                    + ic.getstrNationCode() + "\n" + "出生日期："
                                    + df.format(ic.getBirthDay()) + "\n"
                                    + "证件版本号：" + ic.getstrCertVer() + "\n"
                                    + "申请受理机关：" + ic.getDepartment() + "\n"
                                    + "有效期限：" + ic.getStrartDate() + "-"
                                    + ic.getEndDate() + "\n" + m_FristPFInfo
                                    + "\n" + m_SecondPFInfo);
                        }
                    }

                }
                try {
                    byte[] bmpBuf = new byte[102 * 126 * 3 + 54 + 126 * 2]; // 照片头像bmp数据
                    // String bmpPath = filepath + "/zp.bmp"; // 照片头像保存路径
                    String bmpPath = "";
                    int ret = api.unpack(ic.getwltdata(), bmpBuf, bmpPath);

                    if (ret != 1) {// 解码失败
                        statu.setText("头像解码失败");
                        return;
                    }

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bmpBuf, 0,
                            bmpBuf.length);
                    iv_photo.setImageBitmap(bitmap); // 显示头像

                    //////////////////////////////////////////////////////////////////////////////////////
                    // 生成证件正反面，需要给filepath授权写权限
//                    try {
//                        GetImg.ShowBmp(ic, MainActivity.this, 1, filepath,bitmap);
//                        iv_photo.setImageBitmap(GetImg.ShowBmp(ic, MainActivity.this, 1, filepath,bitmap)); // 显示正反面
//                    } catch (IOException e) {
//                        // TODO 自动生成的 catch 块
//                        e.printStackTrace();
//                    }
                    //////////////////////////////////////////////////////////////////////////////////////
                    if (!m_Auto) {
                        startTime = System.currentTimeMillis() - startTime;
                        statu.setText("读卡成功,用时：" + startTime);
                    }
                    else{
                        statu.setText("读卡成功");
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "头像解码失败",
                            Toast.LENGTH_SHORT).show();
                }

            }
        };
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filepath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/wltlib";// 授权目录
        // filepath = "/mnt/sdcard/wltlib";// 授权目录
        Log.e("LJFDJ", filepath);

        initView();
        setEnven();
    }

    private void setEnven() {

        conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (api == null) {
                    api = new HsOtgApi(h, MainActivity.this);
                }
                int ret = api.init();// 因为第一次需要点击授权，所以第一次点击时候的返回是-1所以我利用了广播接受到授权后用handler发送消息
                if (ret == 1) {
                    statu.setText("连接成功,模块号：" + api.GetSAMID());
                    m_Con = true;

                } else {
                    statu.setText("连接失败");
                    m_Con = false;

                }
            }
        });
        read.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!m_Con) {
                    statu.setText("100U未连接");
                    return;
                }
                cz();
                startTime = System.currentTimeMillis();
                if (api.Authenticate(200, 200) != 1) {
                    statu.setText("卡认证失败");
                    return;
                }
                HSIDCardInfo ici = new HSIDCardInfo();
                if (api.ReadCard(ici, 200, 1300) == 1) {
                    Message msg = Message.obtain();
                    msg.obj = ici;
                    msg.what = HandlerMsg.READ_SUCCESS;
                    h.sendMessage(msg);
                }
            }

        });
        autoread.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!m_Con) {
                    statu.setText("100U未连接");
                    return;
                }
                cz();
                if (m_Auto) {
                    m_Auto = false;
                    autoread.setText("自动读卡");
                } else {
                    m_Auto = true;
                    new Thread(new CPUThread()).start();
                    autoread.setText("停止读卡");
                }
            }
        });
        m_close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (m_Auto) {
                    m_Auto = false;
                    SystemClock.sleep(1500);
                }
                if (api != null) {
                    api.unInit();
                    api = null;
                }
                m_Con = false;
            }
        });
    }

    private void cz() {
        // TODO Auto-generated method stub
        tv_info.setText("");
        iv_photo.setImageBitmap(null);
    }

    public class CPUThread extends Thread {
        public CPUThread() {
            super();
        }

        @Override
        public void run() {
            super.run();
            HSIDCardInfo ici;
            Message msg;
            while (m_Auto) {
                // ///////////////循环读卡，不拿开身份证
                if (api.NotAuthenticate(200, 200) != 1) {
                    // ////////////////循环读卡，需要重新拿开身份证
                    // if (api.Authenticate(200, 200) != 1) {
                    msg = Message.obtain();
                    msg.what = HandlerMsg.READ_ERROR;
                    h.sendMessage(msg);
                } else {
                    ici = new HSIDCardInfo();
                    if (api.ReadCard(ici, 200, 1300) == 1) {
                        msg = Message.obtain();
                        msg.obj = ici;
                        msg.what = HandlerMsg.READ_SUCCESS;
                        h.sendMessage(msg);
                    }
                }
                SystemClock.sleep(300);
                msg = Message.obtain();
                msg.what = HandlerMsg.READ_ERROR;
                h.sendMessage(msg);
                SystemClock.sleep(300);
            }

        }
    }

    private void copy(Context context, String fileName, String saveName,
                      String savePath) {
        File path = new File(savePath);
        if (!path.exists()) {
            path.mkdir();
        }

        try {
            File e = new File(savePath + "/" + saveName);
            if (e.exists() && e.length() > 0L) {
                Log.i("LU", saveName + "存在了");
                return;
            }

            FileOutputStream fos = new FileOutputStream(e);
            InputStream inputStream = context.getResources().getAssets()
                    .open(fileName);
            byte[] buf = new byte[1024];
            boolean len = false;

            int len1;
            while ((len1 = inputStream.read(buf)) != -1) {
                fos.write(buf, 0, len1);
            }

            fos.close();
            inputStream.close();
        } catch (Exception var11) {
            Log.i("LU", "IO异常");
        }

    }

    private void initView() {
        setContentView(R.layout.activity_main);
        tv_info = (TextView) findViewById(R.id.tv_info);
        statu = (TextView) findViewById(R.id.statu);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);

        conn = (Button) findViewById(R.id.conn);
        read = (Button) findViewById(R.id.read);
        autoread = (Button) findViewById(R.id.autoread);
        m_close = (Button) findViewById(R.id.close);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (api == null) {
            return;
        }
        api.unInit();
    }

    /**
     * 指纹 指位代码
     *
     * @param FPcode
     * @return
     */
    String GetFPcode(int FPcode) {
        switch (FPcode) {
            case 11:
                return "右手拇指";
            case 12:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "未知";
        }
    }

    // 将逗号分隔的字符串转换为byte数组
    public int String2byte(byte[] b, String StrBuf) {
        String[] parts = StrBuf.split(",");
        int Itmp;
        int Len = parts.length;
        if (Len == b.length) {
            for (int i = 0; i < Len; i++) {
                try {
                    Itmp = Integer.valueOf(parts[i], 16);
                    b[i] = (byte) Itmp;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return Len;
        }
        return -1;
    }

    private char[] getChars(byte[] bytes) {
        // Charset cs = Charset.forName ("UTF-8");
        Charset cs = Charset.forName("GBK");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }
}
