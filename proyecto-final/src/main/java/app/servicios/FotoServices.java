package app.servicios;



import app.entidades.Foto;

/**
 *
 */
public class FotoServices extends MongoGestionDb<Foto> {

    private static FotoServices instancia;

    private FotoServices(){
        super(Foto.class, "Foto");
    }

    public static FotoServices getInstancia(){
        if(instancia==null){
            instancia = new FotoServices();
        }
        return instancia;
    }

}
