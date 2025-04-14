package app.servicios;

import org.jasypt.util.text.BasicTextEncryptor;

public class EncriptarUser {
    private static final String CLAVE_SECRETA = "123";
    public static String encriptar(String texto) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(CLAVE_SECRETA);
        return encryptor.encrypt(texto);
    }

    public static String desencriptar(String textoEncriptado) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(CLAVE_SECRETA);
        return encryptor.decrypt(textoEncriptado);
    }
}
