package com.cry

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project


class CostTransform extends Transform {

    //保留一个成员变量
    private Project mProject;

    CostTransform(Project project) {
        mProject = project
    }
/**
 * transform的名称
 * transformClassesWithMyClassTransformForDebug 运行时的名字
 * name="transformClassesWith${getName()}For${favor}"
 * @return name
 */
    @Override
    String getName() {
        return "CostTransform"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指Transform要操作内容的范围，官方文档Scope有7种类型：
     * EXTERNAL_LIBRARIES        只有外部库
     * PROJECT                       只有项目内容
     * PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
     * PROVIDED_ONLY                 只提供本地或远程依赖项
     * SUB_PROJECTS              只有子项目。
     * SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * TESTED_CODE                   由当前变量(包括依赖项)测试的代码
     *
     * TransformManager.SCOPE_FULL_PROJECT= Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES
     * @return scope
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 指明当前Transform是否支持增量编译
     *
     * 这项有点不明所以
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 因为这里的transform是转换，如果这一步不进行操作，下一步则会报错，所以需要所有的输入，都输出到输出才可以
     * @param transformInvocation
     * @throws TransformException
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        def outputProvider = transformInvocation.outputProvider
        //先遍历input
        transformInvocation.inputs.each { TransformInput input ->
            //input资源这里又分为两种，一种是文件夹的代码输入
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //如何得到所有的class文件-遍历directoryInput就可以得到所有的class文件了
                CodeInjectors.injects(directoryInput.file.absolutePath, mProject)
                //将拷贝到dest
                def dest = outputProvider.getContentLocation(
                        directoryInput.file.absolutePath,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
                //拷贝到自己想要的路径
                FileUtils.copyDirectory(directoryInput.file, new File("F:\\dev5\\Plugin3\\testCopy"))
            }
            //另外一种是jar包的输入
            input.jarInputs.each { JarInput jarInput ->
                //逻辑和上面一样，都是讲所有的jar包重新拷贝一遍
                //需要将jar包重命名，如果不的话，相同名字的jar包会冲突
                def newJarName = jarInput.name
//                println jarInput.file.absolutePath

                def md5 = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (newJarName.endsWith('.jar')) {
                    newJarName = newJarName.substring(0, newJarName.length() - 4)
                }
                newJarName = newJarName + md5
//                println "newJarName =${newJarName}"

                def dest = outputProvider.getContentLocation(
                        newJarName + md5,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR
                )
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}