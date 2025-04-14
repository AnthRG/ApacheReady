package app.controllers;

import app.entidades.Foto;
import app.entidades.Usuario;
import app.servicios.FotoServices;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class FotoController {

    public static void listarFotos(@NotNull Context ctx) throws Exception {
        List<Foto> fotos = FotoServices.getInstancia().findAll();

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Ejemplo de funcionalidad Thymeleaf");
        modelo.put("fotos", fotos);

        //
        ctx.render("/foto/listar.html", modelo);
    }

    public static void procesarFotos(@NotNull Context ctx) throws Exception {
        ctx.uploadedFiles("foto").forEach(uploadedFile -> {
            try {
                byte[] bytes = uploadedFile.content().readAllBytes();
                String encodedString = Base64.getEncoder().encodeToString(bytes);
                Usuario user = ctx.sessionAttribute("USUARIO");
                Foto foto = new Foto(uploadedFile.filename(), uploadedFile.contentType(), encodedString);
                FotoServices.getInstancia().crear(foto);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctx.redirect("/fotos/listar");
        });
    }

    public static void visualizarFotos(@NotNull Context ctx) throws Exception {
        try {
            Foto foto = FotoServices.getInstancia().find(ctx.pathParamAsClass("id", Long.class).get());
            if(foto==null){
                ctx.redirect("/fotos/listar");
                return;
            }
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("foto", foto);
            //
            ctx.render("/foto/visualizar.html", modelo);
        }catch (Exception e){
            System.out.println("Error: "+e.getMessage());
            ctx.redirect("/fotos/listar");
        }
    }

    public static void eliminarFotos(@NotNull Context ctx) throws Exception {
        try {
            Foto foto = FotoServices.getInstancia().find(ctx.pathParamAsClass("id", Long.class).get());
            if(foto!=null){
                FotoServices.getInstancia().eliminar(foto.getId());
            }
        }catch (Exception e){
            System.out.println("Error: "+e.getMessage());
        }
        ctx.redirect("/fotos/listar");
    }


}
