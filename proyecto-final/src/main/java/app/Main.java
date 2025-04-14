package app;

import app.controllers.*;
import app.entidades.*;
import app.grpc.ShortUrlServiceImpl;
import app.servicios.*;
import app.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.json.JavalinJackson;
import io.javalin.rendering.template.JavalinThymeleaf;
import io.javalin.security.RouteRole;
import io.javalin.websocket.WsContext;
import jakarta.servlet.http.Cookie;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static io.javalin.apibuilder.ApiBuilder.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;


public class Main {
    private static Set<WsContext> wsClients = ConcurrentHashMap.newKeySet();


    public static void main(String[] args) throws IOException, InterruptedException {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");  // SIN la barra inicial "/templates/"
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(true); //if cache no worky worky try turn this off
        templateEngine.setTemplateResolver(resolver);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Crear la aplicación Javalin con el motor de plantillas
        var app = Javalin.create(config -> {
            config.staticFiles.add("/publico"); // Archivos estáticos
            config.fileRenderer(new JavalinThymeleaf(templateEngine)); // Configurar Thymeleaf
            config.jsonMapper(new JavalinJackson());


            config.router.apiBuilder(() -> {
                ApiBuilder.path("/crud-simple", () -> {
                    ApiBuilder.get("/", CrudUsuarioController::listar);
                    ApiBuilder.get("/crear", CrudUsuarioController::crearUsuarioForm);
                    post("/crear", CrudUsuarioController::procesarCreacionUsuario);
                    ApiBuilder.get("/visualizar/{username}", CrudUsuarioController::visualizarUsuario);
                    ApiBuilder.get("/editar/{username}", CrudUsuarioController::editarUsuarioForm);
                    post("/editar", CrudUsuarioController::procesarEditarUsuario);
                    ApiBuilder.get("/eliminar/{username}", CrudUsuarioController::eliminarUsuario);
                });

                ApiBuilder.path("/crud-url", () -> {
                    ApiBuilder.get("/", urlController::listar);
                    ApiBuilder.get("/crear", ctx -> ctx.redirect("/"));
                    ApiBuilder.get("/visualizar/{id}", urlController::expandir);
                    ApiBuilder.get("/eliminar/{id}", urlController::eliminar);

                });

                path("/fotos", () -> {
                    get(ctx -> {
                        ctx.redirect("/fotos/listar");
                    });
                    get("/listar", FotoController::listarFotos);
                    post("/procesarFoto", FotoController::procesarFotos);
                    get("/visualizar/{id}", FotoController::visualizarFotos);
                    get("/eliminar/{id}", FotoController::eliminarFotos);
                });

            });
        }).start(7070);

        app.get("/api/chart-data/{id}", ctx -> {
            String idUrl = ctx.pathParam("id");
            String period = period = "hour"; // Default value

            String dateStr = ctx.queryParam("date");
            List<Visita> visits = VisitaServices.getInstance().findAllByShortUrl(idUrl);

            // Process data based on period
            Map<String, Object> chartData = urlController.processChartData(visits, period, dateStr);

            // Return JSON response
            ctx.json(chartData);
        });

        app.get("/api/urls", ctx -> {
            // Obtener el token del header Authorization
            String authHeader = ctx.header("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).result("Authorization header missing or invalid");
                return;
            }

            String token = authHeader.substring(7); // Eliminar "Bearer "

            try {
                // Validar el token (y opcionalmente obtener el usuario)
                String username = JwtUtil.validateToken(token);

                // Recuperar las URLs del usuario
                List<ShortUrl> misUrl = ShortUrlServices.getInstance().findAllByUsername(username);

                Map<String, Object> response = new HashMap<>();
                response.put("urls", misUrl);
                ctx.json(response);

            } catch (Exception e) {
                ctx.status(401).result("Invalid or expired token");
            }
        });



        app.get("/api/vistas/{id}", ctx -> {
            String authHeader = ctx.header("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).result("Authorization header missing or invalid");
                return;
            }

            String token = authHeader.substring(7); // Remover "Bearer "

            try {
                // Validar el token (esto lanza excepción si es inválido o expirado)
                String username = JwtUtil.validateToken(token);

                // Obtener el ID de la URL acortada desde el path
                String shortUrlId = ctx.pathParam("id");

                // Aquí podrías verificar que esa URL pertenece al usuario si lo deseas

                List<Visita> visits = VisitaServices.getInstance().findAllByShortUrl(shortUrlId);
                Map<String, Object> response = new HashMap<>();
                response.put("visitas", visits);
                ctx.json(response);

            } catch (Exception e) {
                ctx.status(401).result("Invalid or expired token");
            }
        });



        app.post("/api/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            // Aquí deberías verificar el usuario y contraseña contra tu base de datos
            if (Loggin(username, password)) {
                String token = JwtUtil.generateToken(username);
                System.out.println(token);
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                ctx.json(response);
            } else {
                ctx.status(401).result("Credenciales inválidas");
            }
        });

