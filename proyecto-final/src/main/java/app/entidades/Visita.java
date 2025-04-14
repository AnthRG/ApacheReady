package app.entidades;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Visita")
public class Visita {
    @BsonId
    private String id; // Puede generarse automáticamente
    private LocalDateTime fechaAcceso;
    private String ip;
    private String browser;
    private String sistemaOperativo;
    private String clientDomain;
    @DBRef
    private ShortUrl shortUrl;


    public Visita(String id, String ip, String browser, String sistemaOperativo, String clientDomain, ShortUrl shortUrl) {
        this.id = id;
        this.fechaAcceso = LocalDateTime.now();
        this.ip = ip;
        this.browser = browser;
        this.sistemaOperativo = sistemaOperativo;
        this.clientDomain = clientDomain;
        this.shortUrl = shortUrl;
    }

    public Visita() {}

    public ShortUrl getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(ShortUrl shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFechaAcceso() {
        return fechaAcceso;
    }

    public void setFechaAcceso(LocalDateTime fechaAcceso) {
        this.fechaAcceso = fechaAcceso;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    public void setSistemaOperativo(String sistemaOperativo) {
        this.sistemaOperativo = sistemaOperativo;
    }

    public String getClientDomain() {
        return clientDomain;
    }

    public void setClientDomain(String clientDomain) {
        this.clientDomain = clientDomain;
    }
}
