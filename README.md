# 乐优商城

## 1.项目介绍

### 1.1.项目简介

- 乐优商城是一个全品类的电商购物网站（B2C）。
- 用户可以在线购买商品、加入购物车、下单、秒杀商品
- 可以品论已购买商品
- 管理员可以在后台管理商品的上下架、促销活动
- 管理员可以监控商品销售状况
- 客服可以在后台处理退款操作
- 希望未来3到5年可以支持千万用户的使用

### 1.2.系统架构

乐优商城架构缩略图：

![](https://s1.ax1x.com/2020/07/24/UvSMJf.png)

整个乐优商城可以分为两部分：后台管理系统、前台门户系统。

- 后台管理：
  - 后台系统主要包含以下功能：
    - 商品管理，包括商品分类、品牌、商品规格等信息的管理
    - 销售管理，包括订单统计、订单退款处理、促销活动生成等
    - 用户管理，包括用户控制、冻结、解锁等
    - 权限管理，整个网站的权限控制，采用JWT鉴权方案，对用户及API进行权限控制
    - 统计，各种数据的统计分析展示
  - 后台系统会采用前后端分离开发，而且整个后台管理系统会使用Vue.js框架搭建出单页应用（SPA）。
  - 预览图：

![](https://s1.ax1x.com/2020/07/24/UvZbJH.png)

前台门户

- 前台门户面向的是客户，包含与客户交互的一切功能。例如：
  - 搜索商品
  - 加入购物车
  - 下单
  - 评价商品等等
- 前台系统我们会使用Thymeleaf模板引擎技术来完成页面开发。出于SEO优化的考虑，我们将不采用单页应用。
- 预览图：

![](https://s1.ax1x.com/2020/07/24/UvEYm8.png)

### 1.3.技术选型

前端技术：

- HTML、CSS、JavaScript（基于ES6标准）
- Vue.js 2.0以及基于Vue的ui框架：Vuetify
- 前端构建工具：WebPack
- 前端安装包工具：NPM
- Vue脚手架：Vue-cli
- Vue路由：vue-router
- ajax框架：axios
- 基于Vue的富文本框架：quill-editor
- qs序列化工具
- 二维码生成器开源工具：qrcode

后端技术：

- spring核心：SpringBoot-2.0.7
- 分布式框架：Spring Cloud-Finchley.SR2
- 持久层框架：MyBatis3
- Nosql：Redis-5.0.5
- 消息队列技术：RabbitMQ-3.6.10
- 全文搜索引擎：Elasticsearch-5.6.8
- web服务器：nginx-1.14.2：
- 文件上传工具：FastDFS - 5.08
- 数据库中间件：MyCat
- 静态化模板技术：Thymeleaf

数据库：

- MySQL-8.0.18

第三方API：

- 阿里短信服务API

- 微信支付API

<br>

## 2.项目结构

- leyou

  ：后台管理系统后台

  - leyou-registry：注册中心模块
  - leyou-api-gateway：网关模块
  - leyou-item：商品服务模块
  - leyou-common：通用工具模块
  - leyou-upload：图片上传模块
  - leyou-search：搜索服务模块
  - leyou-goods-web：商品详情页服务模块
  - leyou-user：用户中心模块
  - leyou-sms-service：短信服务模块
  - leyou-auth：授权中心模块
  - leyou-cart：购物车服务模块
  - leyou-order：订单服务模块

- leyou-manage-web：后台管理系统前端

- leyou-protal：前台门户



## 3. 附录

### 3.1 host文件模拟域名解析

```
127.0.0.1 manage.leyou.com
127.0.0.1 api.leyou.com
192.168.72.128 image.leyou.com
127.0.0.1 www.leyou.com
```

### 3.2 nginx配置

```
# 门户页面
server {
    listen       80;
    server_name  www.leyou.com;
	proxy_set_header X-Forwarded-Host $host;
    proxy_set_header X-Forwarded-Server $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	location /item {
		# 先找本地
		#root html;
		#if (!-f $request_filename) { #请求的文件不存在，就反向代理
			proxy_pass http://127.0.0.1:8084;
		#	break;
		#}
    }
	location / {
		proxy_pass http://127.0.0.1:9002;
		proxy_connect_timeout 600;
		proxy_read_timeout 600;
    }
}

# 管理页面
server {
    listen       80;
    server_name  manage.leyou.com;
    location / {
		proxy_pass http://127.0.0.1:9001;
		proxy_connect_timeout 600;
		proxy_read_timeout 600;
    }
}

#整个是网关
server {
    listen       80;
    server_name  api.leyou.com;
	proxy_set_header X-Forwarded-Host $host;
    proxy_set_header X-Forwarded-Server $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	proxy_set_header Host $host;
	location /api/upload {	
		proxy_pass http://127.0.0.1:8082;
		proxy_connect_timeout 600;
		proxy_read_timeout 600;
		rewrite "^/api/(.*)$" /$1 break; 
    }
    location / {
		proxy_pass http://127.0.0.1:10010;
		proxy_connect_timeout 600;
		proxy_read_timeout 600;
    }
}
```



