import grpc
import shorturl_pb2
import shorturl_pb2_grpc

def run():
    channel = grpc.insecure_channel('localhost:50051')
    stub = shorturl_pb2_grpc.ShortUrlServiceStub(channel)

    creator = shorturl_pb2.UsuarioMessage(
        username="Admin",
        nombre="Anthony",
        password="Admin",
        administrador=False
    )
    
    create_request = shorturl_pb2.CreateShortUrlRequest(
        originalUrl="https://www.ejemplo.com",
        creador=creator
    )
    
    try:
        create_response = stub.CreateShortUrl(create_request)
        print("ShortUrl creada:")
        print(f"ID: {create_response.shortUrl.id}")
        print(f"Original URL: {create_response.shortUrl.originalUrl}")
        print(f"Fecha: {create_response.shortUrl.fecha}")
        print(f"Visit Count: {create_response.shortUrl.visitCount}")
        print(f"Creador: {create_response.shortUrl.creador.nombre}")
    except grpc.RpcError as e:
        print(f"Error al crear ShortUrl: {e.details()}")

    list_request = shorturl_pb2.ListShortUrlsRequest()
    try:
        list_response = stub.ListShortUrls(list_request)
        print("\nListado de ShortUrls:")
        for shorturl in list_response.shortUrls:
            print("------------------------------")
            print(f"ID: {shorturl.id}")
            print(f"Original URL: {shorturl.originalUrl}")
            print(f"Fecha: {shorturl.fecha}")
            print(f"Visit Count: {shorturl.visitCount}")
            print(f"Creador: {shorturl.creador.nombre}")
    except grpc.RpcError as e:
        print(f"Error al listar ShortUrls: {e.details()}")

if __name__ == '__main__':
    run()
