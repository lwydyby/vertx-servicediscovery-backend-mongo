package io.vertx.servicediscovery.backend.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.servicediscovery.Record;
import org.apache.commons.beanutils.BeanUtils;

/**
 * @author liwei
 * @title: MongoRecord
 * @projectName moho
 * @description: TODO
 * @date 2019-11-29 15:08
 */

public class MongoRecord extends Record {

    @JsonProperty("_id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public Record getRecord(){
        Record record=new Record();
        try {
            BeanUtils.copyProperties(record,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }
    @JsonIgnore
    public static MongoRecord getMongoRecord(Record record){
        MongoRecord mongoRecord=new MongoRecord();
        try {
            BeanUtils.copyProperties(mongoRecord,record);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mongoRecord;
    }
}
