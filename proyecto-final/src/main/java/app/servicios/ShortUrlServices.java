package app.servicios;

import app.entidades.Estudiante;
import app.entidades.ShortUrl;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

public class ShortUrlServices extends MongoGestionDb<ShortUrl> {
    public ShortUrlServices(){super(ShortUrl.class, "ShortUrl");}
    private static ShortUrlServices instancia;


    public static ShortUrlServices getInstance(){
        if(instancia==null){
            instancia = new ShortUrlServices();
        }
        return instancia;
    }

    public List<ShortUrl> findAllByUsername(String username) {
        List<ShortUrl> list = new ArrayList<>();
        collection.find(ShortUrl.class)
                .filter(Filters.eq("creador._id", username))
                .into(list);
        return list;
    }

    public List<ShortUrl> findAllBySessionID(String sessionId) {
        List<ShortUrl> list = new ArrayList<>();
        collection.find(ShortUrl.class)
                .filter(Filters.eq("sessionId", sessionId))
                .into(list);
        return list;
    }


}
