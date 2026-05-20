#!/bin/bash
set -e
# 不指定这获取环境中java命令
JAVA_HOME=/usr/local/jdk-25.0.2

MAIN_CLASS="com.seezoon.MainApplication"
# 可加入自定的目录
#CLASS_PATH=""
LOG_PATH="./logs"

#JVM_MEM="-Xmx512m -Xms128m"
if [ "${IN_CONTAINER}" = true ]; then
  JVM_MEM="-XX:+UseContainerSupport -XX:InitialRAMPercentage=10.0 -XX:MaxRAMPercentage=80.0"
fi
#JVM_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
JVM_ARGS="-XX:+UseStringDeduplication -XX:+UseG1GC -Xlog:gc*,safepoint=info:./logs/gc.log:time,uptime,level,tags:filecount=5,filesize=100M -XX:+HeapDumpOnOutOfMemoryError -XX:+ExitOnOutOfMemoryError -XX:HeapDumpPath=${LOG_PATH}/dump -XX:ErrorFile=${LOG_PATH}/hs_err_%p.log"
#优雅关机等待时间
SHUTDOWN_SECONDS=30
SERVER_OTPS="--spring.profiles.active=cli"
# export for java properties
export LOG_PATH

export JAVA_TOOL_OPTIONS="-javaagent:./bin/opentelemetry-javaagent.jar"

# ========== OTEL配置 ==========
# https://opentelemetry.io/docs/languages/java/configuration/#properties-exporters
# 服务名称
export OTEL_SERVICE_NAME=admin-server
# 资源属性（可选）
export OTEL_RESOURCE_ATTRIBUTES="service.name=${OTEL_SERVICE_NAME},service.version=1.0.0"
# 认证 Token
export OTEL_EXPORTER_OTLP_HEADERS="Authentication=e8dpm1gbu5@52547a3d1950a34_e8dpm1gbu5@53df7ad2afe8301"

# 统一端点（优先级低）
# 阿里云https://trace.console.aliyun.com/#/overview/cn-shenzhen?from=now-3h&to=now&refresh=off
export OTEL_EXPORTER_OTLP_ENDPOINT=http://tracing-analysis-dc-sz.aliyuncs.com:8090

# 专用端点（优先级高，会覆盖统一配置）
#export OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://traces.example.com:4317
#export OTEL_EXPORTER_OTLP_METRICS_ENDPOINT=https://metrics.example.com:4317
#export OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://logs.example.com:4317

# Exporter 配置，none 不上报任何数据,调试console日志中保留trace_id和span_id
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
# 调试
# export OTEL_TRACES_EXPORTER=none
# export OTEL_METRICS_EXPORTER=none
# export OTEL_LOGS_EXPORTER=console
# 正式
# export OTEL_TRACES_EXPORTER=otlp
# export OTEL_METRICS_EXPORTER=otlp
# export OTEL_LOGS_EXPORTER=otlp

# ========== 采样配置 ==========
# 采样器类型: always_on(全采), always_off(不采), traceidratio(按比例), parentbased_traceidratio(推荐)
export OTEL_TRACES_SAMPLER=parentbased_traceidratio
# 采样比例: 0.0-1.0，如 0.1 表示采样 10%，推荐0.1
export OTEL_TRACES_SAMPLER_ARG=1.0

# ========== 批量发送配置 ==========
# Trace 批量发送间隔（毫秒），默认 5000
export OTEL_BSP_SCHEDULE_DELAY=5000
# Trace 批量发送最大数量，默认 512
export OTEL_BSP_MAX_EXPORT_BATCH_SIZE=512
# Trace 队列最大长度，默认 2048
export OTEL_BSP_MAX_QUEUE_SIZE=2048
# Metrics 上报间隔（毫秒），默认 60000
export OTEL_METRIC_EXPORT_INTERVAL=60000

# ========== 其他常用配置 ==========
# 传播器: tracecontext,baggage,b3,b3multi,jaeger,xray,ottrace,默认tracecontext,baggage
export OTEL_PROPAGATORS=tracecontext,baggage
# 禁用特定
# 总开关
#export OTEL_INSTRUMENTATION_COMMON_DISABLED=true
# jdbc
#export OTEL_INSTRUMENTATION_JDBC_ENABLED=true
# 超时时间（毫秒）
export OTEL_EXPORTER_OTLP_TIMEOUT=10000