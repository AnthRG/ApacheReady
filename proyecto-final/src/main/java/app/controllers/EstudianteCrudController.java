package app.controllers;

import app.entidades.Estudiante;
import app.entidades.Usuario;
import app.servicios.EstudianteServices;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstudianteCrudController {
    public static void listar(@NotNull Context ctx) throws Exception {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(5);

        List<Estudiante> lista = EstudianteServices.getInstance().findAllPaginated(page, size);
        long totalEstudiantes = EstudianteServices.getInstance().countEstudiantes();
        int totalPages = (int) Math.ceil((double) totalEstudiantes / size);

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Encuestados");
        ctx.attribute("currentPage", page);
        ctx.attribute("totalPages", totalPages);

        modelo.put("estudiantes", lista);
        ctx.render("/crud-tradicional/listarEstudiante.html", modelo);
    }

    public static void crearEstudianteForm(@NotNull Context ctx) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Creación Estudiante");
        modelo.put("accion", "/crud-estudiante/crear");
        ctx.render("/crud-tradicional/CrudEstudiante.html", modelo);
    }

    public static void procesarCreacionEstudiante(@NotNull Context ctx) throws Exception {
        String nombre = ctx.formParam("nombre");
        String sector = ctx.formParam("sector");
        String nivelEscolar = ctx.formParam("nivelEscolar");
        Usuario user = ctx.sessionAttribute("USUARIO");
        double latitud = 0;
        double longitud = 0;
        try{
            latitud = Double.parseDouble(ctx.formParam("latitud"));
            longitud = Double.parseDouble(ctx.formParam("longitud"));

        }catch(Exception e){
            System.out.println("No tienen ubicacion activada");
        }

        Estudiante estudiante = new Estudiante(nombre, sector, nivelEscolar, user, longitud, latitud);
        EstudianteServices.getInstance().crear(estudiante);
        ctx.redirect("/crud-estudiante/crear");
    }

    public static void listarUbicaciones(@NotNull Context ctx) throws Exception {
        List<Estudiante> lista = EstudianteServices.getInstance().findAll();

        ctx.json(lista); // Devuelve la lista en formato JSON
    }
    public static void obtenerEstudiante(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        Estudiante estudiante = EstudianteServices.getInstance().find(id);

        if (estudiante != null) {
            ctx.json(estudiante);
        } else {
            ctx.status(404).result("Estudiante no encontrado");
        }
    }

}