        app.post("/api/refresh", ctx -> {
            String refresh = ctx.header("Authorization");

            if (refresh == null || !refresh.startsWith("Bearer ")) {
                ctx.status(401).result("Token inválido");
                return;
            }

            String token = refresh.substring(7);
            try {
                String username = JwtUtil.validateToken(token);
                String newAccessToken = JwtUtil.generateToken(username); // nuevo token por 10 minutos
                ctx.json(Map.of("token", newAccessToken));
            } catch (Exception e) {
                ctx.status(401).result("Token expirado o inválido");
            }
        });

        // Endpoint para crear una nueva ShortUrl (POST /api/urls)
        app.post("/api/urls", ctx -> {
            // 1. Validar que se envíe el header Authorization con el esquema "Bearer"
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).result("Authorization header missing or invalid");
                return;
            }
            String token = authHeader.substring(7); // Se remueve "Bearer "

            try {
                // 2. Validar el token y obtener el nombre de usuario
                String username = JwtUtil.validateToken(token);

                // 3. Obtener el parámetro originalUrl de la petición (puede ser enviado desde form data o JSON)
                String originalUrl = ctx.formParam("originalUrl");
                if (originalUrl == null || originalUrl.trim().isEmpty()) {
                    ctx.status(400).result("El parámetro 'originalUrl' es requerido");
                    return;
                }

                // 4. Obtener el Usuario que realiza la petición.
                // Se asume que se cuenta con un servicio de usuarios que permite buscar
                // el usuario por su nombre (por ejemplo: UsuarioService). Puedes ajustar este
                // punto según la implementación de tu aplicación.
                Usuario usuario = UsuarioServices.getInstance().find(username);
                if (usuario == null) {
                    ctx.status(404).result("Usuario no encontrado");
                    return;
                }

                // 5. Crear la nueva ShortUrl, asignándole la URL original y el usuario creador
                ShortUrl newUrl = new ShortUrl(originalUrl, usuario);

                // 6. Persistir la nueva ShortUrl en la base de datos mediante el servicio
                ShortUrlServices.getInstance().crear(newUrl);

                // 7. Preparar la respuesta (opcionalmente, podrías retornar más datos)
                Map<String, Object> response = new HashMap<>();
                response.put("url", newUrl);

                // Responder con la nueva URL en formato JSON
                ctx.json(response);
            } catch (Exception e) {
                // Si ocurre algún error en la validación del token o durante la creación,
                // se responde con un error 401 (no autorizado)
                e.printStackTrace();
                ctx.status(401).result("Token inválido o expirado");
            }
        });






        app.before(ctx -> {
            if (ctx.sessionAttribute("USUARIO") == null) { // Si no hay sesión activa
                Cookie[] cookies = ctx.req().getCookies();

                if (cookies != null) {
                    Optional<Cookie> cookieOpt = Arrays.stream(cookies)
                            .filter(cookie -> "usuarioRecordado".equals(cookie.getName()))
                            .findFirst();

                    if (cookieOpt.isPresent()) {
                        String username = EncriptarUser.desencriptar(cookieOpt.get().getValue());
                        Usuario usuario = UsuarioServices.getInstance().find(username);

                        if (usuario != null) {
                            ctx.sessionAttribute("USUARIO", usuario); // Restaura la sesión
                        }
                    }
                }
            }
        });

        /*app.before("/", ctx -> {
            if (ctx.sessionAttribute("USUARIO") == null) {
                ctx.redirect("/formulario");
            }
        });*/

        /*app.before("/crud-simple*", ctx -> {
            if (!SessionCheckAdmin(ctx)) {
                ctx.redirect("/");
            }
        });*/

        app.get("/login", ctx -> ctx.redirect("/formulario"));
        app.get("/formulario", ctx -> ctx.render("/formulario.html"));
        app.get("/registro", ctx -> ctx.render("registro.html"));
        app.get("/", ctx -> ctx.render("/mainpage/mainpage.html"));






        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.removeCookie("usuarioRecordado");
            ctx.redirect("/formulario");
        });

        app.post("/shorten", urlController::acortarURL);
        app.get("/re/{id}", urlController::urlRedirect);

        //filtros are down for trials
        app.before("/crud-simple*", ctx -> {
            if (!SessionCheckAdmin(ctx)) {
                ctx.redirect("/");
            }
        });
