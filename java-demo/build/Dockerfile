FROM debian:12-slim
LABEL maintainer="734839030@qq.com"

# 必须安装：下载+解压JDK需要
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates wget tar \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
    
# 安装 OpenJDK 25（官方二进制包）
RUN set -eux; \
    JDK_URL="https://download.java.net/java/GA/jdk25.0.2/b1e0dfa218384cb9959bdcb897162d4e/10/GPL/openjdk-25.0.2_linux-x64_bin.tar.gz"; \
    JAVA_HOME="/usr/local/jdk25.0.2"; \
    mkdir -p $JAVA_HOME; \
    wget -q -O /tmp/jdk.tar.gz $JDK_URL; \
    tar -zxf /tmp/jdk.tar.gz -C $JAVA_HOME --strip-components 1; \
    rm -f /tmp/jdk.tar.gz

ARG MODULE_NAME
WORKDIR /data/${MODULE_NAME}
# see /var/lib/docker/volumes/
VOLUME ["/data/${MODULE_NAME}"]

# for shell to choose environment
ENV IN_CONTAINER=true
# just src content
COPY target/${MODULE_NAME} /data/${MODULE_NAME}/

EXPOSE 8080
ENTRYPOINT ["bin/docker-entrypoint.sh"]
CMD ["start"]