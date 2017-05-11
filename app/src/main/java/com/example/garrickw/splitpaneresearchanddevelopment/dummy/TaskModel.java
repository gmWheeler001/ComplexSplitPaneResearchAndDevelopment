package com.example.garrickw.splitpaneresearchanddevelopment.dummy;

/**
 * Created by garrick.w on 2015/10/16.
 *
 */
public class TaskModel {
    private String name;
    private String folio;
    private String process;

    public TaskModel(String name, String description, String process) {
        this.name = name;
        this.folio = description;
        this.process = process;
    }

    public String getProcess() {
        return process;
    }

    public String getFolio() {
        return folio;
    }

    public String getName() {
        return name;
    }
}
