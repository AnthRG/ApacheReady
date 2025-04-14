package app.entidades;

import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "Estudiante")
public class Estudiante {
    @BsonId
    private long id;

    private String nombre;
    private String sector;
    private String nivelEscolar;
    private Usuario user;

    private Double longitud;
    private Double latitud;

    public Estudiante() {

    }

    public Estudiante(String nombre, String sector, String nivelEscolar, Usuario user, Double longitud, Double latitud) {
        this.nombre = nombre;
        this.sector = sector;
        this.nivelEscolar = nivelEscolar;
        this.user = user;
        this.longitud = longitud;
        this.latitud = latitud;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getNivelEscolar() {
        return nivelEscolar;
    }

    public void setNivelEscolar(String nivelEscolar) {
        this.nivelEscolar = nivelEscolar;
    }

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
