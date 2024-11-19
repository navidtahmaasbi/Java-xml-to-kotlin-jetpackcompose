package com.azarpark.cunt.utils;

import java.util.HashMap;
import java.util.Map;

public class Dictionary_char {
    private Map<Character, String> enToFa;
    private Map<String, Character> faToEn;

    public Dictionary_char() {
        this.enToFa = new HashMap<>();
        this.enToFa.put('a', "الف");
        this.enToFa.put('b', "ب");
        this.enToFa.put('c', "ص");
        this.enToFa.put('d', "د");
        this.enToFa.put('e', "e");
        this.enToFa.put('f', "ف");
        this.enToFa.put('h', "ه");
        this.enToFa.put('i', "ع");
        this.enToFa.put('j', "ج");
        this.enToFa.put('l', "ل");
        this.enToFa.put('m', "م");
        this.enToFa.put('n', "ن");
        this.enToFa.put('o', "ث");
        this.enToFa.put('q', "ق");
        this.enToFa.put('s', "س");
        this.enToFa.put('t', "ت");
        this.enToFa.put('v', "و");
        this.enToFa.put('w', "ط");
        this.enToFa.put('y', "ی");
        this.enToFa.put('p', "پ");
        this.enToFa.put('u', "ش");
        this.enToFa.put('z', "ژ");
        this.enToFa.put('0', "۰");
        this.enToFa.put('1', "۱");
        this.enToFa.put('2', "۲");
        this.enToFa.put('3', "۳");
        this.enToFa.put('4', "۴");
        this.enToFa.put('5', "۵");
        this.enToFa.put('6', "۶");
        this.enToFa.put('7', "۷");
        this.enToFa.put('8', "۸");
        this.enToFa.put('9', "۹");

        faToEn = new HashMap<>();
        this.faToEn.put("الف",'a');
        this.faToEn.put("ب",'b');
        this.faToEn.put("ص",'c');
        this.faToEn.put("د",'d');
        this.faToEn.put("e", 'e');
        this.faToEn.put("ف",'f');
        this.faToEn.put("ه",'h');
        this.faToEn.put("ع",'i');
        this.faToEn.put("ج",'j');
        this.faToEn.put("ل",'l');
        this.faToEn.put("م",'m');
        this.faToEn.put("ن",'n');
        this.faToEn.put("ث",'o');
        this.faToEn.put("ق",'q');
        this.faToEn.put("س",'s');
        this.faToEn.put("ت",'t');
        this.faToEn.put("و",'v');
        this.faToEn.put("ط",'w');
        this.faToEn.put("ی",'y');
        this.faToEn.put("پ",'p');
        this.faToEn.put("ش",'u');
        this.faToEn.put("ژ",'z');
        this.faToEn.put("۰",'0');
        this.faToEn.put("۱",'1');
        this.faToEn.put("۲",'2');
        this.faToEn.put("۳",'3');
        this.faToEn.put("۴",'4');
        this.faToEn.put("۵",'5');
        this.faToEn.put("۶",'6');
        this.faToEn.put("۷",'7');
        this.faToEn.put("۸",'8');
        this.faToEn.put("۹",'9');
    }

    public String get_persian_string(Character ch) {
        return this.enToFa.get(ch);
    }

    public char get_english_char(String s) {
        try{
            return this.faToEn.get(s);
        }
        catch (Exception ignored){}

        return '_';
    }
}
