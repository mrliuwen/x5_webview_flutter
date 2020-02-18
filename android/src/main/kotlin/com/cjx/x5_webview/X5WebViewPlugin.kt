@file:Suppress("INACCESSIBLE_TYPE")

package com.cjx.x5_webview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import com.tencent.smtt.sdk.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File

class X5WebViewPlugin(var context: Context, var activity: Activity) : MethodCallHandler {

    private var readerView: TbsReaderView? = null

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.cjx/x5Video")
            channel.setMethodCallHandler(X5WebViewPlugin(registrar.context(), registrar.activity()))
            setCallBack(channel)

            registrar.platformViewRegistry().registerViewFactory("com.cjx/x5WebView", X5WebViewFactory(registrar.messenger(), registrar.activeContext()))

        }

        private fun setCallBack(channel: MethodChannel) {
            QbSdk.setTbsListener(object : TbsListener {
                override fun onInstallFinish(p0: Int) {
                    channel.invokeMethod("onInstallFinish", null)
                }

                override fun onDownloadFinish(p0: Int) {
                    channel.invokeMethod("onDownloadFinish", null)
                }

                override fun onDownloadProgress(p0: Int) {
                    channel.invokeMethod("onDownloadProgress", p0)
                }
            })
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "init" -> {
                QbSdk.initX5Environment(context.applicationContext, object : QbSdk.PreInitCallback {
                    override fun onCoreInitFinished() {

                    }

                    override fun onViewInitFinished(p0: Boolean) {
                        //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                        result.success(p0)
                    }

                })
            }
            "canUseTbsPlayer" -> {
                //返回是否可以使用tbsPlayer
                result.success(TbsVideo.canUseTbsPlayer(context))
            }
            "openVideo" -> {
                val url = call.argument<String>("url")
                val screenMode = call.argument<Int>("screenMode") ?: 103
                val bundle = Bundle()
                bundle.putInt("screenMode", screenMode)
                TbsVideo.openVideo(context, url, bundle)
                result.success(null)
            }
            "openFile" -> {
                val filePath = call.argument<String>("filePath")
                val params = hashMapOf<String, String>()
                params["local"] = call.argument<String>("local") ?: "false"
                params["style"] = call.argument<String>("style") ?: "0"
                params["topBarBgColor"] = call.argument<String>("topBarBgColor") ?: "#2CFC47"
                var menuData = call.argument<String>("menuData")
                if (menuData != null) {
                    params["menuData"] = menuData
                }
                if (!File(filePath).exists()) {
                    Toast.makeText(context, "文件不存在,请确认$filePath 是否正确", Toast.LENGTH_LONG).show()
                    result.success("文件不存在,请确认$filePath 是否正确")
                    return
                }
                QbSdk.canOpenFile(activity, filePath) { canOpenFile ->
                    if (canOpenFile) {
                        val intent = Intent(activity, FileActivity::class.java)
                        intent.putExtra("filepath", filePath);
                        activity.startActivity(intent)
                        /*    readerView = TbsReaderView(context, this)
                            readerView!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            //加载文件
                            val localBundle = Bundle()
                            localBundle.putString("filePath", filePath);
                            localBundle.putBoolean("is_bar_show", false);
                            localBundle.putBoolean("menu_show", false);
                            localBundle.putBoolean("is_bar_animating", false);
                            localBundle.putString("tempPath", filePath);
                            var var3 = com.tencent.smtt.sdk.a.d.c(this.activity)
                            var3 = var3 or !com.tencent.smtt.sdk.a.d.b(this.activity)
                            localBundle.putBoolean("browser6.0", var3)
                            val var4 = 6101625L
                            val var6 = 610000L
                            var var8 = com.tencent.smtt.sdk.a.d.a(this.activity, var4, var6)
                            var8 = var8 or !com.tencent.smtt.sdk.a.d.b(this.activity)
                            localBundle.putBoolean("browser6.1", var8)
                            readerView!!.openFile(localBundle);*/
                        /*    QbSdk.openFileReader(activity, filePath, params) { msg ->
                              //  result.success(msg)
                            }*/
                    } else {
                        Toast.makeText(context, "X5Sdk无法打开此文件", Toast.LENGTH_LONG).show()
                        result.success("X5Sdk无法打开此文件")
                    }
                }


//                val screenMode = call.argument<Int>("screenMode") ?: 103
//                val bundle = Bundle()
//                bundle.putInt("screenMode", screenMode)
//                TbsVideo.openVideo(context, url, bundle)
//                result.success(null)
            }

            "openWebActivity" -> {
                val url = call.argument<String>("url")
                val title = call.argument<String>("title")
                val intent = Intent(activity, X5WebViewActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", title)
                activity.startActivity(intent)
                result.success(null)
            }
            "getCarshInfo" -> {
                val info = WebView.getCrashExtraMessage(context)
                result.success(info)
            }
            "setDownloadWithoutWifi" -> {
                val isWithoutWifi = call.argument<Boolean>("isWithoutWifi")
                QbSdk.setDownloadWithoutWifi(isWithoutWifi ?: false)
                result.success(null)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

}