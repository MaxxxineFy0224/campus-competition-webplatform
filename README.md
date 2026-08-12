# 校园竞赛组队平台

一个面向在校大学生的竞赛信息浏览与组队平台，提供竞赛推荐、组队招募、AI 竞赛助手等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Tailwind CSS + Vue Router |
| 后端 | Spring Boot 3.3 + MyBatis-Plus |
| 数据库 | MySQL 8.0（生产）/ H2（开发） |
| 认证 | JWT（jjwt） |
| AI | 硅基流动 API（DeepSeek-R1 模型，SSE 流式响应） |
| 部署 | Docker Compose（Nginx + Spring Boot + MySQL） |

## 功能模块

- **用户系统**：注册 / 登录、JWT 认证、个人信息管理
- **竞赛浏览**：竞赛列表、分类筛选、详情查看、收藏
- **组队招募**：发布组队帖、浏览招募信息、申请加入
- **申请管理**：队长审核申请（通过 / 拒绝）、申请状态追踪
- **评论互动**：组队帖评论区，支持多级回复、点赞
- **消息通知**：系统通知、申请结果通知、评论通知
- **AI 助手**：基于 DeepSeek 的竞赛推荐与组队建议（SSE 流式输出）

## 项目结构

```
competition-platform/
├── competition-backend/          # Spring Boot 后端
│   ├── src/main/java/com/jingdui/
│   │   ├── controller/           # REST 控制器
│   │   ├── service/              # 业务逻辑层
│   │   ├── mapper/               # MyBatis-Plus Mapper
│   │   ├── entity/               # 数据实体
│   │   ├── dto/                  # 数据传输对象
│   │   ├── config/               # 配置类
│   │   ├── security/             # JWT 认证与拦截器
│   │   └── common/               # 公共类（异常、统一响应）
│   └── src/main/resources/
│       ├── application.yml       # 默认配置（H2 开发环境）
│       ├── application-mysql.yml # MySQL 生产环境配置
│       └── schema.sql            # 建表脚本
├── competition-frontend/         # Vue 3 前端
│   └── src/
│       ├── pages/                # 页面组件
│       ├── components/           # 通用组件
│       ├── composables/          # 组合式函数
│       ├── router/               # 路由配置
│       └── utils/                # 工具函数（API、存储）
├── docker-compose.yml            # Docker 编排
├── nginx.conf                    # Nginx 配置
└── .env.example                  # 环境变量模板
```

## 快速开始

### 前置要求

- JDK 17+
- Node.js 20+
- Maven 3.9+
- MySQL 8.0（生产环境）
- Docker & Docker Compose（容器部署）

### 本地开发

**1. 克隆项目**

```bash
git clone https://github.com/MaxxxineFy0224/campus-competition-webplatform.git
cd campus-competition-webplatform
```

**2. 启动后端**

默认使用 H2 内存数据库，无需额外配置即可启动：

```bash
cd competition-backend
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8080`，H2 控制台可通过 `http://localhost:8080/h2-console` 访问。

**3. 启动前端**

```bash
cd competition-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求自动代理到后端。

### Docker 部署

```bash
# 1. 复制环境变量配置
cp .env.example .env
# 编辑 .env，填写数据库密码、JWT 密钥、AI API Key

# 2. 启动所有服务
docker compose up -d

# 3. 查看运行状态
docker compose ps

# 4. 查看日志
docker compose logs -f backend
```

启动后访问 `http://localhost` 即可使用。

## 环境变量

| 变量 | 说明 | 必需 |
|------|------|------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | 是 |
| `JWT_SECRET` | JWT 签名密钥（至少 32 字符） | 是 |
| `OPENAI_API_KEY` | 硅基流动 API Key | 是（使用 AI 功能时） |
| `MYSQL_PORT` | MySQL 端口，默认 3306 | 否 |
| `BACKEND_PORT` | 后端端口，默认 8080 | 否 |
| `FRONTEND_PORT` | 前端端口，默认 80 | 否 |
| `CORS_ALLOWED_ORIGINS` | 跨域允许来源 | 否 |

## API 概览

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `POST /api/auth/register` | 用户注册 |
| 认证 | `POST /api/auth/login` | 用户登录 |
| 用户 | `GET/PUT /api/users` | 获取/更新个人信息 |
| 竞赛 | `GET /api/competitions` | 竞赛列表（支持筛选） |
| 竞赛 | `GET /api/competitions/{id}` | 竞赛详情 |
| 竞赛 | `POST /api/competitions/{id}/favorite` | 收藏/取消收藏 |
| 组队 | `GET /api/team-posts` | 组队帖列表 |
| 组队 | `POST /api/team-posts` | 发布组队帖 |
| 组队 | `GET /api/team-posts/{id}` | 组队帖详情 |
| 申请 | `POST /api/team-applications` | 提交组队申请 |
| 申请 | `PUT /api/team-applications/{id}/review` | 审核申请 |
| 评论 | `GET/POST /api/team-posts/{id}/comments` | 评论列表/发表评论 |
| 通知 | `GET /api/notifications` | 通知列表 |
| AI | `POST /api/chat/stream` | AI 流式对话（SSE） |

## License

MIT