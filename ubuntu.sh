#!/usr/bin/env bash
echo "Instalando estructura basica para clase virtualhost y proxy reverso"

# Habilitando la memoria de intercambio.
sudo dd if=/dev/zero of=/swapfile count=2048 bs=1MiB
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
sudo cp /etc/fstab /etc/fstab.bak
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Instalando los software necesarios
sudo apt update && sudo apt -y install apache2 certbot python3-certbot-apache unzip zip
sudo a2enmod ssl
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo systemctl restart apache2

# Instalando sdkman y Java
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.3-tem

sudo apt-get update
sudo apt-get install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# Subiendo el servicio de Apache
sudo service apache2 start

# Clonando el proyecto
cd ~/
cd ~/ArSpring

# Configuración de VirtualHost
sudo bash -c 'cat > /etc/apache2/sites-available/seguro.conf << EOF
<VirtualHost *:80>
    ServerName arblocks.do
    Redirect permanent / https://arblocks.do
</VirtualHost>

<VirtualHost *:443>
    ServerName arblocks.do

    DocumentRoot /var/www/html

    SSLEngine on
    SSLCertificateFile /etc/letsencrypt/live/arblocks.do/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/arblocks.do/privkey.pem

    ProxyPass / http://localhost:8080/
    ProxyPassReverse / http://localhost:8080/

    <Directory "/var/www/html">
        AllowOverride All
        Require all granted
    </Directory>
</VirtualHost>
EOF'

sudo a2ensite seguro.conf
sudo systemctl reload apache2

# Certificado SSL
DOMINIO="arblocks.do"
EMAIL="axrg0002@ce.pucmm.edu.do"

sudo systemctl stop apache2
if [ ! -f "/etc/letsencrypt/live/$DOMINIO/cert.pem" ]; then
    echo "Generando certificado SSL para $DOMINIO..."
    sudo certbot certonly --standalone -m $EMAIL -d $DOMINIO --agree-tos --non-interactive
else
    echo "Certificado para $DOMINIO ya existe."
fi

# Configuración de VirtualHost (AFTER certs exist)
sudo bash -c 'cat > /etc/apache2/sites-available/seguro.conf << EOF
<VirtualHost *:80>
    ServerName arblocks.do
    Redirect permanent / https://arblocks.do
</VirtualHost>

<VirtualHost *:443>
    ServerName arblocks.do

    DocumentRoot /var/www/html

    SSLEngine on
    SSLCertificateFile /etc/letsencrypt/live/arblocks.do/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/arblocks.do/privkey.pem

    ProxyPass / http://localhost:8080/
    ProxyPassReverse / http://localhost:8080/

    <Directory "/var/www/html">
        AllowOverride All
        Require all granted
    </Directory>
</VirtualHost>
EOF'

sudo a2ensite seguro.conf
sudo systemctl start apache2
sudo systemctl reload apache2

cd ~/ArSpring
git pull
chmod +x gradlew

# 🔹 Ejecutar creación del fat JAR con la variable de entorno
./gradlew bootJar

# Run the Spring Boot app (ensure port matches Apache Proxy)
java -jar build/libs/ArSpring-0.0.1-SNAPSHOT.jar > build/libs/salida.txt 2> build/lib
s/error.txt &
