package ir.logicbase.mojmessenger.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class TypefaceManager {

private static final Hashtable<String, Typeface> cache = new Hashtable<>();

        public static Typeface get(Context c, String name){
                synchronized(cache){ 
                        if(!cache.containsKey(name)){ 
                                Typeface t = Typeface.createFromAsset( 
                                                c.getAssets(), 
                                                String.format("%s.ttf", name)
                                        ); 
                                cache.put(name, t); 
                        } 
                        return cache.get(name); 
                } 
        }
}