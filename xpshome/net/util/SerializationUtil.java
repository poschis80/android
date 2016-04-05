package xpshome.net.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Poschinger Christian on 14.04.2016.
 */
public class SerializationUtil {

    /**
     * A method to serialize any class object into an json formatted string.
     *
     * example :    MyClass myobject = new MyClass();
     *              .. <do anything with the object> ..
     *              String serializedObject = SerializationUtil.serialize(myobject);
     *
     * @param object which will be serialized into string
     * @param <T> template type of any kind of complex class
     * @return String serialized representation of input object class
     */
    public static <T> String serialize(T object) {
        GsonBuilder builder = new GsonBuilder();
        Gson g = builder.create();
        return g.toJson(object);
    }


    /**
     * A method to deserialize a previously serialized object into the expected object.
     *
     * example :    String mySerializedObject = .. <assign the serialized object> ..
     *              MyClass myobject = SerializationUtil.deserialize(mySerializedObject, MyClass.class);
     *
     *
     * @param object is the string serialized representation of the expected class
     * @param clazz is the class of the object we would like to deserialize
     * @param <T> template type of any kind of complex class
     * @return object of type <T>
     */
    public static <T> T deserialize(String object, Class<T> clazz) {
        GsonBuilder builder = new GsonBuilder();
        Gson g = builder.create();
        return g.fromJson(object, clazz);
    }
}
