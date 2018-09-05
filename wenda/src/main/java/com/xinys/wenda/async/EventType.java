package com.xinys.wenda.async;

public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    EMAIL(3),
    FOLLOW(4),
    UNFOLLOW(5);;

    private int value;

     EventType(int value){
       this.value = value;
    }

    public int getValue(){
         return this.value;
    }
}
