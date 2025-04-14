package app.servicios;

import app.entidades.ShortUrl;
import app.entidades.Visita;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

public class VisitaServices extends MongoGestionDb<Visita> {

    private static VisitaServices instancia;

    private VisitaServices(){
        super(Visita.class, "Visita");
    }

    public static VisitaServices getInstance(){
        if(instancia==null){
            instancia = new VisitaServices();
        }
        return instancia;
    }

    public List<Visita> findAllByShortUrl(String id) {
        List<Visita> list = new ArrayList<>();
        collection.find(Visita.class)
                .filter(Filters.eq("shortUrl._id", id))
                .into(list);
        return list;
    }


}
