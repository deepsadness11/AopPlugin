package com.cry

import com.android.build.gradle.AppExtension
import javassist.ClassPool
import javassist.CtField
import org.gradle.api.Project

public class CodeInjectors {
    //初始化类初始化池
    static ClassPool mClassPool = ClassPool.getDefault()

    public static void injects(String filePath, Project project) {
        println "start injects class~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
        //将文件注入进去，否则会找不到文件
        mClassPool.appendClassPath(filePath)
        def android = project.extensions.getByType(AppExtension)
        //需要将android加进来，否则会找不到android sdk
        def bootPath = android.bootClasspath[0].toString()
        println "bootPath = ${bootPath}"
        mClassPool.appendClassPath(bootPath)

        //因为onCreate方法需要添加bundle，所以把bundle添加进来
        mClassPool.importPackage("android.os.Bundle")

        //开始进行操作，先要过滤所有的生成文件
        File dir = new File(filePath)
        if (dir.isDirectory()) {
            //开始遍历文件夹
            dir.eachFileRecurse { File file ->
                def singleFilePath = file.absolutePath
                def isGenerate =
                        singleFilePath.contains('R$') ||
                                singleFilePath.contains('BuildConfig.class') ||
                                singleFilePath.contains('R.class');

                if (singleFilePath.endsWith(".class") && !isGenerate) {
                    println "start op class name = ${singleFilePath}"
                    //先写死看看
                    if (file.name == "MainActivity.class") {
                        insertCode(filePath, singleFilePath)
                    } else {

                    }
                }
            }
        }
        println "end injects class~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    }

    private static void insertCode(String filePath, String singleFilePath) {
        //拿到class文件的名称
        String className2 = singleFilePath.split("debug")[1].
                substring(1).replace(".class", "")
        className2 = className2.replace("\\", ".");
        def c = mClassPool.getCtClass(className2)

        //解冻
        if (c.isFrozen()) c.defrost()
        //得到onCreate方法
        def m = c.getDeclaredMethod("onCreate")

        c.addField(CtField.make("public long startTime;", c))
        String insetStrBefore = """
                startTime = android.os.SystemClock.elapsedRealtime();
                android.widget.Toast.makeText(this, "I am insert toast~!!", android.widget.Toast.LENGTH_SHORT).show();
                """
        String insetStrAfter = """
                long endTime = android.os.SystemClock.elapsedRealtime();
                android.util.Log.i("JavasisstCostTime", "CostTime=" + (endTime - startTime));
                """
        //在代码头部插入
        m.insertBefore(insetStrBefore)
        m.insertAfter(insetStrAfter)
        //写入
        c.writeFile(filePath)
        //释放
        c.detach()
        println "end op class name -------------------------------------"
    }
}