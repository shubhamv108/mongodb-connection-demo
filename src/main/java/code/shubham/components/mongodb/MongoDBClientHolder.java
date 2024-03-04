package code.shubham.components.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBClientHolder {

    public static MongoClient getClient() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        private static final MongoClient INSTANCE = MongoClients.create(SingletonHolder.getSettings());

        private static MongoClientSettings getSettings() {
            final String uri = "mongodb+srv://root:root1234@myatlasclusteredu.mbfcq0p.mongodb.net/?retryWrites=true&w=majority&appName=myAtlasClusterEDU";

            final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

            return MongoClientSettings.builder()
                    .codecRegistry(pojoCodecRegistry)
                    .applyConnectionString(new ConnectionString(uri))
                    .serverApi(
                            ServerApi.builder()
                                    .version(ServerApiVersion.V1)
                                    .build())
                    .build();
        }
    }

}
