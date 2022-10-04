<!--
 * @Author: Vitcou
 * @Date: 2022-10-04 21:58:04
 * @Description: 
-->
### 介绍
Gona, Andas 所属 Go实现服务

### 环境
#### 1. 依赖
go: 1.15+


### 构建

构建本地的，构建执行文件在build文件夹
```shell
./build.sh
```

### 运行

#### 1. 运行可执行文件
go编译是直接的可执行文件，可以直接运行
```shell
./build/camera
```

#### 2. 查看运行情况

Unix\Linux\MacOS 使用lsof查看打开的网络
```shell
lsof  -i tcp:8080
```
