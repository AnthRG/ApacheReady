package app.servicios;

import app.entidades.Estudiante;

import java.util.List;

public class EstudianteServices extends MongoGestionDb<Estudiante>{
    public EstudianteServices(){super(Estudiante.class, "Estudiante");}
    private static EstudianteServices instancia;


    public static EstudianteServices getInstance(){
        if(instancia==null){
            instancia = new EstudianteServices();
        }
        return instancia;
    }

    public List<Estudiante> findAllByNivel(String nivelEscolar){
        /*EntityManager em = getEntityManager();
        Query query = em.createQuery("select e from Estudiante e where e.nivelEscolar like :nivelEscolar");
        query.setParameter("nivelEscolar", nivelEscolar + "%");
        return query.getResultList();*/
        return null;
    }

    public List<Estudiante> findAllPaginated(int page, int size) {
        /*EntityManager em = getEntityManager();
        Query query = em.createQuery("SELECT e FROM Estudiante e");
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();*/
        return null;
    }

    public long countEstudiantes() {
        /*EntityManager em = getEntityManager();
        Query query = em.createQuery("SELECT COUNT(e) FROM Estudiante e");
        return (long) query.getSingleResult();*/
        return 0;
    }



}
