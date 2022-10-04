<!--
 * @Author: Vitcou
 * @Date: 2022-07-21 01:47:54
 * @Description: 
-->
### 介绍
Juli, Andas 所属 Java实现服务

### 环境
#### 1. 依赖
maven: 3.6.3
java: 1.8+

### 构建
maven 构建，构建执行文件在target
```shell
mvn clean install -DskipTests
```

### 运行

#### 1. 运行可执行环境
依赖 Java运行环境 JDK运行
```shell
java -jar target/juli-0.0.1-SNAPSHOT.jar
```

#### 2. 查看运行情况

Unix\Linux\MacOS 使用lsof查看打开的网络
```shell
lsof  -i