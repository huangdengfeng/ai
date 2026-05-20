# 安装
docker run --name mysql8 --network host -e MYSQL_ROOT_PASSWORD=12345678 -d mysql:8.4.9
# 导入 sql 文件 → 必须用 -i
docker exec -i mysql8 sh -c 'exec mysql -uroot -p12345678' < all.sql