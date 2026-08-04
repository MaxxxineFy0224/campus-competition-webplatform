# 竞队 - 校园竞赛组队平台

一个帮助大学生发现竞赛、组建团队、在线报名的一站式平台。

## 功能特性

- **竞赛浏览**：查看各类校园竞赛信息，支持筛选和搜索
- **竞赛详情**：查看竞赛的详细信息，包括主办方、时间、地点等
- **组队广场**：浏览和加入其他同学发布的组队招募
- **发布组队**：发布竞赛组队需求，招募队友
- **AI 智能匹配**：根据个人技能和兴趣，智能匹配适合的竞赛和队友
- **个人中心**：管理个人信息、报名记录和组队状态
- **用户认证**：JWT 登录注册，支持角色权限管理

## 技术栈

### 前端

| 技术 | 版本 |
|------|------|
| React | 19 |
| Vite | 8 |
| React Router | 7 |
| TailwindCSS | 4 |

### 后端

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.2 |
| MyBatis-Plus | 3.5 |
| MySQL | 8.0+ |
| JWT (jjwt) | 0.12 |
| SpringDoc OpenAPI | 2.6 |

## 项目结构

```
campus-competition/
├── comprtition-backend/          # 后端 Spring Boot 项目
│   └── src/main/java/com/campus/competition/
│       ├── common/               # 公共模块（配置、异常、工具类）
│       ├── controller/           # 控制器层
│       ├── entity/               # 实体类（PO/DTO/VO）
│       ├── mapper/               # MyBatis-Plus Mapper
│       └── service/              # 业务服务层
├── src/                          # 前端 React 项目
│   ├── components/               # 公共组件（Navbar、Modal、Toast）
│   ├── pages/                    # 页面组件
│   ├── data/                     # Mock 数据
│   └── utils/                    # 工具函数
└── public/                       # 静态资源
```

## 快速开始

### 环境要求

- Node.js 18+
- JDK 17+
- MySQL 8.0+
- Maven 3.8+

### 1. 数据库配置

创建数据库并执行初始化脚本：

```sql
CREATE DATABASE IF NOT EXISTS competition DEFAULT CHARACTER SET utf8mb4;
```

项目启动时会自动执行 `schema.sql` 创建表结构。

### 2. 后端启动

```bash
cd comprtition-backend

# 修改 application.yml 中的数据库连接信息
# spring.datasource.username 和 spring.datasource.password

# 启动后端服务
./mvnw spring-boot:run
```

后端服务运行在 `http://localhost:8080`，Swagger 文档地址：`http://localhost:8080/swagger-ui.html`

### 3. 前端启动

```bash
# 在项目根目录安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`

### 4. 构建部署

```bash
# 前端构建
npm run build

# 后端打包
cd comprtition-backend
./mvnw package -DskipTests
```

## 页面路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 竞赛列表浏览 |
| `/competition/:id` | 竞赛详情 | 查看竞赛详细信息 |
| `/team` | 组队广场 | 浏览组队招募 |
| `/team/:id` | 队伍详情 | 查看队伍详情 |
| `/publish` | 发布组队 | 发布组队需求 |
| `/ai-match` | AI 匹配 | 智能匹配推荐 |
| `/mine` | 个人中心 | 个人信息管理 |

## 数据库表

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（用户名、密码、角色等） |
| `competition` | 竞赛表（名称、时间、地点、主办方等） |
| `registration` | 报名记录表（用户-竞赛关联、团队信息） |
