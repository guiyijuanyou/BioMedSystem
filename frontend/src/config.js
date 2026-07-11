export const modules = {
  herbs: {
    title: "种植基地与资源点",
    hint: "维护地图资源点、种植基地位置、生态环境和基础溯源码",
    fields: [
      ["name", "品种名称"],
      ["district", "分布区县"],
      ["longitude", "经度"],
      ["latitude", "纬度"],
      ["scale", "种植规模"],
      ["environment", "生态环境"],
      ["traceCode", "溯源码"]
    ]
  },
  "herb-batches": {
    title: "药材批次管理",
    hint: "建立种植、采收或采购批次，作为生长、溯源、检测、分析和评价的数据主线",
    fields: [
      ["batchName", "批次名称"],
      ["batchCode", "批次编号"],
      ["herbId", "来源资源点"],
      ["traceCode", "溯源码"],
      ["plotName", "地块或基地"],
      ["district", "所属区县"],
      ["longitude", "经度"],
      ["latitude", "纬度"],
      ["scale", "种植规模"],
      ["environment", "生态环境"],
      ["plantingDate", "种植日期"],
      ["expectedHarvestDate", "预计采收日期"],
      ["responsiblePerson", "负责人"],
      ["currentStage", "当前阶段"],
      ["status", "档案状态"]
    ]
  },
  "lab-samples": {
    title: "检测样本管理",
    hint: "管理从药材批次抽取的检测样本，为图谱比对提供稳定样本身份",
    fields: [
      ["sampleCode", "样本编号"],
      ["batchId", "来源批次"],
      ["sampleType", "样本类型"],
      ["collectedAt", "采样时间"],
      ["collector", "采样人"],
      ["sampleLocation", "采样位置"],
      ["storageCondition", "保存条件"],
      ["status", "样本状态"]
    ]
  },
  "growth-records": {
    title: "生长数据采集",
    hint: "APP、传感器和电脑终端采集记录",
    fields: [
      ["batchId", "药材批次"],
      ["herbName", "药材名称"],
      ["district", "采集地点"],
      ["temperature", "温度"],
      ["humidity", "湿度"],
      ["soilPh", "土壤 PH"],
      ["growthStage", "生长阶段"],
      ["collectSource", "采集来源"],
      ["recorder", "采集人"],
      ["recorderRole", "采集角色"],
      ["recordedAt", "采集时间"]
    ]
  },
  "trace-events": {
    title: "溯源管理",
    hint: "按溯源码记录药材种植、采集、检测、加工、入库等流转事件",
    fields: [
      ["batchId", "药材批次"],
      ["herbName", "药材名称"],
      ["traceCode", "溯源码"],
      ["eventType", "事件类型"],
      ["eventContent", "事件内容"],
      ["operatorName", "操作人"],
      ["eventTime", "事件时间"],
      ["location", "发生地点"]
    ]
  },
  "teaching-resources": {
    title: "教学视频与资料",
    hint: "教师和科研人员上传视频、课件、图片等资料，管理员审核后发布给学生学习",
    fields: [
      ["title", "资源标题"],
      ["resourceType", "资源类型"],
      ["courseTitle", "所属课程"],
      ["uploader", "上传人"],
      ["uploaderRole", "上传角色"],
      ["status", "审核状态"],
      ["reviewComment", "审核意见"],
      ["fileId", "资料文件"],
      ["publishedAt", "发布时间"]
    ]
  },
  "spectrum-comparisons": {
    title: "图谱比对",
    hint: "中药材图谱上传、参考图谱比对和相似度判定",
    fields: [
      ["sampleId", "检测样本"],
      ["batchId", "来源批次"],
      ["herbName", "药材名称"],
      ["sampleCode", "样本编号"],
      ["district", "采集区县"],
      ["spectrumType", "图谱类型"],
      ["referenceName", "参考图谱"],
      ["similarity", "相似度"],
      ["result", "比对结果"],
      ["operator", "操作人"],
      ["comparedAt", "比对时间"],
      ["remark", "备注"],
      ["status", "审核状态"]
    ]
  },
  "growth-analysis": {
    title: "数据对比分析",
    hint: "按批次、区县和生态指标对生长数据进行对比分析",
    fields: [
      ["analysisName", "分析名称"],
      ["batchId", "药材批次"],
      ["herbName", "药材名称"],
      ["district", "对比区县"],
      ["indicator", "分析指标"],
      ["baseline", "基准值"],
      ["currentValue", "当前值"],
      ["difference", "差异"],
      ["trend", "趋势判断"],
      ["conclusion", "分析结论"],
      ["analyst", "分析人"],
      ["analyzedAt", "分析时间"],
      ["status", "审核状态"]
    ]
  },
  courses: {
    title: "课程学习",
    hint: "学生查看教师发布的课程与教学视频",
    fields: [
      ["title", "课程名称"],
      ["teacher", "教师"],
      ["hours", "学时"],
      ["materialType", "资料类型"],
      ["status", "状态"]
    ]
  },
  projects: {
    title: "课题研究",
    hint: "教师或科研人员创建课题，管理员审核发布，学生申请加入后由负责人审批",
    fields: [
      ["title", "课题名称"],
      ["leader", "负责人"],
      ["requirements", "加入要求"],
      ["status", "审核状态"],
      ["stage", "研究阶段"],
      ["applicantRequests", "学生申请"],
      ["approvedMembers", "已加入成员"],
      ["rejectedApplicants", "已拒绝申请"],
      ["transformation", "成果转化"]
    ]
  },
  trainings: {
    title: "培训素材与跟踪",
    hint: "素材制作、培训过程、记录归档",
    fields: [
      ["title", "培训主题"],
      ["trainer", "培训人"],
      ["audience", "培训对象"],
      ["tracking", "过程记录"],
      ["status", "审核状态"]
    ]
  },
  evaluations: {
    title: "评价体系",
    hint: "评价过程、结果跟踪、申报素材",
    fields: [
      ["batchId", "药材批次"],
      ["herbName", "药材名称"],
      ["indicator", "评价指标"],
      ["score", "评分"],
      ["result", "评价结果"],
      ["applicationMaterial", "申报素材"],
      ["subjectOwner", "成果负责人"],
      ["evaluator", "评价人"],
      ["status", "审核状态"]
    ]
  },
  achievements: {
    title: "工作业绩管理",
    hint: "录入、审核、编辑、分级分类认定",
    fields: [
      ["title", "业绩名称"],
      ["owner", "所属单位"],
      ["category", "分类"],
      ["level", "级别"],
      ["status", "审核状态"]
    ]
  },
  standards: {
    title: "认定标准",
    hint: "学校业绩分类认定办法动态更新",
    fields: [
      ["name", "标准名称"],
      ["category", "适用分类"],
      ["levelRule", "分级规则"],
      ["effectiveDate", "生效日期"]
    ]
  },
  users: {
    title: "用户层级管理",
    hint: "管理员、教师、学生、科研人员分级授权",
    fields: [
      ["username", "登录账号"],
      ["password", "登录密码"],
      ["name", "用户名称"],
      ["role", "角色"],
      ["department", "部门"],
      ["level", "权限层级"]
    ]
  }
};

