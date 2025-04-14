package app.entidades;

import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "Usuario")
public class Usuario {
    @BsonId
    private String username;
    private String nombre;
    private String password;
    private boolean administrador;
    // If Foto is stored in a separate collection, use @DBRef to create a reference.
    // Otherwise, if you want to embed Foto, simply remove the @DBRef annotation.
    @DBRef
    private Foto foto;

    public Usuario(String username, String nombre, String password, boolean administrador) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.administrador = administrador;
        this.foto = null;
    }

    public Usuario(String username, String nombre, String password, boolean administrador, Foto foto) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.administrador = administrador;
        this.foto = foto;
    }

    public Usuario() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdministrador() {
        return administrador;
    }

    public void setAdministrador(boolean administrador) {
        this.administrador = administrador;
    }


    public Foto getFoto() {
        return foto;
    }

    public void setFoto(Foto foto) {
        this.foto = foto;
    }

}
