package edu.escuelaing.arep.docker.sparklogservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import edu.escuelaing.arep.docker.model.Mensaje;
import edu.escuelaing.arep.docker.model.StandardResponse;
import edu.escuelaing.arep.docker.model.StatusResponse;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.Doc;

import java.util.Iterator;

import static spark.Spark.*;

public class SparkLogService {
    
    /**
     * 
     * 
     * @param args
     */
    public static void main(String... args){
        port(getPort());

        get("/hello", (req, rep) -> {
           return "Hola Mundo";
        });

        post("/logservice", (request, response) -> {
            Mensaje mensaje = createMensaje(request.body());
            String json = new ObjectMapper().writeValueAsString(mensaje);
            MongoClient mongo = new MongoClient("ec2-34-224-94-29.compute-1.amazonaws.com",27017);
            DB db = mongo.getDB("mensaje");
            DBCollection collection = db.getCollection("cuerpo");
            DBObject dbObject = (DBObject) JSON.parse(json);
            collection.insert(dbObject);

            JSONArray result = new JSONArray();
            DBCollection dbCollection = mongo.getDB("mensaje").getCollectionFromString("cuerpo");
            DBCursor cursor = dbCollection.find();

            while(cursor.hasNext()){
                DBObject obj = cursor.next();
                JSONObject jsonObj = new JSONObject(JSON.serialize(obj));
                result.put(jsonObj);
            }
            JSONArray resultDef = new JSONArray();
            if(result.length() > 10){
                System.out.println(result.length());
                int i = 0;
                while(i<10){
                    resultDef.put(result.get(result.length()-i-1));
                    i++;
                }
            }else{
                for(int i = result.length(); i>0; i--){
                    resultDef.put(result.get(i-1));
                }
            }
            //Mensaje mensaje = new Gson().fromJson(request.body(), Mensaje.class);
            return resultDef;
            //return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, "Mensaje añadido"));
        });
    }

    private static Mensaje createMensaje(String body) {
        String descripcion = body.substring(body.indexOf("=")+1,body.indexOf("&")).replace("+"," ");
        String date = body.substring(body.indexOf("=",body.indexOf("&"))+1,body.length()-1).replace("%2F","/");
        return new Mensaje(descripcion,date);
    }

    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567;
    }
    
}