/*
        app.before("/fotos*", ctx -> {
            if (!SessionCheckAdmin(ctx)) {
                ctx.redirect("/");
            }
        });*/

        if (UsuarioServices.getInstance().findAll().isEmpty()) {
            startDB();
            System.out.println("Usuarios no encontrados");
        }


        app.post("/autenticar", ctx -> {
            String usuario = ctx.formParam("usuario");
            String contrasena = ctx.formParam("password");
            boolean recordar = "on".equals(ctx.formParam("recordar"));
            Usuario user = UsuarioServices.getInstance().find(usuario);

            if (user.getPassword().equals(contrasena)) {
                user = UsuarioServices.getInstance().find(user.getUsername());
                ctx.sessionAttribute("USUARIO", user);


                if (recordar) {
                    String datosEncriptados = EncriptarUser.encriptar(user.getUsername());
                    Cookie cookie = new Cookie("usuarioRecordado", datosEncriptados);
                    cookie.setMaxAge(7 * 24 * 60 * 60); // Expira en 1 semana
                    cookie.setPath("/");
                    ctx.res().addCookie(cookie);
                }

                ctx.redirect("/");
            } else {
                ctx.redirect("/formulario?error=credenciales"); // Redirigir con parámetro de error
            }
        });

        app.post("/registro", ctx -> {
            String nombre = ctx.formParam("nombre");
            String username = ctx.formParam("username");
            String contrasena = ctx.formParam("password");
            Foto foto = null;
            try {
                UploadedFile uploadedFile = ctx.uploadedFile("foto");
                if (uploadedFile != null) {
                    byte[] bytes = uploadedFile.content().readAllBytes();
                    String encodedString = Base64.getEncoder().encodeToString(bytes);
                    foto = new Foto(uploadedFile.filename(), uploadedFile.contentType(), encodedString);
                }

            } catch (Exception e) {
                System.out.println("foto aint working");
            }


            if (nombre == null || username == null || contrasena == null) {
                ctx.status(400);
                ctx.result("Llena todos los campos");
                return;
            }
            try {
                if (foto != null) {
                    FotoServices.getInstancia().crear(foto);
                }
            } catch (Exception e) {

            }
            Usuario user = new Usuario(nombre, username, contrasena, false, foto);
            UsuarioServices.getInstance().crear(user);

            ctx.redirect("/");
        });


        app.post("/logout", ctx -> {
            ctx.sessionAttribute("USUARIO", null); // Eliminar sesión

            Cookie cookie = new Cookie("usuarioRecordado", "");
            cookie.setMaxAge(0); // Expirar inmediatamente
            cookie.setPath("/");
            ctx.res().addCookie(cookie);

            ctx.redirect("/login");
        });


        int port = 50051;

        //Inicializando el servidor
        Server server = ServerBuilder.forPort(port)
                .addService(new ShortUrlServiceImpl())// indicando el servicio registrado.
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        System.out.println("Servidor gRPC iniciando y escuchando en " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Cerrando servidor por la JVM ");
            if (server != null) {
                server.shutdown();
            }
            System.err.println("Servidor abajo!...");
        }));
        server.awaitTermination();
    }



    private static boolean SessionCheck(Context ctx) {
        Usuario user = ctx.sessionAttribute("USUARIO");

        if (user == null) {
            Cookie[] cookies = ctx.req().getCookies();

            if (cookies != null) {
                Optional<Cookie> cookieOpt = Arrays.stream(cookies)
                        .filter(cookie -> "usuarioRecordado".equals(cookie.getName()))
                        .findFirst();

                if (cookieOpt.isPresent()) {
                    String username = EncriptarUser.desencriptar(cookieOpt.get().getValue());
                    Usuario usuario = UsuarioServices.getInstance().find(username);

                    if (usuario != null) {
                        ctx.sessionAttribute("USUARIO", usuario);
                        return true;
                    }
                }
            }
            return false; // No hay sesión ni cookie válida
        }

        return true; // Usuario ya tenía sesión activa
    }




    private static boolean SessionCheckAdmin(Context ctx) {
        Usuario user = ctx.sessionAttribute("USUARIO");
        if (user != null) {
            ctx.sessionAttribute("USUARIO", user);
            if (user.isAdministrador()) {
                return true;
            }
        }
        return false; // No hay sesión ni cookie válida
    }

    private static boolean Loggin(String username, String password) {
        Usuario user = UsuarioServices.getInstance().find(username);
        if (user == null) {
            return false;
        } else return user.getPassword().equals(password);
    }


    public static boolean validarUsuario(Usuario user) {
        Usuario usuario = UsuarioServices.getInstance().find(user.getUsername());
        try {
            if (usuario != null) {
                if (usuario.getUsername().equals(user.getUsername())) {
                    return user.getPassword().equals(usuario.getPassword());
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 7000; //Retorna el puerto por defecto en caso de no estar en Heroku.
    }
    
    private static void startDB() {
        Usuario Admin = new Usuario("Admin", "Anthony", "Admin", true);
        UsuarioServices.getInstance().crear(Admin);
        ShortUrlServices.getInstance().crear(new ShortUrl("1","https://www.youtube.com/", Admin));
        UsuarioServices.getInstance().crear(new Usuario("admin", "Gustavo", "admin", true));
        UsuarioServices.getInstance().crear(new Usuario("user1", "Usuario Normal", "user123", false));
        UsuarioServices.getInstance().crear(new Usuario("autor1", "Autor", "autor123", false));
    }


}