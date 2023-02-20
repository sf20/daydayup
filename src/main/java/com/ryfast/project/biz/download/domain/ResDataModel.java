package com.ryfast.project.biz.download.domain;


import java.util.List;

public class ResDataModel {
    private String code;
    private int total;
    private int begin;
    private int end;
    private List<String[]> kline;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public List<String[]> getKline() {
        return kline;
    }

    public void setKline(List<String[]> kline) {
        this.kline = kline;
    }
}
