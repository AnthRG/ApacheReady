package app.controllers;

import app.entidades.ShortUrl;
import app.entidades.Usuario;
import app.entidades.Visita;
import app.servicios.ShortUrlServices;
import app.servicios.UsuarioServices;
import app.servicios.VisitaServices;
import io.javalin.http.Context;
import javassist.NotFoundException;
import org.jetbrains.annotations.NotNull;
import app.servicios.QRCodeGenerator;

import io.javalin.http.Context;


import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class urlController {

    public static void listar(@NotNull Context ctx) throws Exception {
        Usuario user = ctx.sessionAttribute("USUARIO");
        List<ShortUrl> lista = null;
        String normal = "/dashboard/dashboardShortUser.html";
        String admin = "/dashboard/dashboardShortLinks.html";

        if (user != null) {
            if (!user.isAdministrador()) {
                lista = ShortUrlServices.getInstance().findAllByUsername(user.getUsername());
                renderListar(ctx, lista, normal);
            } else {
                lista = ShortUrlServices.getInstance().findAll();
                renderListar(ctx, lista, admin);
            }
        } else {
            String sessionId = ctx.sessionAttribute("sessionId");
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                ctx.sessionAttribute("sessionId", sessionId);
            }
            lista = ShortUrlServices.getInstance().findAllBySessionID(sessionId);
            renderListar(ctx, lista, normal);
        }
    }

    public static void renderListar(@NotNull Context ctx, List<ShortUrl> lista, String path) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Listado de Url Acortadas");
        modelo.put("lista", lista);

        ctx.render(path, modelo);
    }

    public static void expandir(@NotNull Context ctx) throws Exception {
        String idUrl = ctx.pathParam("id");
        List<Visita> lista = VisitaServices.getInstance().findAllByShortUrl(idUrl);
        ShortUrl shortUrl = ShortUrlServices.getInstance().find(idUrl);

        // Get today's date
        LocalDate today = LocalDate.now();

        // Process data for chart - group visits by hour for today
        Map<Integer, Long> visitsByHour = lista.stream()
                .filter(v -> v.getFechaAcceso().toLocalDate().equals(today))
                .collect(Collectors.groupingBy(
                        v -> v.getFechaAcceso().getHour(),
                        Collectors.counting()
                ));

        // Create chart labels (hours from 0-23)
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartData = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            chartLabels.add(String.format("%02d:00", hour));
            chartData.add(visitsByHour.getOrDefault(hour, 0L));
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Dashboard Url Acortada");
        modelo.put("shortUrl", shortUrl);
        URI dominio = new URI(shortUrl.getOriginalUrl());
        modelo.put("Dominio", dominio.getHost());
        modelo.put("fecha", shortUrl.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        modelo.put("lista", lista);
        modelo.put("chartLabels", chartLabels);
        modelo.put("chartData", chartData);
        String domain = ctx.host();
        String fullShortUrl = "http://" + domain + "/re/" + shortUrl.getId();
        String qrBase64 = QRCodeGenerator.generateQRCodeBase64(fullShortUrl, 200, 200);
        modelo.put("fullUrl", fullShortUrl);
        modelo.put("qrBase64", qrBase64);



        ctx.render("/dashboard/dashboardExpandLink.html", modelo);
    }


    public static void acortarURL(@NotNull Context ctx) {
        String originalUrl = ctx.formParam("url");

        String shortId = generateShortId();
        String domain = ctx.host();
        String fullShortUrl = "http://" + domain + "/re/" + shortId;

        String qrBase64 = QRCodeGenerator.generateQRCodeBase64(fullShortUrl, 200, 200);
        Usuario user = ctx.sessionAttribute("USUARIO");
        ShortUrl shortUrl = null;
        if (user != null) {
            shortUrl = new ShortUrl(shortId, originalUrl, ctx.sessionAttribute("USUARIO"));
        } else {
            String sessionId = ctx.sessionAttribute("sessionId");
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                ctx.sessionAttribute("sessionId", sessionId);
            }
            shortUrl = new ShortUrl(shortId, originalUrl, ctx.sessionAttribute("USUARIO"), sessionId);
        }
        shortUrl.setQrCodeBase64(qrBase64);
        ShortUrlServices.getInstance().crear(shortUrl);


        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", fullShortUrl);
        response.put("qrCode", qrBase64);
        ctx.json(response);
    }


    public static void urlRedirect(@NotNull Context ctx) {
        String id = ctx.pathParam("id");
        ShortUrl shortUrl = ShortUrlServices.getInstance().find(id);
        if (shortUrl == null) {
            ctx.redirect("/");
            return;
        }

        shortUrl.addVisitCount();
        ShortUrlServices.getInstance().editar(shortUrl);

        String ip = ctx.ip();
        String userAgent = ctx.header("User-Agent");
        String browser = extractBrowser(userAgent);
        String sistemaOperativo = extractOperatingSystem(userAgent);
        String clientDomain = ctx.host(); // o de otra cabecera según convenga

        Visita visit = new Visita(generateShortId(), ip, browser, sistemaOperativo, clientDomain, shortUrl);
        VisitaServices.getInstance().crear(visit);

        ctx.redirect(shortUrl.getOriginalUrl());

    }

    private static String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 6); // Mejor usar base62
    }

    public static String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Desconocido";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("opr") || ua.contains("opera")) {
            return "Opera";
        } else if (ua.contains("edge") || ua.contains("edg")) {
            return "Edge";
        } else if (ua.contains("msie") || ua.contains("trident")) {
            return "Internet Explorer";
        } else if (ua.contains("brave")) {
            return "Brave";
        } else if (ua.contains("chrome")) {
            return "Chrome";
        }
        return "Desconocido";
    }

    public static String extractOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Desconocido";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac os") || ua.contains("macintosh")) {
            return "Mac OS";
        } else if (ua.contains("x11")) {
            return "Unix";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        return "Desconocido";
    }

    public static Map<String, Object> processChartData(List<Visita> visits, String period, String dateStr) {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        if (period.equals("hour")) {
            // Parse the selected date
            LocalDate selectedDate = LocalDate.parse(dateStr);

            // Group visits by hour for the selected date
            Map<Integer, Long> visitsByHour = visits.stream()
                    .filter(v -> v.getFechaAcceso().toLocalDate().equals(selectedDate))
                    .collect(Collectors.groupingBy(
                            v -> v.getFechaAcceso().getHour(),
                            Collectors.counting()
                    ));

            // Create data for all 24 hours
            for (int hour = 0; hour < 24; hour++) {
                labels.add(String.format("%02d:00", hour));
                data.add(visitsByHour.getOrDefault(hour, 0L));
            }
        } else if (period.equals("day")) {
            // Handle day period (week view)
            // Parse the selected week (format: 2023-W01)
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int week = Integer.parseInt(dateStr.substring(6));

            // Find the first day of the selected week
            LocalDate firstDayOfWeek = LocalDate.ofYearDay(year, 1)
                    .with(ChronoField.ALIGNED_WEEK_OF_YEAR, week)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            // Group visits by day for the selected week
            for (int i = 0; i < 7; i++) {
                LocalDate currentDay = firstDayOfWeek.plusDays(i);
                String dayLabel = currentDay.format(DateTimeFormatter.ofPattern("EEE"));

                long count = visits.stream()
                        .filter(v -> v.getFechaAcceso().toLocalDate().equals(currentDay))
                        .count();

                labels.add(dayLabel);
                data.add(count);
            }
        } else if (period.equals("month")) {
            // Handle month period (year view)
            // Parse the selected month (format: 2023-05)
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(5));

            // Create YearMonth
            YearMonth selectedMonth = YearMonth.of(year, month);

            // Number of days in the month
            int daysInMonth = selectedMonth.lengthOfMonth();

            // Group visits by day for the selected month
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = selectedMonth.atDay(day);
                String dayLabel = String.valueOf(day);

                long count = visits.stream()
                        .filter(v -> v.getFechaAcceso().toLocalDate().equals(currentDate))
                        .count();

                labels.add(dayLabel);
                data.add(count);
            }
        }

        result.put("labels", labels);
        result.put("data", data);

        return result;
    }

    public static void eliminar(Context ctx) {
        String id = ctx.pathParam("id");
        Usuario user = ctx.sessionAttribute("USUARIO");

        if (user != null && user.isAdministrador()) {
            ShortUrlServices.getInstance().eliminar(id);  //
        }

        ctx.redirect("/crud-url");


    }
}





