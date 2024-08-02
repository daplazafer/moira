package com.dpf.moira.yaml;

public class TransitionYml {

    private String result;
    private String next;

    public TransitionYml() {
    }

    public TransitionYml(String result, String next) {
        this.result = result;
        this.next = next;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

}