export const navItems = [
  ["dashboard", "总览"],
  ["herb-batches", "药材批次"],
  ["lab-samples", "检测样本"],
  ["growth-records", "生长采集"],
  ["trace-events", "溯源管理"],
  ["spectrum-comparisons", "图谱比对"],
  ["growth-analysis", "数据分析"],
  ["courses", "课程学习"],
  ["teaching-resources", "教学视频"],
  ["projects", "课题研究"],
  ["trainings", "培训素材"],
  ["evaluations", "评价体系"],
  ["achievements", "业绩管理"],
  ["standards", "认定标准"],
  ["users", "用户层级"],
  ["files", "资料文件"]
];

export const roles = {
  admin: { label: "管理员", title: "系统管理工作台" },
  teacher: { label: "教师", title: "教学工作台" },
  researcher: { label: "科研人员", title: "科研工作台" },
  student: { label: "学生", title: "学习首页" }
};

export const roleMenus = {
  admin: [
    ["dashboard", "工作台"],
    ["herbs", "种植资源点"],
    ["herb-batches", "药材批次"],
    ["lab-samples", "检测样本"],
    ["teaching-resources", "资源审核"],
    ["users", "用户管理"],
    ["growth-records", "生长数据"],
    ["trace-events", "溯源管理"],
    ["spectrum-comparisons", "图谱比对"],
    ["growth-analysis", "数据分析"],
    ["courses", "课程管理"],
    ["projects", "课题审核"],
    ["trainings", "培训素材"],
    ["evaluations", "评价体系"],
    ["achievements", "工作业绩"],
    ["standards", "认定标准"],
    ["files", "资料文件"]
  ],
  teacher: [
    ["dashboard", "教学工作台"],
    ["herbs", "种植资源点"],
    ["herb-batches", "药材批次"],
    ["lab-samples", "检测样本"],
    ["growth-records", "生长采集"],
    ["trace-events", "溯源管理"],
    ["spectrum-comparisons", "图谱比对"],
    ["growth-analysis", "数据分析"],
    ["courses", "试验课程"],
    ["teaching-resources", "视频资料"],
    ["projects", "课题研究"],
    ["trainings", "培训素材"],
    ["evaluations", "评价体系"],
    ["achievements", "工作业绩"],
    ["files", "资料文件"]
  ],
  researcher: [
    ["dashboard", "科研工作台"],
    ["herbs", "种植资源点"],
    ["herb-batches", "药材批次"],
    ["lab-samples", "检测样本"],
    ["growth-records", "生长数据"],
    ["trace-events", "溯源管理"],
    ["spectrum-comparisons", "图谱比对"],
    ["growth-analysis", "数据分析"],
    ["teaching-resources", "视频资料"],
    ["projects", "课题研究"],
    ["evaluations", "评价体系"],
    ["achievements", "工作业绩"],
    ["files", "资料文件"]
  ],
  student: [
    ["dashboard", "学习首页"],
    ["herbs", "资源点查询"],
    ["herb-batches", "药材批次"],
    ["courses", "课程学习"],
    ["teaching-resources", "教学视频"],
    ["growth-records", "生长采集"],
    ["trace-events", "溯源查询"],
    ["projects", "课题研究"],
    ["trainings", "培训素材"],
    ["files", "资料文件"]
  ]
};

