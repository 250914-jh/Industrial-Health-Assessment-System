工业设备健康状态评估系统
基于 Spring Boot + Vue 3 + 决策树（Smile ML）的工业设备健康状态评估与故障预警系统。

对应《工业互联网实施与运维综合实训》课题：工业设备健康状态评估系统（数据采集 + 决策树）。

目录结构
industrial-health-system/
├── backend/         # Spring Boot 3 + Java 17 + Smile ML
│   ├── pom.xml
│   ├── src/main/java/com/industrial/health/...
│   ├── src/main/resources/application.yml
│   └── sample-data/equipment_health.csv
├── frontend/        # Vue 3 + Vite + Element Plus + ECharts + Pinia
│   ├── package.json
│   └── src/...
└── README.md
一、技术栈
层	技术
前端	Vue 3 + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios
后端	Spring Boot 3.2 + Spring Security + JWT + Spring Data JPA
数据库	H2（内存，零配置；可改 MySQL）
机器学习	Smile ML 3.0（CART 决策树）
构建	Maven / npm
二、运行步骤
1. 启动后端
需要 JDK 17+ 与 Maven 3.8+。

cd backend
mvn spring-boot:run
启动后：

后端地址：http://localhost:8080
H2 控制台：http://localhost:8080/h2-console （JDBC URL：jdbc:h2:mem:healthdb）
默认账号：admin / admin123
2. 启动前端
需要 Node 18+。

cd frontend
npm install
npm run dev
打开 http://localhost:5173 ，用默认账号登录。

三、使用流程
登录系统（JWT 鉴权）
数据采集 → 上传 CSV（默认列：temperature,vibration,current,rotation_speed,label，可在 application.yml 改）
模型训练：一键基于历史数据训练决策树
健康评估 / 预警：输入实时设备参数 → 模型预测健康状态（正常 / 警告 / 故障）→ 异常自动写入预警表
可视化大屏：传感器趋势曲线、预警分布、设备健康分布、特征重要性
四、CSV 数据集格式
temperature,vibration,current,rotation_speed,label
65.2,0.21,12.4,1480,normal
82.6,0.55,18.7,1320,warning
95.1,0.92,24.3,1100,fault
...
label 取值：normal / warning / fault
仓库已附 backend/sample-data/equipment_health.csv 作演示数据
如果你的数据列名不同，改 application.yml 中 health.feature-columns 与 health.label-column
五、核心接口
方法	路径	说明
POST	/api/auth/login	登录获取 JWT
POST	/api/data/upload	上传 CSV 数据集
GET	/api/data/list	分页查询采集数据
POST	/api/model/train	训练决策树
GET	/api/model/info	查看模型信息（节点数、准确率、特征重要性）
POST	/api/predict	实时预测单条样本
GET	/api/alerts	查询预警列表
GET	/api/dashboard/stats	大屏统计数据
六、决策树算法说明
使用 Smile ML 的 smile.classification.DecisionTree（CART 算法，Gini 不纯度）
训练时按 8:2 切分训练集 / 测试集，计算准确率
输出每个特征的重要性（基于分裂时的不纯度下降）
预测概率最高的类别若为 warning 或 fault，自动写入预警表并标注等级
完成本工程可对照实训手册「五、实训内容」的四项要求：

✅ 系统总体方案设计
✅ 传感器数据采集（CSV 导入）
✅ 决策树智能预测与可视化
✅ 系统联调与综合测试（前后端联调）
