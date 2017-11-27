package ru.statjobs.loader.dto;

public class HhSpecialization {

    private final String specializationGroup;
    private final String specialization;
    private final String code;

    public HhSpecialization(String specializationGroup, String specialization, String code) {
        this.specializationGroup = specializationGroup;
        this.specialization = specialization;
        this.code = code;
    }

    public String getSpecializationGroup() {
        return specializationGroup;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getCode() {
        return code;
    }
}
