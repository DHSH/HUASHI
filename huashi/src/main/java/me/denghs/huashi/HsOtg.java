package me.denghs.huashi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.huashi.otg.sdk.GetImg;
import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import me.denghs.huashi.otg.R;

public class HsOtg {
    private final Context context;
    private final String portraitpath;
    boolean m_Con = false;
    boolean m_Auto = false;
    long startTime;
    String panoramapath = "";
    SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式


    Handler h = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 99 || msg.what == 100) {
//                statu.setText((String) msg.obj);
//                m_Con = false;
            }
            // 第一次授权时候的判断是利用handler判断，授权过后就不用这个判断了
            if (msg.what == HandlerMsg.CONNECT_SUCCESS) {
                System.out.println("CONNECT_SUCCESS");
                start();
            }
        }
    };
    private HsOtgApi api;
    private CallBack callBack;
    private Thread thread;


    public HsOtg(Context instance) {
        context = instance;
        String path = instance.getString(R.string.cardId_panorama);
        if (!"".equals(path)){
            File panorama = new File(path);
            if (!panorama.exists()) {
                panorama.mkdirs();
            }
        }
        panoramapath = path;
        path = instance.getString(R.string.cardId_portrait);
        if (!"".equals(path)){
            File portrait = new File(path);
            if (!portrait.exists()) {
                portrait.mkdirs();
            }
        }
        portraitpath = path;



//        filepath = Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/wltlib";// 授权目录
        // filepath = "/mnt/sdcard/wltlib";// 授权目录
//        Log.e("LJFDJ", panoramapath);
        api = new HsOtgApi(h, instance);
    }

    /**
     * 初始化
     * 因为第一次需要点击授权，所以第一次点击时候的返回是-1所以我利用了广播接受到授权后用handler发送消息
     *
     * @return 1 表示成功
     */
    public void init(CallBack callBack) {
        int init = api.init();
        if (init == 1) {
            this.callBack = callBack;
            start();
            callBack.connect_success(api.GetSAMID());
        }
    }

    private void start() {
        if (callBack != null) {
            callBack.connect_success(api.GetSAMID());
        }
        m_Con = true;
        m_Auto = true;
        thread = new Thread(new CPUThread());
        thread.start();
    }

    /**
     * @return
     */
    public void close() {

        if (m_Auto) {
            m_Auto = false;
//            SystemClock.sleep(1500);
        }
        thread.interrupt();
        if (api != null) {
            api.unInit();
//            api = null;
        }
        m_Con = false;
        h.removeCallbacksAndMessages(null);
        callBack = null;
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

    public class CPUThread extends Thread {

        private String idCard;

        public CPUThread() {
            super();
        }

        @Override
        public void run() {
            super.run();
            HSIDCardInfo ici;
            Message msg;
            try {
                while (m_Auto) {
                    // ///////////////循环读卡，不拿开身份证
                    if (api.NotAuthenticate(200, 200) != 1) {
                        // ////////////////循环读卡，需要重新拿开身份证
                        // if (api.Authenticate(200, 200) != 1) {
                        //                    msg = Message.obtain();
                        //                    msg.what = HandlerMsg.READ_ERROR;
                        //                    h.sendMessage(msg);
                    } else {
                        ici = new HSIDCardInfo();
                        if (api.ReadCard(ici, 200, 1300) == 1) {
                            if (!ici.getIDCard().equals(idCard)) {
                                idCard = ici.getIDCard();
                                byte[] bmpBuf = new byte[102 * 126 * 3 + 54 + 126 * 2]; // 照片头像bmp数据
                                // String bmpPath = filepath + "/zp.bmp"; // 照片头像保存路径
                                int ret = api.unpack(ici.getwltdata(), bmpBuf, portraitpath);
                                Bitmap bitmap = null;
                                Bitmap panorama = null;
                                if (ret == 1) {// 解码失败
                                    bitmap = BitmapFactory.decodeByteArray(bmpBuf, 0,
                                            bmpBuf.length);
                                    if (!TextUtils.isEmpty(panoramapath)) {
                                        try {
                                            panorama = GetImg.ShowBmp(ici, context, 1, panoramapath, bitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                final Bitmap finalBitmap = bitmap;
                                final Bitmap finalPanorama = panorama;
                                final HSIDCardInfo finalici = ici;
                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callBack != null) {
                                            callBack.read_success(finalici, finalBitmap, finalPanorama);
                                        }
                                    }
                                });
                            }
                            Thread.sleep(700);
                        }
                    }
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface CallBack {
        /**
         * 连接成功
         *
         * @param samid 设备Id
         */
        void connect_success(String samid);

        /**
         *
         * @param Info
         * @param portraitBmp 身份证头像
         * @param panoramaBmp 身份证正反照
         */
        void read_success(HSIDCardInfo Info, Bitmap portraitBmp, Bitmap panoramaBmp);

    }

}