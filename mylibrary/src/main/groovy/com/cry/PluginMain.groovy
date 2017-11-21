package com.cry

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginMain implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "插件开始工作~~~"
        def ex = project.extensions
        //注册transform
        //这里appextension取到的就是android{...}这个里面的内容
        def android = ex.getByType(AppExtension)
        def ct = new CostTransform(project)
        android.registerTransform(ct)

        //创建拓展
        createAJavaClass(project)
        println "插件结束工作~~~"
    }

    private void createAJavaClass(Project project) {
        project.extensions.create("PluginExtension", MyPluginExtension)
        if (project.plugins.hasPlugin(AppPlugin)) {
            AppExtension android = project.extensions.getByType(AppExtension)
            android.applicationVariants.all { ApplicationVariant variant ->
                com.android.build.gradle.internal.variant.ApkVariantData apkVariantData = variant.variantData
                def scope = apkVariantData.scope
                //现在去拿extension的值
                def ex = project.extensions.getByName("PluginExtension")

                //创建一个task
                def createTaskName = scope.getTaskName("Ceshi", "MyTestPlugin")
                def createTask = project.task(createTaskName)

                createTask.doLast {
                    createJavaTest(variant, ex)
                }
                //创建项目的依赖 让这个任务再 generateBuildConfigTask 后执行
                def gbct = project.tasks.getByName(scope.generateBuildConfigTask.name)

                if (gbct) {
                    createTask.dependsOn gbct
                    gbct.finalizedBy createTask
                }
            }
        }
    }

    def createJavaTest(ApplicationVariant variant, MyPluginExtension ex) {
        def content = """package com.example.administrator.plugin3;
                      
                      /**
                       * Created by Administrator on 2017/11/21 0021.
                       */
                      
                      public class MyPluginTestClass {
                          public static final String str= "${ex.str}";
                      }"""
        def fileOutDir = variant.variantData.scope.buildConfigSourceOutputDir
        new File(fileOutDir, "MyPluginTestClass.java").write(content, 'UTF-8')
    }
}