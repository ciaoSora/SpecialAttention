
package com.yang.spat;

import android.app.Notification;
import android.os.UserHandle;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import android.app.Notification;
import android.app.NotificationManager;

public class Handler implements IXposedHookLoadPackage {
    private static final String TAG = "处理者";
    private HashMap<String, HashSet<String>> ruleData;

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mm")) {
            return;
        }
        XSharedPreferences pref = new XSharedPreferences("com.yang.spat", "user_settings");
        final String rule = pref.getString("rule", "default");
        updateRule(rule);

        findAndHookMethod("android.app.NotificationManager", lpparam.classLoader, "notifyAsUser", String.class, int.class, Notification.class, UserHandle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Notification notification = (Notification)param.args[2];
                String title = notification.extras.getCharSequence("android.title").toString();
                String text  = notification.extras.getCharSequence("android.text").toString();

                String[] arr = new String[2];
                int numUnread = retrive(text, arr);
                if (numUnread == 0) {
                    return;
                }
                String nickname = arr[0];
                String content = arr[1];

                mylog(numUnread + ";;" + nickname + ";;" + content);
                if (!ruleData.containsKey(title)) {
                    return;
                }
                HashSet<String> specialAttention = ruleData.get(title);
                if (specialAttention.contains(nickname)) {
                    return;
                }
                param.setResult(null);
            }
        });
    }

    private void updateRule(String rule) {
        if (ruleData == null) {
            ruleData = new HashMap<>();
        }
        else {
            ruleData.clear();
        }

        String[] block = rule.split("\\n{2,}");
        if (block.length == 0) {
            return;
        }
        int startIndex = 0;
        HashSet<String> specialFriends = new HashSet<>();
        if (block[0].length() >= 4 && block[0].substring(0, 4).equals("$$$\n")) {
            startIndex = 1;
            String[] friends = block[0].substring(4).split("\\n");
            for (String friend : friends) {
                specialFriends.add(friend);
            }
        }

        for (int i = startIndex; i < block.length; ++i) {
            String[] rows = block[i].split("\\n");
            HashSet<String> specialAttention = (HashSet<String>)specialFriends.clone();
            for (int j = 1; j < rows.length; ++j) {
                specialAttention.add(rows[j]);
            }
            ruleData.put(rows[0], specialAttention);
        }
    }

    /* in代表通知正文，out[0]代表发消息者的备注或群昵称，out[1]代表他发的消息内容，返回值代表有几条未读 */
    private static int retrive(String in, String[] out) {
        int rt = -666;
        if (Pattern.matches("^\\[\\d+条\\].*?: [\\s\\S]*", in)) {
            rt = retrive0(in, out);
        }
        else if (Pattern.matches("^.*?: [\\s\\S]*", in)) {
            rt = 1;
            retrive1(in, out);
        }
        else {
            rt = 0;
            out[0] = out[1] = "";
        }
        return rt;
    }

    private static int retrive0(String in, String[] out) {
        int index = in.indexOf("条]");
        retrive1(in.substring(index + 2), out);
        return Integer.parseInt(in.substring(1, index));
    }

    private static void retrive1(String in, String[] out) {
        int index = in.indexOf(": ");
        out[0] = in.substring(0, index);
        out[1] = in.substring(index + 2);
    }

    private static void mylog(String text) {
        Log.d(TAG, text);
        XposedBridge.log(TAG + ": " + text);
    }
}