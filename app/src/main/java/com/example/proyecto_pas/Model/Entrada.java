package com.example.proyecto_pas.Model;

public class Entrada {

    private String datasetid;
    private String recordid;
    private Fields fields;
    private Geometry geometry;
    private String record_timestamp;

    public String getDatasetid() {
        return datasetid;
    }

    public String getRecordid() {
        return recordid;
    }

    public Fields getFields() {
        return fields;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getRecord_timestamp() {
        return record_timestamp;
    }
}
