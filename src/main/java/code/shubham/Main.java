package code.shubham;

import code.shubham.components.mongodb.MongoDBClientHolder;
import code.shubham.entities.Address;
import code.shubham.entities.Person;
import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Main {

    public static void main( String[] args ) {
        final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        // Replace the placeholder with your MongoDB deployment's connection string
        final String uri = "mongodb+srv://root:root1234@myatlasclusteredu.mbfcq0p.mongodb.net/?retryWrites=true&w=majority&appName=myAtlasClusterEDU";
        final MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(
                        ServerApi.builder()
                                .version(ServerApiVersion.V1)
                                .build())
                .build();

        try (final MongoClient mongoClient = MongoDBClientHolder.getClient()) {
            printDatabases(mongoClient);
            final MongoDatabase database = mongoClient.getDatabase("sample_weatherdata");
            final MongoCollection<Document> collection = database
                    .getCollection("data")
                    .withCodecRegistry(pojoCodecRegistry);
            System.out.println(collection.getDocumentClass());

            final Document doc = collection.find(eq("type", "FM-13")).first();
            if (doc != null)
                System.out.println(doc.toJson());
            else
                System.out.println("No matching documents found.");

            person(mongoClient, pojoCodecRegistry);
        }
    }

    private static void person(final MongoClient mongoClient, final CodecRegistry pojoCodecRegistry) {
        final MongoDatabase database = mongoClient.getDatabase("generic");
        final MongoCollection<Person> collection = database
                .getCollection("persons", Person.class)
                .withCodecRegistry(pojoCodecRegistry);
        collection.createIndex(Indexes.ascending("age"));
        collection.createIndex(Indexes.compoundIndex(Indexes.descending("age"), Indexes.ascending("name"))); // compound index
        collection.createIndex(Indexes.text("name"));
        collection.createIndex(Indexes.hashed("_id"));

        IndexOptions indexOptions = new IndexOptions().unique(true);
        collection.createIndex(Indexes.ascending("email"), indexOptions);

        final Person ada = new Person("email@email.com", "Ada Byron", 20, new Address("St James Square", "London", "W1"));
        collection.insertOne(ada);

        System.out.println(collection.find(lte("age", 20)).first());


        final List<Person> people = asList(
                new Person("Charles@fsa", "Charles Babbage", 45, new Address("5 Devonshire Street", "London", "W11")),
                new Person("dsa@sad", "Alan Turing", 28, new Address("Bletchley Hall", "Bletchley Park", "MK12")),
                new Person("dsad@sasd", "Timothy Berners-Lee", 61, new Address("Colehill", "Wimborne", null)));

        collection.insertMany(people);

        final Block<Person> printBlock = System.out::println;

        collection.find().forEach(printBlock::apply);

        collection.updateOne(
                eq("name", "Ada Byron"),
                combine(set("age", 23),
                        set("name", "Ada Lovelace")));

        final UpdateResult updateResult = collection.updateMany(
                not(eq("zip", null)),
                set("zip", null));
        System.out.println(updateResult.getModifiedCount());

        collection.replaceOne(eq("name", "Ada Lovelace"), ada);

        collection.deleteOne(eq("address.city", "Wimborne"));

        final DeleteResult deleteResult = collection.deleteMany(eq("address.city", "London"));
        System.out.println(deleteResult.getDeletedCount());

        final MongoCollection<Document> personsDocumentCollection = database
                .getCollection("persons")
                .withCodecRegistry(pojoCodecRegistry);

        final Block<Document> printBlockDocument = System.out::println;
        personsDocumentCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.lte("age", 50)),
                        Aggregates.group("$address.city", Accumulators.sum("count", 1))))
                .forEach(printBlockDocument::apply);

        collection.drop();
    }

    private static void printDatabases(final MongoClient mongoClient) {
        final ArrayList<Document> databases = new ArrayList<>();
        mongoClient.listDatabases().into(databases);
        System.out.println(databases);
    }
}