package app.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import app.entidades.ShortUrl;
import app.entidades.Usuario;
import app.servicios.ShortUrlServices;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ShortUrlServiceImpl extends ShortUrlServiceGrpc.ShortUrlServiceImplBase {

    @Override
    public void createShortUrl(CreateShortUrlRequest request, StreamObserver<CreateShortUrlResponse> responseObserver) {
        String originalUrl = request.getOriginalUrl();
        // Conversión del mensaje gRPC a entidad Usuario
        Usuario creador = convertToUsuario(request.getCreador());

        // Generación de un ID (se puede usar Base62 en producción, aquí se usa UUID)
        String idGenerado = UUID.randomUUID().toString();

        // Creación de la entidad ShortUrl usando el constructor indicado.
        ShortUrl shortUrl = new ShortUrl(idGenerado, originalUrl, creador);

        // Almacena la ShortUrl en el repositorio.
        ShortUrlServices.getInstance().crear(shortUrl);

        // Conversión de la entidad a mensaje gRPC
        ShortUrlMessage grpcShortUrl = convertToGrpcShortUrl(shortUrl);

        CreateShortUrlResponse response = CreateShortUrlResponse.newBuilder()
                .setShortUrl(grpcShortUrl)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getShortUrl(GetShortUrlRequest request, StreamObserver<GetShortUrlResponse> responseObserver) {
        String id = request.getId();
        ShortUrl shortUrl = ShortUrlServices.getInstance().find(id);

        if (shortUrl == null) {
            responseObserver.onError(new Exception("ShortUrl no encontrada"));
            return;
        }

        ShortUrlMessage grpcShortUrl = convertToGrpcShortUrl(shortUrl);
        GetShortUrlResponse response = GetShortUrlResponse.newBuilder()
                .setShortUrl(grpcShortUrl)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listShortUrls(ListShortUrlsRequest request, StreamObserver<ListShortUrlsResponse> responseObserver) {
        List<ShortUrl> shortUrls = ShortUrlServices.getInstance().findAll();
        ListShortUrlsResponse.Builder responseBuilder = ListShortUrlsResponse.newBuilder();

        for (ShortUrl su : shortUrls) {
            responseBuilder.addShortUrls(convertToGrpcShortUrl(su));
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    // Método auxiliar para convertir UsuarioMessage (gRPC) en entidad Usuario
    private Usuario convertToUsuario(UsuarioMessage usuarioMsg) {
        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioMsg.getUsername());
        usuario.setNombre(usuarioMsg.getNombre());
        usuario.setPassword(usuarioMsg.getPassword());
        usuario.setAdministrador(usuarioMsg.getAdministrador());
        // Si requieres convertir la foto, agregar la conversión aquí.
        return usuario;
    }

    // Método auxiliar para convertir la entidad ShortUrl en el mensaje gRPC ShortUrlMessage
    private ShortUrlMessage convertToGrpcShortUrl(ShortUrl shortUrl) {
        ShortUrlMessage.Builder builder = ShortUrlMessage.newBuilder();
        builder.setId(shortUrl.getId());
        builder.setOriginalUrl(shortUrl.getOriginalUrl());
        // Convertir LocalDateTime a String (formato ISO 8601)
        String fechaStr = shortUrl.getFecha().format(DateTimeFormatter.ISO_DATE_TIME);
        builder.setFecha(fechaStr);
        builder.setVisitCount(shortUrl.getVisitCount());
        builder.setCreador(convertToGrpcUsuario(shortUrl.getCreador()));
        if (shortUrl.getQrCodeBase64() != null) {
            builder.setQrCodeBase64(shortUrl.getQrCodeBase64());
        }
        if (shortUrl.getSessionId() != null) {
            builder.setSessionId(shortUrl.getSessionId());
        }
        return builder.build();
    }

    // Método auxiliar para convertir la entidad Usuario en el mensaje gRPC UsuarioMessage
    private UsuarioMessage convertToGrpcUsuario(Usuario usuario) {
        UsuarioMessage.Builder builder = UsuarioMessage.newBuilder();
        builder.setUsername(usuario.getUsername());
        builder.setNombre(usuario.getNombre());
        builder.setPassword(usuario.getPassword());
        builder.setAdministrador(usuario.isAdministrador());
        // Incluir conversión para la foto si es necesario.
        return builder.build();
    }
}

