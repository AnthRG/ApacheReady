package app.servicios;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Filters.eq;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.*;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoGestionDb<T> {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    protected MongoCollection<T> collection;
    private Class<T> entityClass;


    public MongoGestionDb(Class<T> entityClass, String collectionName) {
        this.entityClass = entityClass;
        if (mongoClient == null) {
            // Read the connection string from the environment variable MONGODB_URL
            String connectionStringValue = System.getenv("MONGODB_URL");
            if (connectionStringValue == null || connectionStringValue.isEmpty()) {
                // Fallback to a default connection string with a default database name
                connectionStringValue = "mongodb://localhost:27017/testdb";
            }
            ConnectionString connectionString = new ConnectionString(connectionStringValue);

            // Extract the database name from the connection string; fallback if necessary.
            String dbName = connectionString.getDatabase();
            if (dbName == null || dbName.isEmpty()) {
                dbName = "SusLinks";
            }

            // Setup CodecRegistry for POJOs
            CodecRegistry pojoCodecRegistry = fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .codecRegistry(pojoCodecRegistry)
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(dbName);
        }
        // Get the collection for the entity type
        collection = database.getCollection(collectionName, entityClass);
    }

    public T crear(T entidad) {
        collection.insertOne(entidad);
        return entidad;
    }


    public T editar(T entidad) {
        Object id = getIdValue(entidad);
        if (id == null) {
            throw new IllegalArgumentException("El objeto entidad no tiene definido un id.");
        }
        String idFieldName = getIdFieldName();
        collection.replaceOne(eq(idFieldName, id), entidad);
        return entidad;
    }

    public T find(Object id) {
        String idFieldName = getIdFieldName();
        return collection.find(eq(idFieldName, id)).first();
    }

    public boolean eliminar(Object entidadId) {
        String idFieldName = getIdFieldName();
        DeleteResult result = collection.deleteOne(eq(idFieldName, entidadId));
        return result.getDeletedCount() > 0;
    }
    public List<T> findAll() {
        List<T> list = new ArrayList<>();
        collection.find().into(list);
        return list;
    }

    private Object getIdValue(T entidad) {
        if (entidad == null) {
            return null;
        }
        Class<?> clazz = entidad.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(org.bson.codecs.pojo.annotations.BsonId.class)) {
                    field.setAccessible(true);
                    try {
                        return field.get(entidad);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
            clazz = clazz.getSuperclass();  // Check superclasses as well
        }
        return null;
    }


    private String getIdFieldName() {
        if (entityClass == null) {
            return "_id"; // Fallback if entityClass is null
        }
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                return field.getName();
            }
        }
        return "_id"; // Fallback if no field annotated with @Id is found
    }

    public static void cerrarConexion() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }
}
