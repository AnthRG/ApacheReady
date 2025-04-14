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

# Subiendo el servicio de Apache
sudo service apache2 start

# Clonando el proyecto
cd ~/
git clone https://github.com/AnthRG/ApacheReady.git
cd ~/ApacheReady/proyecto-final

# Configuración de VirtualHost
sudo bash -c 'cat > /etc/apache2/sites-available/seguro.conf << EOF
<VirtualHost *:80>
    ServerName suslinks.sus.codes
    Redirect permanent / https://suslinks.sus.codes
</VirtualHost>

<VirtualHost *:443>
    ServerName suslinks.sus.codes

    DocumentRoot /var/www/html

    SSLEngine on
    SSLCertificateFile /etc/letsencrypt/live/suslinks.sus.codes/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/suslinks.sus.codes/privkey.pem

    ProxyPass / http://localhost:7070/
    ProxyPassReverse / http://localhost:7070/

    <Directory "/var/www/html">
        AllowOverride All
        Require all granted
    </Directory>
</VirtualHost>
EOF'

sudo a2ensite seguro.conf
sudo systemctl reload apache2

# Certificado SSL
DOMINIO="suslinks.sus.codes"
EMAIL="axrg0002@ce.pucmm.edu.do"

sudo systemctl stop apache2
if [ ! -f "/etc/letsencrypt/live/$DOMINIO/cert.pem" ]; then
    echo "Generando certificado SSL para $DOMINIO..."
    sudo certbot certonly --standalone -m $EMAIL -d $DOMINIO --agree-tos --non-interactive
else
    echo "Certificado para $DOMINIO ya existe."
fi
sudo systemctl start apache2

cd ~/ApacheReady/proyecto-final
chmod +x gradlew

# 🔹 Ejecutar creación del fat JAR con la variable de entorno
export MONGODB_URL="mongodb+srv://axrg0002:zKtiFY0qtxu9vdhy@finalpro.jhwraki.mongodb.net/?retryWrites=true&w=majority&appName=FinalPro"
./gradlew shadowjar

# 🔹 Iniciar la app en segundo plano con la variable de entorno
nohup env MONGODB_URL="mongodb+srv://axrg0002:zKtiFY0qtxu9vdhy@finalpro.jhwraki.mongodb.net/?retryWrites=true&w=majority&appName=FinalPro" \
     java -jar build/libs/app.jar > build/libs/salida.txt 2> build/libs/error.txt &

# 🔹 Asegurar puerto 50051 esté accesible (gRPC)
echo "Asegurando acceso al puerto 50051 para gRPC..."
sudo ufw allow 50051/tcp  # O equivalente para tu entorno si no usas UFW
