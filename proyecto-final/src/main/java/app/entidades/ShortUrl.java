package app.entidades;

import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "ShortUrl")
public class ShortUrl {
    @BsonId
    private String id;            // hash corto, puede ser generado con Base62, por ejemplo
    private String originalUrl;   // URL completa original
    private LocalDateTime fecha;
    private int visitCount;
    @DBRef
    Usuario creador;
    @DBRef
    private String qrCodeBase64;
    private String SessionId;


    public ShortUrl(String originalUrl, Usuario creador) {
        this.id = generateShortId();
        this.originalUrl = originalUrl;
        this.creador = creador;
        this.visitCount = 0;
        this.fecha = LocalDateTime.now();
        this.SessionId = null;
    }
    public ShortUrl(String id, String originalUrl, Usuario creador) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.creador = creador;
        this.visitCount = 0;
        this.fecha = LocalDateTime.now();
        this.SessionId = null;
    }


    public ShortUrl(String id, String originalUrl, Usuario creador, String SessionId) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.creador = creador;
        this.fecha = LocalDateTime.now();
        this.visitCount = 0;
        this.SessionId = SessionId;
    }







    public ShortUrl() {
    }

    public String getSessionId() {
        return SessionId;
    }

    public void setSessionId(String sessionId) {
        SessionId = sessionId;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }



    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void addVisitCount() {
        this.visitCount ++;
    }

    private static String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 6); // Mejor usar base62
    }

}
