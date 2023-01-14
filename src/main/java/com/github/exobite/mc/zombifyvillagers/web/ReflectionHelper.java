package com.github.exobite.mc.zombifyvillagers.web;

import com.github.exobite.mc.zombifyvillagers.utils.Version;
import com.github.exobite.mc.zombifyvillagers.utils.VersionHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {

    private static ReflectionHelper instance;

    public static ReflectionHelper getInstance() {
        if(instance==null) {
            instance = new ReflectionHelper();
        }
        return instance;
    }
    private Method gsonParserMethod;
    private final boolean usePast118Methods;

    private ReflectionHelper(){
        //Check for the Version
        usePast118Methods = VersionHelper.isEqualOrLarger(VersionHelper.getBukkitVersion(), new Version(1, 18, 0));

        //Cache the Methods for later use
        setParseReaderMethod();
    }

    private void setParseReaderMethod() {
        Class<JsonParser> clazz = JsonParser.class;
        try {
            if(usePast118Methods) {
                gsonParserMethod = clazz.getMethod("parseReader", Reader.class);
            }else{
                gsonParserMethod = clazz.getMethod("parse", Reader.class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    //Surpressing deprecated "new JsonParser()" as it already is only called when using a Version, where it isn't deprecated.
    public JsonElement parseReader(InputStreamReader isr) {
        if(gsonParserMethod==null) return null;
        JsonElement rVal = null;
        try {
            if(usePast118Methods) {
                rVal = (JsonElement) gsonParserMethod.invoke(null, isr);
            }else{
                rVal = (JsonElement) gsonParserMethod.invoke(new JsonParser(), isr);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return rVal;
    }


}
