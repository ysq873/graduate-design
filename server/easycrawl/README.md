# esay

#### 介绍
基于webmagic的通用爬虫抓取应用，核心在于简单易用，搭建好后轻松抓取数据

#### 在线演示地址
在线演示地址: http://easycrawl.lomoye.top/
(在线演示地址第一次打开有点慢，请耐心等待)

#### 教程文档
教程文档地址: http://doc.lomoye.top/guide/doc.html

#### 前端代码
https://gitee.com/mountFuji/easy-crawl-front

#### 近期目标
- 支持动态js渲染的页面爬取[已完成]

#### 软件架构
软件架构说明
- 基于springboot实现
- 爬虫框架使用的是webmagic
- 数据库默认的是h2，配置文件在application.yml，如果想切换成mysql，请参考分支useMysql里的application.yml配置

#### 安装教程
1. EasyApplication 启动入口
2. 浏览器打开localhost:8080
3. 详细使用方式请看文档 http://doc.lomoye.top/guide/doc.html

#### 使用说明
- 基本流程
    1. EasyApplication 启动入口
    2. 打开 localhost:8080
    3. 新建爬虫(默认会导入两个爬虫示例)
    4. 运行
    5. 查看任务

    
#### FAQ
- 如何查看数据库数据：默认是使用嵌入式的h2数据库，可以用浏览器打开http://localhost:8080/h2-console登录, 默认用户名root，默认密码test

- 如何替换数据源: 默认是使用嵌入式的h2数据库，如果想替换成mysql，请在application.yml中更改数据源配置

#### 页面预览
http://easycrawl.lomoye.top/

#### 参与贡献
1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

#### 最近待修复问题

#### 最近完成功能
1.通过网页页面中的文字推测字段xpath规则

#### 待优化
1.字段长度有些可能偏长，现在默认最多存储255个字节，应该提供用户选择字段长度
2.可以定义字段的类型，比如图片链接，这样在显示的时候可以直接根据图片属性显示出图片

#### 待修复的bug

#### 已修复的bug
1.解决豆瓣爬取报403的问题，403不是因为豆瓣防爬，而是因为0.7.3版本的webmagic的SSL协议只支持TLSv1.0，自己重写了逻辑，可以支持TLSv1.2
2.在jdk下的jre/lib/security/java.security文件里面有一个配置项为jdk.tls.disabledAlgorithms，最好把这个给注释了，不然版本新一点的jdk可能会因为这个请求https的url会报错



#### 码云特技

1. 使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2. 码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3. 你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4. [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5. 码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6. 码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)