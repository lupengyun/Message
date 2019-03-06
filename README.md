# Message
* 消息收集注解处理

#### 使用

* 全局gradle 添加仓库直接地址
 ```
 repositories {
        maven { url 'https://dl.bintray.com/lupengyun/maven' }
  } 
 ```

* 在使用的模块添加依赖
``` 
 implementation 'com.lupy:annotation:1.1' 
 annotationProcessor 'com.lupy:compiler:1.1' 
```

* 设置模块名称
 ``` 
 javaCompileOptions{
            annotationProcessorOptions{
                arguments = ['MODEL_NAME':project.getName()]
            }
 } 
 ```

#### 代码调用
```
 HashMap<Integer, Class> container = MsgLoaderOfApp.get().getContainer();
 获取到收集的集合，通过反射拿到相应的实体，或者直接使用类型；
```
