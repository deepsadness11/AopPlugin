#Aop
阅读Tinker的源码，最后一段存在代码的插入（Qzone方案插砖的实现原理），故来练习一下。
Tinker使用的是Asm.
Asm与javasisst不同的地方在于，javasisst对于java程序员更加友好，而asm对字节码进行操作，实时性和效率性更高
###javasisst
自定义gradle插件，并使用transform api