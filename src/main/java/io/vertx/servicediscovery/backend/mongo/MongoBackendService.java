package io.vertx.servicediscovery.backend.mongo;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceDiscoveryBackend;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author liwei
 * @title: MongoBackendService
 * @projectName moho
 * @description: TODO
 * @date 2019-11-29 14:50
 */
public class MongoBackendService implements ServiceDiscoveryBackend {


    private MongoClient mongoClient;

    private static final String COLLECTION_NAME="servers";

    public void init(Vertx vertx, JsonObject configuration) {
        this.mongoClient=MongoClient.createShared(vertx,configuration);
        this.mongoClient.createCollection(COLLECTION_NAME,res->{

        });

    }

    public void store(Record record, Handler<AsyncResult<Record>> handler) {
        String uuid= UUID.randomUUID().toString();
        record.setRegistration(uuid);
        MongoRecord mongoRecord=MongoRecord.getMongoRecord(record);
        mongoRecord.setId(uuid);
        mongoClient.insert(COLLECTION_NAME,JsonObject.mapFrom(mongoRecord),res->{
            if(res.succeeded()){
                handler.handle(Future.succeededFuture(record));
            }else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public void remove(Record record, Handler<AsyncResult<Record>> handler) {
        Objects.requireNonNull(record.getRegistration(), "No registration id in the record");
        remove(record.getRegistration(),handler);
    }

    public void remove(String uuid, Handler<AsyncResult<Record>> handler) {
        Objects.requireNonNull(uuid, "No registration id in the record");
        mongoClient.findOne(COLLECTION_NAME,new JsonObject().put("_id",uuid),null,res->{
            if(res.succeeded()){
                JsonObject recordObject=res.result();
                if(recordObject==null){
                    handler.handle(Future.failedFuture("Record '" + uuid + "' not found"));
                }else {
                    mongoClient.removeDocument(COLLECTION_NAME,new JsonObject().put("_id",uuid),removeRes->{
                        if(removeRes.succeeded()){
                            handler.handle(Future.succeededFuture(recordObject.mapTo(MongoRecord.class).getRecord()));
                        }else {
                            handler.handle(Future.failedFuture(removeRes.cause()));
                        }
                    });
                }
            }else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public void update(Record record, Handler<AsyncResult<Void>> handler) {
        Objects.requireNonNull(record.getRegistration(), "No registration id in the record");
        mongoClient.updateCollection(COLLECTION_NAME,new JsonObject().put("_id",record.getRegistration()),JsonObject.mapFrom(record),res->{
            if(res.succeeded()){
                handler.handle(Future.succeededFuture());
            }else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public void getRecords(Handler<AsyncResult<List<Record>>> handler) {
        mongoClient.find(COLLECTION_NAME,new JsonObject(),res->{
           if(res.succeeded()){
               handler.handle(Future.succeededFuture(getRecordFromJsonObject(res.result())));
           }else {
               handler.handle(Future.failedFuture(res.cause()));
           }
        });
    }

    private List<Record> getRecordFromJsonObject(List<JsonObject> jsonObjects){
        return jsonObjects.stream().map(jsonObject -> jsonObject.mapTo(MongoRecord.class).getRecord()).collect(Collectors.toList());
    }

    public void getRecord(String uuid, Handler<AsyncResult<Record>> handler) {
        mongoClient.findOne(COLLECTION_NAME,new JsonObject().put("_id",uuid),null,res->{
            if(res.succeeded()){
                JsonObject recordObject=res.result();
                if(recordObject==null){
                    handler.handle(Future.succeededFuture(null));
                }else {
                    handler.handle(Future.succeededFuture(res.result().mapTo(MongoRecord.class).getRecord()));
                }
            }else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }
}
