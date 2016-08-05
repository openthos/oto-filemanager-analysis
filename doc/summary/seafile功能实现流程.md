#seafile功能实现流程
####参数说明
```
            -s 服务器地址  
            -n 资料库名称  
            -u 用户名  
            -p 密码   
            -l 资料库id   
            -d 本地文件夹
```
####实现流程
1、初始化seafilie环境<br>
```
seaf-cli init -d /home/zhu/test   初始化seafile配置文件夹，/home/zhu/test是本地的一个文件夹，用来存储配置文件
```
2、启动seafile服务<br>
```
seaf-cli start    启动seafile服务
```
3、显示服务器端资料库<br>
```
seaf-cli list-remote -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716    显示服务器端的所有资料库
```
4、显示本地已同步的资料库境<br>
```
seaf-cli list   显示本地已经同步的资料库
```
5、从服务器下载资料库境<br>
```
seaf-cli download -l f3c0b8e3-3644-43af-afa7-4b7e63869e87 -s  https://dev.openthos.org/ -d /home/zhu/  -u 1799858922@qq.com -p 279716   从服务器下载制定的资料库
```
6、同步本地文件夹境<br>
```
上传本地的文件夹需要经过两步：在服务器端创建对应的资料库、建立相应的同步关系并同步到服务器<br>
seaf-cli create -n zhangsan -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716   在服务器端创建对应的资料库<br>
seaf-cli sync -l 7fd8f246-2fa5-4868-b565-45098e7f52b4 -s  https://dev.openthos.org/ -d /home/zhu/wang/  -u 1799858922@qq.com -p 279716    建立对应的同步关系并同步到服务器
```
7、解除同步境<br>
```
seaf-cli desync -d /home/zhu/wang/    解除同步关系
```
