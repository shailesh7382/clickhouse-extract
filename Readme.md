To install ClickHouse on macOS without using Homebrew, follow these steps:

1. **Download ClickHouse**:
   ```sh
   curl -O https://builds.clickhouse.com/master/macos/clickhouse
   ```

2. **Make the binary executable**:
   ```sh
   chmod +x clickhouse
   ```

3. **Move the binary to `/usr/local/bin`**:
   ```sh
   sudo mv clickhouse /usr/local/bin/
   ```

4. **Create the directory for the configuration file**:
   ```sh
   sudo mkdir -p /usr/local/etc/clickhouse-server
   ```

5. **Download the default configuration file**:
   ```sh
   sudo curl -o /usr/local/etc/clickhouse-server/config.xml https://raw.githubusercontent.com/ClickHouse/ClickHouse/master/programs/server/config.xml
   ```

6. **Start ClickHouse Server**:
   ```sh
   clickhouse server --config-file=/usr/local/etc/clickhouse-server/config.xml
   ```

7. **Connect to ClickHouse Client**:
   ```sh
   clickhouse client
   ```

cd /usr/local/etc/clickhouse-server
sudo curl -o config.xml https://raw.githubusercontent.com/ClickHouse/ClickHouse/master/programs/server/config.xml
sudo chmod 777 config.xml
cd ~/IdeaProjects/clickhouse-extract/
ln -s /usr/local/etc/clickhouse-server/config.xml config.xml
sudo curl -o config-default.xml https://raw.githubusercontent.com/ClickHouse/ClickHouse/master/programs/server/config.xml


sudo mkdir -p  /etc/clickhouse-server/
sudo touch users.xml
sudo chmod 777 users.xml
vi users.xml
cd ~/IdeaProjects/clickhouse-extract/
ln -s /etc/clickhouse-server/users.xml users.xml

rename clickhouse data directory 


Port Availability: Verify that port 9100 is not being used by another process. You can use the following command to check if the port is in use:
lsof -i :9100


Check Listening Ports: Verify that ClickHouse is listening on port 9100. You can use the following command to check:
netstat -an | grep 9100