export const roleModulePermissions = {
  admin: {
    default: { create: true, edit: true, duplicate: true, delete: true, export: true, batchDelete: true },
    "teaching-resources": { create: false, edit: true, duplicate: false, delete: true, export: true, batchDelete: true }
  },
  teacher: {
    default: { create: true, edit: true, duplicate: true, delete: true, export: true, batchDelete: true },
    users: { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false },
    standards: { create: false, edit: false, duplicate: false, delete: false, export: true, batchDelete: false }
  },
  researcher: {
    default: { create: true, edit: true, duplicate: true, delete: true, export: true, batchDelete: true },
    courses: { create: false, edit: false, duplicate: false, delete: false, export: true, batchDelete: false },
    trainings: { create: false, edit: false, duplicate: false, delete: false, export: true, batchDelete: false },
    users: { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false },
    standards: { create: false, edit: false, duplicate: false, delete: false, export: true, batchDelete: false }
  },
  student: {
    default: { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false },
    "growth-records": { create: true, edit: true, duplicate: false, delete: false, export: false, batchDelete: false },
    courses: { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false },
    "teaching-resources": { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false },
    "trace-events": { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false },
    projects: { create: false, edit: true, duplicate: false, delete: false, export: false, batchDelete: false },
    files: { create: false, edit: false, duplicate: false, delete: false, export: false, batchDelete: false }
  }
};
