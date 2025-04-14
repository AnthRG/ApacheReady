package app.servicios;

import app.entidades.Foto;
import app.entidades.Usuario;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class UsuarioServices extends MongoGestionDb<Usuario> {

    private static UsuarioServices instancia;

    private UsuarioServices(){
        super(Usuario.class, "Usuario");
    }

    public static UsuarioServices getInstance(){
        if(instancia==null){
            instancia = new UsuarioServices();
        }
        return instancia;
    }


}
