package com.cjx.x5_webview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.util.List;


/**
 * Created by HaiyuKing
 * Used 调用腾讯浏览服务预览文件
 */

public class FileActivity extends Activity {

    private static final String TAG = FileActivity.class.getSimpleName();

    private TbsReaderView mTbsReaderView;//用于预览文件5-1

    private String filePath = "";
    private String fileName = "";

    public static void openDispalyFileActivity(Context context, String filePath, String fileName) {
        Intent intent = new Intent(context, FileActivity.class);
        intent.putExtra("filepath", filePath);
        intent.putExtra("filename", fileName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displayfile);
        initTbsReaderView();//用于预览文件5-2
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filepath");
        ActionBar actionBar = getActionBar();
        String title = "晶盘";
        try {
            title = filePath.substring(filePath.lastIndexOf("/") + 1);
        } catch (Exception e) {
        }
        actionBar.setTitle(title);
        displayFile(filePath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();//用于预览文件5-5
    }

    RelativeLayout rootRl;

    //初始化TbsReaderView 5-3
    private void initTbsReaderView() {
        mTbsReaderView = new TbsReaderView(FileActivity.this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {
                //ReaderCallback 接口提供的方法可以不予处理（目前不知道有什么用途，但是一定要实现这个接口类）
            }
        });
        rootRl = (RelativeLayout) findViewById(R.id.root_layout);
        rootRl.addView(mTbsReaderView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
    //预览文件5-4

    /**
     * filePath :文件路径。格式为 android 本地存储路径格式，例如：/sdcard/Download/xxx.doc. 不支持 file:///格式。暂不支持在线文件。
     * fileName : 文件的文件名（含后缀）
     */
    private void displayFile(String filePath) {
        boolean isCan = false;
        isCan = this.mTbsReaderView.preOpen(parseFormat(filePath), false);
        Log.d("FileActivity2222", isCan + "");
        if (isCan) {
            String bsReaderTemp = "/storage/emulated/0/TbsReaderTemp";
            File bsReaderTempFile = new File(bsReaderTemp);

            if (!bsReaderTempFile.exists()) {
                Log.d("TAG", "准备创建/storage/emulated/0/TbsReaderTemp！！");
                boolean mkdir = bsReaderTempFile.mkdir();
                if (!mkdir) {
                    Log.e("TAG", "创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
                }
            }
            Bundle bundle = new Bundle();
            bundle.putString("filePath", filePath);
            bundle.putString("tempPath", Environment.getExternalStorageDirectory().toString() + "/" + "TbsReaderTemp");
            mTbsReaderView.openFile(bundle);
        }

    }

    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

}